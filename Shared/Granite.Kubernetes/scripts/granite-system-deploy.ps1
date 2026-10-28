#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Namespace = "granite"
$CertsDir  = Join-Path $PSScriptRoot "..\..\Granite.Foundation\Components\Granite.Foundation.Core\certs"
$CaCert    = Join-Path $CertsDir "ca.crt"
$CaKey     = Join-Path $CertsDir "ca.key"

# =============================================================================
# Prompt for DEPLOY_ENV
# =============================================================================
Write-Host ""
Write-Host "Select deployment environment:" -ForegroundColor Cyan
Write-Host "  1) local"
Write-Host "  2) development"
Write-Host "  3) test"
Write-Host "  4) production"
Write-Host ""

$envMap = @{ "1" = "local"; "2" = "development"; "3" = "test"; "4" = "production" }

do {
    $choice = Read-Host "Enter choice (1-4)"
} while (-not $envMap.ContainsKey($choice))

$DeployEnv = $envMap[$choice]
$OverlayDir = Join-Path $PSScriptRoot "..\$DeployEnv"
Write-Host "Selected: $DeployEnv" -ForegroundColor Green
Write-Host ""

# =============================================================================
# Validate environment variables
# =============================================================================
$missing = @()

$requiredVars = @(
    "PROPERTIES_ENCRYPTION_KEY",
    "REPOSITORY_USER",
    "REPOSITORY_PASSWORD",
    "DOCKER_USER",
    "DOCKER_PASSWORD"
)
foreach ($var in $requiredVars) {
    $val = [System.Environment]::GetEnvironmentVariable($var)
    if ([string]::IsNullOrWhiteSpace($val)) {
        $missing += $var
    }
}
if ($missing.Count -gt 0) {
    Write-Host "Error: missing required environment variables:" -ForegroundColor Red
    foreach ($var in $missing) {
        Write-Host "  - $var" -ForegroundColor Red
    }
    exit 1
}

# =============================================================================
# Validate local CA files exist
# =============================================================================
if ($DeployEnv -eq "local") {
    if (-not (Test-Path $CaCert)) {
        Write-Host "Error: CA certificate not found at $CaCert" -ForegroundColor Red
        exit 1
    }
    if (-not (Test-Path $CaKey)) {
        Write-Host "Error: CA key not found at $CaKey" -ForegroundColor Red
        exit 1
    }
}

# =============================================================================
# Verify prerequisites
# =============================================================================
Write-Host "Checking prerequisites..." -ForegroundColor Cyan
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: 'kubectl' is required but not found on PATH." -ForegroundColor Red
    exit 1
}
if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
    Write-Host "  helm not found. Installing via winget..." -ForegroundColor Yellow
    winget install Helm.Helm --accept-source-agreements --accept-package-agreements
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    $env:PATH = [System.Environment]::GetEnvironmentVariable("PATH", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("PATH", "User")
    if (-not (Get-Command helm -ErrorAction SilentlyContinue)) {
        Write-Host "Error: helm installed but not found on PATH. Restart your terminal and try again." -ForegroundColor Red
        exit 1
    }
    Write-Host "  helm installed." -ForegroundColor Green
}
$Openssl = (Get-Command openssl -ErrorAction SilentlyContinue).Source
if (-not $Openssl) {
    $git = (Get-Command git -ErrorAction SilentlyContinue).Source
    if ($git) {
        $Openssl = Get-ChildItem -Path (Split-Path (Split-Path $git -Parent) -Parent) -Recurse -Filter openssl.exe -ErrorAction SilentlyContinue |
            Select-Object -First 1 -ExpandProperty FullName
    }
}
if (-not $Openssl) {
    Write-Host "Error: 'openssl' is required to generate JWT signing keys but was not found (install OpenSSL or Git for Windows)." -ForegroundColor Red
    exit 1
}

# =============================================================================
# Ensure nginx-ingress-controller
# =============================================================================
Write-Host "Checking for nginx-ingress-controller..." -ForegroundColor Cyan

$nginxDeployment = $null
try { $nginxDeployment = kubectl get deployment ingress-nginx-controller -n ingress-nginx -o jsonpath='{.metadata.name}' 2>$null } catch { }

