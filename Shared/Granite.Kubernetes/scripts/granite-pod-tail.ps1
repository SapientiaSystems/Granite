#Requires -Version 5.1
param(
    [ValidateSet("local", "development", "test", "production")]
    [string] $Environment
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

. (Join-Path $PSScriptRoot "granite-environment.ps1")
$selection = Select-GraniteEnvironment $Environment
$DeployEnv = $selection.Environment
$Namespace = $selection.Namespace
Use-GraniteKubeContext $DeployEnv

# =============================================================================
# List pods
# =============================================================================
$ErrorActionPreference = "Continue"
$podOutput = kubectl get pods --namespace=$Namespace -o jsonpath="{.items[*].metadata.name}"
$podExit = $LASTEXITCODE
$ErrorActionPreference = "Stop"
if ($podExit -ne 0 -or [string]::IsNullOrWhiteSpace($podOutput)) {
    Write-Host "No pods found in namespace $Namespace." -ForegroundColor Red
    exit 1
}

$pods = $podOutput -split "\s+"

Write-Host ""
Write-Host "Pods in namespace $Namespace`:" -ForegroundColor Cyan
Write-Host ""
for ($i = 0; $i -lt $pods.Count; $i++) {
    Write-Host "  $($i + 1)) $($pods[$i])"
}
Write-Host ""

# =============================================================================
# Select pod
# =============================================================================
do {
    $choice = Read-Host "Select pod (1-$($pods.Count))"
    $index = 0
    $valid = [int]::TryParse($choice, [ref]$index) -and $index -ge 1 -and $index -le $pods.Count
} while (-not $valid)

$selectedPod = $pods[$index - 1]
Write-Host ""
Write-Host "Streaming logs for $selectedPod ..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop." -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Tail logs
# =============================================================================
kubectl logs --namespace=$Namespace $selectedPod --follow --tail=100
