# Granite Image Tagging

How Granite container images are named, tagged, built, deployed, promoted and cleaned up. Jars and npm packages are versioned
separately (Strata approach: `X.Y.0-SNAPSHOT`/`X.Y.0`, npm dist-tags `snapshot-X.Y.0`/`release-X.Y.0`, `granite-promote`);
see "Versioning and Release" in the root `CLAUDE.md`.

Sources: `gradle/granite-image.gradle`, `FrontEnds/Granite.Web/docker-image.js`, `FrontEnds/Granite.Web/docker-auth.js`,
`Shared/Granite.Kubernetes/scripts/granite-build-images.{ps1,sh}`, `granite-system-deploy.{ps1,sh}`,
`granite-environment.{ps1,sh}`, `granite-image-tags.{ps1,sh}`, the Kustomize overlays,
`.github/workflows/build-pipeline.yml` and `.github/workflows/image-promotion.yml`.

## 1. Tag format

```
<version>-<yyyyMMddHHmmss>-<suffix>
e.g. 1.0.0-SNAPSHOT-20261125120000-local
```

| Part | Source |
|---|---|
| `version` | `gradle/granite-versions.gradle`: `major` + `minor` + `patch` (currently `1` `.0` `.0-SNAPSHOT` = `1.0.0-SNAPSHOT`). Every script reads it from this file. |
| timestamp | UTC build time, 14 digits |
| `suffix` | `local` (developer build), `dev` (CI build), `test` / `prod` (created only by promotion) |

Rules enforced everywhere:

- A **build** may only produce `-local` or `-dev` tags (`^<version>-[0-9]{14}-(local|dev)$`).
- `-test` and `-prod` tags are never built; they are **retags of an existing digest** (promotion).
- No `latest` tag and no extra tags: Jib's `to.tags` is emptied and `docker-image.js` tags one name.
- One tag covers the **whole system**: every Java and web image of one build gets the same tag.

## 2. Image names

All images live in `ghcr.io/johnliebenau/granite/` (29 repositories):

| Kind | Repository | Defined in |
|---|---|---|
| Java hosts (Jib) | `granite-<svc>-<host>` (e.g. `granite-user-commandhost`, `granite-gateway-servicehost`) | each host's `build.gradle` `jib.to.image` |
| Test suites (Jib) | `granite-<svc>-testsuite` (9) | test suite `build.gradle` |
| Web hosts (docker) | `granite-web-apphost`, `granite-web-clienthost` | `FrontEnds/Granite.Web/package.json` (`containerize-*`, `push-*`) |

The managed list is `IMAGES` in `granite-image-tags.{ps1,sh}`.

## 3. Build metadata

| Variable | Meaning | Set by |
|---|---|---|
| `BUILD_IMAGE_TAG` | the tag; **required** to publish anything | `granite-build-images` locally, `prepare-build-tag` in CI |
| `BUILD_GIT_SHA` | commit SHA | `git rev-parse HEAD` / `GITHUB_SHA` |
| `BUILD_CREATED` | UTC ISO-8601 time | script / CI |
| `BUILD_USER` | `git config user.name` (or `USERNAME`/`USER`) / `GITHUB_ACTOR` | script / CI |

Every image gets the labels `org.opencontainers.image.version`, `.revision`, `.created`, `granite.build.user` and
`granite.image.tag` (empty values are skipped).

## 4. Tagging components

- `gradle/granite-image.gradle`, applied after the `jib {}` block of every Jib host and TestSuite
  (`apply from: rootProject.file('../../gradle/granite-image.gradle')`). Each of those projects also has
  `tasks.named('build') { finalizedBy('jib') }`. It validates `BUILD_IMAGE_TAG`, sets `jib.to.image = <repository>:<tag>` and
  `jib.to.tags = []`, and adds the labels. `jib`, `jibBuildTar` and `jibDockerBuild` are **skipped** (with a warning) when the
  tag is unset, so `./gradlew build` never publishes, and **fail** when it is malformed.
