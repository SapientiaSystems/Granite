#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
NAMESPACE="granite"

RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }

# =============================================================================
# List pods
# =============================================================================
if ! pod_output=$(kubectl get pods --namespace="$NAMESPACE" -o jsonpath="{.items[*].metadata.name}") || [[ -z "${pod_output//[[:space:]]/}" ]]; then
    say "No pods found in namespace $NAMESPACE." "$RED"
    exit 1
fi

read -r -a pods <<< "$pod_output"

echo ""
say "Pods in namespace $NAMESPACE:" "$CYAN"
echo ""
for i in "${!pods[@]}"; do
    echo "  $((i + 1))) ${pods[$i]}"
done
echo ""

# =============================================================================
# Select pod
# =============================================================================
while true; do
    read -r -p "Select pod (1-${#pods[@]}): " choice
    if [[ "$choice" =~ ^[0-9]+$ ]] && (( 10#$choice >= 1 && 10#$choice <= ${#pods[@]} )); then
        break
    fi
done

selected_pod="${pods[$((10#$choice - 1))]}"
echo ""
say "Streaming logs for $selected_pod ..." "$GREEN"
say "Press Ctrl+C to stop." "$GRAY"
echo ""

# =============================================================================
# Tail logs
# =============================================================================
kubectl logs --namespace="$NAMESPACE" "$selected_pod" --follow --tail=100
