#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
KUBERNETES_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
CERTS_DIR="$SCRIPT_DIR/../../Granite.Foundation/Components/Granite.Foundation.Core/certs"
CA_CERT="$CERTS_DIR/ca.crt"
CA_KEY="$CERTS_DIR/ca.key"
IMAGE_TAG_FILE="$REPO_DIR/.build/image-tag"
VERSIONS_FILE="$REPO_DIR/gradle/granite-versions.gradle"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# shellcheck source=granite-environment.sh
source "$SCRIPT_DIR/granite-environment.sh"

usage() {
    cat >&2 <<EOF
usage: $(basename "$0") [local|development|test|production] [--tag <tag>] [--print-current-tag]
                        [--yes] [--wait] [--timeout <seconds>]
EOF
    exit 1
}

# =============================================================================
# Arguments
# =============================================================================
REQUESTED_ENV=""
REQUESTED_TAG=""
PRINT_CURRENT_TAG=false
ASSUME_YES=false
WAIT=false
TIMEOUT=300
while (( $# > 0 )); do
    case "$1" in
        --tag)               REQUESTED_TAG="${2:-}"; shift 2 || { say "--tag needs a value" "$RED"; exit 1; } ;;
        --tag=*)             REQUESTED_TAG="${1#--tag=}"; shift ;;
        --print-current-tag) PRINT_CURRENT_TAG=true; shift ;;
        --yes)               ASSUME_YES=true; shift ;;
        --wait)              WAIT=true; shift ;;
        --timeout)           TIMEOUT="${2:-}"; shift 2 || { say "--timeout needs a value" "$RED"; exit 1; } ;;
        --timeout=*)         TIMEOUT="${1#--timeout=}"; shift ;;
        -h|--help)           usage ;;
        -*)                  say "Unknown option: $1" "$RED"; usage ;;
        *)                   [[ -z "$REQUESTED_ENV" ]] || usage; REQUESTED_ENV="$1"; shift ;;
    esac
done
[[ "$TIMEOUT" =~ ^[1-9][0-9]*$ ]] || { say "--timeout must be a positive number of seconds" "$RED"; exit 1; }
if [[ "$ASSUME_YES" == true && -z "$REQUESTED_ENV" ]]; then
    say "--yes needs an environment argument" "$RED"; exit 1
fi
if [[ "$PRINT_CURRENT_TAG" == true && -z "$REQUESTED_ENV" ]]; then
    say "--print-current-tag needs an environment argument" "$RED"; exit 1
fi

granite_select_environment "$REQUESTED_ENV"
granite_use_kube_context

if [[ "$PRINT_CURRENT_TAG" == true ]]; then
    image="$(kubectl -n "$NAMESPACE" get deployment granite-gateway-servicehost \
             -o jsonpath='{.spec.template.spec.containers[0].image}' 2>/dev/null || true)"
    [[ -n "$image" ]] || exit 1
    echo "${image##*:}"
    exit 0
fi

OVERLAY_DIR="$KUBERNETES_DIR/$DEPLOY_ENV"
say "Selected: $DEPLOY_ENV (namespace $NAMESPACE)" "$GREEN"
echo ""

# =============================================================================
# Resolve the image tag
# =============================================================================
gradle_value() { tr -d '\r' < "$VERSIONS_FILE" | sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*\"\([^\"]*\)\".*/\1/p" | head -n 1; }
VERSION="$(gradle_value major)$(gradle_value minor)$(gradle_value patch)"
[[ -n "$VERSION" ]] || { say "Error: cannot read the version from $VERSIONS_FILE" "$RED"; exit 1; }
VERSION_PATTERN="$(printf '%s' "$VERSION" | sed 's/[.]/\\./g')"

case "$DEPLOY_ENV" in
    local)       TAG_SUFFIX="local" ;;
    development) TAG_SUFFIX="dev" ;;
    test)        TAG_SUFFIX="test" ;;
    production)  TAG_SUFFIX="prod" ;;
esac

if [[ "$DEPLOY_ENV" == "local" || "$DEPLOY_ENV" == "development" ]]; then
    IMAGE_TAG="$REQUESTED_TAG"
    if [[ -z "$IMAGE_TAG" && -s "$IMAGE_TAG_FILE" ]]; then
        IMAGE_TAG="$(tr -d '\r\n' < "$IMAGE_TAG_FILE")"
    fi
    [[ -n "$IMAGE_TAG" ]] || { say "Error: no image tag; build first: granite-build-images (or pass --tag)" "$RED"; exit 1; }
