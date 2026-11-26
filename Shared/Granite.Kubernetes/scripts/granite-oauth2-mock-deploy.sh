#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAMESPACE="granite-local"
MANIFEST_DIR="$SCRIPT_DIR/../oauth2-mock"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# =============================================================================
# Verify prerequisites
# =============================================================================
say "Checking prerequisites..." "$CYAN"
if ! command -v kubectl >/dev/null 2>&1; then
    say "Error: 'kubectl' is required but not found on PATH." "$RED"
    exit 1
fi

# =============================================================================
# Confirm cluster
# =============================================================================
# shellcheck source=granite-environment.sh
source "$(cd "$(dirname "$0")" && pwd)/granite-environment.sh"
granite_select_environment local
granite_use_kube_context
say "  The OAuth2 mock server is for local testing only (namespace $NAMESPACE)." "$GRAY"

# =============================================================================
# Create namespace
# =============================================================================
say "Creating namespace $NAMESPACE..." "$CYAN"
kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# =============================================================================
# Deploy OAuth2 mock server
# =============================================================================
say "Deploying granite-oauth2-mock..." "$CYAN"
kubectl apply -k "$MANIFEST_DIR"

say "Waiting for granite-oauth2-mock to become ready..." "$CYAN"
kubectl -n "$NAMESPACE" rollout status deployment/granite-oauth2-mock --timeout=180s

# =============================================================================
# Okta client key store
# =============================================================================
OKTA_KEYSTORE_FILE="${GRANITE_OKTA_KEYSTORE_FILE:-$HOME/.granite/dev-keys/okta-client.p12}"
OKTA_KEYSTORE_PASSWORD="${OKTA_KEYSTORE_PASSWORD:-granite-local}"

say "Checking Okta client key store..." "$CYAN"
if [[ -e "$OKTA_KEYSTORE_FILE" ]]; then
    say "  Using existing key store:" "$GREEN"
else
    keytool_cmd="$(command -v keytool 2>/dev/null || true)"
    if [[ -z "$keytool_cmd" && -n "${JAVA_HOME-}" && -x "$JAVA_HOME/bin/keytool" ]]; then
        keytool_cmd="$JAVA_HOME/bin/keytool"
    fi
    if [[ -z "$keytool_cmd" ]]; then
        say "Error: 'keytool' (JDK) is required to generate the Okta client key store but not found on PATH or in JAVA_HOME." "$RED"
        exit 1
    fi
    say "  Generating RSA 2048-bit Okta client key store..." "$YELLOW"
    mkdir -p "$(dirname "$OKTA_KEYSTORE_FILE")"
    "$keytool_cmd" -genkeypair -alias okta-client -keyalg RSA -keysize 2048 -validity 3650 \
        -dname "CN=granite-session-local" -storetype PKCS12 \
        -keystore "$OKTA_KEYSTORE_FILE" -storepass "$OKTA_KEYSTORE_PASSWORD" >/dev/null
    chmod 600 "$OKTA_KEYSTORE_FILE"
    say "  Generated:" "$GREEN"
fi
say "    $OKTA_KEYSTORE_FILE" "$GRAY"

say "Deploying granite-session-okta..." "$CYAN"
kubectl create secret generic granite-session-okta \
    --namespace="$NAMESPACE" \
    --from-file=okta-client.p12="$OKTA_KEYSTORE_FILE" \
    --from-literal=keystore-password="$OKTA_KEYSTORE_PASSWORD" \
    --dry-run=client -o yaml | kubectl apply -f -

# =============================================================================
# Point Session and the FrontEndControllers at the mock
# =============================================================================
if kubectl -n "$NAMESPACE" get deployment granite-session-commandhost >/dev/null 2>&1; then
    say "Configuring granite-session-commandhost for the mock issuer..." "$CYAN"
    kubectl -n "$NAMESPACE" patch deployment granite-session-commandhost \
        --type=strategic --patch-file="$MANIFEST_DIR/session-commandhost-okta-patch.yaml"
    kubectl -n "$NAMESPACE" set env deployment/granite-session-commandhost GRANITE_SESSION_LOGIN_OKTA_ENABLED-
    mock_ip="$(kubectl -n "$NAMESPACE" get service granite-oauth2-mock -o jsonpath='{.spec.clusterIP}')"
    say "  Mapping oauth2-mock.localhost to $mock_ip inside granite-session-commandhost..." "$GRAY"
    alias_patch="$(mktemp)"
    printf '{"spec":{"template":{"spec":{"hostAliases":[{"ip":"%s","hostnames":["oauth2-mock.localhost"]}]}}}}' "$mock_ip" > "$alias_patch"
    kubectl -n "$NAMESPACE" patch deployment granite-session-commandhost --type=merge --patch-file="$alias_patch"
    rm -f "$alias_patch"
else
    say "  granite-session-commandhost not deployed; skipping (re-run after granite-system-deploy.sh)." "$YELLOW"
fi
for dep in granite-frontendcontroller-userhost granite-frontendcontroller-adminhost; do
    if kubectl -n "$NAMESPACE" get deployment "$dep" >/dev/null 2>&1; then
        say "Enabling Okta login on $dep..." "$CYAN"
        kubectl -n "$NAMESPACE" set env "deployment/$dep" OKTA_ENABLED=true GRANITE_SESSION_LOGIN_OKTA_ENABLED-
    else
        say "  $dep not deployed; skipping (re-run after granite-system-deploy.sh)." "$YELLOW"
    fi
done

echo ""
say "Deployment complete. Run granite-oauth2-mock-start.sh to expose it on the host." "$GREEN"