- `FrontEnds/Granite.Web/docker-image.js build|push <image> [context]`: same version, regex and labels; skips when the tag is
  unset. `DOCKER_IMAGE_DRY_RUN=1` prints the docker command instead of running it. `docker-auth.js from` logs in to Docker
  Hub (`DOCKER_USER`/`DOCKER_PASSWORD`) for base images; `docker-auth.js to` logs in to `ghcr.io`
  (`REPOSITORY_USER`/`REPOSITORY_PASSWORD`).

## 5. Local / developer flow

### 5.1 Build: `granite-build-images.ps1` (`.sh` equivalent)

```
granite-build-images.ps1 [-Environment local|development] [-Tag <tag>] [-JavaOnly | -WebOnly] [-DryRun]
granite-build-images.sh  [--environment local|development] [--tag <tag>] [--java-only | --web-only] [--dry-run]
```

1. **Tag**: `-Tag`, else `BUILD_IMAGE_TAG`, else `<version>-<UTC now>-<suffix>`. Suffix: `development` gives `dev`, `local`
   gives `local`; without it, from the given tag, else `dev` when `CI=true`, otherwise `local`.
2. **Metadata**: fills `BUILD_GIT_SHA`, `BUILD_CREATED`, `BUILD_USER` if unset (the PowerShell script restores them on exit).
3. **Prerequisites**: `docker`, `git` (+ `node`, `yarn` for web); `REPOSITORY_USER`, `REPOSITORY_PASSWORD`,
   `PROPERTIES_ENCRYPTION_KEY`, `DOCKER_USER`, `DOCKER_PASSWORD`.
4. **Uniqueness**: `docker manifest inspect granite-gateway-servicehost:<tag>`; aborts if the tag already exists.
5. **Lanes**, in parallel, logging to `.build/image-build/<tag>-{java,web}.log`:
   - Java: `./gradlew build jib` (CommitStage tests, then every Jib image).
   - Web: `FrontEnds/Granite.Web` `build-applications`, then `containerize-granite-web-{apphost,clienthost}` and
     `push-granite-web-{apphost,clienthost}`. The web apps are built from the published `snapshot-*` npm packages;
     **no npm package is published** by an image build.
6. On success writes the tag to `.build/image-tag` (git-ignored); on failure prints the last 30 log lines and exits 1.

### 5.2 Deploy: `granite-system-deploy.ps1` (`.sh` equivalent)

```
granite-system-deploy.ps1 [-Environment local|development|test|production] [-Tag <tag>]
                          [-PrintCurrentTag] [-Yes] [-Wait] [-TimeoutSeconds <n>]
granite-system-deploy.sh  [env] [--tag <tag>] [--print-current-tag] [--yes] [--wait] [--timeout <n>]
```

| Flag | Effect |
|---|---|
| `--tag` / `-Tag` | the image tag to deploy (local/development only) |
| `--print-current-tag` / `-PrintCurrentTag` | prints only the tag the environment runs (from `granite-gateway-servicehost`) and exits; nothing is deployed; needs an environment |
| `--yes` / `-Yes` | never prompts: needs an environment; a local deploy keeps the legacy namespace `granite`; missing test/production JWT keys fail instead of being generated |
| `--wait` / `-Wait` | waits until every Granite deployment has rolled out; fails on the first that doesn't |
| `--timeout` / `-TimeoutSeconds` | per-deployment rollout timeout for `--wait` (default 300, must be positive) |

For **local** and **development**:

1. Tag = `--tag`, else `.build/image-tag` (error "build first: granite-build-images" if neither).
2. The tag must match the current version and the environment's suffix (`-local`, `-dev`).
3. The overlay is rendered, every `image:` repository is collected, and a temporary kustomization in
   `Shared/Granite.Kubernetes/.build/deploy-<env>-<timestamp>/` sets `images: newTag: <tag>` for **every** image; it is
   applied with `kubectl apply -k` and deleted.
