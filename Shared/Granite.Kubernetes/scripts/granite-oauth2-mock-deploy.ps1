#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Namespace   = "granite"
$ManifestDir = Join-Path $PSScriptRoot "..\oauth2-mock"

# =============================================================================
# Verify prerequisites
# =============================================================================
Write-Host "Checking prerequisites..." -ForegroundColor Cyan
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: 'kubectl' is required but not found on PATH." -ForegroundColor Red
    exit 1
}

# =============================================================================
# Confirm cluster
# =============================================================================
$ErrorActionPreference = "Continue"
$context = kubectl config current-context
$ErrorActionPreference = "Stop"
Write-Host "  Current context: $context" -ForegroundColor DarkGray
if ($context -ne "docker-desktop") {
    Write-Host "The OAuth2 mock server is for local testing only." -ForegroundColor Yellow
    $answer = Read-Host "Deploy it to context '$context' anyway? (y/N)"
    if ($answer -notmatch '^[Yy]$') {
        Write-Host "Aborted." -ForegroundColor Red
        exit 0
    }
}

# =============================================================================
# Create namespace
# =============================================================================
Write-Host "Creating namespace $Namespace..." -ForegroundColor Cyan
kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Deploy OAuth2 mock server
# =============================================================================
Write-Host "Deploying granite-oauth2-mock..." -ForegroundColor Cyan
kubectl apply -k $ManifestDir
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Waiting for granite-oauth2-mock to become ready..." -ForegroundColor Cyan
kubectl -n $Namespace rollout status deployment/granite-oauth2-mock --timeout=180s
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Okta client key store
# =============================================================================
$oktaKeystoreFile     = if ([string]::IsNullOrWhiteSpace($env:GRANITE_OKTA_KEYSTORE_FILE)) { Join-Path $HOME ".granite\dev-keys\okta-client.p12" } else { $env:GRANITE_OKTA_KEYSTORE_FILE }
$oktaKeystorePassword = if ([string]::IsNullOrWhiteSpace($env:OKTA_KEYSTORE_PASSWORD)) { "granite-local" } else { $env:OKTA_KEYSTORE_PASSWORD }

Write-Host "Checking Okta client key store..." -ForegroundColor Cyan
if (Test-Path $oktaKeystoreFile) {
    Write-Host "  Using existing key store:" -ForegroundColor Green
}
else {
    $Keytool = (Get-Command keytool -ErrorAction SilentlyContinue).Source
    if (-not $Keytool -and -not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)) {
        $candidate = Join-Path $env:JAVA_HOME "bin\keytool.exe"
        if (Test-Path $candidate) { $Keytool = $candidate }
    }
    if (-not $Keytool) {
        Write-Host "Error: 'keytool' (JDK) is required to generate the Okta client key store but was not found on PATH or in JAVA_HOME." -ForegroundColor Red
        exit 1
    }
    Write-Host "  Generating RSA 2048-bit Okta client key store..." -ForegroundColor Yellow
    New-Item -ItemType Directory -Force -Path (Split-Path $oktaKeystoreFile -Parent) | Out-Null
    $previousPreference    = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $Keytool -genkeypair -alias okta-client -keyalg RSA -keysize 2048 -validity 3650 -dname "CN=granite-session-local" -storetype PKCS12 -keystore $oktaKeystoreFile -storepass $oktaKeystorePassword 2>&1 | Out-Null
    $keytoolExit           = $LASTEXITCODE
    $ErrorActionPreference = $previousPreference
    if ($keytoolExit -ne 0) {
        Remove-Item -Force -ErrorAction SilentlyContinue $oktaKeystoreFile
        Write-Host "Error: keytool failed to generate the Okta client key store." -ForegroundColor Red
        exit 1
    }
    Write-Host "  Generated:" -ForegroundColor Green
}
Write-Host "    $oktaKeystoreFile" -ForegroundColor DarkGray

Write-Host "Deploying granite-session-okta..." -ForegroundColor Cyan
kubectl create secret generic granite-session-okta `
    --namespace=$Namespace `
    --from-file=okta-client.p12="$oktaKeystoreFile" `
    --from-literal=keystore-password="$oktaKeystorePassword" `
    --dry-run=client -o yaml | kubectl apply -f -
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Point Session and the FrontEndControllers at the mock
# =============================================================================
$patchFile = Join-Path $ManifestDir "session-commandhost-okta-patch.yaml"

$ErrorActionPreference = "Continue"
kubectl -n $Namespace get deployment granite-session-commandhost 2>$null | Out-Null
$sessionExists = ($LASTEXITCODE -eq 0)
$ErrorActionPreference = "Stop"
if ($sessionExists) {
    Write-Host "Configuring granite-session-commandhost for the mock issuer..." -ForegroundColor Cyan
    kubectl -n $Namespace patch deployment granite-session-commandhost --type=strategic --patch-file="$patchFile"
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
else {
    Write-Host "  granite-session-commandhost not deployed; skipping (re-run after granite-system-deploy.ps1)." -ForegroundColor Yellow
}

foreach ($dep in @("granite-frontendcontroller-userhost","granite-frontendcontroller-adminhost")) {
    $ErrorActionPreference = "Continue"
    kubectl -n $Namespace get deployment $dep 2>$null | Out-Null
    $depExists = ($LASTEXITCODE -eq 0)
    $ErrorActionPreference = "Stop"
    if ($depExists) {
        Write-Host "Enabling Okta login on $dep..." -ForegroundColor Cyan
        kubectl -n $Namespace set env "deployment/$dep" GRANITE_SESSION_LOGIN_OKTA_ENABLED=true
        if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
    }
    else {
        Write-Host "  $dep not deployed; skipping (re-run after granite-system-deploy.ps1)." -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Deployment complete. Run granite-oauth2-mock-start.ps1 to expose it on the host." -ForegroundColor Green
