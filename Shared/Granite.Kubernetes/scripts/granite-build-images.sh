#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"
WEB_DIR="$REPO_DIR/FrontEnds/Granite.Web"
VERSIONS_FILE="$REPO_DIR/gradle/granite-versions.gradle"
IMAGE_TAG_FILE="$REPO_DIR/.build/image-tag"
LOG_DIR="$REPO_DIR/.build/image-build"
REGISTRY_IMAGE="ghcr.io/johnliebenau/granite/granite-gateway-servicehost"

NAME="$(basename "$0")"
RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say() { printf '%s%s%s\n' "$2" "$1" "$RESET"; }
die() { say "$NAME: $*" "$RED" >&2; exit 1; }

usage() {
    cat >&2 <<EOF
usage: $NAME [--environment local|development] [--tag <tag>] [--java-only | --web-only] [--dry-run]

Builds every Granite image under one tag <version>-<yyyyMMddHHmmss>-<local|dev> and pushes it:
  Java lane: ./gradlew build jib (CommitStage tests, then Jib for every host and TestSuite)
  Web lane:  FrontEnds/Granite.Web build-applications, containerize and push of AppHost and ClientHost
The lanes run in parallel and log to .build/image-build/<tag>-{java,web}.log. On success the tag
is written to .build/image-tag for granite-system-deploy. No npm package is published.
EOF
    exit 1
}

ENVIRONMENT=""
TAG=""
JAVA=true
WEB=true
DRY_RUN=false
while (( $# > 0 )); do
    case "$1" in
        --environment) ENVIRONMENT="${2:-}"; shift 2 || usage ;;
        --tag)         TAG="${2:-}"; shift 2 || usage ;;
        --java-only)   WEB=false; shift ;;
        --web-only)    JAVA=false; shift ;;
        --dry-run)     DRY_RUN=true; shift ;;
        -h|--help)     usage ;;
        *)             say "unknown option: $1" "$RED" >&2; usage ;;
    esac
done
$JAVA || $WEB || die "--java-only and --web-only exclude each other"

# =============================================================================
# Tag
# =============================================================================
gradle_value() { tr -d '\r' < "$VERSIONS_FILE" | sed -n "s/^[[:space:]]*$1[[:space:]]*=[[:space:]]*\"\([^\"]*\)\".*/\1/p" | head -n 1; }
VERSION="$(gradle_value major)$(gradle_value minor)$(gradle_value patch)"
[[ -n "$VERSION" ]] || die "cannot read the version from $VERSIONS_FILE"
VERSION_PATTERN="$(printf '%s' "$VERSION" | sed 's/[.]/\\./g')"

case "$ENVIRONMENT" in
    local)       SUFFIX="local" ;;
    development) SUFFIX="dev" ;;
    "")          SUFFIX="" ;;
    *)           die "--environment must be local or development" ;;
esac
[[ -n "$TAG" ]] || TAG="${BUILD_IMAGE_TAG:-}"
if [[ -z "$SUFFIX" ]]; then
    if [[ -n "$TAG" ]]; then
        SUFFIX="${TAG##*-}"
    elif [[ "${CI:-}" == "true" ]]; then
        SUFFIX="dev"
    else
        SUFFIX="local"
    fi
fi
[[ -n "$TAG" ]] || TAG="$VERSION-$(date -u +%Y%m%d%H%M%S)-$SUFFIX"
printf '%s' "$TAG" | grep -Eq "^$VERSION_PATTERN-[0-9]{14}-(local|dev)\$" \
    || die "tag '$TAG' is invalid; expected $VERSION-<yyyyMMddHHmmss>-<local|dev>"
[[ "$TAG" == *"-$SUFFIX" ]] || die "tag '$TAG' does not end in -$SUFFIX"

