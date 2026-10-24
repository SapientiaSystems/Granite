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

Write-Host ""
Write-Host "Deployment complete. Run granite-oauth2-mock-start.ps1 to expose it on the host." -ForegroundColor Green