4. `kubectl rollout restart` of every deployment labelled `system=granite` in the environment's namespace
   (`imagePullPolicy: Always`).

For **test** and **production**: `kubectl apply -k` of the overlay as is. The tag is the overlay's `IMAGE_TAG`; the deploy
refuses `unset` or a tag without the `-test` / `-prod` suffix.

`--wait` runs for every environment.

### 5.3 Kustomize configuration

- `base/granite-system.yml` references every image as `<repository>:unset` and carries no namespace.
- Each overlay sets a top-level `namespace:` (which also renames the base `Namespace` object), an `IMAGE_TAG` literal in the
  `granite-environment` ConfigMap, and a `replacements` rule that writes it into `containers.0.image` of every Deployment.

| Overlay | Namespace | `IMAGE_TAG` |
|---|---|---|
| `local` | `granite-local` | `unset` (always overridden by the deploy script) |
| `development` | `granite-dev` | `unset` (always overridden by the deploy script) |
| `test` | `granite-test` | the promoted test tag, e.g. `1.0.0-SNAPSHOT-20261125120000-test` |
| `production` | `granite-prod` | the promoted production tag, e.g. `1.0.0-SNAPSHOT-20261125120000-prod` |

For test and production the overlay value **is** the release record: after `granite-image-tags promote`, set `IMAGE_TAG`
in `Shared/Granite.Kubernetes/<test|production>/kustomization.yaml`, commit it, then deploy.

Certificates: each overlay has its own namespaced `Issuer` (`granite-ca-issuer` local, `granite-letsencrypt-staging`
development and test, `granite-letsencrypt-prod` production); the Ingress uses `cert-manager.io/issuer`. The oauth2-mock
overlay deploys into `granite-local`.

### 5.4 Environments, namespaces and contexts

`scripts/granite-environment.{ps1,sh}` holds the maps used by the deploy, clean, monitor, pod-tail and oauth2-mock scripts:

| Environment | Namespace | kubectl context checked | Tags it runs |
|---|---|---|---|
| `local` | `granite-local` | `docker-desktop` | `-local` (from `.build/image-tag` or `--tag`) |
| `development` | `granite-dev` | `GRANITE_DEVELOPMENT_CONTEXT` (required, no default) | `-dev` |
| `test` | `granite-test` | not checked (warning only) | promoted `-test` |
| `production` | `granite-prod` | not checked (warning only) | promoted `-prod` |

- The scripts take the environment as `-Environment <env>` (PowerShell) or the first argument (bash), or show a menu.
- To see which tag an environment runs: `granite-system-deploy <env> --print-current-tag`, or
  `kubectl -n granite-local get deploy granite-gateway-servicehost -o jsonpath='{.spec.template.spec.containers[0].image}'`.
- The namespace `granite`, which every environment used to share, is legacy. A local deploy offers to delete it, because its
  Ingress claims the same hosts.
- Service certificates: `granite-create-private-ca` writes SANs `<svc>.<ns>.svc.cluster.local` for every namespace in
  `--san-namespaces` / `-SanNamespaces` (default `granite-local`, `granite-dev`, `granite-test`). Services call each other
  by short name, which is always a SAN, so existing certificates keep working in the new namespaces.

## 6. CI flow

`build-pipeline.yml` (push to `development` or `release-*`, or `workflow_dispatch`):

| Job | Tagging role |
|---|---|
| `prepare-build-tag` | computes `BUILD_IMAGE_TAG=<version>-<UTC now>-dev` plus `BUILD_GIT_SHA`/`BUILD_CREATED`/`BUILD_USER` and validates it |
| `java-commit-stage` | `./gradlew build` pushes every Java and TestSuite image under the tag |
| `typescript-commit-stage` | `yarn run test` (npm snapshots), then `containerize-granite-web` and `push-granite-web` under the tag |
| `java-integration-stage`, `java-acceptance-stage` | placeholders naming the TestSuite images of the tag |
| `deploy-stage` | Maven snapshots to GitHub Packages |
| `cleanup-failed-build` | on failure: `granite-image-tags.sh delete "$BUILD_IMAGE_TAG" --yes` removes that build's images |