else
    [[ -z "$REQUESTED_TAG" ]] || { say "Error: --tag applies to local and development only; $DEPLOY_ENV uses IMAGE_TAG in $DEPLOY_ENV/kustomization.yaml" "$RED"; exit 1; }
    IMAGE_TAG="$(tr -d '\r' < "$OVERLAY_DIR/kustomization.yaml" | sed -n 's/^[[:space:]]*-[[:space:]]*IMAGE_TAG=\(.*\)$/\1/p' | head -n 1)"
    VERSION_PATTERN="[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?"
    VERSION="<version>"
fi
if ! printf '%s' "$IMAGE_TAG" | grep -Eq "^$VERSION_PATTERN-[0-9]{14}-$TAG_SUFFIX\$"; then
    if [[ "$IMAGE_TAG" == "unset" ]]; then
        say "Error: IMAGE_TAG in $DEPLOY_ENV/kustomization.yaml is unset; promote a tag with granite-image-tags, set it there and commit it" "$RED"
    else
        say "Error: image tag '$IMAGE_TAG' is not valid for $DEPLOY_ENV; expected $VERSION-<yyyyMMddHHmmss>-$TAG_SUFFIX" "$RED"
    fi
    exit 1
fi
say "Image tag: $IMAGE_TAG" "$GREEN"
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

if [[ "$DEPLOY_ENV" == "local" ]] && kubectl get namespace "$GRANITE_LEGACY_NAMESPACE" >/dev/null 2>&1; then
    say "  The legacy namespace '$GRANITE_LEGACY_NAMESPACE' still exists; its Ingress claims the same hosts as $NAMESPACE." "$YELLOW"
    if [[ "$ASSUME_YES" == true ]]; then
        legacy_confirm=""
    else
        read -r -p "  Delete namespace '$GRANITE_LEGACY_NAMESPACE' and everything in it? Type 'yes' to delete: " legacy_confirm
    fi
    if [[ "$legacy_confirm" == "yes" ]]; then
        kubectl delete namespace "$GRANITE_LEGACY_NAMESPACE" --wait
        say "  Deleted namespace '$GRANITE_LEGACY_NAMESPACE'." "$GREEN"
    else
        say "  Kept namespace '$GRANITE_LEGACY_NAMESPACE'. Remove it later with: kubectl delete namespace $GRANITE_LEGACY_NAMESPACE" "$YELLOW"
    fi
fi

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
    "PASSWORD_ENABLED"
    "PASSWORD_ENABLED_DEVELOPMENT"
    "PASSWORD_ENABLED_TEST"
    "OKTA_ISSUER"
    "OKTA_ISSUER_DEVELOPMENT"
    "OKTA_ISSUER_TEST"
    "OKTA_CLIENT_ID"
    "OKTA_CLIENT_ID_DEVELOPMENT"
    "OKTA_CLIENT_ID_TEST"
    "OKTA_REDIRECT_URI"
    "OKTA_REDIRECT_URI_DEVELOPMENT"
    "OKTA_REDIRECT_URI_TEST"
    "OKTA_REDIRECT_URI_CLIENT"
    "OKTA_REDIRECT_URI_CLIENT_DEVELOPMENT"
    "OKTA_REDIRECT_URI_CLIENT_TEST"
    "OKTA_ROLE_ASSIGNMENT"
    "OKTA_UNMAPPED_GROUPS"
    "OKTA_ROLE_ASSIGNMENT_DEVELOPMENT"
    "OKTA_UNMAPPED_GROUPS_DEVELOPMENT"
    "OKTA_ROLE_ASSIGNMENT_TEST"
    "OKTA_UNMAPPED_GROUPS_TEST"
    "OKTA_KEYSTORE_LOCATION"
    "OKTA_KEYSTORE_ALIAS"
    "OKTA_KEYSTORE_PASSWORD"
    "OKTA_KEYSTORE_PASSWORD_DEVELOPMENT"
    "OKTA_KEYSTORE_PASSWORD_TEST"
    "GRANITE_BOOTSTRAP_ADMIN_PASSWORD"
    "KAFKA_BOOTSTRAP_SERVERS"
    "KAFKA_SECURITY_PROTOCOL"
    "KAFKA_API_KEY"
    "KAFKA_API_SECRET"
    "KAFKA_SCHEMA_REGISTRY_URL"
    "KAFKA_SCHEMA_REGISTRY_API_KEY"
    "KAFKA_SCHEMA_REGISTRY_API_SECRET"
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
        if [[ "$ASSUME_YES" == true ]]; then
            say "Aborted (--yes): no JWT keys for $DEPLOY_ENV; set GRANITE_JWT_PRIVATE_KEY_FILE and GRANITE_JWT_PUBLIC_KEY_FILE" "$RED"
            exit 1
        fi
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
# Deploy FEC service client credential
# =============================================================================
FEC_SECRET_FILE="$JWT_KEY_DIR/fec-service-client-secret"

