# Granite

Multi-service Spring Boot system with a React web front end. See `CLAUDE.md` for the architecture and the extension points.

## Versioning

| | Development snapshot | Release |
|---|---|---|
| Maven jars | `X.Y.0-SNAPSHOT` | `X.Y.0` |
| npm packages | `X.Y.0-snapshot.<timestamp>`, dist-tag `snapshot-X.Y.0` | `X.Y.0-release.<timestamp>`, dist-tags `release-X.Y.0` and `latest` |
| Docker images | `<version>-<yyyyMMddHHmmss>-local` or `-dev` | retagged `-test` and `-prod` |

`development` is at `1.0.0-SNAPSHOT` and depends on the Strata `1.0.0` releases. The version is set in `gradle/granite-versions.gradle`.

To release `X.Y.0`:

1. Merge `development` into `release-X.Y`; the build pipeline publishes the snapshot.
2. Run `./granite-promote.sh --version X.Y.0 --dry-run` (or `.\granite-promote.ps1 -Version X.Y.0 -DryRun`), check the output, then run it without the dry-run flag.
3. Tag the commit `vX.Y.0`.
4. Move `development` to the next minor version.

Images are tagged and promoted separately; see `Shared/Granite.Kubernetes/IMAGE-TAGGING.md`.
