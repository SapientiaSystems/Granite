#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAMESPACE="granite"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# =============================================================================
# Confirm
# =============================================================================
echo ""
say "This will delete ALL resources in the '$NAMESPACE' namespace:" "$YELLOW"
echo "  - Deployments, Services, HPAs, Ingresses"
echo "  - Secrets (granite-secrets, granite-tls, granite-ca-key-pair, ghcr-registry, granite-session-jwt, granite-fec-service-client)"
echo "  - ClusterIssuers (granite-letsencrypt-staging, granite-letsencrypt-prod)"
echo "  - Namespace"
echo ""

read -r -p "Type 'yes' to confirm: " confirm || confirm=""
if [[ "$confirm" != "yes" ]]; then
    say "Aborted." "$RED"
    exit 0
fi

echo ""

# =============================================================================
# Delete all resources in the namespace
# =============================================================================
say "Deleting all resources in namespace $NAMESPACE..." "$CYAN"
if ! kubectl delete all --all --namespace="$NAMESPACE"; then
    say "Warning: some resources may not have been deleted" "$YELLOW"
fi

say "Deleting ingresses..." "$CYAN"
if ! kubectl delete ingress --all --namespace="$NAMESPACE"; then
    say "Warning: no ingresses found" "$YELLOW"
fi

say "Deleting secrets..." "$CYAN"
kubectl delete secret granite-secrets --namespace="$NAMESPACE" --ignore-not-found || true
kubectl delete secret granite-tls --namespace="$NAMESPACE" --ignore-not-found || true
kubectl delete secret granite-ca-key-pair --namespace="$NAMESPACE" --ignore-not-found || true
kubectl delete secret ghcr-registry --namespace="$NAMESPACE" --ignore-not-found || true
kubectl delete secret granite-session-jwt --namespace="$NAMESPACE" --ignore-not-found || true
kubectl delete secret granite-fec-service-client --namespace="$NAMESPACE" --ignore-not-found || true

say "Deleting ClusterIssuers..." "$CYAN"
kubectl delete clusterissuer granite-letsencrypt-staging granite-letsencrypt-prod --ignore-not-found || true

say "Deleting HPAs..." "$CYAN"
if ! kubectl delete hpa --all --namespace="$NAMESPACE"; then
    say "Warning: no HPAs found" "$YELLOW"
fi

# =============================================================================
# Delete namespace
# =============================================================================
say "Deleting namespace $NAMESPACE..." "$CYAN"
kubectl delete namespace "$NAMESPACE" --ignore-not-found || true

echo ""
say "Clean complete. Ready for redeployment." "$GREEN"
