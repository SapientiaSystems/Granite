#Requires -Version 5.1
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Namespace = "granite"

Write-Host ""
Write-Host "Granite System Status" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Pods:" -ForegroundColor Yellow
kubectl get pods --namespace=$Namespace -o wide
Write-Host ""

Write-Host "Services:" -ForegroundColor Yellow
kubectl get services --namespace=$Namespace
Write-Host ""

Write-Host "Deployments:" -ForegroundColor Yellow
kubectl get deployments --namespace=$Namespace
Write-Host ""

Write-Host "HPAs:" -ForegroundColor Yellow
kubectl get hpa --namespace=$Namespace
Write-Host ""

Write-Host "Ingress:" -ForegroundColor Yellow
kubectl get ingress --namespace=$Namespace
Write-Host ""

Write-Host "Certificates and Issuers:" -ForegroundColor Yellow
kubectl get certificate,issuer --namespace=$Namespace
Write-Host ""

Write-Host "ClusterIssuers:" -ForegroundColor Yellow
kubectl get clusterissuer
Write-Host ""