say "Checking FEC service client secret..." "$CYAN"
fec_env_secret="${FEC_SERVICE_CLIENT_SECRET:-}"
if [[ -n "${fec_env_secret//[[:space:]]/}" ]]; then
    fec_secret="$fec_env_secret"
    say "  Using FEC_SERVICE_CLIENT_SECRET from the environment." "$GREEN"
elif [[ -s "$FEC_SECRET_FILE" ]]; then
    fec_secret="$(tr -d '\r\n' < "$FEC_SECRET_FILE")"
    say "  Using existing secret: $FEC_SECRET_FILE" "$GREEN"
else
    say "  Generating FEC service client secret..." "$YELLOW"
    mkdir -p "$(dirname "$FEC_SECRET_FILE")"
    fec_secret="$(openssl rand -base64 32 | tr -d '\r\n')"
    printf '%s' "$fec_secret" > "$FEC_SECRET_FILE"
    chmod 600 "$FEC_SECRET_FILE"
    say "  Generated: $FEC_SECRET_FILE" "$GREEN"
fi
fec_secret_sha256="$(printf '%s' "$fec_secret" | openssl dgst -sha256 -hex | sed 's/^.*= *//')"

say "Deploying granite-fec-service-client..." "$CYAN"
fec_tmp_dir="$(umask 077 && mktemp -d)"
printf '%s' "$fec_secret" > "$fec_tmp_dir/secret"
printf '%s' "$fec_secret_sha256" > "$fec_tmp_dir/secret-sha256"
fec_status=0
kubectl create secret generic granite-fec-service-client \
    --namespace="$NAMESPACE" \
    --from-file=secret="$fec_tmp_dir/secret" \
    --from-file=secret-sha256="$fec_tmp_dir/secret-sha256" \
    --dry-run=client -o yaml | kubectl apply -f - || fec_status=$?
rm -rf "$fec_tmp_dir"
if (( fec_status != 0 )); then exit "$fec_status"; fi

# =============================================================================
# Deploy Agent service client credential
# =============================================================================
AGENT_SECRET_FILE="$JWT_KEY_DIR/agent-service-client-secret"

say "Checking Agent service client secret..." "$CYAN"
agent_env_secret="${AGENT_SERVICE_CLIENT_SECRET:-}"
if [[ -n "${agent_env_secret//[[:space:]]/}" ]]; then
    agent_secret="$agent_env_secret"
    say "  Using AGENT_SERVICE_CLIENT_SECRET from the environment." "$GREEN"
elif [[ -s "$AGENT_SECRET_FILE" ]]; then
    agent_secret="$(tr -d '\r\n' < "$AGENT_SECRET_FILE")"
    say "  Using existing secret: $AGENT_SECRET_FILE" "$GREEN"
else
    say "  Generating Agent service client secret..." "$YELLOW"
    mkdir -p "$(dirname "$AGENT_SECRET_FILE")"
    agent_secret="$(openssl rand -base64 32 | tr -d '\r\n')"
    printf '%s' "$agent_secret" > "$AGENT_SECRET_FILE"
    chmod 600 "$AGENT_SECRET_FILE"
    say "  Generated: $AGENT_SECRET_FILE" "$GREEN"
fi
agent_secret_sha256="$(printf '%s' "$agent_secret" | openssl dgst -sha256 -hex | sed 's/^.*= *//')"

