#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAMESPACE="granite"
CERTS_DIR="$SCRIPT_DIR/../../Granite.Foundation/Components/Granite.Foundation.Core/certs"
CA_CERT="$CERTS_DIR/ca.crt"
CA_KEY="$CERTS_DIR/ca.key"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# =============================================================================
# Prompt for DEPLOY_ENV
# =============================================================================
echo ""
say "Select deployment environment:" "$CYAN"
echo "  1) local"
echo "  2) development"
echo "  3) test"
echo "  4) production"
echo ""

while true; do
    read -r -p "Enter choice (1-4): " choice
    case "$choice" in
        1) DEPLOY_ENV="local"; break ;;
        2) DEPLOY_ENV="development"; break ;;
        3) DEPLOY_ENV="test"; break ;;
        4) DEPLOY_ENV="production"; break ;;
    esac
done

OVERLAY_DIR="$SCRIPT_DIR/../$DEPLOY_ENV"
say "Selected: $DEPLOY_ENV" "$GREEN"
echo ""

# =============================================================================
# Validate environment variables
# =============================================================================
missing=()

required_vars=(
    "PROPERTIES_ENCRYPTION_KEY"
    "REPOSITORY_USER"
    "REPOSITORY_PASSWORD"
    "DOCKER_USER"
    "DOCKER_PASSWORD"
)
for var in "${required_vars[@]}"; do
    val="${!var-}"
    if [[ -z "${val//[[:space:]]/}" ]]; then
        missing+=("$var")
    fi
