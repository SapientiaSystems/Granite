#!/usr/bin/env bash
set -euo pipefail

REGISTRY="ghcr.io"
OWNER="johnliebenau"
OWNER_SCOPE="users"
IMAGE_NAMESPACE="granite"
API="https://api.github.com"
IMAGES=(
    granite-agent-commandhost
    granite-agent-queryhost
    granite-agent-testsuite
    granite-client-commandhost
    granite-client-queryhost
    granite-client-testsuite
    granite-content-commandhost
    granite-content-queryhost
    granite-content-testsuite
    granite-feedback-commandhost
    granite-feedback-queryhost
    granite-feedback-testsuite
    granite-foundation-testsuite
    granite-frontendcontroller-adminhost
    granite-frontendcontroller-testsuite
    granite-frontendcontroller-userhost
    granite-gateway-servicehost
    granite-outbox-servicehost
    granite-session-commandhost
    granite-session-queryhost
    granite-session-testsuite
    granite-survey-commandhost
    granite-survey-queryhost
    granite-survey-testsuite
    granite-user-commandhost
    granite-user-queryhost
    granite-user-testsuite
    granite-web-apphost
    granite-web-clienthost
)
BUILD_TAG_PATTERN='-[0-9]{14}-(local|dev|test|prod)$'

NAME="$(basename "$0")"
RED=$'\033[31m'; GREEN=$'\033[32m'; YELLOW=$'\033[33m'; CYAN=$'\033[36m'; GRAY=$'\033[90m'; RESET=$'\033[0m'
say()  { printf '%s%s%s\n' "$2" "$1" "$RESET"; }
die()  { say "$NAME: $*" "$RED" >&2; exit 1; }

usage() {
    cat >&2 <<EOF
usage: $NAME list [<tag>] [--suffix local|dev|test|prod]
       $NAME promote <candidate-tag> <test|prod>
       $NAME delete <tag>
                     [--dry-run] [--yes]

Manages the Granite build tags (<version>-<yyyyMMddHHmmss>-<local|dev|test|prod>) of the
${#IMAGES[@]} images in $REGISTRY/$OWNER/$IMAGE_NAMESPACE.

  list              every build tag and how many repositories have it (incomplete ones are flagged)
  list <tag>        each repository: present, missing or skipped, with its digest; exit 2 if incomplete
  promote           -dev -> -test or -test -> -prod, same timestamp; retags the same digest
                    (docker buildx imagetools create) and verifies every target digest
  delete            deletes a -local or -dev tag from every repository that has it; refuses when
                    the image version also carries another tag (GHCR deletes whole versions)

Environment: REPOSITORY_USER, REPOSITORY_PASSWORD (token with read, write and delete:packages).
Prompts for confirmation unless --yes or CI=true.
EOF
    exit 1
}

COMMAND="${1:-}"; [[ -n "$COMMAND" ]] || usage; shift
ARGS=()
SUFFIX=""
DRY_RUN=false
ASSUME_YES=false
[[ "${CI:-}" == "true" ]] && ASSUME_YES=true
while (( $# > 0 )); do
    case "$1" in
        --suffix)   SUFFIX="${2:-}"; shift 2 || usage ;;
        --dry-run)  DRY_RUN=true; shift ;;
        --yes)      ASSUME_YES=true; shift ;;
        -h|--help)  usage ;;
        -*)         say "unknown option: $1" "$RED" >&2; usage ;;
        *)          ARGS+=("$1"); shift ;;
    esac
done
[[ -z "$SUFFIX" || "$SUFFIX" =~ ^(local|dev|test|prod)$ ]] || die "--suffix must be local, dev, test or prod"

[[ -n "${REPOSITORY_USER:-}" && -n "${REPOSITORY_PASSWORD:-}" ]] || die "REPOSITORY_USER and REPOSITORY_PASSWORD must be set"
command -v curl >/dev/null || die "curl is required"
command -v node >/dev/null || die "node is required"

WORK="$(mktemp -d "${TMPDIR:-/tmp}/granite-image-tags-XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
API_AUTH="$WORK/api.curlrc"
( umask 077
  {
      printf 'header = "Authorization: Bearer %s"\n' "$REPOSITORY_PASSWORD"
      printf 'header = "Accept: application/vnd.github+json"\n'
      printf 'header = "X-GitHub-Api-Version: 2022-11-28"\n'
  } > "$API_AUTH" )

confirm() {
    $ASSUME_YES && return 0
    local answer
    read -r -p "$1 [y/N] " answer
    [[ "$answer" =~ ^(y|yes)$ ]]
}