if (-not [string]::IsNullOrWhiteSpace($nginxDeployment)) {
    Write-Host "  nginx-ingress-controller already installed." -ForegroundColor Green
} else {
    Write-Host "  nginx-ingress-controller not found. Installing via Helm..." -ForegroundColor Yellow

    # Clean up orphaned resources from a previous install that lack Helm ownership labels
    $orphanedNs = $null
    try { $orphanedNs = kubectl get namespace ingress-nginx -o jsonpath='{.metadata.name}' 2>$null } catch { }
    if (-not [string]::IsNullOrWhiteSpace($orphanedNs)) {
        Write-Host "  Cleaning up orphaned ingress-nginx resources..." -ForegroundColor Yellow
        $helmRelease = $null
        try { $helmRelease = helm list -n ingress-nginx -q --filter ingress-nginx 2>$null } catch { }
        if (-not [string]::IsNullOrWhiteSpace($helmRelease)) {
            helm uninstall ingress-nginx -n ingress-nginx 2>$null
        }
        kubectl delete namespace ingress-nginx --ignore-not-found --wait
    }

    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    helm repo update
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    $ingressArgs = @()
    if ($DeployEnv -eq "local") {
        $ingressArgs = @("--set", "controller.hostPort.enabled=true", "--set", "controller.service.type=NodePort")
    }

    helm install ingress-nginx ingress-nginx/ingress-nginx `
        --namespace ingress-nginx `
        --create-namespace `
        @ingressArgs `
        --wait `
        --timeout 5m
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Write-Host "  nginx-ingress-controller installed." -ForegroundColor Green
}

# =============================================================================
# Ensure cert-manager
# =============================================================================
Write-Host "Checking for cert-manager..." -ForegroundColor Cyan

$certManagerDeployment = $null
try { $certManagerDeployment = kubectl get deployment cert-manager -n cert-manager -o jsonpath='{.metadata.name}' 2>$null } catch { }

if (-not [string]::IsNullOrWhiteSpace($certManagerDeployment)) {
    Write-Host "  cert-manager already installed." -ForegroundColor Green
} else {
    Write-Host "  cert-manager not found. Installing via Helm..." -ForegroundColor Yellow

    helm repo add jetstack https://charts.jetstack.io
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    helm repo update
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    helm install cert-manager jetstack/cert-manager `
        --namespace cert-manager `
        --create-namespace `
        --set crds.enabled=true `
        --wait `
        --timeout 5m
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

    Write-Host "  cert-manager installed." -ForegroundColor Green
}

# =============================================================================
# Create namespace
# =============================================================================
Write-Host "Creating namespace $Namespace..." -ForegroundColor Cyan
kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Deploy application secrets
# =============================================================================
Write-Host "Deploying granite-secrets..." -ForegroundColor Cyan
$secretArgs = @(
    "--from-literal=PROPERTIES_ENCRYPTION_KEY=$env:PROPERTIES_ENCRYPTION_KEY",
    "--from-literal=REPOSITORY_USER=$env:REPOSITORY_USER",
    "--from-literal=REPOSITORY_PASSWORD=$env:REPOSITORY_PASSWORD",
    "--from-literal=DOCKER_USER=$env:DOCKER_USER",
    "--from-literal=DOCKER_PASSWORD=$env:DOCKER_PASSWORD"
)
$optionalVars = @(
    "DB_USERNAME",
    "DB_PASSWORD",
    "GRANITE_KEYSTORE_LOCATION",
    "GRANITE_KEYSTORE_PASSWORD",
    "GRANITE_TRUSTSTORE_LOCATION",
    "GRANITE_TRUSTSTORE_PASSWORD",
    "OKTA_ENABLED",
    "OKTA_ENABLED_DEVELOPMENT",
    "OKTA_ENABLED_TEST",
    "OKTA_ISSUER",
    "OKTA_ISSUER_DEVELOPMENT",
    "OKTA_ISSUER_TEST",
    "OKTA_CLIENT_ID",
    "OKTA_CLIENT_ID_DEVELOPMENT",
    "OKTA_CLIENT_ID_TEST",
    "OKTA_REDIRECT_URI",
    "OKTA_REDIRECT_URI_DEVELOPMENT",
    "OKTA_REDIRECT_URI_TEST",
    "OKTA_POST_LOGOUT_REDIRECT_URI",
    "OKTA_POST_LOGOUT_REDIRECT_URI_DEVELOPMENT",
    "OKTA_POST_LOGOUT_REDIRECT_URI_TEST",
    "OKTA_ROLE_ASSIGNMENT",
    "OKTA_UNMAPPED_GROUPS",
    "OKTA_ROLE_ASSIGNMENT_DEVELOPMENT",
    "OKTA_UNMAPPED_GROUPS_DEVELOPMENT",
    "OKTA_ROLE_ASSIGNMENT_TEST",
    "OKTA_UNMAPPED_GROUPS_TEST",
    "OKTA_KEYSTORE_LOCATION",
    "OKTA_KEYSTORE_ALIAS",
    "OKTA_KEYSTORE_PASSWORD",
    "OKTA_KEYSTORE_PASSWORD_DEVELOPMENT",
    "OKTA_KEYSTORE_PASSWORD_TEST",
    "GRANITE_BOOTSTRAP_ADMIN_PASSWORD"
)
foreach ($var in $optionalVars) {
    $val = [System.Environment]::GetEnvironmentVariable($var)
    if (-not [string]::IsNullOrWhiteSpace($val)) {
        $secretArgs += "--from-literal=$var=$val"
        Write-Host "    + $var" -ForegroundColor DarkGray
    }
}
kubectl create secret generic granite-secrets `
    --namespace=$Namespace `
    @secretArgs `
    --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Deploy ghcr.io registry credentials
# =============================================================================
Write-Host "Deploying ghcr-registry secret..." -ForegroundColor Cyan
kubectl create secret docker-registry ghcr-registry `
    --namespace=$Namespace `
    --docker-server=ghcr.io `
    --docker-username=$env:REPOSITORY_USER `
    --docker-password=$env:REPOSITORY_PASSWORD `
    --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Deploy Session JWT signing keys