say "Deploying granite-agent-service-client..." "$CYAN"
agent_tmp_dir="$(umask 077 && mktemp -d)"
printf '%s' "$agent_secret" > "$agent_tmp_dir/secret"
printf '%s' "$agent_secret_sha256" > "$agent_tmp_dir/secret-sha256"
agent_status=0
kubectl create secret generic granite-agent-service-client \
    --namespace="$NAMESPACE" \
    --from-file=secret="$agent_tmp_dir/secret" \
    --from-file=secret-sha256="$agent_tmp_dir/secret-sha256" \
    --dry-run=client -o yaml | kubectl apply -f - || agent_status=$?
rm -rf "$agent_tmp_dir"
if (( agent_status != 0 )); then exit "$agent_status"; fi

# =============================================================================
# Deploy Claude API key for the virtual assistant (never generated or logged)
# =============================================================================
say "Checking Claude API key..." "$CYAN"
if [[ -n "${ANTHROPIC_API_KEY//[[:space:]]/}" ]]; then
    claude_tmp_dir="$(umask 077 && mktemp -d)"
    printf '%s' "$ANTHROPIC_API_KEY" > "$claude_tmp_dir/ANTHROPIC_API_KEY"
    claude_status=0
    kubectl create secret generic granite-agent-claude-api \
        --namespace="$NAMESPACE" \
        --from-file=ANTHROPIC_API_KEY="$claude_tmp_dir/ANTHROPIC_API_KEY" \
        --dry-run=client -o yaml | kubectl apply -f - || claude_status=$?
    rm -rf "$claude_tmp_dir"
    if (( claude_status != 0 )); then exit "$claude_status"; fi
    say "  Deployed granite-agent-claude-api from ANTHROPIC_API_KEY." "$GREEN"
elif kubectl get secret granite-agent-claude-api --namespace="$NAMESPACE" >/dev/null 2>&1; then
    say "  Keeping the existing granite-agent-claude-api secret." "$GREEN"
else
    say "  ANTHROPIC_API_KEY is not set: the virtual assistant replies that it is not configured until granite-agent-claude-api exists." "$YELLOW"
fi



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
if [[ "$DEPLOY_ENV" == "local" || "$DEPLOY_ENV" == "development" ]]; then
    say "Deploying overlay $DEPLOY_ENV with image tag $IMAGE_TAG..." "$CYAN"
    deploy_dir="$KUBERNETES_DIR/.build/deploy-$DEPLOY_ENV-$(date -u +%Y%m%d%H%M%S)"
    mkdir -p "$deploy_dir"
    trap 'rm -rf "$deploy_dir"' EXIT
    repositories="$(kubectl kustomize "$OVERLAY_DIR" | sed -n 's/^[[:space:]]*image:[[:space:]]*\([^[:space:]]*\).*/\1/p' \
                    | sed 's/:[^:/]*$//' | sort -u)"
    {
        echo "apiVersion: kustomize.config.k8s.io/v1beta1"
        echo "kind: Kustomization"
        echo "resources:"
        echo "  - ../../$DEPLOY_ENV"
        echo "images:"
        for repository in $repositories; do
            echo "  - name: $repository"
            echo "    newTag: $IMAGE_TAG"
        done
    } > "$deploy_dir/kustomization.yaml"
    kubectl apply -k "$deploy_dir"
    rm -rf "$deploy_dir"
    trap - EXIT

    say "Restarting the Granite deployments in $NAMESPACE..." "$CYAN"
    for dep in $(kubectl -n "$NAMESPACE" get deployments -l system=granite -o jsonpath='{.items[*].metadata.name}'); do
        say "    - $dep" "$GRAY"
        kubectl -n "$NAMESPACE" rollout restart "deployment/$dep"
    done
else
    say "Deploying overlay $DEPLOY_ENV (image tag $IMAGE_TAG)..." "$CYAN"
    kubectl apply -k "$OVERLAY_DIR"
fi

# =============================================================================
# Wait for the rollouts
# =============================================================================
if [[ "$WAIT" == true ]]; then
    say "Waiting for rollouts (timeout ${TIMEOUT}s each)..." "$CYAN"
    for dep in $(kubectl -n "$NAMESPACE" get deployments -l system=granite -o jsonpath='{.items[*].metadata.name}'); do
        kubectl -n "$NAMESPACE" rollout status "deployment/$dep" --timeout="${TIMEOUT}s" \
            || { say "Rollout of $dep did not finish" "$RED"; exit 1; }
    done
fi

echo ""
say "Deployment complete ($DEPLOY_ENV)." "$GREEN"
