#Requires -Version 5.1
# Dot-sourced by the Granite Kubernetes scripts; writes messages with Write-Host only.

$GraniteEnvironments    = @("local", "development", "test", "production")
$GraniteLegacyNamespace = "granite"

function Get-GraniteNamespace([string]$Environment) {
    switch ($Environment) {
        "local"       { return "granite-local" }
        "development" { return "granite-dev" }
        "test"        { return "granite-test" }
        "production"  { return "granite-prod" }
        default       { return $null }
    }
}

function Select-GraniteEnvironment([string]$Environment) {
    if ($Environment) {
        $namespace = Get-GraniteNamespace $Environment
        if (-not $namespace) {
            Write-Host "Unknown environment '$Environment' (expected: $($GraniteEnvironments -join ', '))" -ForegroundColor Red
            exit 1
        }
        return @{ Environment = $Environment; Namespace = $namespace }
    }
    Write-Host ""
    Write-Host "Select environment:" -ForegroundColor Cyan
    for ($i = 0; $i -lt $GraniteEnvironments.Count; $i++) {
        Write-Host ("  {0}) {1,-12} (namespace {2})" -f ($i + 1), $GraniteEnvironments[$i], (Get-GraniteNamespace $GraniteEnvironments[$i]))
    }
    while ($true) {
        $choice = Read-Host "Enter choice (1-$($GraniteEnvironments.Count))"
        $index  = 0
        if ([int]::TryParse($choice, [ref]$index) -and $index -ge 1 -and $index -le $GraniteEnvironments.Count) {
            $selected = $GraniteEnvironments[$index - 1]
            return @{ Environment = $selected; Namespace = (Get-GraniteNamespace $selected) }
        }
    }
}

function Use-GraniteKubeContext([string]$Environment) {
    $current = $null
    try { $current = (kubectl config current-context 2>$null) } catch { }
    if (-not $current) { $current = "none" }
    switch ($Environment) {
        "local" {
            if ($current -ne "docker-desktop") {
                Write-Host "Refusing: environment 'local' needs kubectl context 'docker-desktop' (current: '$current')." -ForegroundColor Red
                exit 1
            }
        }
        "development" {
            if (-not $env:GRANITE_DEVELOPMENT_CONTEXT) {
                Write-Host "Refusing: set GRANITE_DEVELOPMENT_CONTEXT to the kubectl context of the development cluster." -ForegroundColor Red
                exit 1
            }
            if ($current -ne $env:GRANITE_DEVELOPMENT_CONTEXT) {
                Write-Host "Refusing: environment 'development' needs kubectl context '$($env:GRANITE_DEVELOPMENT_CONTEXT)' (current: '$current')." -ForegroundColor Red
                exit 1
            }
        }
        default {
            Write-Host "Warning: the kubectl context is not checked for '$Environment' (current: '$current')." -ForegroundColor Yellow
        }
    }
}
