#Requires -Version 5.1
param(
    [Parameter(Mandatory = $true, Position = 0)]
    [ValidateSet("list", "promote", "delete")]
    [string] $Command,
    [Parameter(Position = 1)] [string] $Tag,
    [Parameter(Position = 2)] [string] $Target,
    [ValidateSet("local", "dev", "test", "prod")]
    [string] $Suffix,
    [switch] $DryRun,
    [switch] $Yes
)
Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$Registry       = "ghcr.io"
$Owner          = "johnliebenau"
$OwnerScope     = "users"
$ImageNamespace = "granite"
$Api            = "https://api.github.com"
$Images = @(
    "granite-agent-commandhost",
    "granite-agent-queryhost",
    "granite-agent-testsuite",
    "granite-client-commandhost",
    "granite-client-queryhost",
    "granite-client-testsuite",
    "granite-content-commandhost",
    "granite-content-queryhost",
    "granite-content-testsuite",
    "granite-feedback-commandhost",
    "granite-feedback-queryhost",
    "granite-feedback-testsuite",
    "granite-foundation-testsuite",
    "granite-frontendcontroller-adminhost",
    "granite-frontendcontroller-testsuite",
    "granite-frontendcontroller-userhost",
    "granite-gateway-servicehost",
    "granite-outbox-servicehost",
    "granite-session-commandhost",
    "granite-session-queryhost",
    "granite-session-testsuite",
    "granite-survey-commandhost",
    "granite-survey-queryhost",
    "granite-survey-testsuite",
    "granite-user-commandhost",
    "granite-user-queryhost",
    "granite-user-testsuite",
    "granite-web-apphost",
    "granite-web-clienthost"
)
$BuildTagPattern = '-[0-9]{14}-(local|dev|test|prod)$'
if ($env:CI -eq "true") { $Yes = $true }

function Stop-Tags([string]$Message) { Write-Host "granite-image-tags: $Message" -ForegroundColor Red; exit 1 }

if (-not $env:REPOSITORY_USER -or -not $env:REPOSITORY_PASSWORD) { Stop-Tags "REPOSITORY_USER and REPOSITORY_PASSWORD must be set" }
$Headers = @{
    Authorization          = "Bearer $($env:REPOSITORY_PASSWORD)"
    Accept                 = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

function Get-VersionsUrl([string]$Image) { return "$Api/$OwnerScope/$Owner/packages/container/$ImageNamespace%2F$Image/versions" }

function Get-ImageVersions([string]$Image) {
    $result = @()
    $page = 1
    while ($true) {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Headers $Headers -Uri ("{0}?per_page=100&page={1}" -f (Get-VersionsUrl $Image), $page)
        } catch {
            $status = $null
            if ($_.Exception.Response) { $status = [int]$_.Exception.Response.StatusCode }
            if ($status -eq 404) { return ,$result }
            Stop-Tags "GitHub API request failed for $Image ($($_.Exception.Message))"
        }
        $versions = @($response.Content | ConvertFrom-Json)
        if ($versions.Count -eq 0) { return ,$result }
        foreach ($version in $versions) {
            $tags = @()
            if ($version.metadata -and $version.metadata.container -and $version.metadata.container.tags) { $tags = @($version.metadata.container.tags) }
            $result += [pscustomobject]@{ Id = $version.id; Digest = $version.name; Tags = $tags }
        }
        $page++
    }
}

function Get-AllVersions {
    $all = @{}
    foreach ($image in $Images) { $all[$image] = Get-ImageVersions $image }
    return $all
}

function Find-VersionWithTag($Versions, [string]$Name) {
    foreach ($version in $Versions) { if ($version.Tags -contains $Name) { return $version } }
    return $null
}

function Test-HasBuildTags($Versions) {
    foreach ($version in $Versions) { foreach ($t in $version.Tags) { if ($t -match $BuildTagPattern) { return $true } } }
    return $false
}