versions_url() { printf '%s/%s/%s/packages/container/%s%%2F%s/versions' "$API" "$OWNER_SCOPE" "$OWNER" "$IMAGE_NAMESPACE" "$1"; }

# Writes "<version id> <digest> <tag>,<tag>,..." lines for one image to stdout; empty when the package does not exist.
image_versions() {
    local image="$1" page=1 json lines
    while true; do
        json="$(curl -sS -K "$API_AUTH" -w '\n%{http_code}' "$(versions_url "$image")?per_page=100&page=$page")" \
            || die "GitHub API request failed for $image"
        local status="${json##*$'\n'}"
        json="${json%$'\n'*}"
        [[ "$status" == "404" ]] && return 0
        [[ "$status" == "200" ]] || die "GitHub API returned HTTP $status for $image"
        lines="$(printf '%s' "$json" | node -e '
            let s = "";
            process.stdin.on("data", d => s += d).on("end", () => {
                const v = JSON.parse(s);
                for (const x of v) {
                    const tags = ((x.metadata || {}).container || {}).tags || [];
                    process.stdout.write(x.id + " " + x.name + " " + tags.join(",") + "\n");
                }
            });')"
        [[ -n "$lines" ]] || return 0
        printf '%s\n' "$lines"
        page=$((page + 1))
    done
}

load_all() {
    local image
    mkdir -p "$WORK/versions"
    for image in "${IMAGES[@]}"; do
        image_versions "$image" > "$WORK/versions/$image"
    done
}

# Prints "<id> <digest> <tags>" of the version of <image> that carries <tag>, or nothing.
version_with_tag() {
    awk -v tag="$2" '{ n = split($3, t, ","); for (i = 1; i <= n; i++) if (t[i] == tag) { print; exit } }' "$WORK/versions/$1"
}

has_build_tags() { cut -d' ' -f3 "$WORK/versions/$1" | tr ',' '\n' | grep -Eq -- "$BUILD_TAG_PATTERN"; }

check_tag() {
    printf '%s' "$1" | grep -Eq -- "^[0-9]+\.[0-9]+\.[0-9]+(-SNAPSHOT)?$BUILD_TAG_PATTERN" \
        || die "'$1' is not a build tag (<version>-<yyyyMMddHHmmss>-<local|dev|test|prod>)"
}

