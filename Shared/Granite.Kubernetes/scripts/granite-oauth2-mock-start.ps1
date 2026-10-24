#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Namespace = "granite"
$Port      = if ([string]::IsNullOrWhiteSpace($env:GRANITE_OAUTH2_MOCK_PORT)) { "8090" } else { $env:GRANITE_OAUTH2_MOCK_PORT }
$IssuerId  = "default"
$Issuer    = "http://host.docker.internal:$Port/$IssuerId"

# =============================================================================
# Verify prerequisites
# =============================================================================
if (-not (Get-Command kubectl -ErrorAction SilentlyContinue)) {
    Write-Host "Error: 'kubectl' is required but not found on PATH." -ForegroundColor Red
    exit 1
}

$ErrorActionPreference = "Continue"
$deployment = kubectl -n $Namespace get deployment granite-oauth2-mock -o jsonpath="{.metadata.name}" 2>$null
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($deployment)) {
    Write-Host "Error: granite-oauth2-mock is not deployed. Run granite-oauth2-mock-deploy.ps1 first." -ForegroundColor Red
    exit 1
}

Write-Host "Waiting for granite-oauth2-mock to become ready..." -ForegroundColor Cyan
kubectl -n $Namespace rollout status deployment/granite-oauth2-mock --timeout=180s
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# =============================================================================
# Expose on the host
# =============================================================================
Write-Host ""
Write-Host "OAuth2 mock server" -ForegroundColor Cyan
Write-Host "  Issuer:    $Issuer" -ForegroundColor DarkGray
Write-Host "  Discovery: $Issuer/.well-known/openid-configuration" -ForegroundColor DarkGray
Write-Host "  Session:   granite.session.okta.issuer=$Issuer" -ForegroundColor DarkGray
Write-Host ""
Write-Host "Port-forwarding 0.0.0.0:$Port -> svc/granite-oauth2-mock:8080 (Ctrl+C to stop)..." -ForegroundColor Green
kubectl -n $Namespace port-forward --address 0.0.0.0 svc/granite-oauth2-mock "${Port}:8080"
