#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

OUTPUT_DIR=""
CA_COMMON_NAME=""
CA_ORGANIZATION=""
CERT_COMMON_NAME=""
CA_VALIDITY_DAYS=3650
CERT_VALIDITY_DAYS=825
KEYSTORE_PASSWORD=""
TRUSTSTORE_PASSWORD=""
SERVICE_NAMES=""
KUBERNETES_NAMESPACE="granite"
CLUSTER_DOMAIN="cluster.local"
KEY_SIZE=2048
CREATE_K8S_SECRETS=false
FORCE=false

DEFAULT_PASSWORD="granite-test"

DEFAULT_SERVICE_NAMES=(
    "granite-frontendcontroller-userhost"
    "granite-frontendcontroller-adminhost"
    "granite-user-commandhost"
    "granite-user-queryhost"
    "granite-session-commandhost"
    "granite-session-queryhost"
    "granite-gateway-servicehost"
    "granite-outbox-servicehost"
    "granite-web-apphost"
)

FOUNDATION_CORE_DIR="$(cd "$SCRIPT_DIR/../../Granite.Foundation/Components/Granite.Foundation.Core" && pwd)"
FOUNDATION_SHARED_RESOURCE_DIR="$FOUNDATION_CORE_DIR/src/main/resources"
MICROSERVICES_DIR="$(cd "$SCRIPT_DIR/../../../Microservices" 2>/dev/null && pwd || true)"

usage() {
    cat <<USAGE
Usage: $(basename "$0") [options]

  --output-dir <dir>
  --ca-common-name <name>
  --ca-organization <name>
  --cert-common-name <name>
  --ca-validity-days <days>        (default: $CA_VALIDITY_DAYS)
  --cert-validity-days <days>      (default: $CERT_VALIDITY_DAYS)
  --keystore-password <password>
  --truststore-password <password>
  --service-names <a,b,c>
  --kubernetes-namespace <name>    (default: $KUBERNETES_NAMESPACE)
  --cluster-domain <domain>        (default: $CLUSTER_DOMAIN)
  --key-size <bits>                (default: $KEY_SIZE)
  --create-k8s-secrets
  --force
USAGE
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --output-dir)           OUTPUT_DIR="$2"; shift 2 ;;
        --ca-common-name)       CA_COMMON_NAME="$2"; shift 2 ;;
        --ca-organization)      CA_ORGANIZATION="$2"; shift 2 ;;
        --cert-common-name)     CERT_COMMON_NAME="$2"; shift 2 ;;
        --ca-validity-days)     CA_VALIDITY_DAYS="$2"; shift 2 ;;
        --cert-validity-days)   CERT_VALIDITY_DAYS="$2"; shift 2 ;;
        --keystore-password)    KEYSTORE_PASSWORD="$2"; shift 2 ;;
        --truststore-password)  TRUSTSTORE_PASSWORD="$2"; shift 2 ;;
        --service-names)        SERVICE_NAMES="$2"; shift 2 ;;
        --kubernetes-namespace) KUBERNETES_NAMESPACE="$2"; shift 2 ;;
        --cluster-domain)       CLUSTER_DOMAIN="$2"; shift 2 ;;
        --key-size)             KEY_SIZE="$2"; shift 2 ;;
        --create-k8s-secrets)   CREATE_K8S_SECRETS=true; shift ;;
        --force)                FORCE=true; shift ;;
        -h|--help)              usage; exit 0 ;;
        *) echo "Unknown option: $1" >&2; usage >&2; exit 1 ;;
    esac
done

# =============================================================================
# Helper functions
# =============================================================================

if [[ -t 1 ]]; then
    C_CYAN=$'\033[36m'; C_GREEN=$'\033[32m'; C_YELLOW=$'\033[33m'; C_RED=$'\033[31m'; C_GRAY=$'\033[90m'; C_RESET=$'\033[0m'
else
    C_CYAN=""; C_GREEN=""; C_YELLOW=""; C_RED=""; C_GRAY=""; C_RESET=""
fi

write_step()    { echo "${C_CYAN}==> $1${C_RESET}"; }
write_success() { echo "${C_GREEN}$1${C_RESET}"; }
write_warn()    { echo "${C_YELLOW}$1${C_RESET}"; }
write_gray()    { echo "${C_GRAY}$1${C_RESET}"; }

