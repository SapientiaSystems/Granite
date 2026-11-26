#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

CYAN=$'\033[36m'; YELLOW=$'\033[33m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# shellcheck source=granite-environment.sh
source "$SCRIPT_DIR/granite-environment.sh"
granite_select_environment "${1:-}"
granite_use_kube_context

echo ""
say "Granite System Status ($DEPLOY_ENV, namespace $NAMESPACE)" "$CYAN"
say "========================" "$CYAN"
echo ""

say "Pods:" "$YELLOW"
kubectl get pods --namespace="$NAMESPACE" -o wide || true
echo ""

say "Services:" "$YELLOW"
kubectl get services --namespace="$NAMESPACE" || true
echo ""

say "Deployments:" "$YELLOW"
kubectl get deployments --namespace="$NAMESPACE" || true
echo ""

say "HPAs:" "$YELLOW"
kubectl get hpa --namespace="$NAMESPACE" || true
echo ""

say "Ingress:" "$YELLOW"
kubectl get ingress --namespace="$NAMESPACE" || true
echo ""

say "Certificates and Issuers:" "$YELLOW"
kubectl get certificate,issuer --namespace="$NAMESPACE" || true
echo ""

