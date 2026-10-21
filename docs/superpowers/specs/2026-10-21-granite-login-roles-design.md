# Granite Login Roles (ADMIN / AUDITOR) — Design Spec

**Date:** 2026-10-21
**Status:** Draft — awaiting user review
**Scope:** `Granite.Session` (login gate), `Granite.Gateway` (role rules), `Granite.Web` (role-aware UI).
**Follow-up:** the Users screen (master-detail grid via FrontEndController) is a separate spec that builds on this one.

---

## 1. Intent

Granite is an administration application. Only two kinds of people should be able to use it:

- **Administrators** (`ROLE_ADMIN`) — full access: read, create, update, delete.
- **Auditors** (`ROLE_AUDITOR`) — see every view, change nothing.

Anyone else must not be able to log in at all. Health checks and (outside production) Swagger stay open to everyone.

Today the deployed Gateway (`Granite.Gateway.ServiceHost` `HostConfiguration`) declares no authorization rules, so any valid token can call every route, and Session issues a token to any user with valid credentials. This spec closes both gaps before the Users screen makes user management reachable from the UI.

## 2. Goals

1. Session refuses login (password and OAuth2) for users who hold neither `ROLE_ADMIN` nor `ROLE_AUDITOR`. No session row, no token.
2. The Gateway enforces the read/write split in §4.2 for every authenticated request.
3. The web app hides editing actions from auditors and keeps working normally for admins.

## 3. Non-goals and anti-goals

**Non-goals**