done
if (( ${#missing[@]} > 0 )); then
    say "Error: missing required environment variables:" "$RED"
    for var in "${missing[@]}"; do
        say "  - $var" "$RED"
    done
    exit 1
fi

# =============================================================================
# Validate local CA files exist
# =============================================================================
if [[ "$DEPLOY_ENV" == "local" ]]; then
    if [[ ! -e "$CA_CERT" ]]; then
        say "Error: CA certificate not found at $CA_CERT" "$RED"
        exit 1
    fi
    if [[ ! -e "$CA_KEY" ]]; then
        say "Error: CA key not found at $CA_KEY" "$RED"
        exit 1
    fi
fi

# =============================================================================
# Verify prerequisites
# =============================================================================
say "Checking prerequisites..." "$CYAN"
if ! command -v kubectl >/dev/null 2>&1; then
    say "Error: 'kubectl' is required but not found on PATH." "$RED"
    exit 1
fi
if ! command -v helm >/dev/null 2>&1; then
    say "helm not found — install it (https://helm.sh/docs/intro/install/) and re-run" "$RED"
    exit 1
fi
if ! command -v openssl >/dev/null 2>&1; then
    say "Error: 'openssl' is required to generate JWT signing keys but not found on PATH." "$RED"
    exit 1
fi

# =============================================================================
# Ensure nginx-ingress-controller
# =============================================================================
say "Checking for nginx-ingress-controller..." "$CYAN"

nginx_deployment="$(kubectl get deployment ingress-nginx-controller -n ingress-nginx -o jsonpath='{.metadata.name}' 2>/dev/null || true)"

if [[ -n "${nginx_deployment//[[:space:]]/}" ]]; then
    say "  nginx-ingress-controller already installed." "$GREEN"
else
    say "  nginx-ingress-controller not found. Installing via Helm..." "$YELLOW"

    orphaned_ns="$(kubectl get namespace ingress-nginx -o jsonpath='{.metadata.name}' 2>/dev/null || true)"
    if [[ -n "${orphaned_ns//[[:space:]]/}" ]]; then
        say "  Cleaning up orphaned ingress-nginx resources..." "$YELLOW"
        helm_release="$(helm list -n ingress-nginx -q --filter ingress-nginx 2>/dev/null || true)"
        if [[ -n "${helm_release//[[:space:]]/}" ]]; then
            helm uninstall ingress-nginx -n ingress-nginx 2>/dev/null || true
        fi
        kubectl delete namespace ingress-nginx --ignore-not-found --wait
    fi

    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx

    helm repo update

    ingress_args=()
    if [[ "$DEPLOY_ENV" == "local" ]]; then
        ingress_args=(--set controller.hostPort.enabled=true --set controller.service.type=NodePort)
    fi

    helm install ingress-nginx ingress-nginx/ingress-nginx \
        --namespace ingress-nginx \
        --create-namespace \
        ${ingress_args[@]+"${ingress_args[@]}"} \
        --wait \
        --timeout 5m

    say "  nginx-ingress-controller installed." "$GREEN"
fi

# =============================================================================
# Ensure cert-manager
# =============================================================================
say "Checking for cert-manager..." "$CYAN"

cert_manager_deployment="$(kubectl get deployment cert-manager -n cert-manager -o jsonpath='{.metadata.name}' 2>/dev/null || true)"

if [[ -n "${cert_manager_deployment//[[:space:]]/}" ]]; then
    say "  cert-manager already installed." "$GREEN"
else
    say "  cert-manager not found. Installing via Helm..." "$YELLOW"

    helm repo add jetstack https://charts.jetstack.io

    helm repo update

    helm install cert-manager jetstack/cert-manager \
        --namespace cert-manager \
        --create-namespace \
        --set crds.enabled=true \
        --wait \
        --timeout 5m

    say "  cert-manager installed." "$GREEN"
fi

# =============================================================================
# Create namespace
# =============================================================================
say "Creating namespace $NAMESPACE..." "$CYAN"
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# =============================================================================
# Deploy application secrets
# =============================================================================
say "Deploying granite-secrets..." "$CYAN"
secret_args=(
    --from-literal=PROPERTIES_ENCRYPTION_KEY="$PROPERTIES_ENCRYPTION_KEY"
    --from-literal=REPOSITORY_USER="$REPOSITORY_USER"
    --from-literal=REPOSITORY_PASSWORD="$REPOSITORY_PASSWORD"
    --from-literal=DOCKER_USER="$DOCKER_USER"
    --from-literal=DOCKER_PASSWORD="$DOCKER_PASSWORD"
)
optional_vars=(
    "DB_USERNAME"
    "DB_PASSWORD"
    "GRANITE_KEYSTORE_LOCATION"
    "GRANITE_KEYSTORE_PASSWORD"
    "GRANITE_TRUSTSTORE_LOCATION"
    "GRANITE_TRUSTSTORE_PASSWORD"
    "OKTA_ENABLED"
    "OKTA_ENABLED_DEVELOPMENT"
    "OKTA_ENABLED_TEST"
    "OKTA_ISSUER"
    "OKTA_ISSUER_DEVELOPMENT"
    "OKTA_ISSUER_TEST"
    "OKTA_CLIENT_ID"
    "OKTA_CLIENT_ID_DEVELOPMENT"
    "OKTA_CLIENT_ID_TEST"
    "OKTA_REDIRECT_URI"
    "OKTA_REDIRECT_URI_DEVELOPMENT"
    "OKTA_REDIRECT_URI_TEST"
    "OKTA_POST_LOGOUT_REDIRECT_URI"
    "OKTA_POST_LOGOUT_REDIRECT_URI_DEVELOPMENT"
    "OKTA_POST_LOGOUT_REDIRECT_URI_TEST"
    "OKTA_AUTHORITIES_SOURCE"
    "OKTA_AUTHORITIES_SOURCE_DEVELOPMENT"
    "OKTA_AUTHORITIES_SOURCE_TEST"
    "OKTA_KEYSTORE_LOCATION"
    "OKTA_KEYSTORE_ALIAS"
    "OKTA_KEYSTORE_PASSWORD"
    "OKTA_KEYSTORE_PASSWORD_DEVELOPMENT"
    "OKTA_KEYSTORE_PASSWORD_TEST"
    "GRANITE_BOOTSTRAP_ADMIN_PASSWORD"
)
for var in "${optional_vars[@]}"; do
    val="${!var-}"
    if [[ -n "${val//[[:space:]]/}" ]]; then
        secret_args+=(--from-literal="$var=$val")
        say "    + $var" "$GRAY"
    fi
done
kubectl create secret generic granite-secrets \
    --namespace="$NAMESPACE" \
    "${secret_args[@]}" \
    --dry-run=client -o yaml | kubectl apply -f -

# =============================================================================
# Deploy ghcr.io registry credentials
# =============================================================================
say "Deploying ghcr-registry secret..." "$CYAN"
kubectl create secret docker-registry ghcr-registry \
    --namespace="$NAMESPACE" \
    --docker-server=ghcr.io \
    --docker-username="$REPOSITORY_USER" \
    --docker-password="$REPOSITORY_PASSWORD" \
    --dry-run=client -o yaml | kubectl apply -f -

# =============================================================================
# Deploy Session JWT signing keys
# =============================================================================
if [[ "$DEPLOY_ENV" == "local" ]]; then
    JWT_KEY_DIR="$HOME/.granite/dev-keys"
    DEFAULT_JWT_PRIVATE="$JWT_KEY_DIR/jwt-test-private-key.pem"
    DEFAULT_JWT_PUBLIC="$JWT_KEY_DIR/jwt-test-public-key.pem"
else
    JWT_KEY_DIR="$HOME/.granite/$DEPLOY_ENV-keys"
    DEFAULT_JWT_PRIVATE="$JWT_KEY_DIR/jwt-private-key.pem"
    DEFAULT_JWT_PUBLIC="$JWT_KEY_DIR/jwt-public-key.pem"
fi
JWT_PRIVATE_KEY_FILE="${GRANITE_JWT_PRIVATE_KEY_FILE:-$DEFAULT_JWT_PRIVATE}"
JWT_PUBLIC_KEY_FILE="${GRANITE_JWT_PUBLIC_KEY_FILE:-$DEFAULT_JWT_PUBLIC}"

say "Checking Session JWT signing keys..." "$CYAN"
if [[ -e "$JWT_PRIVATE_KEY_FILE" && -e "$JWT_PUBLIC_KEY_FILE" ]]; then
    say "  Using existing keys:" "$GREEN"
elif [[ -e "$JWT_PRIVATE_KEY_FILE" || -e "$JWT_PUBLIC_KEY_FILE" ]]; then
    say "Error: only one JWT key file exists; provide both or neither:" "$RED"
    say "  $JWT_PRIVATE_KEY_FILE" "$RED"
    say "  $JWT_PUBLIC_KEY_FILE" "$RED"
    exit 1
else
    if [[ "$DEPLOY_ENV" == "test" || "$DEPLOY_ENV" == "production" ]]; then
        say "  No JWT keys found for $DEPLOY_ENV. New keys invalidate every token issued with previous keys." "$YELLOW"
        read -r -p "  Generate a new JWT key pair? (y/N): " answer
        if [[ ! "$answer" =~ ^[Yy]$ ]]; then
            say "Aborted: set GRANITE_JWT_PRIVATE_KEY_FILE and GRANITE_JWT_PUBLIC_KEY_FILE to the existing keys." "$RED"
            exit 1
        fi
    fi
    say "  Generating RSA 2048-bit JWT key pair..." "$YELLOW"
    mkdir -p "$(dirname "$JWT_PRIVATE_KEY_FILE")" "$(dirname "$JWT_PUBLIC_KEY_FILE")"
    openssl genpkey -quiet -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "$JWT_PRIVATE_KEY_FILE" 2>/dev/null
    openssl pkey -in "$JWT_PRIVATE_KEY_FILE" -pubout -out "$JWT_PUBLIC_KEY_FILE" 2>/dev/null
    chmod 600 "$JWT_PRIVATE_KEY_FILE"
    say "  Generated:" "$GREEN"
fi
say "    $JWT_PRIVATE_KEY_FILE" "$GRAY"
say "    $JWT_PUBLIC_KEY_FILE" "$GRAY"

say "Deploying granite-session-jwt..." "$CYAN"
kubectl create secret generic granite-session-jwt \
    --namespace="$NAMESPACE" \
    --from-file=jwt-private-key.pem="$JWT_PRIVATE_KEY_FILE" \
    --from-file=jwt-public-key.pem="$JWT_PUBLIC_KEY_FILE" \
    --dry-run=client -o yaml | kubectl apply -f -



# =============================================================================
# Deploy local CA key pair
# =============================================================================
if [[ "$DEPLOY_ENV" == "local" ]]; then
    say "Deploying granite-ca-key-pair..." "$CYAN"
    kubectl create secret tls granite-ca-key-pair \
        --namespace="$NAMESPACE" \
        --cert="$CA_CERT" \
        --key="$CA_KEY" \
        --dry-run=client -o yaml | kubectl apply -f -
fi

# =============================================================================
# Deploy system manifest
# =============================================================================
say "Deploying overlay $DEPLOY_ENV..." "$CYAN"
kubectl apply -k "$OVERLAY_DIR"

# =============================================================================
# Restart all deployments
# =============================================================================
if [[ "$DEPLOY_ENV" == "local" || "$DEPLOY_ENV" == "development" ]]; then
    say "Restarting all deployments to pick up latest images..." "$CYAN"
    deployments="$(kubectl -n "$NAMESPACE" get deployments -o jsonpath='{.items[*].metadata.name}')"
    for dep in $deployments; do
        say "    - $dep" "$GRAY"
        kubectl -n "$NAMESPACE" rollout restart "deployment/$dep"
    done
fi

echo ""
say "Deployment complete ($DEPLOY_ENV)." "$GREEN"
