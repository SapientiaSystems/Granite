#!/usr/bin/env bash
# Sourced by the Granite Kubernetes scripts; writes messages to stderr only.

GRANITE_ENVIRONMENTS=(local development test production)
GRANITE_LEGACY_NAMESPACE="granite"

granite_namespace_for() {
    case "$1" in
        local)       echo "granite-local" ;;
        development) echo "granite-dev" ;;
        test)        echo "granite-test" ;;
        production)  echo "granite-prod" ;;
        *)           return 1 ;;
    esac
}

granite_env_say() { printf '%s%s\033[0m\n' "$2" "$1" >&2; }

granite_select_environment() {
    local requested="${1:-}" choice i
    if [[ -n "$requested" ]]; then
        if ! NAMESPACE="$(granite_namespace_for "$requested")"; then
            granite_env_say "Unknown environment '$requested' (expected: ${GRANITE_ENVIRONMENTS[*]})" $'\033[31m'
            exit 1
        fi
        DEPLOY_ENV="$requested"
        return 0
    fi
    granite_env_say "" ""
    granite_env_say "Select environment:" $'\033[36m'
    for i in "${!GRANITE_ENVIRONMENTS[@]}"; do
        printf '  %d) %-12s (namespace %s)\n' "$((i + 1))" "${GRANITE_ENVIRONMENTS[$i]}" \
            "$(granite_namespace_for "${GRANITE_ENVIRONMENTS[$i]}")" >&2
    done
    while true; do
        read -r -p "Enter choice (1-${#GRANITE_ENVIRONMENTS[@]}): " choice
        if [[ "$choice" =~ ^[1-9][0-9]*$ ]] && (( choice <= ${#GRANITE_ENVIRONMENTS[@]} )); then
            DEPLOY_ENV="${GRANITE_ENVIRONMENTS[$((choice - 1))]}"
            NAMESPACE="$(granite_namespace_for "$DEPLOY_ENV")"
            return 0
        fi
    done
}

granite_use_kube_context() {
    local current
    current="$(kubectl config current-context 2>/dev/null || true)"
    case "$DEPLOY_ENV" in
        local)
            if [[ "$current" != "docker-desktop" ]]; then
                granite_env_say "Refusing: environment 'local' needs kubectl context 'docker-desktop' (current: '${current:-none}')." $'\033[31m'
                exit 1
            fi
            ;;
        development)
            if [[ -z "${GRANITE_DEVELOPMENT_CONTEXT:-}" ]]; then
                granite_env_say "Refusing: set GRANITE_DEVELOPMENT_CONTEXT to the kubectl context of the development cluster." $'\033[31m'
                exit 1
            fi
            if [[ "$current" != "$GRANITE_DEVELOPMENT_CONTEXT" ]]; then
                granite_env_say "Refusing: environment 'development' needs kubectl context '$GRANITE_DEVELOPMENT_CONTEXT' (current: '${current:-none}')." $'\033[31m'
                exit 1
            fi
            ;;
        *)
            granite_env_say "Warning: the kubectl context is not checked for '$DEPLOY_ENV' (current: '${current:-none}')." $'\033[33m'
            ;;
    esac
}