cmd_list() {
    load_all
    if (( ${#ARGS[@]} == 0 )); then
        local managed=0 image
        for image in "${IMAGES[@]}"; do has_build_tags "$image" && managed=$((managed + 1)); done
        local tags
        tags="$(cut -d' ' -f3 "$WORK"/versions/* 2>/dev/null | tr ',' '\n' | grep -E -- "$BUILD_TAG_PATTERN" \
                | { if [[ -n "$SUFFIX" ]]; then grep -E -- "-$SUFFIX\$"; else cat; fi; } || true)"
        [[ -n "$tags" ]] || say "No build tags${SUFFIX:+ with suffix -$SUFFIX}." "$YELLOW"
        [[ -z "$tags" ]] || printf '%s\n' "$tags" \
            | sort | uniq -c | sort -k2 | while read -r count tag; do
                if (( count < managed )); then
                    say "$tag  $count of $managed repositories (incomplete)" "$YELLOW"
                else
                    say "$tag  $count of $managed repositories" "$GREEN"
                fi
            done
        for image in "${IMAGES[@]}"; do has_build_tags "$image" || say "skipped: $image (no build tags)" "$GRAY"; done
        return 0
    fi
    local tag="${ARGS[0]}" image line present=0 missing=0 managed=0
    check_tag "$tag"
    for image in "${IMAGES[@]}"; do
        if ! has_build_tags "$image"; then
            say "  skipped  $image (no build tags)" "$GRAY"; continue
        fi
        managed=$((managed + 1))
        line="$(version_with_tag "$image" "$tag")"
        if [[ -n "$line" ]]; then
            present=$((present + 1)); say "  present  $image  $(cut -d' ' -f2 <<< "$line")" "$GREEN"
        else
            missing=$((missing + 1)); say "  missing  $image" "$RED"
        fi
    done
    say "$tag: $present of $managed repositories" "$CYAN"
    (( missing == 0 && present > 0 )) || exit 2
}

docker_login() {
    $DRY_RUN && return 0
    command -v docker >/dev/null || die "docker is required"
    printf '%s' "$REPOSITORY_PASSWORD" | docker login "$REGISTRY" -u "$REPOSITORY_USER" --password-stdin >/dev/null \
        || die "docker login to $REGISTRY failed"
}

image_digest() {
    docker buildx imagetools inspect "$REGISTRY/$OWNER/$IMAGE_NAMESPACE/$1:$2" --format '{{json .Manifest}}' 2>/dev/null \
        | node -e 'let s="";process.stdin.on("data",d=>s+=d).on("end",()=>{try{process.stdout.write(JSON.parse(s).digest||"")}catch(e){}})'
}

cmd_promote() {
    (( ${#ARGS[@]} == 2 )) || usage
    local candidate="${ARGS[0]}" target_env="${ARGS[1]}" from to target image line refused=0
    check_tag "$candidate"
    case "$target_env" in
        test) from="dev";  to="test" ;;
        prod) from="test"; to="prod" ;;
        *)    die "promote target must be test or prod" ;;
    esac
    [[ "$candidate" == *"-$from" ]] || die "only a -$from tag can be promoted to $to (got $candidate)"
    target="${candidate%-$from}-$to"
    step_line() { say "==> $*" "$CYAN"; }
    step_line "Promote $candidate -> $target (${#IMAGES[@]} repositories)"
    load_all
    for image in "${IMAGES[@]}"; do
        if [[ -z "$(version_with_tag "$image" "$candidate")" ]]; then
            say "  missing candidate: $image:$candidate" "$RED"; refused=1
        fi
        if [[ -n "$(version_with_tag "$image" "$target")" ]]; then
            say "  target already exists: $image:$target" "$RED"; refused=1
        fi
    done
    (( refused == 0 )) || die "refused: the candidate must exist and the target must not exist in every repository"
    if $DRY_RUN; then
        for image in "${IMAGES[@]}"; do
            say "  [dry-run] docker buildx imagetools create --tag $REGISTRY/$OWNER/$IMAGE_NAMESPACE/$image:$target $REGISTRY/$OWNER/$IMAGE_NAMESPACE/$image:$candidate" "$YELLOW"
        done
        return 0
    fi
    confirm "Retag ${#IMAGES[@]} images $candidate -> $target?" || { say "Aborted." "$RED"; exit 1; }
    docker_login
    for image in "${IMAGES[@]}"; do
        docker buildx imagetools create --tag "$REGISTRY/$OWNER/$IMAGE_NAMESPACE/$image:$target" \
            "$REGISTRY/$OWNER/$IMAGE_NAMESPACE/$image:$candidate" >/dev/null \
            || die "retag failed for $image"
        local source_digest target_digest
        source_digest="$(image_digest "$image" "$candidate")"
        target_digest="$(image_digest "$image" "$target")"
        [[ -n "$source_digest" && "$source_digest" == "$target_digest" ]] \
            || die "digest mismatch for $image: $candidate=$source_digest $target=$target_digest"
        say "  $image:$target = $target_digest" "$GREEN"
    done
    say "Promoted $candidate -> $target in ${#IMAGES[@]} repositories." "$GREEN"
}

cmd_delete() {
    (( ${#ARGS[@]} == 1 )) || usage
    local tag="${ARGS[0]}" image line others refused=0
    check_tag "$tag"
    [[ "$tag" =~ -(local|dev)$ ]] || die "only -local and -dev tags can be deleted (got $tag)"
    load_all
    local targets=()
    for image in "${IMAGES[@]}"; do
        line="$(version_with_tag "$image" "$tag")"
        [[ -n "$line" ]] || continue
        others="$(cut -d' ' -f3 <<< "$line" | tr ',' '\n' | grep -vxF -- "$tag" | tr '\n' ' ' || true)"
        if [[ -n "${others// /}" ]]; then
            say "  $image: version $(cut -d' ' -f2 <<< "$line") also carries: $others" "$RED"; refused=1
        fi
        targets+=("$image $(cut -d' ' -f1 <<< "$line")")
    done
    (( refused == 0 )) || die "refused: GHCR deletes whole image versions, and these versions carry other tags"
    (( ${#targets[@]} > 0 )) || { say "No repository has $tag." "$YELLOW"; return 0; }
    if $DRY_RUN; then
        for line in "${targets[@]}"; do say "  [dry-run] would DELETE ${line% *}:$tag (version id ${line#* })" "$YELLOW"; done
        return 0
    fi
    confirm "Delete $tag from ${#targets[@]} repositories?" || { say "Aborted." "$RED"; exit 1; }
    for line in "${targets[@]}"; do
        curl -sS -f -K "$API_AUTH" -X DELETE "$(versions_url "${line% *}")/${line#* }" >/dev/null \
            || die "delete failed for ${line% *} (token needs delete:packages)"
        say "  deleted ${line% *}:$tag" "$GREEN"
    done
}

case "$COMMAND" in
    list)    cmd_list ;;
    promote) cmd_promote ;;
    delete)  cmd_delete ;;
    *)       usage ;;
esac
