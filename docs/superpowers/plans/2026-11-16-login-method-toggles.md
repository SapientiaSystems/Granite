# Login Method Toggles Implementation Plan

**Goal:** Wire both login toggles the same way on every host and environment, and refuse to start when both login methods are disabled.

**Branch:** `feature/login-method-toggles` off `development`.

## Decisions

- Okta: Session CommandHost `application-local.properties` reads `granite.session.login.okta.enabled=${OKTA_ENABLED:false}` (was hard-coded `false`), matching the FEC hosts.
- Password: Session CommandHost, FEC UserHost and FEC AdminHost read `granite.session.login.password.enabled` from `${PASSWORD_ENABLED:true}` (local, production), `${PASSWORD_ENABLED_DEVELOPMENT:true}`, `${PASSWORD_ENABLED_TEST:true}`. `PASSWORD_ENABLED`, `PASSWORD_ENABLED_DEVELOPMENT`, `PASSWORD_ENABLED_TEST` become optional `granite-secrets` entries in `granite-system-deploy.sh` and `.ps1`, next to the Okta ones.
- At least one method: Session CommandHost and both FEC hosts fail at startup with `IllegalStateException("at least one login method must be enabled: granite.session.login.password.enabled or granite.session.login.okta.enabled")` when both are false.
  - Session: a singleton `loginMethodsCheck(PasswordLoginSettings,OktaSettings)` bean on `GraniteSessionCommandServerConfiguration` (seam-overridable, like the others).
  - FEC: the per-request `SessionLoginSettings.fromConfiguration(configuration)` becomes a singleton `sessionLoginSettings()` bean (built once at startup, like `sessionCookies()`), reused by `createContext`; `SessionLoginSettings` throws from its constructor when both are false.
- oauth2-mock: `session-commandhost-okta-patch.yaml` sets `OKTA_ENABLED=true` instead of `GRANITE_SESSION_LOGIN_OKTA_ENABLED`; both mock deploy scripts (`.sh`, `.ps1`) also remove a stale `GRANITE_SESSION_LOGIN_OKTA_ENABLED` from the Session CommandHost after patching.

## Tasks

1. **Session**: local Okta placeholder, password placeholders in all four profiles, `loginMethodsCheck` bean. Test in `GraniteSessionCommandServerConfigurationTest`: both false throws the message; either true passes. Commit.
2. **FEC**: password placeholders in both hosts' four profiles; `SessionLoginSettings` constructor check; `sessionLoginSettings()` singleton bean used by `createContext`. Tests: `SessionLoginSettings` both false throws, one true passes; existing tests stay green. Commit.
3. **Kubernetes scripts**: optional `PASSWORD_ENABLED*` entries (.sh, .ps1); oauth2-mock patch and stale-variable removal (.sh, .ps1). Check with `bash -n` and a `kustomize`/`kubectl kustomize` render of the oauth2-mock overlay if it is a kustomization. Commit.
4. **Docs**: root `CLAUDE.md` (login methods paragraph), Session `Claude.md`, FEC `CLAUDE.md`, Kubernetes docs if they list the optional variables. Commit.

Then: build and push Session CommandHost, FEC UserHost and FEC AdminHost images; you deploy and test; merge on approval.

## Acceptance

- `OKTA_ENABLED=true` turns Okta on for Session and the FEC in every profile, local included.
- `PASSWORD_ENABLED=false` turns password login off on Session and the FEC.
- Both off: Session CommandHost and FEC hosts fail to start with the message above.
