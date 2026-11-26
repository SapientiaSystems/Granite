#Requires -Version 5.1

[CmdletBinding()]
param(
    [string]   $OutputDir,
    [string]   $CaCommonName,
    [string]   $CaOrganization,
    [string]   $CertCommonName,
    [int]      $CaValidityDays    = 3650,
    [int]      $CertValidityDays  = 825,
    [string]   $KeystorePassword,
    [string]   $TruststorePassword,
    [string[]] $ServiceNames,
    [string]   $KubernetesNamespace = "granite-local",
    [string[]] $SanNamespaces      = @("granite-local","granite-dev","granite-test"),
    [string]   $ClusterDomain      = "cluster.local",
    [int]      $KeySize            = 2048,
    [switch]   $CreateK8sSecrets,
    [switch]   $Force
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ScriptDir = $PSScriptRoot

$DefaultPassword = "granite-test"

$DefaultServiceNames = @(
    "granite-frontendcontroller-userhost"
    "granite-frontendcontroller-adminhost"
    "granite-user-commandhost"
    "granite-user-queryhost"
    "granite-session-commandhost"
    "granite-session-queryhost"
    "granite-gateway-servicehost"
    "granite-outbox-servicehost"
    "granite-feedback-commandhost"
    "granite-feedback-queryhost"
    "granite-survey-commandhost"
    "granite-survey-queryhost"
    "granite-content-commandhost"
    "granite-content-queryhost"
    "granite-agent-commandhost"
    "granite-agent-queryhost"
    "granite-client-commandhost"
    "granite-client-queryhost"
    "granite-web-apphost"
    "granite-web-clienthost"
)

$FoundationCoreDir = [System.IO.Path]::GetFullPath((Join-Path $ScriptDir "..\..\Granite.Foundation\Components\Granite.Foundation.Core"))
$FoundationSharedResourceDir = Join-Path $FoundationCoreDir "src\main\resources"
$MicroservicesDir = [System.IO.Path]::GetFullPath((Join-Path $ScriptDir "..\..\..\Microservices"))

# =============================================================================
# Helper functions
# =============================================================================

function Write-Step([string]$Message) {
    Write-Host "==> $Message" -ForegroundColor Cyan
}

function Write-Success([string]$Message) {
    Write-Host $Message -ForegroundColor Green
}

function Write-Warn([string]$Message) {
    Write-Host $Message -ForegroundColor Yellow
}

function Resolve-Tool([string]$Name, [string]$Fallback) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    if ($Fallback -and (Test-Path $Fallback)) { return $Fallback }
    return $null
}

function Assert-Prerequisite([string]$ToolPath, [string]$CommandName, [string]$HintMessage) {
    if (-not $ToolPath) {
        Write-Host "Error: '$CommandName' is required but not found on PATH." -ForegroundColor Red
        if ($HintMessage) {
            Write-Host "  Hint: $HintMessage" -ForegroundColor Yellow
        }
        exit 1
    }
}

function Prompt-IfEmpty([string]$Value, [string]$PromptMessage, [string]$Default) {
    if (-not [string]::IsNullOrWhiteSpace($Value)) {
        return $Value
    }
    if ($Default) {
        $answer = Read-Host "$PromptMessage [$Default]"
        if ([string]::IsNullOrWhiteSpace($answer)) { return $Default }
        return $answer
    }
    do {
        $answer = Read-Host $PromptMessage
    } while ([string]::IsNullOrWhiteSpace($answer))
    return $answer
}

function Prompt-Password([string]$Value, [string]$PromptMessage, [string]$Default) {
    if (-not [string]::IsNullOrWhiteSpace($Value)) {
        return $Value
    }
    do {
        $secure = Read-Host "$PromptMessage [$Default]" -AsSecureString
        $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
        $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($BSTR)
        if ($plain.Length -eq 0) { $plain = $Default }
        if ($plain.Length -lt 6) {
            Write-Warn "Password must be at least 6 characters."
        }
    } while ($plain.Length -lt 6)
    return $plain
}

function Confirm-Overwrite([string]$Path) {
    if ($Force) { return $true }
    if (-not (Test-Path $Path)) { return $true }
    $answer = Read-Host "File exists: $Path. Overwrite? (y/N)"
    return ($answer -eq "y" -or $answer -eq "Y")
}

