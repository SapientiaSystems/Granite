#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Namespace = "granite"

# =============================================================================
# Confirm
# =============================================================================
Write-Host ""
Write-Host "This will delete ALL resources in the '$Namespace' namespace:" -ForegroundColor Yellow
Write-Host "  - Deployments, Services, HPAs, Ingresses"
Write-Host "  - Secrets (granite-secrets, granite-tls, granite-ca-key-pair, ghcr-registry, granite-session-jwt, granite-fec-service-client)"
Write-Host "  - ClusterIssuers (granite-letsencrypt-staging, granite-letsencrypt-prod)"
Write-Host "  - Namespace"
Write-Host ""

$confirm = Read-Host "Type 'yes' to confirm"
if ($confirm -ne "yes") {
    Write-Host "Aborted." -ForegroundColor Red
    exit 0
}

Write-Host ""

# =============================================================================
# Delete all resources in the namespace
# =============================================================================
Write-Host "Deleting all resources in namespace $Namespace..." -ForegroundColor Cyan
kubectl delete all --all --namespace=$Namespace
if ($LASTEXITCODE -ne 0) { Write-Host "Warning: some resources may not have been deleted" -ForegroundColor Yellow }

Write-Host "Deleting ingresses..." -ForegroundColor Cyan
kubectl delete ingress --all --namespace=$Namespace
if ($LASTEXITCODE -ne 0) { Write-Host "Warning: no ingresses found" -ForegroundColor Yellow }

Write-Host "Deleting secrets..." -ForegroundColor Cyan
kubectl delete secret granite-secrets --namespace=$Namespace --ignore-not-found
kubectl delete secret granite-tls --namespace=$Namespace --ignore-not-found
kubectl delete secret granite-ca-key-pair --namespace=$Namespace --ignore-not-found
kubectl delete secret ghcr-registry --namespace=$Namespace --ignore-not-found
kubectl delete secret granite-session-jwt --namespace=$Namespace --ignore-not-found
kubectl delete secret granite-fec-service-client --namespace=$Namespace --ignore-not-found

Write-Host "Deleting ClusterIssuers..." -ForegroundColor Cyan
kubectl delete clusterissuer granite-letsencrypt-staging granite-letsencrypt-prod --ignore-not-found

Write-Host "Deleting HPAs..." -ForegroundColor Cyan
kubectl delete hpa --all --namespace=$Namespace
if ($LASTEXITCODE -ne 0) { Write-Host "Warning: no HPAs found" -ForegroundColor Yellow }

# =============================================================================
# Delete namespace
# =============================================================================
Write-Host "Deleting namespace $Namespace..." -ForegroundColor Cyan
kubectl delete namespace $Namespace --ignore-not-found

Write-Host ""
Write-Host "Clean complete. Ready for redeployment." -ForegroundColor Green
