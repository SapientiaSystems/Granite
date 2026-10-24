#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAMESPACE="granite"
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
context="$(kubectl config current-context 2>/dev/null || true)"
say "  Current context: $context" "$GRAY"
if [[ "$context" != "docker-desktop" ]]; then
    say "The OAuth2 mock server is for local testing only." "$YELLOW"
    read -r -p "Deploy it to context '$context' anyway? (y/N): " answer || answer=""
    if [[ ! "$answer" =~ ^[Yy]$ ]]; then
        say "Aborted." "$RED"
        exit 0
    fi
fi

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

echo ""
say "Deployment complete. Run granite-oauth2-mock-start.sh to expose it on the host." "$GREEN"