function Invoke-External([string]$Description, [scriptblock]$Command) {
    $previous = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $Command
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previous
    if ($exitCode -ne 0) {
        Write-Host "Error: $Description failed (exit code $exitCode)." -ForegroundColor Red
        exit $exitCode
    }
}

function Invoke-Capture([string]$Description, [scriptblock]$Command) {
    $previous = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $output = & $Command
    $exitCode = $LASTEXITCODE
    $ErrorActionPreference = $previous
    if ($exitCode -ne 0) {
        Write-Host "Error: $Description failed (exit code $exitCode)." -ForegroundColor Red
        exit $exitCode
    }
    return $output
}

function Write-TextFile([string]$Path, [string]$Content) {
    $utf8NoBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText($Path, $Content, $utf8NoBom)
}

# =============================================================================
# Prerequisites
# =============================================================================

Write-Host ""
Write-Step "Checking prerequisites"

$git = Get-Command git -ErrorAction SilentlyContinue
$gitOpenSsl = $null
if ($git) { $gitOpenSsl = Join-Path (Split-Path (Split-Path $git.Source)) "usr\bin\openssl.exe" }
$javaKeytool = $null
if ($env:JAVA_HOME) { $javaKeytool = Join-Path $env:JAVA_HOME "bin\keytool.exe" }

$OpenSsl = Resolve-Tool "openssl" $gitOpenSsl
Assert-Prerequisite $OpenSsl "openssl" "Install OpenSSL or ensure Git-for-Windows openssl is on PATH."
Write-Host "  openssl ... OK" -ForegroundColor DarkGray
$Keytool = Resolve-Tool "keytool" $javaKeytool
Assert-Prerequisite $Keytool "keytool" "keytool ships with the JDK. Ensure JAVA_HOME\bin is on PATH."
Write-Host "  keytool ... OK" -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Resolve parameters (prompt if not supplied)
# =============================================================================

Write-Step "Resolving parameters"

$DefaultOutputDir = Join-Path $FoundationCoreDir "certs"

if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Prompt-IfEmpty $OutputDir "Output directory" $DefaultOutputDir
}

$CaCommonName      = Prompt-IfEmpty $CaCommonName      "CA Common Name"              "Granite Private CA"
$CaOrganization    = Prompt-IfEmpty $CaOrganization    "CA Organization"              "Granite"
$CertCommonName    = Prompt-IfEmpty $CertCommonName    "Service certificate CN/alias"  "granite-services"
$KeystorePassword   = Prompt-Password $KeystorePassword   "Enter keystore password (min 6 chars)"   $DefaultPassword
$TruststorePassword = Prompt-Password $TruststorePassword "Enter truststore password (min 6 chars)" $DefaultPassword

if (-not $ServiceNames -or $ServiceNames.Count -eq 0) {
    Write-Host ""
    Write-Host "  Default services (used for SAN entries):" -ForegroundColor Cyan
    for ($i = 0; $i -lt $DefaultServiceNames.Count; $i++) {
        Write-Host "    $($i + 1). $($DefaultServiceNames[$i])" -ForegroundColor DarkGray
    }
    Write-Host ""
    $useDefaults = Read-Host "Use default service list? (Y/n)"
    if ($useDefaults -eq "n" -or $useDefaults -eq "N") {
        $csvInput = Read-Host "Enter comma-separated service names"
        $ServiceNames = $csvInput.Split(',') | ForEach-Object { $_.Trim() } | Where-Object { $_ -ne "" }
    } else {
        $ServiceNames = $DefaultServiceNames
    }
}

Write-Host ""
Write-Host "  Output directory:     $OutputDir" -ForegroundColor DarkGray
Write-Host "  CA Common Name:       $CaCommonName" -ForegroundColor DarkGray
Write-Host "  CA Organization:      $CaOrganization" -ForegroundColor DarkGray
Write-Host "  Cert CN/alias:        $CertCommonName" -ForegroundColor DarkGray
Write-Host "  CA validity:          $CaValidityDays days" -ForegroundColor DarkGray
Write-Host "  Cert validity:        $CertValidityDays days" -ForegroundColor DarkGray
Write-Host "  Key size:             $KeySize bits" -ForegroundColor DarkGray
Write-Host "  SAN services:         $($ServiceNames.Count)" -ForegroundColor DarkGray
Write-Host "  K8s namespace:        $KubernetesNamespace" -ForegroundColor DarkGray
Write-Host "  SAN namespaces:       $($SanNamespaces -join ', ')" -ForegroundColor DarkGray
Write-Host "  Cluster domain:       $ClusterDomain" -ForegroundColor DarkGray
Write-Host "  Create K8s secrets:   $CreateK8sSecrets" -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Prepare output directory
# =============================================================================

