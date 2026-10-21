# Granite Login Roles Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Only `ROLE_ADMIN` and `ROLE_AUDITOR` users can log in; the Gateway gives admins full access and auditors read-only access; the web app hides editing from auditors.

**Architecture:** Session gains a configurable login gate (`ILoginGate`) applied in the password and OAuth2 login pipelines, and stops provisioning users on first Google sign-in. The Gateway gains a role-rule configuration class, switched on by property, that the deployed host extends and the tests exercise directly. The web app reads `state.authorities` through a small helper to drive the grid's `show*` flags.

**Tech Stack:** Java 23, Spring Boot 3.4 / Spring Security (reactive, Spring Cloud Gateway), JUnit 5 + Mockito, React 19 + TypeScript + vitest.

**Spec:** `docs/superpowers/specs/2026-10-21-granite-login-roles-design.md`

## Global Constraints

- Session property: `granite.session.login.required-authorities` (comma-separated; user needs at least one; empty/absent = no gate).
- Gateway property: `granite.gateway.authorization.role-rules.enabled` (boolean, default `false`; `false` = today's authentication-only behaviour).
- Both properties are set (`ROLE_ADMIN,ROLE_AUDITOR` / `true`) only in the `local` and `development` host profiles; left unset in `test` and `production`. Do not touch Session's `src/test/resources/application-local.properties` (smoke-test shadow file).
- Every refused login replies `success=false`, `failureMessage="invalid credentials"`, HTTP 200. Nothing about roles or account existence is revealed.
- Java style per component CLAUDE.md: Allman braces, return type on its own line, fluent setters, `////` file header/footer. No static inner classes. No explanatory comments.
- Build and test only with existing Gradle tasks and Yarn scripts. Don't modify build tooling or `package.json` scripts/devDependencies. Don't start/stop daemons. Never run `jib` or `publish*` tasks.
- Java tests run from the microservice directory, e.g. `cd Microservices/Granite.Session && ./gradlew :Tests:Granite.Session.ServerTest:test --tests "<class>"`. If that task path doesn't resolve, use the path printed by `./gradlew projects` from the same directory.

## Review Focus

1. **Password login for a user whose `authorities` is null** (legacy rows) → refused, not a NullPointerException. Test: `LoginTest.loginWithNullAuthoritiesIsRefused` (Task 2).
2. **Property set but blank (`granite.session.login.required-authorities=`)** → no gate, exactly like absent. Test: `RequiredAuthoritiesLoginGateTest.blankEntriesAreIgnored` (Task 1).
3. **Authority names differing only by case (`role_admin`)** → refused; matching is exact. Test: `RequiredAuthoritiesLoginGateTest.matchingIsCaseSensitive` (Task 1).
4. **Auditor calling `handle-input` on the admin FrontEndController** (not only the user one) → 403. Test: in `GatewayRoleRulesTest.auditorMatrix` (Task 4).
5. **A path not in the rule table (e.g. the Gateway actuator) with an auditor token** → 403, admin → not 401/403. Test: `GatewayRoleRulesTest.unlistedPathsAreAdminOnly` (Task 4).

---

### Task 1: Session login gate type

**Files:**
- Create: `Microservices/Granite.Session/Components/Granite.Session.Server/src/main/java/granite/session/server/application/ILoginGate.java`
- Create: `Microservices/Granite.Session/Components/Granite.Session.Server/src/main/java/granite/session/server/application/RequiredAuthoritiesLoginGate.java`
- Test: `Microservices/Granite.Session/Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/RequiredAuthoritiesLoginGateTest.java`

**Interfaces:**
- Produces: `interface ILoginGate { boolean permits(Collection<String> authorities); }`; `RequiredAuthoritiesLoginGate(Collection<String> requiredAuthorities)` implements it. Null `authorities` argument is treated as empty. Blank/null entries in `requiredAuthorities` are dropped; if none remain, `permits` always returns `true`.

- [ ] **Step 1: Write failing tests** (`@Tag("CommitStage")`, matching neighbouring tests)

```java
@Test public void emptyRequirementPermitsEveryone()
{ assertTrue(new RequiredAuthoritiesLoginGate(List.of()).permits(Set.of())); }

@Test public void blankEntriesAreIgnored()
{ assertTrue(new RequiredAuthoritiesLoginGate(List.of("", " ")).permits(Set.of("ROLE_USER"))); }

@Test public void anyListedAuthorityPermits()
{
    ILoginGate gate = new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN","ROLE_AUDITOR"));
    assertTrue(gate.permits(Set.of("ROLE_USER","ROLE_AUDITOR")));
    assertTrue(gate.permits(Set.of("ROLE_ADMIN")));
}

@Test public void missingAuthorityIsRefused()
{ assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN","ROLE_AUDITOR")).permits(Set.of("ROLE_USER"))); }

@Test public void nullAuthoritiesAreRefused()
{ assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN")).permits(null)); }

@Test public void matchingIsCaseSensitive()
{ assertFalse(new RequiredAuthoritiesLoginGate(List.of("ROLE_ADMIN")).permits(Set.of("role_admin"))); }
```

- [ ] **Step 2: Run, expect compile failure** — `./gradlew :Tests:Granite.Session.ServerTest:test --tests "*RequiredAuthoritiesLoginGateTest"`
- [ ] **Step 3: Implement both types** (store required set after trimming and dropping blanks).
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** — `feat(session): add ILoginGate and RequiredAuthoritiesLoginGate`

---

### Task 2: Apply the gate to password and OAuth2 login; fix ACCESS_TOKEN authorities

**Files:**
- Modify: `.../granite/session/server/application/SessionCommandService.java` (constructor; `login` pipeline ~L110; `loginWithOauth2` pipeline ~L128; `dispatchAccessToken` ~L555)
- Modify: `.../granite/session/server/application/ApplicationConfiguration.java` (`sessionCommandService` bean gains an `ILoginGate loginGate` parameter)
- Modify: `.../granite/session/server/configuration/GraniteSessionCommandServerConfiguration.java` (new seam bean)
- Modify: `Microservices/Granite.Session/Applications/Granite.Session.CommandHost/src/main/resources/application-local.properties`, `application-development.properties`
- Modify tests that construct `SessionCommandService`: `LoginTest`, `LoginWithOauth2Test`, `LogoutTest`, `LogoutAllForUserTest`, `RefreshTest` (pass `new RequiredAuthoritiesLoginGate(List.of())` except where a test sets its own gate)
- Test: `LoginTest.java`, `LoginWithOauth2Test.java`, `configuration/GraniteSessionCommandServerConfigurationTest.java`
- Docs: `Microservices/Granite.Session/CLAUDE.md`

**Interfaces:**
- Consumes: `ILoginGate`, `RequiredAuthoritiesLoginGate` (Task 1).
- Produces: `SessionCommandService(..., IOauth2IdTokenValidator oauth2Validator, ILoginGate loginGate, ISessionEventSender sender, ...)` — `loginGate` inserted directly after `oauth2Validator`. Seam: `@Bean @Scope("singleton") public ILoginGate loginGate(Environment environment)` in `GraniteSessionCommandServerConfiguration`, binding `granite.session.login.required-authorities` with `Binder...bind(..., Bindable.listOf(String.class)).orElse(List.of())` (same style as `serviceTokenProvider`).

- [ ] **Step 1: Write failing tests**

`LoginTest` (gate = `ROLE_ADMIN,ROLE_AUDITOR` in a new helper or per-test subject):
- `loginWithoutPermittedAuthorityIsRefused` — authenticateUser valid with authorities `{ROLE_USER}` → `!reply.isSuccess()`, `"invalid credentials".equals(reply.getFailureMessage())`, `repository.save` never called, `jwtTokenService.mint` never called.
- `loginWithAuditorAuthoritySucceeds` — authorities `{ROLE_USER, ROLE_AUDITOR}` → success.
- `loginWithNullAuthoritiesIsRefused` — authorities `null` → refused as above.
- `loginWithAccessTokenCarriesExistingSessionAuthorities` — existing session with authorities `{ROLE_ADMIN}`; new session saved with `{ROLE_ADMIN}` (capture the saved `Session`).
- `loginWithAccessTokenWithoutPermittedAuthorityIsRefused` — existing session authorities `{ROLE_USER}` → refused.

`LoginWithOauth2Test` (gate = `ROLE_ADMIN,ROLE_AUDITOR`):
- `loginForKnownUserWithoutPermittedAuthorityIsRefused` — known user `{ROLE_USER}` → `"invalid credentials"`, no session saved.
- `loginForKnownAdminSucceeds` — known user `{ROLE_ADMIN}` → success.

`GraniteSessionCommandServerConfigurationTest`:
- `loginGateReadsRequiredAuthorities` — property `ROLE_ADMIN,ROLE_AUDITOR` → bean refuses `{ROLE_USER}`, permits `{ROLE_AUDITOR}`.
- `loginGateAbsentPermitsEveryone` — property unset → bean permits `{ROLE_USER}`.

- [ ] **Step 2: Run, expect failures** — `./gradlew :Tests:Granite.Session.ServerTest:test --tests "*LoginTest" --tests "*LoginWithOauth2Test" --tests "*GraniteSessionCommandServerConfigurationTest"`
- [ ] **Step 3: Implement**
  - `login` pipeline: add `.thenApply(this::applyLoginGate)` between `dispatch` and `createSession`. `protected LoginContext applyLoginGate(LoginContext context)`: when `validatedUserId != null` and `!loginGate.permits(validatedAuthorities)`, clear `validatedUserId` so the existing `buildReply` returns `"invalid credentials"`.
  - `loginWithOauth2` pipeline: add `.thenApply(this::applyOauth2LoginGate)` between `resolveUser` and `createOauth2Session`. `protected LoginWithOauth2Context applyOauth2LoginGate(LoginWithOauth2Context context)`: when `userId != null`, no failure reason yet, and `!loginGate.permits(authorities)`, set `failureReason("invalid credentials")`.
  - `dispatchAccessToken`: also `.setValidatedAuthorities(existing.getAuthorities())`.
  - Seam bean, `ApplicationConfiguration` wiring, and the five test constructors.
  - Add `granite.session.login.required-authorities=ROLE_ADMIN,ROLE_AUDITOR` to the CommandHost `application-local.properties` and `application-development.properties` only.
  - `CLAUDE.md`: document the property, the seam, and that the login gate is the one authorization-style decision outside the Gateway, made at login only (refresh is not re-gated).
- [ ] **Step 4: Run the whole ServerTest suite, expect PASS** — `./gradlew :Tests:Granite.Session.ServerTest:test`
- [ ] **Step 5: Commit** — `feat(session): gate login on required authorities; carry authorities on ACCESS_TOKEN login`

---

### Task 3: Remove OAuth2 just-in-time provisioning

**Files:**
- Modify: `.../granite/session/server/application/SessionCommandService.java` (`resolveUser` ~L394-465)
- Test: `LoginWithOauth2Test.java`
- Docs: `Microservices/Granite.Session/CLAUDE.md` (remove any description of provisioning; state unknown identities are refused)

**Interfaces:**
- Consumes: Task 2's `SessionCommandService` constructor.
- Produces: `resolveUser` sets `failureReason("invalid credentials")` when `findUserByExternalIdentity` returns null; it never calls `userCommandServiceClient.createUser`.

- [ ] **Step 1: Replace tests.** Delete `loginWithValidIdTokenForUnknownUserProvisionsThenMints`, `jitProvisioningRaceResolvesToSameUser`, `jitProvisioningRaceWithRecoveryFailurePropagatesOriginalException`, `jitProvisioningRaceSoftFailureResolvesToSameUser` (and helpers used only by them, e.g. `stubCreateUserReturns`). Add:
  - `loginForUnknownIdentityIsRefusedWithoutProvisioning` — find returns null → `!isSuccess()`, `"invalid credentials"`, `createUser` never called, `repository.save` never called.
- [ ] **Step 2: Run, expect the new test to FAIL** — `--tests "*LoginWithOauth2Test"`
- [ ] **Step 3: Implement** — `resolveUser` keeps the existing-user branch; the not-found branch returns `context.setFailureReason("invalid credentials")`. Remove now-unused imports.
- [ ] **Step 4: Run the whole ServerTest suite, expect PASS.**
- [ ] **Step 5: Commit** — `feat(session): stop provisioning users on first OAuth2 sign-in`

---

### Task 4: Gateway role rules

**Files:**
- Create: `Microservices/Granite.Gateway/Components/Granite.Gateway.Server/src/main/java/granite/gateway/server/configuration/GraniteRoleRulesGatewayServerConfiguration.java`
- Modify: `Microservices/Granite.Gateway/Applications/Granite.Gateway.ServiceHost/src/main/java/granite/gateway/servicehost/main/HostConfiguration.java` (extend the new class)
- Modify: `Microservices/Granite.Gateway/Applications/Granite.Gateway.ServiceHost/src/main/resources/application-local.properties`, `application-development.properties`
- Modify: `Microservices/Granite.Gateway/Tests/Granite.Gateway.ServerTest/src/test/java/granite/gateway/server/configuration/GatewayTestBackend.java` (`registerProperties` also points `granite.frontendcontroller-user-service.host`, `granite.frontendcontroller-admin-service.host`, `granite.outbox-service.host` at the backend)
- Create test: `.../configuration/RoleRulesGatewayTestConfiguration.java` (same annotations as `AuthorizingGatewayTestConfiguration`, extends the new class, empty body)
- Create test: `.../configuration/GatewayRoleRulesTest.java` (`@SpringBootTest(classes = RoleRulesGatewayTestConfiguration.class, webEnvironment = RANDOM_PORT, properties = "granite.gateway.authorization.role-rules.enabled=true")`)
- Create test: `.../configuration/GatewayRoleRulesDisabledTest.java` (same, property `false`)
- Docs: `Microservices/Granite.Gateway/CLAUDE.md` (Granite's rule table and the property)

**Interfaces:**
- Produces: `public class GraniteRoleRulesGatewayServerConfiguration extends GraniteGatewayServerConfiguration` overriding `protected AuthorizeExchangeSpec configureAuthorization(AuthorizeExchangeSpec exchanges, Environment environment)`. When the property is false it returns `super.configureAuthorization(...)` unchanged. Not annotated `@Configuration` (the host and test configs carry annotations).

Rules when enabled (each allowed list also includes `SCOPE_internal`), in this order:

| Matcher | Authorities |
|---|---|
| `POST /frontendcontroller-user-service/get-output`, `POST /frontendcontroller-admin-service/get-output` | ROLE_ADMIN, ROLE_AUDITOR |
| `/user-query-service/**` | ROLE_ADMIN, ROLE_AUDITOR |
| `POST /session-command-service/logout` | ROLE_ADMIN, ROLE_AUDITOR |
| `/session-query-service/**` | ROLE_ADMIN, ROLE_AUDITOR |
| `/**` (catch-all) | ROLE_ADMIN |

Use `pathMatchers("/**")` for the catch-all, not `anyExchange()`: the base chain registers `anyExchange().authenticated()` afterwards, and a second `anyExchange()` throws.

- [ ] **Step 1: Write failing tests** (`@Tag("CommitStage")`, reuse `GatewayTestBackend`; "allowed" means status is neither 401 nor 403)

`GatewayRoleRulesTest`:
- `adminMatrix` — `ROLE_ADMIN` allowed on: both `get-output`, both `handle-input`, `/user-query-service/find-users-by-key`, `/user-command-service/create-user`, `/session-command-service/logout`, `/session-command-service/logout-all-for-user`, `/session-query-service/find-sessions-by-key`, `/outbox-service/anything`.
- `auditorMatrix` — `ROLE_AUDITOR` allowed on both `get-output`, user-query, `logout`, session-query; **403** on both `handle-input`, `/user-command-service/create-user`, `logout-all-for-user`, `/outbox-service/anything`.
- `roleUserIsForbiddenEverywhere` — `ROLE_USER` → 403 on `get-output` and user-query.
- `serviceTokenKeepsAccess` — `backend.signServiceToken(List.of("SCOPE_internal"))` allowed on user-command and session-query.
- `unlistedPathsAreAdminOnly` — `GET /unlisted-service/anything` (no route; admin gets 404): auditor 403, admin allowed.
- `publicPathsStayOpen` — no token: `/session-command-service/login` and the health path (`/health/` under the test properties) not 401/403.
- `missingTokenIsUnauthorized` — no token on `get-output` → 401.

`GatewayRoleRulesDisabledTest`:
- `rulesDisabledRequiresOnlyAuthentication` — `ROLE_USER` allowed on `/user-command-service/create-user` and `handle-input`.

- [ ] **Step 2: Run, expect compile failure** — `cd Microservices/Granite.Gateway && ./gradlew :Tests:Granite.Gateway.ServerTest:test --tests "*GatewayRoleRules*"`
- [ ] **Step 3: Implement** the class, the `GatewayTestBackend` property additions, `HostConfiguration extends GraniteRoleRulesGatewayServerConfiguration`, and `granite.gateway.authorization.role-rules.enabled=true` in ServiceHost `application-local.properties` and `application-development.properties` only. Update `CLAUDE.md`.
- [ ] **Step 4: Run the whole Gateway ServerTest suite, expect PASS** (existing `GatewayAuthorizationTest` etc. must still pass) — `./gradlew :Tests:Granite.Gateway.ServerTest:test`
- [ ] **Step 5: Commit** — `feat(gateway): admin/auditor role rules behind granite.gateway.authorization.role-rules.enabled`

---

### Task 5: Web authority helper

**Files:**
- Create: `FrontEnds/Granite.Web/Components/Granite.Web.Authentication/src/main/typescript/Authentication/Authorities.ts`
- Modify: `.../Authentication/index.ts` (export the two functions)
- Test: `FrontEnds/Granite.Web/Components/Granite.Web.Authentication/src/test/typescript/Authorities.test.ts`

**Interfaces:**
- Produces: `export function hasAuthority(state: IAuthenticationState, authority: string): boolean` (false unless `state.status === "authenticated"` and `state.authorities` contains `authority`); `export function canEdit(state: IAuthenticationState): boolean` (= `hasAuthority(state, "ROLE_ADMIN")`).

- [ ] **Step 1: Write failing tests** — using `TestSupport.ts` helpers or literal states built from `ANONYMOUS`:
  - `canEdit` is true for authenticated `["ROLE_USER","ROLE_ADMIN"]`, false for `["ROLE_AUDITOR"]`, false for `ANONYMOUS` even if `authorities` contained `ROLE_ADMIN`.
  - `hasAuthority(state,"ROLE_AUDITOR")` true for authenticated `["ROLE_AUDITOR"]`.
- [ ] **Step 2: Run, expect FAIL** — `cd FrontEnds/Granite.Web/Components/Granite.Web.Authentication && yarn test`
- [ ] **Step 3: Implement** in the file style of `JwtClaims.ts`.
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** — `feat(web): add hasAuthority/canEdit helpers`

---

### Task 6: Read-only Users grid for auditors

**Files:**
- Modify: `FrontEnds/Granite.Web/Components/Granite.Web.Navigator/src/main/typescript/User/UsersView.tsx`
- Modify: `FrontEnds/Granite.Web/Components/Granite.Web.User/src/main/typescript/EditableView/UsersEditableView.tsx` (`handleMasterKeyDown` ~L163-190)

**Interfaces:**
- Consumes: `canEdit`, `useAuthentication` from `granite.web.authentication` (Task 5; Navigator already depends on that package).

- [ ] **Step 1: Implement**
  - `UsersView`: `const editable = canEdit(useAuthentication().state);` and pass `showCreate`, `showEdit`, `showSave`, `showUndo`, `showApprove`, `showCancel`, `showDelete` all `= editable`.
  - `UsersEditableView.handleMasterKeyDown`: each shortcut runs only if its matching flag is on (`(props.showX ?? true)`): Ctrl+N→showCreate, Ctrl+E→showEdit, Ctrl+S→showSave, Escape→showSave, Delete→showDelete, Ctrl+Z/Ctrl+Y→showUndo.
- [ ] **Step 2: Type-check** — run each component's existing `yarn build` (Granite.Web.User first, then Granite.Web.Navigator), expect no `tsc` errors. These components have no test suites and adding test tooling is out of scope.
- [ ] **Step 3: Manual verification in the local cluster** (after the user redeploys Session, Gateway and web): log in as the bootstrap admin → toolbar buttons and shortcuts work. Log in as a user with only `ROLE_AUDITOR` (grant it via the User service as admin) → grid visible, no toolbar buttons, Ctrl+N/Ctrl+E/Delete do nothing. Log in as a `ROLE_USER`-only user → login form shows "invalid credentials".
- [ ] **Step 4: Commit** — `feat(web): hide user editing actions from non-admins`

---

## Self-review notes

- Spec §4.1 → Tasks 1–3; §4.2 → Task 4; §4.3 → Tasks 5–6; §5 docs → Tasks 2, 3, 4; §7 rollout profiles → Global Constraints + Tasks 2, 4.
- Spec §4.3 "view shows an error on 403": `UsersView` still uses the fake client until the Users-screen project, so there is no real request to fail yet; deferred to that project.