# =============================================================================
# Metadata
# =============================================================================
export BUILD_IMAGE_TAG="$TAG"
export BUILD_GIT_SHA="${BUILD_GIT_SHA:-$(git -C "$REPO_DIR" rev-parse HEAD 2>/dev/null || true)}"
export BUILD_CREATED="${BUILD_CREATED:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"
export BUILD_USER="${BUILD_USER:-$(git -C "$REPO_DIR" config user.name 2>/dev/null || echo "${USERNAME:-${USER:-}}")}"

say "Image tag: $TAG" "$GREEN"
say "  revision $BUILD_GIT_SHA, created $BUILD_CREATED, user $BUILD_USER" "$GRAY"

# =============================================================================
# Prerequisites
# =============================================================================
missing=()
tools=(docker git)
$WEB && tools+=(node yarn)
for tool in "${tools[@]}"; do command -v "$tool" >/dev/null 2>&1 || missing+=("command $tool"); done
for var in REPOSITORY_USER REPOSITORY_PASSWORD PROPERTIES_ENCRYPTION_KEY DOCKER_USER DOCKER_PASSWORD; do
    [[ -n "${!var:-}" ]] || missing+=("variable $var")
done
if (( ${#missing[@]} > 0 )); then
    for item in "${missing[@]}"; do say "  missing $item" "$RED"; done
    $DRY_RUN || die "prerequisites missing"
else
    say "Prerequisites: ok" "$GREEN"
fi

JAVA_COMMAND="cd \"$REPO_DIR\" && ./gradlew build jib"
WEB_COMMAND="cd \"$WEB_DIR\" && yarn run build-applications && yarn run containerize-granite-web-apphost && yarn run containerize-granite-web-clienthost && yarn run push-granite-web-apphost && yarn run push-granite-web-clienthost"

if $DRY_RUN; then
    say "[dry-run] docker manifest inspect $REGISTRY_IMAGE:$TAG (must not exist)" "$YELLOW"
    $JAVA && say "[dry-run] java: $JAVA_COMMAND  > .build/image-build/$TAG-java.log" "$YELLOW"
    $WEB  && say "[dry-run] web:  $WEB_COMMAND  > .build/image-build/$TAG-web.log" "$YELLOW"
    say "[dry-run] on success: write $TAG to .build/image-tag" "$YELLOW"
    exit 0
fi

# =============================================================================
# Uniqueness
# =============================================================================
printf '%s' "$REPOSITORY_PASSWORD" | docker login ghcr.io -u "$REPOSITORY_USER" --password-stdin >/dev/null \
    || die "docker login to ghcr.io failed"
if docker manifest inspect "$REGISTRY_IMAGE:$TAG" >/dev/null 2>&1; then
    die "Tag $TAG already exists ($REGISTRY_IMAGE)"
fi

# =============================================================================
# Build lanes
# =============================================================================
mkdir -p "$LOG_DIR"
pids=()
lanes=()
if $JAVA; then
    say "Java lane: logging to .build/image-build/$TAG-java.log" "$CYAN"
    bash -c "$JAVA_COMMAND" > "$LOG_DIR/$TAG-java.log" 2>&1 & pids+=($!); lanes+=(java)
fi
if $WEB; then
    say "Web lane:  logging to .build/image-build/$TAG-web.log" "$CYAN"
    bash -c "$WEB_COMMAND" > "$LOG_DIR/$TAG-web.log" 2>&1 & pids+=($!); lanes+=(web)
fi

failed=()
for i in "${!pids[@]}"; do
    if wait "${pids[$i]}"; then
        say "  ${lanes[$i]} lane succeeded" "$GREEN"
    else
        failed+=("${lanes[$i]}")
    fi
done
if (( ${#failed[@]} > 0 )); then
    for lane in "${failed[@]}"; do
        say "${lane} lane failed; last 30 lines of .build/image-build/$TAG-$lane.log:" "$RED"
        tail -n 30 "$LOG_DIR/$TAG-$lane.log" >&2
    done
    exit 1
fi

mkdir -p "$(dirname "$IMAGE_TAG_FILE")"
printf '%s' "$TAG" > "$IMAGE_TAG_FILE"
say "Built and pushed $TAG; written to .build/image-tag" "$GREEN"