if (-not (Test-Path $OutputDir)) {
    New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
    Write-Host "  Created directory: $OutputDir" -ForegroundColor DarkGray
}

$caKeyPath  = Join-Path $OutputDir "ca.key"
$caCertPath = Join-Path $OutputDir "ca.crt"
$caCnfPath  = Join-Path $OutputDir "ca.cnf"

if ((Test-Path $caKeyPath) -or (Test-Path $caCertPath)) {
    if (-not (Confirm-Overwrite $caCertPath)) {
        Write-Host "Aborted. Existing CA will not be overwritten." -ForegroundColor Yellow
        exit 0
    }
}

# =============================================================================
# Phase 1: Generate CA private key and self-signed certificate
# =============================================================================

Write-Step "Generating CA private key (RSA 4096-bit)"
Invoke-External "CA key generation" {
    & $OpenSsl genrsa -out $caKeyPath 4096
}
Write-Host "  $caKeyPath" -ForegroundColor DarkGray

Write-Step "Generating self-signed CA certificate"

$caCnfContent = @"
[req]
default_bits = 4096
prompt = no
distinguished_name = dn
x509_extensions = v3_ca

[dn]
CN = $CaCommonName
O = $CaOrganization

[v3_ca]
basicConstraints = critical, CA:TRUE
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always, issuer
"@

Write-TextFile $caCnfPath ($caCnfContent.Replace("`r`n", "`n") + "`n")

Invoke-External "CA certificate generation" {
    & $OpenSsl req -new -x509 `
        -key $caKeyPath `
        -out $caCertPath `
        -days $CaValidityDays `
        -config $caCnfPath
}
Write-Host "  $caCertPath" -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Phase 2: Generate shared service certificate with all SANs
# =============================================================================

$svcKeyPath  = Join-Path $OutputDir "$CertCommonName.key"
$svcCsrPath  = Join-Path $OutputDir "$CertCommonName.csr"
$svcCrtPath  = Join-Path $OutputDir "$CertCommonName.crt"
$svcCnfPath  = Join-Path $OutputDir "$CertCommonName.cnf"
$svcP12Path  = Join-Path $OutputDir "$CertCommonName.p12"

Write-Step "Building OpenSSL config with SANs for all services"

$dnsIndex = 1
$altNamesLines = @()
$altNamesLines += "DNS.$dnsIndex = localhost"
$dnsIndex++
$altNamesLines += "DNS.$dnsIndex = host.docker.internal"
$dnsIndex++

foreach ($svc in $ServiceNames) {
    $altNamesLines += "DNS.$dnsIndex = $svc"
    $dnsIndex++
    foreach ($sanNamespace in $SanNamespaces) {
        $altNamesLines += "DNS.$dnsIndex = $svc.$sanNamespace.svc.$ClusterDomain"
        $dnsIndex++
    }
}

$altNamesLines += "IP.1 = 127.0.0.1"
$altNamesBlock = $altNamesLines -join "`n"

$cnfContent = @"
[req]
default_bits = $KeySize
prompt = no
distinguished_name = dn
req_extensions = v3_req

[dn]
CN = $CertCommonName
O = $CaOrganization

[v3_req]
subjectAltName = @alt_names
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth

[alt_names]
$altNamesBlock
"@

Write-TextFile $svcCnfPath ($cnfContent.Replace("`r`n", "`n") + "`n")
Write-Host "  Config: $svcCnfPath" -ForegroundColor DarkGray
Write-Host "  SANs:   $($ServiceNames.Count) services x 2 DNS + localhost + host.docker.internal + 127.0.0.1" -ForegroundColor DarkGray
Write-Host ""

Write-Step "Generating shared service private key (RSA $KeySize-bit)"
Invoke-External "Service key generation" {
    & $OpenSsl genrsa -out $svcKeyPath $KeySize
}
Write-Host "  $svcKeyPath" -ForegroundColor DarkGray