function Assert-BuildTag([string]$Name) {
    if ($Name -notmatch ('^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?' + $BuildTagPattern)) {
        Stop-Tags "'$Name' is not a build tag (<version>-<yyyyMMddHHmmss>-<local|dev|test|prod>)"
    }
}

function Confirm-Tags([string]$Message) {
    if ($Yes) { return $true }
    return ((Read-Host "$Message [y/N]") -match '^(y|yes)$')
}

function Connect-Registry {
    if ($DryRun) { return }
    $env:REPOSITORY_PASSWORD | docker login $Registry -u $env:REPOSITORY_USER --password-stdin | Out-Null
    if ($LASTEXITCODE -ne 0) { Stop-Tags "docker login to $Registry failed" }
}

function Get-ImageDigest([string]$Image, [string]$Name) {
    $json = docker buildx imagetools inspect "$Registry/$Owner/$ImageNamespace/${Image}:$Name" --format '{{json .Manifest}}' 2>$null
    if ($LASTEXITCODE -ne 0 -or -not $json) { return $null }
    return (($json -join "`n") | ConvertFrom-Json).digest
}

function Invoke-List {
    $all = Get-AllVersions
    $managed = @($Images | Where-Object { Test-HasBuildTags $all[$_] })
    if (-not $Tag) {
        $counts = @{}
        foreach ($image in $Images) {
            foreach ($version in $all[$image]) {
                foreach ($t in $version.Tags) {
                    if ($t -notmatch $BuildTagPattern) { continue }
                    if ($Suffix -and -not $t.EndsWith("-$Suffix")) { continue }
                    $counts[$t] = 1 + $(if ($counts.ContainsKey($t)) { $counts[$t] } else { 0 })
                }
            }
        }
        if ($counts.Count -eq 0) { Write-Host "No build tags$(if ($Suffix) { " with suffix -$Suffix" })." -ForegroundColor Yellow }
        foreach ($t in ($counts.Keys | Sort-Object)) {
            if ($counts[$t] -lt $managed.Count) {
                Write-Host "$t  $($counts[$t]) of $($managed.Count) repositories (incomplete)" -ForegroundColor Yellow
            } else {
                Write-Host "$t  $($counts[$t]) of $($managed.Count) repositories" -ForegroundColor Green
            }
        }
        foreach ($image in $Images) { if ($managed -notcontains $image) { Write-Host "skipped: $image (no build tags)" -ForegroundColor DarkGray } }
        return
    }
    Assert-BuildTag $Tag
    $present = 0; $missing = 0
    foreach ($image in $Images) {
        if ($managed -notcontains $image) { Write-Host "  skipped  $image (no build tags)" -ForegroundColor DarkGray; continue }
        $version = Find-VersionWithTag $all[$image] $Tag
        if ($version) { $present++; Write-Host "  present  $image  $($version.Digest)" -ForegroundColor Green }
        else { $missing++; Write-Host "  missing  $image" -ForegroundColor Red }
    }
    Write-Host "${Tag}: $present of $($managed.Count) repositories" -ForegroundColor Cyan
    if ($missing -gt 0 -or $present -eq 0) { exit 2 }
}