- Per-resource or per-field authorization. Rules are path + method only (Gateway convention).
- Moving the Users screen to the admin FrontEndController. It stays on the user FrontEndController; it may be copied to the admin one later.
- Re-reading a user's authorities on token refresh. Today `refresh` re-mints from the authorities stored on the session; an authority change takes effect at the user's next login. Unchanged by this spec.
- Restricting service-to-service calls. Session→User, FrontEndController→User and Gateway→Session revocation lookups go directly to the service hosts, not through the Gateway, and are unaffected.
- Narrowing the Gateway actuator exposure (`management.endpoints.web.exposure.include=*`). Under §4.2 the actuator becomes ADMIN-only; reducing what it exposes is a follow-up.
- Audit logging or rate-limiting of refused logins, and equalising response timing between "wrong password" and "no permitted role" *(builder's call: judged low risk for an internal admin tool)*.

**Anti-goals** (failure even if it "works")

- A user without either role ends up holding a valid Granite token.
- An auditor can change data by calling an endpoint directly, bypassing the UI.
- The login refusal reveals that an account exists or what roles it lacks.
- Admins lose any capability they have today.

## 4. Design

### 4.1 Session: login gate

**Property** (follows the existing `granite.session.*` convention, read via `Environment`/`IConfiguration` in `AbstractGraniteSessionServerConfiguration` like the other Session properties):

```
granite.session.login.required-authorities=ROLE_ADMIN,ROLE_AUDITOR
```

- Comma-separated. A user may log in if they hold **at least one** listed authority. Other authorities (e.g. `ROLE_USER`) may coexist and are ignored by the gate.
- Authority names pass unchanged from User → session → JWT `authorities` claim → Gateway `GraniteJwtAuthenticationConverter`; there is no prefix mapping, so the same `ROLE_*` strings are used everywhere.
- Empty or absent → no gate (today's behaviour). This keeps Session reusable outside Granite.
- Set to `ROLE_ADMIN,ROLE_AUDITOR` in the `local` and `development` Session CommandHost profiles, where the User bootstrap account (`ROLE_USER,ROLE_ADMIN`) guarantees an admin exists.
- Left **empty** in `test` and `production` (user decision). Nobody holds `ROLE_ADMIN` or `ROLE_AUDITOR` there yet, and the User bootstrap is forbidden in those profiles. The gate is switched on there by a later property change, after roles have been assigned by hand.
- Exposed as an overridable seam in the Session configuration, consistent with the 2026-10-14 extensibility pattern (`@Bean` seam taking only injectable types).

**Where the check sits** in `SessionCommandService`:

- `login` pipeline: a new step between `dispatch` and `createSession`, applied to `context.getValidatedAuthorities()`.
- `loginWithOauth2` pipeline: a new step between `resolveUser` and `createOauth2Session`, applied to `context.getAuthorities()`.
- On refusal the reply is identical to a bad password: `success=false`, `failureMessage="invalid credentials"`, HTTP 200 (Session's existing failure convention). No session is created and no token minted. The web `LoginView` already shows `failureMessage` verbatim, so no UI change is needed for the message.

**`ACCESS_TOKEN` login fix.** `dispatchAccessToken` currently sets only the validated user id, so sessions created this way carry no authorities and would always be refused by the gate. It must also copy the existing session's authorities onto the context. This is a required part of this spec.

**`refresh`** is not gated (it re-mints from the session's stored authorities, which passed the gate at login).

**OAuth2 just-in-time provisioning is removed** (user decision). Today `resolveUser` creates a User record (`externalIdentity=provider:sub`, `userName=sub`, default `ROLE_USER`) for a first-time Google sign-in. After this change, if no User has that external identity, `loginWithOauth2` fails with the same generic `"invalid credentials"` reply and creates nothing. This applies in every profile, whether or not the gate is configured. People sign in with Google only after an admin has created their User with the matching `externalIdentity`. The race-handling re-query that exists only for provisioning goes away with it, and `LoginWithOauth2Test` cases that assert provisioning change accordingly.

### 4.2 Gateway: role rules

Declared by overriding `configureAuthorization` in the deployed `HostConfiguration` (Gateway convention: the application declares its rules; `GraniteGatewayServerConfiguration`'s default stays "authentication only"). Public paths (`getPublicPathPatterns`) are unchanged and still permitted before these rules.

**Switched on with the login gate** *(my call)*. The rules apply only when `granite.gateway.authorization.role-rules.enabled=true`, set in the same profiles as the Session gate (`local`, `development`) and left `false` in `test` and `production`. Otherwise, in test and production, users could log in (no Session gate) but every request would be refused with 403 because nobody holds either role. When disabled, behaviour is today's `anyExchange().authenticated()`. Both switches are turned on together once roles exist in an environment.

| Path | Method | Allowed authorities |
|---|---|---|
| `/frontendcontroller-user-service/get-output`, `/frontendcontroller-admin-service/get-output` | POST | ADMIN, AUDITOR |
| `/frontendcontroller-user-service/handle-input`, `/frontendcontroller-admin-service/handle-input` | POST | ADMIN |
| `/user-query-service/**` | any | ADMIN, AUDITOR |
| `/user-command-service/**` | any | ADMIN |
| `/session-command-service/logout` | POST | ADMIN, AUDITOR |
| `/session-command-service/logout-all-for-user` | POST | ADMIN |
| `/session-query-service/**` | any | ADMIN, AUDITOR |
| everything else (incl. `/outbox-service/**`, other FrontEndController paths, actuator) | any | ADMIN |

Every row also allows `SCOPE_internal`, so service tokens keep the access they have today (no current caller sends one through the Gateway; this is defensive).

- "Everything else → ADMIN" replaces `anyExchange().authenticated()` as the effective default for authenticated traffic: unknown or future paths are admin-only until a rule says otherwise.
- Missing/invalid token → 401 (unchanged). Valid token without a permitted authority → 403.
- `logout-all-for-user` destroys another user's sessions, so it is a write and admin-only *(my call)*. Auditors can still log themselves out via `logout`.
- Known limitation: every `handle-input` is treated as a write. A future read-only interaction sent through `handle-input` would need its own rule.

**Testability.** `HostConfiguration` lives in an Application project with no test suite, while Gateway tests live in `Granite.Gateway.ServerTest`. The rules must be exercised by tests in `Granite.Gateway.ServerTest` (in the style of `GatewayAuthorizationTest`); how the rule set is shared between the host and the tests is left to the builder (see §6).

### 4.3 Web: role-aware UI

- Authorities are already available as `useAuthentication().state.authorities` (populated from `sessionData.authorities` at login/refresh).
- Add a small helper in `Granite.Web.Authentication` (e.g. `hasAuthority(state, "ROLE_ADMIN")` / `canEdit(state)`) so views don't hard-code role logic.
- `UsersView` passes `showCreate/showEdit/showSave/showUndo/showDelete` (and `showApprove/showCancel`) to `UsersEditableView` as `true` only for admins. Keyboard shortcuts for those actions must follow the same flags (verify `UsersEditableView` gates shortcuts on the `show*` props; fix it if not).
- Auditors still see every menu tab and every route.
- If any admin-only request is refused with 403 anyway, the view shows an error rather than failing silently (builder chooses the presentation).
  - Deferred to the Users-screen project: `UsersView` has no real write path until then, so there is no admin-only request to surface an error for.

## 5. Constraints

- Follow existing style per the component CLAUDE.md files (Allman braces, return type on its own line, fluent setters, file header/footer comment lines). No explanatory comments unless they already match the surrounding density.
- Build and test only with the project's existing Gradle tasks and Yarn scripts; don't change build tooling; don't start/stop daemons.
- Update `Granite.Session/CLAUDE.md` (new property and gate; note it is the one authorization-style decision outside the Gateway, made at login), `Granite.Gateway/CLAUDE.md` (Granite's rule table) and the web Authentication docs if any.

## 6. Left to the builder

- How the Gateway rule set is shared between `HostConfiguration` and `Granite.Gateway.ServerTest` (e.g. a protected helper in `GraniteGatewayServerConfiguration`, or a reusable configuration class the host extends), as long as the tests exercise the same rules the host runs.
- Exact name and shape of the Session seam and of the web role helper.
- Test cases, provided they cover: admin allowed/denied matrix, auditor allowed/denied matrix, rules disabled = authentication only; user-without-role refused at password login, OAuth2 login, and `ACCESS_TOKEN` login; empty property = no gate; unknown external identity refused with nothing created; 401 vs 403.

## 7. Rollout

1. Ship with gate and Gateway role rules on in `local`/`development` only.
2. In `test`, then `production`: assign `ROLE_ADMIN` (and any `ROLE_AUDITOR`) to the right users by hand, then set `granite.session.login.required-authorities=ROLE_ADMIN,ROLE_AUDITOR` and `granite.gateway.authorization.role-rules.enabled=true` and redeploy Session CommandHost and Gateway. How roles are assigned there is outside this spec.
   - Switching the gate on does not evict live sessions: `refresh` is not re-gated and is a public Gateway path, so sessions minted before the switch keep refreshing until `granite.session.max-lifetime` (24h default) unless ended with `logout-all-for-user`.
3. Removing Google JIT provisioning takes effect everywhere at once: in test and production, Google users who don't already have a User record can no longer sign in.