Write-Step "Generating CSR"
Invoke-External "CSR generation" {
    & $OpenSsl req -new `
        -key $svcKeyPath `
        -out $svcCsrPath `
        -config $svcCnfPath
}
Write-Host "  $svcCsrPath" -ForegroundColor DarkGray

Write-Step "Signing certificate with CA"
Invoke-External "Certificate signing" {
    & $OpenSsl x509 -req `
        -in $svcCsrPath `
        -CA $caCertPath `
        -CAkey $caKeyPath `
        -CAcreateserial `
        -out $svcCrtPath `
        -days $CertValidityDays `
        -extensions v3_req `
        -extfile $svcCnfPath
}
Write-Host "  $svcCrtPath" -ForegroundColor DarkGray

Write-Step "Packaging PKCS12 keystore"
Invoke-External "PKCS12 keystore creation" {
    & $OpenSsl pkcs12 -export `
        -in $svcCrtPath `
        -inkey $svcKeyPath `
        -certfile $caCertPath `
        -out $svcP12Path `
        -name $CertCommonName `
        -password "pass:$KeystorePassword"
}
Write-Host "  $svcP12Path" -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Phase 3: Create CA truststore
# =============================================================================

$truststorePath = Join-Path $OutputDir "granite-truststore.p12"

Write-Step "Creating CA truststore"

if (Test-Path $truststorePath) {
    Remove-Item $truststorePath -Force
}

Invoke-External "Truststore creation" {
    & $Keytool -importcert `
        -alias "granite-ca" `
        -file $caCertPath `
        -keystore $truststorePath `
        -storetype PKCS12 `
        -storepass $TruststorePassword `
        -noprompt
}
Write-Host "  $truststorePath" -ForegroundColor DarkGray
Write-Host ""

# =============================================================================
# Phase 4: Verification
# =============================================================================

Write-Step "Verifying certificate against CA"
$result = & $OpenSsl verify -CAfile $caCertPath $svcCrtPath
if ($LASTEXITCODE -ne 0) {
    Write-Host "  FAIL: $result" -ForegroundColor Red
    exit 1
}
Write-Host "  $result" -ForegroundColor DarkGray

Write-Host ""
Write-Step "Certificate SANs"
$sanText = Invoke-Capture "Certificate SAN listing" { & $OpenSsl x509 -in $svcCrtPath -noout -text }
$sanText | Select-String -Pattern "DNS:|IP Address:" | ForEach-Object {
    Write-Host "  $($_.Line.Trim())" -ForegroundColor DarkGray
}

Write-Host ""
Write-Step "CA certificate details"
$caDetails = Invoke-Capture "CA certificate details" { & $OpenSsl x509 -in $caCertPath -noout -subject -issuer -dates }
$caDetails | ForEach-Object {
    Write-Host "  $_" -ForegroundColor DarkGray
}

Write-Host ""
Write-Step "Keystore contents"
$keystoreListing = Invoke-Capture "Keystore listing" { & $Keytool -list -keystore $svcP12Path -storetype PKCS12 -storepass $KeystorePassword }
$keystoreListing | ForEach-Object {
    Write-Host "  $_" -ForegroundColor DarkGray
}

Write-Host ""
Write-Step "Truststore contents"
$truststoreListing = Invoke-Capture "Truststore listing" { & $Keytool -list -keystore $truststorePath -storetype PKCS12 -storepass $TruststorePassword }
$truststoreListing | ForEach-Object {
    Write-Host "  $_" -ForegroundColor DarkGray
}
Write-Host ""

# =============================================================================
# Phase 5: Create Kubernetes secrets (optional)
# =============================================================================

if ($CreateK8sSecrets) {
    Write-Step "Creating Kubernetes CA secret"

    $Kubectl = Resolve-Tool "kubectl" $null
    Assert-Prerequisite $Kubectl "kubectl" "Install kubectl to create K8s secrets."

    $namespaceYaml = Invoke-Capture "Namespace manifest generation" {
        & $Kubectl create namespace $KubernetesNamespace --dry-run=client -o yaml
    }
    Invoke-External "Namespace creation" {
        $namespaceYaml | & $Kubectl apply -f -
    }

    Write-Host "  Creating secret: granite-ca-key-pair" -ForegroundColor DarkGray
    $secretYaml = Invoke-Capture "granite-ca-key-pair manifest generation" {
        & $Kubectl create secret tls granite-ca-key-pair `
            --namespace=$KubernetesNamespace `
            --cert=$caCertPath `
            --key=$caKeyPath `
            --dry-run=client -o yaml
    }
    Invoke-External "granite-ca-key-pair secret creation" {
        $secretYaml | & $Kubectl apply -f -
    }

    Write-Success "  Kubernetes secret created in namespace '$KubernetesNamespace'."
    Write-Host ""
}

# =============================================================================
# Phase 6: Distribute to Foundation.Core (shared via classpath)
# =============================================================================

Write-Step "Distributing shared keystore and truststore to Granite.Foundation.Core"

$resourceDir = $FoundationSharedResourceDir

if (-not (Test-Path $resourceDir)) {
    New-Item -ItemType Directory -Path $resourceDir -Force | Out-Null
    Write-Host "  Created directory: $resourceDir" -ForegroundColor DarkGray
}

$keystoreDest = Join-Path $resourceDir "$CertCommonName.p12"
Copy-Item -Path $svcP12Path -Destination $keystoreDest -Force
Write-Host "  $CertCommonName.p12 -> $resourceDir" -ForegroundColor DarkGray

$truststoreDest = Join-Path $resourceDir "granite-truststore.p12"
Copy-Item -Path $truststorePath -Destination $truststoreDest -Force
Write-Host "  granite-truststore.p12 -> $resourceDir" -ForegroundColor DarkGray

$caCertDest = Join-Path $resourceDir "granite-ca.crt"
Copy-Item -Path $caCertPath -Destination $caCertDest -Force
Write-Host "  granite-ca.crt -> $resourceDir" -ForegroundColor DarkGray

Write-Host ""
Write-Success "  Distribution complete. Certs are in Granite.Foundation.Core (shared via classpath)."
Write-Host ""

# =============================================================================
# Phase 7: Update microservice SSL passwords in properties files
# =============================================================================

Write-Step "Updating SSL passwords in microservice properties files"

$propertiesFiles = @()
if (Test-Path $MicroservicesDir) {
    $propertiesFiles = @(Get-ChildItem -Path $MicroservicesDir -Recurse -Filter "application-*.properties" |
        Where-Object {
            $_.FullName -notlike "*\.build\*" -and
            $_.Name -ne "application-production.properties" -and
            $_.FullName -like "*\src\main\resources\*"
        })
}

$keystorePattern   = '(?m)^(spring\.ssl\.bundle\.jks\.granite-services\.keystore\.password=)[^\r\n]*(?=\r?$)'
$truststorePattern = '(?m)^(spring\.ssl\.bundle\.jks\.granite-services\.truststore\.password=)[^\r\n]*(?=\r?$)'

$updatedCount = 0
foreach ($file in $propertiesFiles) {
    $bytes   = [System.IO.File]::ReadAllBytes($file.FullName)
    $hasBom  = ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)
    $content = [System.IO.File]::ReadAllText($file.FullName)

    if ($content -notmatch $keystorePattern) {
        continue
    }

    $updated = [regex]::Replace($content, $keystorePattern, { param($m) $m.Groups[1].Value + $KeystorePassword })
    $updated = [regex]::Replace($updated, $truststorePattern, { param($m) $m.Groups[1].Value + $TruststorePassword })

    if ($updated -ne $content) {
        $encoding = New-Object System.Text.UTF8Encoding($hasBom)
        [System.IO.File]::WriteAllText($file.FullName, $updated, $encoding)
        $relativePath = $file.FullName.Substring($MicroservicesDir.Length - "Microservices".Length)
        Write-Host "  Updated: $relativePath" -ForegroundColor DarkGray
        $updatedCount++
    }
}

Write-Host ""
Write-Success "  $updatedCount properties file(s) updated with SSL passwords."
Write-Host ""

# =============================================================================
# Summary
# =============================================================================

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Host "  Private CA Generation Complete" -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Output directory: $OutputDir" -ForegroundColor Cyan
Write-Host "CA certificate:   $(Join-Path $OutputDir 'ca.crt')" -ForegroundColor Cyan
Write-Host "CA private key:   $(Join-Path $OutputDir 'ca.key')" -ForegroundColor Yellow
Write-Host "Service keystore: $svcP12Path" -ForegroundColor Cyan
Write-Host "Truststore:       $truststorePath" -ForegroundColor Cyan
Write-Host "Key alias:        $CertCommonName" -ForegroundColor Cyan
Write-Host ""
Write-Warn "Keep ca.key secure -- it can sign certificates trusted by all services."
Write-Host ""
