#!/usr/bin/env bash
set -euo pipefail

NAMESPACE="granite"
PORT="${GRANITE_OAUTH2_MOCK_PORT:-8090}"
ISSUER_ID="default"
ISSUER="http://host.docker.internal:$PORT/$ISSUER_ID"

RED=$'\033[31m'; GREEN=$'\033[32m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# =============================================================================
# Verify prerequisites
# =============================================================================
if ! command -v kubectl >/dev/null 2>&1; then
    say "Error: 'kubectl' is required but not found on PATH." "$RED"
    exit 1
fi

deployment="$(kubectl -n "$NAMESPACE" get deployment granite-oauth2-mock -o jsonpath='{.metadata.name}' 2>/dev/null || true)"
if [[ -z "${deployment//[[:space:]]/}" ]]; then
    say "Error: granite-oauth2-mock is not deployed. Run granite-oauth2-mock-deploy.sh first." "$RED"
    exit 1
fi

say "Waiting for granite-oauth2-mock to become ready..." "$CYAN"
kubectl -n "$NAMESPACE" rollout status deployment/granite-oauth2-mock --timeout=180s

# =============================================================================
# Expose on the host
# =============================================================================
echo ""
say "OAuth2 mock server" "$CYAN"
say "  Issuer:    $ISSUER" "$GRAY"
say "  Discovery: $ISSUER/.well-known/openid-configuration" "$GRAY"
say "  Session:   granite.session.okta.issuer=$ISSUER" "$GRAY"
echo ""
say "Port-forwarding 0.0.0.0:$PORT -> svc/granite-oauth2-mock:8080 (Ctrl+C to stop)..." "$GREEN"
kubectl -n "$NAMESPACE" port-forward --address 0.0.0.0 svc/granite-oauth2-mock "$PORT:8080"