# =============================================================================
if ($DeployEnv -eq "local") {
    $jwtKeyDir         = Join-Path $HOME ".granite\dev-keys"
    $defaultJwtPrivate = Join-Path $jwtKeyDir "jwt-test-private-key.pem"
    $defaultJwtPublic  = Join-Path $jwtKeyDir "jwt-test-public-key.pem"
}
else {
    $jwtKeyDir         = Join-Path $HOME ".granite\$DeployEnv-keys"
    $defaultJwtPrivate = Join-Path $jwtKeyDir "jwt-private-key.pem"
    $defaultJwtPublic  = Join-Path $jwtKeyDir "jwt-public-key.pem"
}
$jwtPrivateKeyFile = if ([string]::IsNullOrWhiteSpace($env:GRANITE_JWT_PRIVATE_KEY_FILE)) { $defaultJwtPrivate } else { $env:GRANITE_JWT_PRIVATE_KEY_FILE }
$jwtPublicKeyFile  = if ([string]::IsNullOrWhiteSpace($env:GRANITE_JWT_PUBLIC_KEY_FILE))  { $defaultJwtPublic }  else { $env:GRANITE_JWT_PUBLIC_KEY_FILE }

Write-Host "Checking Session JWT signing keys..." -ForegroundColor Cyan
$hasPrivate = Test-Path $jwtPrivateKeyFile
$hasPublic  = Test-Path $jwtPublicKeyFile
if ($hasPrivate -and $hasPublic) {
    Write-Host "  Using existing keys:" -ForegroundColor Green
}
elseif ($hasPrivate -or $hasPublic) {
    Write-Host "Error: only one JWT key file exists; provide both or neither:" -ForegroundColor Red
    Write-Host "  $jwtPrivateKeyFile" -ForegroundColor Red
    Write-Host "  $jwtPublicKeyFile" -ForegroundColor Red
    exit 1
}
else {
    if ($DeployEnv -eq "test" -or $DeployEnv -eq "production") {
        Write-Host "  No JWT keys found for $DeployEnv. New keys invalidate every token issued with previous keys." -ForegroundColor Yellow
        $answer = Read-Host "  Generate a new JWT key pair? (y/N)"
        if ($answer -notmatch '^[Yy]$') {
            Write-Host "Aborted: set GRANITE_JWT_PRIVATE_KEY_FILE and GRANITE_JWT_PUBLIC_KEY_FILE to the existing keys." -ForegroundColor Red
            exit 1
        }
    }
    Write-Host "  Generating RSA 2048-bit JWT key pair..." -ForegroundColor Yellow
    foreach ($keyDir in @((Split-Path $jwtPrivateKeyFile -Parent),(Split-Path $jwtPublicKeyFile -Parent))) {
        New-Item -ItemType Directory -Force -Path $keyDir | Out-Null
    }
    $previousPreference    = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $Openssl genpkey -quiet -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out $jwtPrivateKeyFile 2>&1 | Out-Null
    $genpkeyExit = $LASTEXITCODE
    if ($genpkeyExit -eq 0) {
        & $Openssl pkey -in $jwtPrivateKeyFile -pubout -out $jwtPublicKeyFile 2>&1 | Out-Null
    }
    $pkeyExit              = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    if ($genpkeyExit -ne 0 -or $pkeyExit -ne 0) {
        Remove-Item -Force -ErrorAction SilentlyContinue $jwtPrivateKeyFile,$jwtPublicKeyFile
        Write-Host "Error: openssl failed to generate the JWT key pair." -ForegroundColor Red
        exit 1
    }
    Write-Host "  Generated:" -ForegroundColor Green
}
Write-Host "    $jwtPrivateKeyFile" -ForegroundColor DarkGray
Write-Host "    $jwtPublicKeyFile" -ForegroundColor DarkGray

