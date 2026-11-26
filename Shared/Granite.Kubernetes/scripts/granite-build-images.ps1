#Requires -Version 5.1
param(
    [ValidateSet("local", "development")]
    [string] $Environment,
    [string] $Tag,
    [switch] $JavaOnly,
    [switch] $WebOnly,
    [switch] $DryRun
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$RepoDir       = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..")).Path
$WebDir        = Join-Path $RepoDir "FrontEnds\Granite.Web"
$VersionsFile  = Join-Path $RepoDir "gradle\granite-versions.gradle"
$ImageTagFile  = Join-Path $RepoDir ".build\image-tag"
$LogDir        = Join-Path $RepoDir ".build\image-build"
$RegistryImage = "ghcr.io/johnliebenau/granite/granite-gateway-servicehost"

function Stop-Build([string]$Message) { Write-Host "granite-build-images: $Message" -ForegroundColor Red; exit 1 }

if ($JavaOnly -and $WebOnly) { Stop-Build "-JavaOnly and -WebOnly exclude each other" }
$Java = -not $WebOnly
$Web  = -not $JavaOnly

# =============================================================================
# Tag
# =============================================================================
function Get-GradleValue([string]$Name) {
    $match = Select-String -Path $VersionsFile -Pattern "^\s*$Name\s*=\s*`"([^`"]*)`"" | Select-Object -First 1
    if ($match) { return $match.Matches[0].Groups[1].Value }
    return ""
}
$Version = (Get-GradleValue "major") + (Get-GradleValue "minor") + (Get-GradleValue "patch")
if (-not $Version) { Stop-Build "cannot read the version from $VersionsFile" }

$Suffix = switch ($Environment) { "local" { "local" } "development" { "dev" } default { "" } }
if (-not $Tag) { $Tag = $env:BUILD_IMAGE_TAG }
if (-not $Suffix) {
    if ($Tag)                   { $Suffix = ($Tag -split '-')[-1] }
    elseif ($env:CI -eq "true") { $Suffix = "dev" }
    else                        { $Suffix = "local" }
}
if (-not $Tag) { $Tag = "$Version-" + [DateTime]::UtcNow.ToString("yyyyMMddHHmmss") + "-$Suffix" }
if ($Tag -notmatch ("^" + [regex]::Escape($Version) + '-[0-9]{14}-(local|dev)$')) {
    Stop-Build "tag '$Tag' is invalid; expected $Version-<yyyyMMddHHmmss>-<local|dev>"
}
if (-not $Tag.EndsWith("-$Suffix")) { Stop-Build "tag '$Tag' does not end in -$Suffix" }

# =============================================================================
# Metadata (restored on exit)
# =============================================================================
$saved = @{}
foreach ($name in "BUILD_IMAGE_TAG", "BUILD_GIT_SHA", "BUILD_CREATED", "BUILD_USER") {
    $saved[$name] = [Environment]::GetEnvironmentVariable($name, "Process")
}
try {
    $env:BUILD_IMAGE_TAG = $Tag
    if (-not $env:BUILD_GIT_SHA) { $env:BUILD_GIT_SHA = (git -C $RepoDir rev-parse HEAD 2>$null) }
    if (-not $env:BUILD_CREATED) { $env:BUILD_CREATED = [DateTime]::UtcNow.ToString("yyyy-MM-ddTHH:mm:ssZ") }
    if (-not $env:BUILD_USER) {
        $gitUser = (git -C $RepoDir config user.name 2>$null)
        $env:BUILD_USER = if ($gitUser) { $gitUser } elseif ($env:USERNAME) { $env:USERNAME } else { $env:USER }
    }

    Write-Host "Image tag: $Tag" -ForegroundColor Green
    Write-Host "  revision $($env:BUILD_GIT_SHA), created $($env:BUILD_CREATED), user $($env:BUILD_USER)" -ForegroundColor DarkGray

    # =========================================================================
    # Prerequisites
    # =========================================================================
    $missing = @()
    $tools = @("docker", "git")
    if ($Web) { $tools += @("node", "yarn") }
    foreach ($tool in $tools) { if (-not (Get-Command $tool -ErrorAction SilentlyContinue)) { $missing += "command $tool" } }
    foreach ($var in "REPOSITORY_USER", "REPOSITORY_PASSWORD", "PROPERTIES_ENCRYPTION_KEY", "DOCKER_USER", "DOCKER_PASSWORD") {
        if (-not [Environment]::GetEnvironmentVariable($var)) { $missing += "variable $var" }
    }
    if ($missing.Count -gt 0) {
        foreach ($item in $missing) { Write-Host "  missing $item" -ForegroundColor Red }
        if (-not $DryRun) { Stop-Build "prerequisites missing" }
    } else {
        Write-Host "Prerequisites: ok" -ForegroundColor Green
    }

    $javaCommand = "Set-Location '$RepoDir'; .\gradlew.bat build jib"
    $webCommand  = "Set-Location '$WebDir'; yarn run build-applications; if (`$LASTEXITCODE) { exit 1 }; " +
                   "foreach (`$s in 'containerize-granite-web-apphost','containerize-granite-web-clienthost','push-granite-web-apphost','push-granite-web-clienthost') { yarn run `$s; if (`$LASTEXITCODE) { exit 1 } }"

    if ($DryRun) {
        Write-Host "[dry-run] docker manifest inspect ${RegistryImage}:$Tag (must not exist)" -ForegroundColor Yellow
        if ($Java) { Write-Host "[dry-run] java: $javaCommand  > .build\image-build\$Tag-java.log" -ForegroundColor Yellow }
        if ($Web)  { Write-Host "[dry-run] web:  $webCommand  > .build\image-build\$Tag-web.log" -ForegroundColor Yellow }
        Write-Host "[dry-run] on success: write $Tag to .build\image-tag" -ForegroundColor Yellow
        exit 0
    }

    # =========================================================================
    # Uniqueness
    # =========================================================================
    $env:REPOSITORY_PASSWORD | docker login ghcr.io -u $env:REPOSITORY_USER --password-stdin | Out-Null
    if ($LASTEXITCODE -ne 0) { Stop-Build "docker login to ghcr.io failed" }
    docker manifest inspect "${RegistryImage}:$Tag" 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { Stop-Build "Tag $Tag already exists ($RegistryImage)" }

    # =========================================================================
    # Build lanes
    # =========================================================================
    New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
    $lanes = @()
    if ($Java) { $lanes += @{ Name = "java"; Command = $javaCommand } }
    if ($Web)  { $lanes += @{ Name = "web";  Command = $webCommand } }
    $processes = @()
    foreach ($lane in $lanes) {
        $log = Join-Path $LogDir "$Tag-$($lane.Name).log"
        Write-Host "$($lane.Name) lane: logging to .build\image-build\$Tag-$($lane.Name).log" -ForegroundColor Cyan
        $process = Start-Process powershell -NoNewWindow -PassThru `
            -ArgumentList @("-NoProfile", "-Command", "& { $($lane.Command) } *> '$log'; exit `$LASTEXITCODE")
        $processes += @{ Name = $lane.Name; Process = $process; Log = $log }
    }
    $failed = @()
    foreach ($entry in $processes) {
        $entry.Process.WaitForExit()
        if ($entry.Process.ExitCode -eq 0) { Write-Host "  $($entry.Name) lane succeeded" -ForegroundColor Green }
        else { $failed += $entry }
    }
    if ($failed.Count -gt 0) {
        foreach ($entry in $failed) {
            Write-Host "$($entry.Name) lane failed; last 30 lines of $($entry.Log):" -ForegroundColor Red
            Get-Content $entry.Log -Tail 30 | ForEach-Object { Write-Host $_ }
        }
        exit 1
    }

    New-Item -ItemType Directory -Force -Path (Split-Path $ImageTagFile -Parent) | Out-Null
    Set-Content -NoNewline -Path $ImageTagFile -Value $Tag
    Write-Host "Built and pushed $Tag; written to .build\image-tag" -ForegroundColor Green
} finally {
    foreach ($name in $saved.Keys) { [Environment]::SetEnvironmentVariable($name, $saved[$name], "Process") }
}