Pull requests build and test only: no tag, so nothing is published.

`image-promotion.yml` (manual `workflow_dispatch`, inputs `candidate_tag` and `target` `test|prod`) runs
`granite-image-tags.sh promote "$CANDIDATE_TAG" <target> --yes` and lists the overlay change to commit in the job summary.

Secrets and variables: `PROPERTIES_ENCRYPTION_KEY`, `REPOSITORY_USER` (var), `REPOSITORY_PASSWORD` (token with
`read:packages`, `write:packages` and `delete:packages`), `DOCKER_USER` (var), `DOCKER_PASSWORD`, `NODE_AUTH_TOKEN`.

## 7. Registry management: `granite-image-tags.ps1` (`.sh` equivalent)

```
granite-image-tags.ps1 list [<tag>] [-Suffix local|dev|test|prod]
granite-image-tags.ps1 promote <candidate-tag> <test|prod>
granite-image-tags.ps1 delete <tag>
                       [-DryRun] [-Yes]
```

Uses the GitHub Packages API (`/users/johnliebenau/packages/container/granite%2F<image>/versions`) with
`REPOSITORY_PASSWORD`. Repositories without build tags are reported as *skipped*.

- **list** (no tag): every build tag and how many managed repositories have it; incomplete tags are flagged.
- **list <tag>**: per repository present/missing/skipped with digests; exit 2 if incomplete.
- **promote**: fixed chain `-dev` → `-test` → `-prod`, timestamp kept. Refuses when the candidate is missing anywhere or the
  target already exists anywhere. Retags with `docker buildx imagetools create --tag <target> <candidate>` (no rebuild, same
  digest) and verifies every target digest. Asks for confirmation unless `--yes` or `CI=true`.
- **delete**: only `-local` and `-dev` tags. GHCR deletes whole image versions (the digest and all its tags), so delete
  **refuses** when a version carrying the tag also carries any other tag (for example after that build was promoted).

## 8. Lifecycle summary

```
developer:  granite-build-images (-local) ─▶ .build/image-tag ─▶ granite-system-deploy local       (granite-local)
CI:         prepare-build-tag (-dev) ─▶ jib + web push ─▶ integration ─▶ acceptance
                                   └─ failure ─▶ granite-image-tags delete <tag>
release:    granite-image-tags promote <x>-dev test ─▶ <x>-test   (same digests)
              ─▶ set test/kustomization.yaml IMAGE_TAG=<x>-test, commit ─▶ granite-system-deploy test
            granite-image-tags promote <x>-test prod ─▶ <x>-prod  (same digests)
              ─▶ set production/kustomization.yaml IMAGE_TAG=<x>-prod, commit ─▶ granite-system-deploy production
cleanup:    granite-image-tags list / delete (-local, -dev only)
```

## 9. Warnings

- **Partial builds.** `-JavaOnly` / `-WebOnly` still write `.build/image-tag` but publish only part of the images. The next
  deploy applies that tag to every image, and the missing ones fail with `ImagePullBackOff`. Run a full build first, or update
  only the affected deployments with `kubectl -n <namespace> set image deploy/<name> "*=<repo>:<tag>"` and put the previous
  full-build tag back in `.build/image-tag`.
- **A push that fails part-way** leaves some repositories with the tag; `.build/image-tag` is not written and a rerun with the
  same tag is refused ("already exists"). Re-run only the failed push with the same `BUILD_IMAGE_TAG` (web:
  `yarn run push-granite-web-<apphost|clienthost>` in `FrontEnds/Granite.Web`; Java: `./gradlew jib`), confirm with
  `granite-image-tags list <tag>` (exit 0), then write `.build/image-tag` yourself. Or delete the tag and build again. Never
  deploy an incomplete tag.
- **A local or development deploy restarts every Granite deployment in its namespace.** Other environments are never touched.