assert_prerequisite() {
    local command_name="$1" hint="$2"
    if ! command -v "$command_name" >/dev/null 2>&1; then
        echo "${C_RED}Error: '$command_name' is required but not found on PATH.${C_RESET}"
        if [[ -n "$hint" ]]; then
            echo "${C_YELLOW}  Hint: $hint${C_RESET}"
        fi
        exit 1
    fi
}

prompt_if_empty() {
    local value="$1" prompt_message="$2" default="${3:-}" answer=""
    if [[ -n "${value// /}" ]]; then
        echo "$value"
        return
    fi
    if [[ -n "$default" ]]; then
        read -r -p "$prompt_message [$default]: " answer >&2
        if [[ -z "${answer// /}" ]]; then echo "$default"; else echo "$answer"; fi
        return
    fi
    while [[ -z "${answer// /}" ]]; do
        read -r -p "$prompt_message: " answer >&2
    done
    echo "$answer"
}

prompt_password() {
    local value="$1" prompt_message="$2" default="$3" plain=""
    if [[ -n "${value// /}" ]]; then
        echo "$value"
        return
    fi
    while true; do
        read -r -s -p "$prompt_message [$default]: " plain >&2
        echo >&2
        if [[ -z "$plain" ]]; then plain="$default"; fi
        if [[ ${#plain} -ge 6 ]]; then break; fi
        echo "${C_YELLOW}Password must be at least 6 characters.${C_RESET}" >&2
    done
    echo "$plain"
}

confirm_overwrite() {
    local path="$1" answer=""
    if [[ "$FORCE" == true ]]; then return 0; fi
    if [[ ! -e "$path" ]]; then return 0; fi
    read -r -p "File exists: $path. Overwrite? (y/N) " answer
    [[ "$answer" == "y" || "$answer" == "Y" ]]
}

indent_gray() {
    while IFS= read -r line; do
        write_gray "  $line"
    done
}

escape_sed_replacement() {
    printf '%s' "$1" | sed -e 's/[\\|&]/\\&/g'
}

# =============================================================================
# Prerequisites
# =============================================================================

echo ""
write_step "Checking prerequisites"
assert_prerequisite openssl "Install OpenSSL."
write_gray "  openssl ... OK"
assert_prerequisite keytool "keytool ships with the JDK. Ensure JAVA_HOME/bin is on PATH."
write_gray "  keytool ... OK"
echo ""

# =============================================================================
# Resolve parameters (prompt if not supplied)
# =============================================================================

write_step "Resolving parameters"

DEFAULT_OUTPUT_DIR="$FOUNDATION_CORE_DIR/certs"

if [[ -z "${OUTPUT_DIR// /}" ]]; then
    OUTPUT_DIR="$(prompt_if_empty "$OUTPUT_DIR" "Output directory" "$DEFAULT_OUTPUT_DIR")"
fi

CA_COMMON_NAME="$(prompt_if_empty "$CA_COMMON_NAME" "CA Common Name" "Granite Private CA")"
CA_ORGANIZATION="$(prompt_if_empty "$CA_ORGANIZATION" "CA Organization" "Granite")"
CERT_COMMON_NAME="$(prompt_if_empty "$CERT_COMMON_NAME" "Service certificate CN/alias" "granite-services")"
KEYSTORE_PASSWORD="$(prompt_password "$KEYSTORE_PASSWORD" "Enter keystore password (min 6 chars)" "$DEFAULT_PASSWORD")"
TRUSTSTORE_PASSWORD="$(prompt_password "$TRUSTSTORE_PASSWORD" "Enter truststore password (min 6 chars)" "$DEFAULT_PASSWORD")"

SERVICES=()
if [[ -n "${SERVICE_NAMES// /}" ]]; then
    IFS=',' read -r -a RAW_SERVICES <<< "$SERVICE_NAMES"
    for svc in "${RAW_SERVICES[@]}"; do
        svc="$(echo "$svc" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
        if [[ -n "$svc" ]]; then SERVICES+=("$svc"); fi
    done
fi

if [[ ${#SERVICES[@]} -eq 0 ]]; then
    echo ""
    echo "${C_CYAN}  Default services (used for SAN entries):${C_RESET}"
    for i in "${!DEFAULT_SERVICE_NAMES[@]}"; do
        write_gray "    $((i + 1)). ${DEFAULT_SERVICE_NAMES[$i]}"
    done
    echo ""
    use_defaults=""
    read -r -p "Use default service list? (Y/n) " use_defaults || true
    if [[ "$use_defaults" == "n" || "$use_defaults" == "N" ]]; then
        read -r -p "Enter comma-separated service names: " csv_input
        IFS=',' read -r -a RAW_SERVICES <<< "$csv_input"
        for svc in "${RAW_SERVICES[@]}"; do
            svc="$(echo "$svc" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
            if [[ -n "$svc" ]]; then SERVICES+=("$svc"); fi
        done
    else
        SERVICES=("${DEFAULT_SERVICE_NAMES[@]}")
    fi
fi

echo ""
write_gray "  Output directory:     $OUTPUT_DIR"
write_gray "  CA Common Name:       $CA_COMMON_NAME"
write_gray "  CA Organization:      $CA_ORGANIZATION"
write_gray "  Cert CN/alias:        $CERT_COMMON_NAME"
write_gray "  CA validity:          $CA_VALIDITY_DAYS days"
write_gray "  Cert validity:        $CERT_VALIDITY_DAYS days"
write_gray "  Key size:             $KEY_SIZE bits"
write_gray "  SAN services:         ${#SERVICES[@]}"
write_gray "  K8s namespace:        $KUBERNETES_NAMESPACE"
write_gray "  Cluster domain:       $CLUSTER_DOMAIN"
write_gray "  Create K8s secrets:   $CREATE_K8S_SECRETS"
echo ""

# =============================================================================
# Prepare output directory
# =============================================================================

if [[ ! -d "$OUTPUT_DIR" ]]; then
    mkdir -p "$OUTPUT_DIR"
    write_gray "  Created directory: $OUTPUT_DIR"
fi

CA_KEY_PATH="$OUTPUT_DIR/ca.key"
CA_CERT_PATH="$OUTPUT_DIR/ca.crt"
CA_CNF_PATH="$OUTPUT_DIR/ca.cnf"

if [[ -e "$CA_KEY_PATH" || -e "$CA_CERT_PATH" ]]; then
    if ! confirm_overwrite "$CA_CERT_PATH"; then
        echo "${C_YELLOW}Aborted. Existing CA will not be overwritten.${C_RESET}"
        exit 0
    fi
fi

# =============================================================================
# Phase 1: Generate CA private key and self-signed certificate
# =============================================================================

write_step "Generating CA private key (RSA 4096-bit)"
openssl genrsa -out "$CA_KEY_PATH" 4096
write_gray "  $CA_KEY_PATH"

write_step "Generating self-signed CA certificate"

cat > "$CA_CNF_PATH" <<CACNF
[req]
default_bits = 4096
prompt = no
distinguished_name = dn
x509_extensions = v3_ca

[dn]
CN = $CA_COMMON_NAME
O = $CA_ORGANIZATION

[v3_ca]
basicConstraints = critical, CA:TRUE
keyUsage = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
authorityKeyIdentifier = keyid:always, issuer
CACNF

openssl req -new -x509 \
    -key "$CA_KEY_PATH" \
    -out "$CA_CERT_PATH" \
    -days "$CA_VALIDITY_DAYS" \
    -config "$CA_CNF_PATH"
write_gray "  $CA_CERT_PATH"
echo ""

# =============================================================================
# Phase 2: Generate shared service certificate with all SANs
# =============================================================================

SVC_KEY_PATH="$OUTPUT_DIR/$CERT_COMMON_NAME.key"
SVC_CSR_PATH="$OUTPUT_DIR/$CERT_COMMON_NAME.csr"
SVC_CRT_PATH="$OUTPUT_DIR/$CERT_COMMON_NAME.crt"
SVC_CNF_PATH="$OUTPUT_DIR/$CERT_COMMON_NAME.cnf"
SVC_P12_PATH="$OUTPUT_DIR/$CERT_COMMON_NAME.p12"

write_step "Building OpenSSL config with SANs for all services"

DNS_INDEX=1
ALT_NAMES_BLOCK="DNS.$DNS_INDEX = localhost"
DNS_INDEX=$((DNS_INDEX + 1))
ALT_NAMES_BLOCK+=$'\n'"DNS.$DNS_INDEX = host.docker.internal"
DNS_INDEX=$((DNS_INDEX + 1))

for svc in "${SERVICES[@]}"; do
    ALT_NAMES_BLOCK+=$'\n'"DNS.$DNS_INDEX = $svc"
    DNS_INDEX=$((DNS_INDEX + 1))
    ALT_NAMES_BLOCK+=$'\n'"DNS.$DNS_INDEX = $svc.$KUBERNETES_NAMESPACE.svc.$CLUSTER_DOMAIN"
    DNS_INDEX=$((DNS_INDEX + 1))
done

ALT_NAMES_BLOCK+=$'\n'"IP.1 = 127.0.0.1"

cat > "$SVC_CNF_PATH" <<SVCCNF
[req]
default_bits = $KEY_SIZE
prompt = no
distinguished_name = dn
req_extensions = v3_req

[dn]
CN = $CERT_COMMON_NAME
O = $CA_ORGANIZATION

[v3_req]
subjectAltName = @alt_names
basicConstraints = CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth

[alt_names]
$ALT_NAMES_BLOCK
SVCCNF

write_gray "  Config: $SVC_CNF_PATH"
write_gray "  SANs:   ${#SERVICES[@]} services x 2 DNS + localhost + host.docker.internal + 127.0.0.1"
echo ""

write_step "Generating shared service private key (RSA $KEY_SIZE-bit)"
openssl genrsa -out "$SVC_KEY_PATH" "$KEY_SIZE"
write_gray "  $SVC_KEY_PATH"

write_step "Generating CSR"
openssl req -new \
    -key "$SVC_KEY_PATH" \
    -out "$SVC_CSR_PATH" \
    -config "$SVC_CNF_PATH"
write_gray "  $SVC_CSR_PATH"

write_step "Signing certificate with CA"
openssl x509 -req \
    -in "$SVC_CSR_PATH" \
    -CA "$CA_CERT_PATH" \
    -CAkey "$CA_KEY_PATH" \
    -CAcreateserial \
    -out "$SVC_CRT_PATH" \
    -days "$CERT_VALIDITY_DAYS" \
    -extensions v3_req \
    -extfile "$SVC_CNF_PATH"
write_gray "  $SVC_CRT_PATH"

write_step "Packaging PKCS12 keystore"
openssl pkcs12 -export \
    -in "$SVC_CRT_PATH" \
    -inkey "$SVC_KEY_PATH" \
    -certfile "$CA_CERT_PATH" \
    -out "$SVC_P12_PATH" \
    -name "$CERT_COMMON_NAME" \
    -password "pass:$KEYSTORE_PASSWORD"
write_gray "  $SVC_P12_PATH"
echo ""

# =============================================================================
# Phase 3: Create CA truststore
# =============================================================================

TRUSTSTORE_PATH="$OUTPUT_DIR/granite-truststore.p12"

write_step "Creating CA truststore"

rm -f "$TRUSTSTORE_PATH"

keytool -importcert \
    -alias "granite-ca" \
    -file "$CA_CERT_PATH" \
    -keystore "$TRUSTSTORE_PATH" \
    -storetype PKCS12 \
    -storepass "$TRUSTSTORE_PASSWORD" \
    -noprompt
write_gray "  $TRUSTSTORE_PATH"
echo ""

# =============================================================================
# Phase 4: Verification
# =============================================================================

write_step "Verifying certificate against CA"
if ! RESULT="$(openssl verify -CAfile "$CA_CERT_PATH" "$SVC_CRT_PATH" 2>&1)"; then
    echo "${C_RED}  FAIL: $RESULT${C_RESET}"
    exit 1
fi
write_gray "  $RESULT"

echo ""
write_step "Certificate SANs"
openssl x509 -in "$SVC_CRT_PATH" -noout -text | { grep -E "DNS:|IP Address:" || true; } | sed -e 's/^[[:space:]]*//' | indent_gray

echo ""
write_step "CA certificate details"
openssl x509 -in "$CA_CERT_PATH" -noout -subject -issuer -dates | indent_gray

echo ""
write_step "Keystore contents"
keytool -list -keystore "$SVC_P12_PATH" -storetype PKCS12 -storepass "$KEYSTORE_PASSWORD" | indent_gray

echo ""
write_step "Truststore contents"
keytool -list -keystore "$TRUSTSTORE_PATH" -storetype PKCS12 -storepass "$TRUSTSTORE_PASSWORD" | indent_gray
echo ""

# =============================================================================
# Phase 5: Create Kubernetes secrets (optional)
# =============================================================================

if [[ "$CREATE_K8S_SECRETS" == true ]]; then
    write_step "Creating Kubernetes CA secret"

    assert_prerequisite kubectl "Install kubectl to create K8s secrets."

    kubectl create namespace "$KUBERNETES_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

    write_gray "  Creating secret: granite-ca-key-pair"
    kubectl create secret tls granite-ca-key-pair \
        --namespace="$KUBERNETES_NAMESPACE" \
        --cert="$CA_CERT_PATH" \
        --key="$CA_KEY_PATH" \
        --dry-run=client -o yaml | kubectl apply -f -

    write_success "  Kubernetes secret created in namespace '$KUBERNETES_NAMESPACE'."
    echo ""
fi

# =============================================================================
# Phase 6: Distribute to Foundation.Core (shared via classpath)
# =============================================================================

write_step "Distributing shared keystore and truststore to Granite.Foundation.Core"

RESOURCE_DIR="$FOUNDATION_SHARED_RESOURCE_DIR"

if [[ ! -d "$RESOURCE_DIR" ]]; then
    mkdir -p "$RESOURCE_DIR"
    write_gray "  Created directory: $RESOURCE_DIR"
fi

cp -f "$SVC_P12_PATH" "$RESOURCE_DIR/$CERT_COMMON_NAME.p12"
write_gray "  $CERT_COMMON_NAME.p12 -> $RESOURCE_DIR"

cp -f "$TRUSTSTORE_PATH" "$RESOURCE_DIR/granite-truststore.p12"
write_gray "  granite-truststore.p12 -> $RESOURCE_DIR"

cp -f "$CA_CERT_PATH" "$RESOURCE_DIR/granite-ca.crt"
write_gray "  granite-ca.crt -> $RESOURCE_DIR"

echo ""
write_success "  Distribution complete. Certs are in Granite.Foundation.Core (shared via classpath)."
echo ""

# =============================================================================
# Phase 7: Update microservice SSL passwords in properties files
# =============================================================================

write_step "Updating SSL passwords in microservice properties files"

CR=$'\r'
KEYSTORE_REPLACEMENT="$(escape_sed_replacement "$KEYSTORE_PASSWORD")"
TRUSTSTORE_REPLACEMENT="$(escape_sed_replacement "$TRUSTSTORE_PASSWORD")"
UPDATED_COUNT=0

if [[ -n "$MICROSERVICES_DIR" && -d "$MICROSERVICES_DIR" ]]; then
    while IFS= read -r file; do
        if ! grep -q 'spring\.ssl\.bundle\.jks\.granite-services\.keystore\.password=' "$file"; then
            continue
        fi
        BACKUP="$(mktemp)"
        cp "$file" "$BACKUP"
        sed -b -i -E \
            -e "s|^(spring\.ssl\.bundle\.jks\.granite-services\.keystore\.password=)[^${CR}]*(${CR}?)\$|\1${KEYSTORE_REPLACEMENT}\2|" \
            -e "s|^(spring\.ssl\.bundle\.jks\.granite-services\.truststore\.password=)[^${CR}]*(${CR}?)\$|\1${TRUSTSTORE_REPLACEMENT}\2|" \
            "$file"
        if ! cmp -s "$file" "$BACKUP"; then
            write_gray "  Updated: ${file#"${MICROSERVICES_DIR%/*}"/}"
            UPDATED_COUNT=$((UPDATED_COUNT + 1))
        fi
        rm -f "$BACKUP"
    done < <(find "$MICROSERVICES_DIR" -type f -name 'application-*.properties' -path '*/src/main/resources/*' -not -path '*/.build/*' -not -name 'application-production.properties')
fi

echo ""
write_success "  $UPDATED_COUNT properties file(s) updated with SSL passwords."
echo ""

# =============================================================================
# Summary
# =============================================================================

echo ""
echo "${C_GREEN}=========================================${C_RESET}"
echo "${C_GREEN}  Private CA Generation Complete${C_RESET}"
echo "${C_GREEN}=========================================${C_RESET}"
echo ""
echo "${C_CYAN}Output directory: $OUTPUT_DIR${C_RESET}"
echo "${C_CYAN}CA certificate:   $OUTPUT_DIR/ca.crt${C_RESET}"
echo "${C_YELLOW}CA private key:   $OUTPUT_DIR/ca.key${C_RESET}"
echo "${C_CYAN}Service keystore: $SVC_P12_PATH${C_RESET}"
echo "${C_CYAN}Truststore:       $TRUSTSTORE_PATH${C_RESET}"
echo "${C_CYAN}Key alias:        $CERT_COMMON_NAME${C_RESET}"
echo ""
write_warn "Keep ca.key secure -- it can sign certificates trusted by all services."
echo ""