function Invoke-Promote {
    if (-not $Tag -or -not $Target) { Stop-Tags "usage: promote <candidate-tag> <test|prod>" }
    Assert-BuildTag $Tag
    switch ($Target) {
        "test"  { $from = "dev";  $to = "test" }
        "prod"  { $from = "test"; $to = "prod" }
        default { Stop-Tags "promote target must be test or prod" }
    }
    if (-not $Tag.EndsWith("-$from")) { Stop-Tags "only a -$from tag can be promoted to $to (got $Tag)" }
    $targetTag = $Tag.Substring(0, $Tag.Length - $from.Length) + $to
    Write-Host "==> Promote $Tag -> $targetTag ($($Images.Count) repositories)" -ForegroundColor Cyan
    $all = Get-AllVersions
    $refused = $false
    foreach ($image in $Images) {
        if (-not (Find-VersionWithTag $all[$image] $Tag)) { Write-Host "  missing candidate: ${image}:$Tag" -ForegroundColor Red; $refused = $true }
        if (Find-VersionWithTag $all[$image] $targetTag) { Write-Host "  target already exists: ${image}:$targetTag" -ForegroundColor Red; $refused = $true }
    }
    if ($refused) { Stop-Tags "refused: the candidate must exist and the target must not exist in every repository" }
    if ($DryRun) {
        foreach ($image in $Images) {
            Write-Host "  [dry-run] docker buildx imagetools create --tag $Registry/$Owner/$ImageNamespace/${image}:$targetTag $Registry/$Owner/$ImageNamespace/${image}:$Tag" -ForegroundColor Yellow
        }
        return
    }
    if (-not (Confirm-Tags "Retag $($Images.Count) images $Tag -> $targetTag?")) { Write-Host "Aborted." -ForegroundColor Red; exit 1 }
    Connect-Registry
    foreach ($image in $Images) {
        docker buildx imagetools create --tag "$Registry/$Owner/$ImageNamespace/${image}:$targetTag" "$Registry/$Owner/$ImageNamespace/${image}:$Tag" | Out-Null
        if ($LASTEXITCODE -ne 0) { Stop-Tags "retag failed for $image" }
        $sourceDigest = Get-ImageDigest $image $Tag
        $targetDigest = Get-ImageDigest $image $targetTag
        if (-not $sourceDigest -or $sourceDigest -ne $targetDigest) { Stop-Tags "digest mismatch for ${image}: $Tag=$sourceDigest $targetTag=$targetDigest" }
        Write-Host "  ${image}:$targetTag = $targetDigest" -ForegroundColor Green
    }
    Write-Host "Promoted $Tag -> $targetTag in $($Images.Count) repositories." -ForegroundColor Green
}

function Invoke-Delete {
    if (-not $Tag) { Stop-Tags "usage: delete <tag>" }
    Assert-BuildTag $Tag
    if ($Tag -notmatch '-(local|dev)$') { Stop-Tags "only -local and -dev tags can be deleted (got $Tag)" }
    $all = Get-AllVersions
    $targets = @()
    $refused = $false
    foreach ($image in $Images) {
        $version = Find-VersionWithTag $all[$image] $Tag
        if (-not $version) { continue }
        $others = @($version.Tags | Where-Object { $_ -ne $Tag })
        if ($others.Count -gt 0) {
            Write-Host "  ${image}: version $($version.Digest) also carries: $($others -join ', ')" -ForegroundColor Red
            $refused = $true
        }
        $targets += [pscustomobject]@{ Image = $image; Id = $version.Id }
    }
    if ($refused) { Stop-Tags "refused: GHCR deletes whole image versions, and these versions carry other tags" }
    if ($targets.Count -eq 0) { Write-Host "No repository has $Tag." -ForegroundColor Yellow; return }
    if ($DryRun) {
        foreach ($t in $targets) { Write-Host "  [dry-run] would DELETE $($t.Image):$Tag (version id $($t.Id))" -ForegroundColor Yellow }
        return
    }
    if (-not (Confirm-Tags "Delete $Tag from $($targets.Count) repositories?")) { Write-Host "Aborted." -ForegroundColor Red; exit 1 }
    foreach ($t in $targets) {
        try {
            Invoke-WebRequest -UseBasicParsing -Method Delete -Headers $Headers -Uri "$(Get-VersionsUrl $t.Image)/$($t.Id)" | Out-Null
        } catch {
            Stop-Tags "delete failed for $($t.Image) (token needs delete:packages)"
        }
        Write-Host "  deleted $($t.Image):$Tag" -ForegroundColor Green
    }
}

switch ($Command) {
    "list"    { Invoke-List }
    "promote" { Invoke-Promote }
    "delete"  { Invoke-Delete }
}