Write-Host "Deploying granite-session-jwt..." -ForegroundColor Cyan
kubectl create secret generic granite-session-jwt `
    --namespace=$Namespace `
    "--from-file=jwt-private-key.pem=$jwtPrivateKeyFile" `
    "--from-file=jwt-public-key.pem=$jwtPublicKeyFile" `
    --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Deploy FEC service client credential
# =============================================================================
$fecSecretFile = Join-Path $jwtKeyDir "fec-service-client-secret"

Write-Host "Checking FEC service client secret..." -ForegroundColor Cyan
if (-not [string]::IsNullOrWhiteSpace($env:FEC_SERVICE_CLIENT_SECRET)) {
    $fecSecret = $env:FEC_SERVICE_CLIENT_SECRET
    Write-Host "  Using FEC_SERVICE_CLIENT_SECRET from the environment." -ForegroundColor Green
}
elseif ((Test-Path $fecSecretFile) -and (Get-Item $fecSecretFile).Length -gt 0) {
    $fecSecret = (Get-Content -Raw $fecSecretFile).TrimEnd("`r","`n")
    Write-Host "  Using existing secret: $fecSecretFile" -ForegroundColor Green
}
else {
    Write-Host "  Generating FEC service client secret..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path (Split-Path $fecSecretFile -Parent) | Out-Null
    $fecSecretBytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($fecSecretBytes)
    $rng.Dispose()
    $fecSecret = [Convert]::ToBase64String($fecSecretBytes)
    [System.IO.File]::WriteAllText($fecSecretFile, $fecSecret)
    & icacls $fecSecretFile /inheritance:r /grant:r "$($env:USERNAME):F" | Out-Null
    Write-Host "  Generated: $fecSecretFile" -ForegroundColor Green
}
$sha256 = [System.Security.Cryptography.SHA256]::Create()
$fecSecretSha256 = ([BitConverter]::ToString($sha256.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($fecSecret))) -replace '-', '').ToLowerInvariant()
$sha256.Dispose()

Write-Host "Deploying granite-fec-service-client..." -ForegroundColor Cyan
$fecTempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("granite-fec-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $fecTempDir | Out-Null
& icacls $fecTempDir /inheritance:r /grant:r "$($env:USERNAME):(OI)(CI)F" | Out-Null
try {
    [System.IO.File]::WriteAllText((Join-Path $fecTempDir "secret"), $fecSecret)
    [System.IO.File]::WriteAllText((Join-Path $fecTempDir "secret-sha256"), $fecSecretSha256)
    kubectl create secret generic granite-fec-service-client `
        --namespace=$Namespace `
        "--from-file=secret=$(Join-Path $fecTempDir 'secret')" `
        "--from-file=secret-sha256=$(Join-Path $fecTempDir 'secret-sha256')" `
        --dry-run=client -o yaml | kubectl apply -f -
    $fecExit = $LASTEXITCODE
}
finally {
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $fecTempDir
}
if ($fecExit -ne 0) { exit $fecExit }



# =============================================================================
# Deploy local CA key pair
# =============================================================================
if ($DeployEnv -eq "local") {
    Write-Host "Deploying granite-ca-key-pair..." -ForegroundColor Cyan
    kubectl create secret tls granite-ca-key-pair `
        --namespace=$Namespace `
        --cert=$CaCert `
        --key=$CaKey `
        --dry-run=client -o yaml | kubectl apply -f -
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# =============================================================================
# Deploy system manifest
# =============================================================================
Write-Host "Deploying overlay $DeployEnv..." -ForegroundColor Cyan
kubectl apply -k $OverlayDir
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Restart all deployments
# =============================================================================
if ($DeployEnv -eq "local" -or $DeployEnv -eq "development") {
    Write-Host "Restarting all deployments to pick up latest images..." -ForegroundColor Cyan
    $deployments = kubectl -n $Namespace get deployments -o jsonpath='{.items[*].metadata.name}'
    foreach ($dep in $deployments.Split(' ')) {
        if ([string]::IsNullOrWhiteSpace($dep)) { continue }
        Write-Host "    - $dep" -ForegroundColor DarkGray
        kubectl -n $Namespace rollout restart deployment/$dep
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
}

Write-Host ""
Write-Host "Deployment complete ($DeployEnv)." -ForegroundColor Green
