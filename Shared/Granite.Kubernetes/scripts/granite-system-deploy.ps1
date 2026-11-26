#Requires -Version 5.1
param(
    [ValidateSet("local", "development", "test", "production")]
    [string] $Environment,
    [string] $Tag,
    [switch] $PrintCurrentTag,
    [switch] $Yes,
    [switch] $Wait,
    [int]    $TimeoutSeconds = 300
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$KubernetesDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$RepoDir       = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
$CertsDir      = Join-Path $PSScriptRoot "..\..\Granite.Foundation\Components\Granite.Foundation.Core\certs"
$CaCert        = Join-Path $CertsDir "ca.crt"
$CaKey         = Join-Path $CertsDir "ca.key"
$ImageTagFile  = Join-Path $RepoDir ".build\image-tag"
$VersionsFile  = Join-Path $RepoDir "gradle\granite-versions.gradle"

. (Join-Path $PSScriptRoot "granite-environment.ps1")

# =============================================================================
# Arguments
# =============================================================================
if ($TimeoutSeconds -le 0) {
    Write-Host "-TimeoutSeconds must be a positive number of seconds" -ForegroundColor Red
    exit 1
}
if ($Yes -and -not $Environment) {
    Write-Host "-Yes needs -Environment" -ForegroundColor Red
    exit 1
}
if ($PrintCurrentTag -and -not $Environment) {
    Write-Host "-PrintCurrentTag needs -Environment" -ForegroundColor Red
    exit 1
}

$selection = Select-GraniteEnvironment $Environment
$DeployEnv = $selection.Environment
$Namespace = $selection.Namespace
Use-GraniteKubeContext $DeployEnv

if ($PrintCurrentTag) {
    $image = $null
    try { $image = kubectl -n $Namespace get deployment granite-gateway-servicehost -o jsonpath='{.spec.template.spec.containers[0].image}' 2>$null } catch { }
    if ([string]::IsNullOrWhiteSpace($image)) { exit 1 }
    Write-Output ($image -replace '^.*:', '')
    exit 0
}

$OverlayDir = Join-Path $KubernetesDir $DeployEnv
Write-Host "Selected: $DeployEnv (namespace $Namespace)" -ForegroundColor Green
Write-Host ""

# =============================================================================
# Resolve the image tag
# =============================================================================
function Get-GradleValue([string]$Name) {
    $match = Select-String -Path $VersionsFile -Pattern "^\s*$Name\s*=\s*`"([^`"]*)`"" | Select-Object -First 1
    if ($match) { return $match.Matches[0].Groups[1].Value }
    return ""
}
$Version = (Get-GradleValue "major") + (Get-GradleValue "minor") + (Get-GradleValue "patch")
if (-not $Version) {
    Write-Host "Error: cannot read the version from $VersionsFile" -ForegroundColor Red
    exit 1
}
$TagSuffix = @{ "local" = "local"; "development" = "dev"; "test" = "test"; "production" = "prod" }[$DeployEnv]

if ($DeployEnv -eq "local" -or $DeployEnv -eq "development") {
    $ImageTag = $Tag
    if (-not $ImageTag -and (Test-Path $ImageTagFile)) {
        $ImageTag = (Get-Content -Raw $ImageTagFile).Trim()
    }
    if (-not $ImageTag) {
        Write-Host "Error: no image tag; build first: granite-build-images (or pass -Tag)" -ForegroundColor Red
        exit 1
    }
    $VersionPattern = [regex]::Escape($Version)
    $VersionLabel   = $Version
} else {
    if ($Tag) {
        Write-Host "Error: -Tag applies to local and development only; $DeployEnv uses IMAGE_TAG in $DeployEnv/kustomization.yaml" -ForegroundColor Red
        exit 1
    }
    $tagLine  = Select-String -Path (Join-Path $OverlayDir "kustomization.yaml") -Pattern '^\s*-\s*IMAGE_TAG=(.*)$' | Select-Object -First 1
    $ImageTag = if ($tagLine) { $tagLine.Matches[0].Groups[1].Value.Trim() } else { "" }
    $VersionPattern = '[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?'
    $VersionLabel   = "<version>"
}
if ($ImageTag -notmatch "^$VersionPattern-[0-9]{14}-$TagSuffix$") {
    if ($ImageTag -eq "unset") {
        Write-Host "Error: IMAGE_TAG in $DeployEnv/kustomization.yaml is unset; promote a tag with granite-image-tags, set it there and commit it" -ForegroundColor Red
    } else {
        Write-Host "Error: image tag '$ImageTag' is not valid for $DeployEnv; expected $VersionLabel-<yyyyMMddHHmmss>-$TagSuffix" -ForegroundColor Red
    }
    exit 1
}
Write-Host "Image tag: $ImageTag" -ForegroundColor Green
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

if ($DeployEnv -eq "local") {
    $legacy = $null
    try { $legacy = kubectl get namespace $GraniteLegacyNamespace -o jsonpath='{.metadata.name}' 2>$null } catch { }
    if (-not [string]::IsNullOrWhiteSpace($legacy)) {
        Write-Host "  The legacy namespace '$GraniteLegacyNamespace' still exists; its Ingress claims the same hosts as $Namespace." -ForegroundColor Yellow
        $legacyConfirm = if ($Yes) { "" } else { Read-Host "  Delete namespace '$GraniteLegacyNamespace' and everything in it? Type 'yes' to delete" }
        if ($legacyConfirm -eq "yes") {
            kubectl delete namespace $GraniteLegacyNamespace --wait
            if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
            Write-Host "  Deleted namespace '$GraniteLegacyNamespace'." -ForegroundColor Green
        } else {
            Write-Host "  Kept namespace '$GraniteLegacyNamespace'. Remove it later with: kubectl delete namespace $GraniteLegacyNamespace" -ForegroundColor Yellow
        }
    }
}
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
    "PASSWORD_ENABLED",
    "PASSWORD_ENABLED_DEVELOPMENT",
    "PASSWORD_ENABLED_TEST",
    "OKTA_ISSUER",
    "OKTA_ISSUER_DEVELOPMENT",
    "OKTA_ISSUER_TEST",
    "OKTA_CLIENT_ID",
    "OKTA_CLIENT_ID_DEVELOPMENT",
    "OKTA_CLIENT_ID_TEST",
    "OKTA_REDIRECT_URI",
    "OKTA_REDIRECT_URI_DEVELOPMENT",
    "OKTA_REDIRECT_URI_TEST",
    "OKTA_REDIRECT_URI_CLIENT",
    "OKTA_REDIRECT_URI_CLIENT_DEVELOPMENT",
    "OKTA_REDIRECT_URI_CLIENT_TEST",
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
    "GRANITE_BOOTSTRAP_ADMIN_PASSWORD",
    "KAFKA_BOOTSTRAP_SERVERS",
    "KAFKA_SECURITY_PROTOCOL",
    "KAFKA_API_KEY",
    "KAFKA_API_SECRET",
    "KAFKA_SCHEMA_REGISTRY_URL",
    "KAFKA_SCHEMA_REGISTRY_API_KEY",
    "KAFKA_SCHEMA_REGISTRY_API_SECRET"
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
        if ($Yes) {
            Write-Host "Aborted (-Yes): no JWT keys for $DeployEnv; set GRANITE_JWT_PRIVATE_KEY_FILE and GRANITE_JWT_PUBLIC_KEY_FILE" -ForegroundColor Red
            exit 1
        }
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
# Deploy Agent service client credential
# =============================================================================
$agentSecretFile = Join-Path $jwtKeyDir "agent-service-client-secret"

Write-Host "Checking Agent service client secret..." -ForegroundColor Cyan
if (-not [string]::IsNullOrWhiteSpace($env:AGENT_SERVICE_CLIENT_SECRET)) {
    $agentSecret = $env:AGENT_SERVICE_CLIENT_SECRET
    Write-Host "  Using AGENT_SERVICE_CLIENT_SECRET from the environment." -ForegroundColor Green
}
elseif ((Test-Path $agentSecretFile) -and (Get-Item $agentSecretFile).Length -gt 0) {
    $agentSecret = (Get-Content -Raw $agentSecretFile).TrimEnd("`r","`n")
    Write-Host "  Using existing secret: $agentSecretFile" -ForegroundColor Green
}
else {
    Write-Host "  Generating Agent service client secret..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path (Split-Path $agentSecretFile -Parent) | Out-Null
    $agentSecretBytes = New-Object byte[] 32
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    $rng.GetBytes($agentSecretBytes)
    $rng.Dispose()
    $agentSecret = [Convert]::ToBase64String($agentSecretBytes)
    [System.IO.File]::WriteAllText($agentSecretFile, $agentSecret)
    & icacls $agentSecretFile /inheritance:r /grant:r "$($env:USERNAME):F" | Out-Null
    Write-Host "  Generated: $agentSecretFile" -ForegroundColor Green
}
$sha256 = [System.Security.Cryptography.SHA256]::Create()
$agentSecretSha256 = ([BitConverter]::ToString($sha256.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($agentSecret))) -replace '-', '').ToLowerInvariant()
$sha256.Dispose()

Write-Host "Deploying granite-agent-service-client..." -ForegroundColor Cyan
$agentTempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("granite-agent-" + [Guid]::NewGuid().ToString("N"))
New-Item -ItemType Directory -Force -Path $agentTempDir | Out-Null
& icacls $agentTempDir /inheritance:r /grant:r "$($env:USERNAME):(OI)(CI)F" | Out-Null
try {
    [System.IO.File]::WriteAllText((Join-Path $agentTempDir "secret"), $agentSecret)
    [System.IO.File]::WriteAllText((Join-Path $agentTempDir "secret-sha256"), $agentSecretSha256)
    kubectl create secret generic granite-agent-service-client `
        --namespace=$Namespace `
        "--from-file=secret=$(Join-Path $agentTempDir 'secret')" `
        "--from-file=secret-sha256=$(Join-Path $agentTempDir 'secret-sha256')" `
        --dry-run=client -o yaml | kubectl apply -f -
    $agentExit = $LASTEXITCODE
}
finally {
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $agentTempDir
}
if ($agentExit -ne 0) { exit $agentExit }

# =============================================================================
# Deploy Claude API key for the virtual assistant (never generated or logged)
# =============================================================================
Write-Host "Checking Claude API key..." -ForegroundColor Cyan
if (-not [string]::IsNullOrWhiteSpace($env:ANTHROPIC_API_KEY)) {
    $claudeTempDir = Join-Path ([System.IO.Path]::GetTempPath()) ("granite-claude-" + [Guid]::NewGuid().ToString("N"))
    New-Item -ItemType Directory -Force -Path $claudeTempDir | Out-Null
    & icacls $claudeTempDir /inheritance:r /grant:r "$($env:USERNAME):(OI)(CI)F" | Out-Null
    try {
        [System.IO.File]::WriteAllText((Join-Path $claudeTempDir "ANTHROPIC_API_KEY"), $env:ANTHROPIC_API_KEY)
        kubectl create secret generic granite-agent-claude-api `
            --namespace=$Namespace `
            "--from-file=ANTHROPIC_API_KEY=$(Join-Path $claudeTempDir 'ANTHROPIC_API_KEY')" `
            --dry-run=client -o yaml | kubectl apply -f -
        $claudeExit = $LASTEXITCODE
    }
    finally {
        Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $claudeTempDir
    }
    if ($claudeExit -ne 0) { exit $claudeExit }
    Write-Host "  Deployed granite-agent-claude-api from ANTHROPIC_API_KEY." -ForegroundColor Green
}
else {
    $previousPreference    = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    kubectl get secret granite-agent-claude-api --namespace=$Namespace 2>&1 | Out-Null
    $claudeSecretExit      = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    if ($claudeSecretExit -eq 0) {
        Write-Host "  Keeping the existing granite-agent-claude-api secret." -ForegroundColor Green
    }
    else {
        Write-Host "  ANTHROPIC_API_KEY is not set: the virtual assistant replies that it is not configured until granite-agent-claude-api exists." -ForegroundColor Yellow
    }
}



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
function Get-GraniteDeployments {
    $names = kubectl -n $Namespace get deployments -l system=granite -o jsonpath='{.items[*].metadata.name}'
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    return @("$names".Split(' ') | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
}

if ($DeployEnv -eq "local" -or $DeployEnv -eq "development") {
    Write-Host "Deploying overlay $DeployEnv with image tag $ImageTag..." -ForegroundColor Cyan
    $deployDir = Join-Path $KubernetesDir (".build\deploy-$DeployEnv-" + [DateTime]::UtcNow.ToString("yyyyMMddHHmmss"))
    New-Item -ItemType Directory -Force -Path $deployDir | Out-Null
    try {
        $rendered = kubectl kustomize $OverlayDir
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
        $repositories = @($rendered | ForEach-Object { if ($_ -match '^\s*image:\s*(\S+)') { $Matches[1] -replace ':[^:/]*$', '' } } | Sort-Object -Unique)
        $lines = @(
            "apiVersion: kustomize.config.k8s.io/v1beta1",
            "kind: Kustomization",
            "resources:",
            "  - ../../$DeployEnv",
            "images:"
        )
        foreach ($repository in $repositories) {
            $lines += "  - name: $repository"
            $lines += "    newTag: $ImageTag"
        }
        [System.IO.File]::WriteAllLines((Join-Path $deployDir "kustomization.yaml"), [string[]]$lines)
        kubectl apply -k $deployDir
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    } finally {
        Remove-Item -Recurse -Force $deployDir -ErrorAction SilentlyContinue
    }

    Write-Host "Restarting the Granite deployments in $Namespace..." -ForegroundColor Cyan
    foreach ($dep in Get-GraniteDeployments) {
        Write-Host "    - $dep" -ForegroundColor DarkGray
        kubectl -n $Namespace rollout restart deployment/$dep
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
} else {
    Write-Host "Deploying overlay $DeployEnv (image tag $ImageTag)..." -ForegroundColor Cyan
    kubectl apply -k $OverlayDir
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}

# =============================================================================
# Wait for the rollouts
# =============================================================================
if ($Wait) {
    Write-Host "Waiting for rollouts (timeout ${TimeoutSeconds}s each)..." -ForegroundColor Cyan
    foreach ($dep in Get-GraniteDeployments) {
        kubectl -n $Namespace rollout status deployment/$dep --timeout="${TimeoutSeconds}s"
        if ($LASTEXITCODE -ne 0) {
            Write-Host "Rollout of $dep did not finish" -ForegroundColor Red
            exit 1
        }
    }
}

Write-Host ""
Write-Host "Deployment complete ($DeployEnv)." -ForegroundColor Green
