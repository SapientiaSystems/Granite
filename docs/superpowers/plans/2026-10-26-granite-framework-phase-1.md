# Granite Framework — Phase 1 (Backend Extension Points) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Granite's Gateway, FrontEndController, User and Session hosts extendable by applications (roles, hierarchy, default role, Okta modes, FEC inheritance, Gateway hooks) while Granite's own application keeps working.

**Architecture:** A shared `ApplicationRoles` catalog in Granite.Foundation.Core is returned by an overridable `applicationRoles()` `@Bean` on each service's entry configuration. User stores and validates expanded role sets; Session assigns default and Okta roles through User; FEC gains an abstract generic configuration/controller and a context object; Gateway gains application hooks between Granite's rules and the `ROLE_Admin` catch-all.

**Tech Stack:** Java 23, Spring Boot 3 (MVC for FEC/User/Session, WebFlux for Gateway), Strata framework, JUnit 5, TypeScript/Vitest (one small web task).

**Spec:** `docs/superpowers/specs/2026-10-26-granite-framework-extensibility-design.md`

## Global Constraints

- Role names are mixed case: `ROLE_Admin`, `ROLE_Auditor`, `ROLE_User`. Granite core owns only `ROLE_Admin`; `ROLE_Auditor`/`ROLE_User` belong to the Granite application catalog.
- `ROLE_Admin` implies no application role and is implied by none.
- Stored authority sets are always closed under the hierarchy (expanded set stored).
- Renames: `ROLE_ADMIN→ROLE_Admin`, `ROLE_AUDITOR→ROLE_Auditor`, `ROLE_USER→ROLE_User`.
- Session properties: `granite.session.okta.role-assignment=ON_CREATE|EVERY_LOGIN` (default `ON_CREATE`), `granite.session.okta.unmapped-groups=DEFAULT_ROLE|REFUSE` (default `DEFAULT_ROLE`); `granite.session.okta.authorities-source` is removed.
- Seams are Java overrides of `@Bean`/protected methods on entry configuration classes (Spring Boot forbids redefining imported bean names).
- Build/test only with existing Gradle tasks from the repo root (`./gradlew <path>:test --rerun`) and existing yarn scripts. No tool-config changes, no daemon start/stop, no `publish*`/jib.
- No hand-written DDL; data conversion is Java code.
- Never log tokens, handles, Okta codes, passwords, or authority values of other users in bulk (counts only).
- Style: `//////` file headers/footers, Granite/Strata Java formatting, no explanatory comments, no static inner classes.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`. Don't commit `.idea/**`, AppHost `dist/**` or `.gradle` lock files.

## Deviation from the spec

The spec has Session rename stored session authorities. Session re-reads the user's stored authorities on every JWT re-mint: `resolveSession` renewal and, since the final fix wave, `refresh` (`remintJwt` uses the user from `loadRefreshUser`; a missing user is rejected), so after Task 3 renames User records, sessions pick up the new names at their next renewal; no Session renamer is built. Until then an Admin holding a pre-rename JWT may see 403 on admin paths for at most one access-token lifetime.

## Review Focus

1. A user record holding a legacy or non-catalog value (e.g. `SCOPE_internal`, `ROLE_Legacy`) is edited in the Users screen → the save succeeds and the unknown value is preserved (Task 2 test `updateKeepsExistingUnknownAuthorities`).
2. An Okta user whose groups map to nothing, with `unmapped-groups=REFUSE` → login refused, no user created (Task 4 test `refuseModeRejectsUnmappedNewUser`).
3. `EVERY_LOGIN` with a User write failure → login refused rather than a session with stale roles (Task 4 test `everyLoginWriteFailureRefuses`).
4. An application Gateway rule that tries to permit `/session-command-service/resolve-session` → still denied (Task 7 test `applicationRulesCannotReopenDeniedPaths`).
5. A JWT minted before the rename (`ROLE_ADMIN`) → refreshed with renamed roles on the next renewal (Task 5 test `renewalPicksUpRenamedAuthorities`).

---

### Task 1: Role catalog in Granite.Foundation.Core

**Files:**
- Create: `Shared/Granite.Foundation/Components/Granite.Foundation.Core/src/main/java/granite/foundation/core/security/RoleDefinition.java`
- Create: `.../security/ApplicationRoles.java`
- Create: `.../security/GraniteApplicationRoles.java`
- Test: `Shared/Granite.Foundation/Tests/Granite.Foundation.CoreTest/src/test/java/granite/foundation/core/security/ApplicationRolesTest.java`

**Interfaces:**
- Produces:
  - `RoleDefinition(String name,String label,Set<String> impliedRoles)` with getters `getName()`, `getLabel()`, `getImpliedRoles()` (immutable).
  - `ApplicationRoles(List<RoleDefinition> applicationRoles,String defaultRole)`; constant `ApplicationRoles.ADMIN = "ROLE_Admin"`. Methods: `List<RoleDefinition> getRoles()` (Admin first, then application roles), `String getDefaultRole()`, `boolean isRole(String)`, `Set<String> expand(Set<String>)` (transitive closure; non-catalog values pass through unchanged), `Set<String> dependentsOf(String)` (roles that imply it, transitively).
  - `GraniteApplicationRoles.create()` → `ApplicationRoles` with `ROLE_Auditor` ("Auditor"), `ROLE_User` ("User"), no implications, default `ROLE_User`.
- Constructor rejects: unknown implied role, implication cycle, default role not in the application list, an application role named `ROLE_Admin`, a role implying `ROLE_Admin` (`IllegalArgumentException`).

- [ ] **Step 1: Write failing tests** in `ApplicationRolesTest`, using a Poplar-shaped catalog (`ROLE_PromotionsReviewer`; `ROLE_PromotionsPlanner` implies Reviewer; `ROLE_PromotionsApprover` implies Planner; default Reviewer):
  - `expandAddsTransitiveImplications`: `expand({ROLE_PromotionsApprover})` == `{ROLE_PromotionsApprover,ROLE_PromotionsPlanner,ROLE_PromotionsReviewer}`.
  - `expandKeepsUnknownValues`: `expand({SCOPE_internal})` == `{SCOPE_internal}`.
  - `adminIsAlwaysPresentAndSeparate`: `getRoles().get(0).getName()` == `ROLE_Admin`; `expand({ROLE_Admin})` == `{ROLE_Admin}`.
  - `dependentsOfReviewer` == `{ROLE_PromotionsPlanner,ROLE_PromotionsApprover}`.
  - `rejectsCycle`, `rejectsUnknownImplied`, `rejectsDefaultOutsideCatalog`, `rejectsImplyingAdmin`.
  - `graniteCatalogHasAuditorAndUser`: names `[ROLE_Admin,ROLE_Auditor,ROLE_User]`, default `ROLE_User`.
- [ ] **Step 2: Run** `./gradlew :Granite.Foundation:Tests:Granite.Foundation.CoreTest:test --rerun` → FAIL (classes missing).
- [ ] **Step 3: Implement** the three classes.
- [ ] **Step 4: Run** the same command → PASS.
- [ ] **Step 5: Commit** `feat(foundation): ApplicationRoles catalog with hierarchy`.

---

### Task 2: User service — catalog bean, validation and expansion

**Files:**
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/configuration/AbstractGraniteUserServerConfiguration.java` (add `@Bean @Scope("singleton") public ApplicationRoles applicationRoles() { return GraniteApplicationRoles.create(); }`)
- Modify: `.../server/application/ApplicationConfiguration.java` (`userCommandService` bean gains an `ApplicationRoles` parameter)
- Modify: `.../server/application/UserCommandService.java` (constructor + `mapToUser` + `applyUpdate`)
- Modify: `.../server/configuration/GraniteUserCommandServerConfiguration.java` (bootstrap authorities expanded via the catalog; default `List.of("ROLE_User")`)
- Modify: `Microservices/Granite.User/Applications/Granite.User.CommandHost/src/main/resources/application-{development,local}.properties` (`granite.user.bootstrap.authorities=ROLE_User,ROLE_Admin`)
- Test: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/application/UserCommandServiceRolesTest.java`; update existing tests that use `ROLE_ADMIN`/`ROLE_USER` literals.

**Interfaces:**
- Consumes: `ApplicationRoles` (Task 1).
- Produces: `UserCommandService(IUserRepository,IUserEventSender,ModelMapper,PasswordEncoder,LoginLockoutPolicy,ApplicationRoles)`. Rule: on create every authority must satisfy `isRole`; on update only authorities **added** relative to the stored set must satisfy `isRole`; the stored result is `expand(result)`. A violation fails the reply with failure message `unknown role: <name>` (create/update replies `success=false`), nothing saved.

- [ ] **Step 1: Write failing tests** (Granite catalog plus a test subclass catalog with Poplar roles):
  - `createStoresExpandedSet`: create with `{ROLE_PromotionsApprover}` → stored set of 3.
  - `createRejectsNonCatalogRole`: `{ROLE_Bogus}` → `success=false`, message `unknown role: ROLE_Bogus`, repository has no user.
  - `updateExpandsAddedRole`: stored `{ROLE_PromotionsReviewer}`, update to `{ROLE_PromotionsReviewer,ROLE_PromotionsApprover}` → stored 3.
  - `updateKeepsExistingUnknownAuthorities`: stored `{ROLE_Legacy}` (written directly via repository), update to `{ROLE_Legacy,ROLE_PromotionsReviewer}` → success, stored `{ROLE_Legacy,ROLE_PromotionsReviewer}`.
  - `updateRejectsAddedUnknown`: stored `{}`, update to `{SCOPE_internal}` → `success=false`.
  - `configurationExposesOverridableCatalog`: `ExtendedUserCommandServerConfiguration` overriding `applicationRoles()` → the context's `ApplicationRoles` bean is the override.
- [ ] **Step 2: Run** `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test --rerun` → new tests FAIL.
- [ ] **Step 3: Implement** the changes above.
- [ ] **Step 4: Run** the same command → all PASS (existing tests updated to mixed-case names).
- [ ] **Step 5: Commit** `feat(user): validate and expand stored roles against ApplicationRoles`.

---

### Task 3: User service — legacy authority renamer

**Files:**
- Create: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/platform/AuthorityRenamer.java`
- Modify: `.../configuration/GraniteUserCommandServerConfiguration.java` (`@Bean` `authorityRenamer(...)` and protected `Map<String,String> legacyAuthorityRenames()` returning the three renames)
- Test: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/platform/AuthorityRenamerTest.java`

**Interfaces:**
- Consumes: `ApplicationRoles`, `IUserRepository.findAllUsers()`.
- Produces: `AuthorityRenamer(ObjectProvider<IUserRepository>,ObjectProvider<ISpringUnitOfWorkManager>,Map<String,String> renames,ApplicationRoles roles)` implementing `ApplicationListener<ContextRefreshedEvent>` with `public int rename()` returning the count of changed users. Same request-attribute and `TransactionTemplate` handling as `UserBootstrapper`. Each user: replace renamed values, then `roles.expand`; save only if changed. Logs `"renamed authorities for {} users"` (count only).

- [ ] **Step 1: Write failing tests:**
  - `renamesLegacyValues`: user `{ROLE_ADMIN,ROLE_USER}` → `{ROLE_Admin,ROLE_User}`, `rename()` == 1.
  - `isIdempotent`: second `rename()` == 0.
  - `expandsClosure`: Poplar catalog, user `{ROLE_PromotionsApprover}` → 3 roles.
  - `leavesUnknownValues`: user `{ROLE_Legacy}` unchanged, count 0.
- [ ] **Step 2: Run** `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test --rerun` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command, then `./gradlew :Granite.User:Applications:Granite.User.CommandHost:test --rerun` → PASS.
- [ ] **Step 5: Commit** `feat(user): rename legacy role authorities at startup`.

---

### Task 4: Session — catalog bean and Okta role assignment modes

**Files:**
- Modify: `Microservices/Granite.Session/Components/Granite.Session.Server/src/main/java/granite/session/server/configuration/AbstractGraniteSessionServerConfiguration.java` (`applicationRoles()` bean as in Task 2)
- Create: `.../server/application/OktaRoleAssignment.java` (enum `ON_CREATE`, `EVERY_LOGIN`), `.../application/OktaUnmappedGroups.java` (enum `DEFAULT_ROLE`, `REFUSE`), `.../application/OktaRoles.java`
- Delete: `.../application/OktaAuthorities.java`, `.../application/OktaAuthoritiesSource.java` (`git rm`)
- Modify: `.../application/OktaSettings.java` (replace `authoritiesSource` with `roleAssignment`, `unmappedGroups`), `.../configuration/GraniteSessionCommandServerConfiguration.java` (bind the two new properties), `.../application/ApplicationConfiguration.java` + `SessionCommandService.java` (constructor gains `ApplicationRoles`; `OKTA_ROLE` removed; `resolveOktaUser`, `createOktaUser`, `authoritiesFor`)
- Test: `Microservices/Granite.Session/Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/OktaRolesTest.java`, `SessionCommandServiceOktaRolesTest.java`; update existing Okta tests that use `authorities-source`.

**Interfaces:**
- Consumes: `ApplicationRoles`; existing `IUserCommandServiceClient.createUser`/`updateUser`.
- Produces: `OktaRoles(OktaSettings,ApplicationRoles)` with `Optional<Set<String>> rolesFor(Set<String> groups)`: mapped catalog roles expanded; if none map → `Optional.of(expand({defaultRole}))` for `DEFAULT_ROLE`, `Optional.empty()` for `REFUSE`. Group mapping values not in the catalog are ignored.
- Behaviour in `resolveOktaUser`:
  - New user: `rolesFor` empty → reject `"user unavailable"` without creating; else create with those roles.
  - Existing user, `ON_CREATE`: authorities = stored roles.
  - Existing user, `EVERY_LOGIN`: `rolesFor` empty → reject; else if different from stored, `updateUser` with them; failed reply → reject `"user unavailable"`; authorities = written roles.
- `authoritiesFor(session,user)` returns the user's stored authorities for every sign-in method.

- [ ] **Step 1: Write failing tests:**
  - `OktaRolesTest`: `mappedGroupsExpand` (group `GC-Approvers`→`ROLE_PromotionsApprover` gives 3 roles); `unmappedFallsBackToDefault`; `refuseModeReturnsEmpty`; `nonCatalogMappingIgnored`.
  - `SessionCommandServiceOktaRolesTest` (fake User clients as in existing Okta tests): `onCreateStoresGroupRolesForNewUser`; `onCreateIgnoresGroupsForExistingUser` (stored roles win); `everyLoginWritesBackChangedRoles` (updateUser called once with expanded roles, session authorities equal them); `everyLoginSkipsWriteWhenUnchanged`; `everyLoginWriteFailureRefuses`; `refuseModeRejectsUnmappedNewUser` (no createUser call).
- [ ] **Step 2: Run** `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command → PASS.
- [ ] **Step 5: Commit** `feat(session): Okta ON_CREATE/EVERY_LOGIN role assignment with catalog fallback`.

---

### Task 5: Session — default role on first password login, login gate default, renames

**Files:**
- Modify: `.../session/server/application/SessionCommandService.java` (new step `assignDefaultRole` between `dispatch` and `applyLoginGate` in `login`)
- Create: `.../application/CatalogRoleLoginGate.java`
- Modify: `.../configuration/GraniteSessionCommandServerConfiguration.java` (`loginGate(Environment,ApplicationRoles)`: property list non-empty → `RequiredAuthoritiesLoginGate`, else `CatalogRoleLoginGate`)
- Modify: `Microservices/Granite.Session/Applications/Granite.Session.CommandHost/src/main/resources/application-{development,local}.properties` (`granite.session.login.required-authorities=ROLE_Admin,ROLE_Auditor`)
- Test: `.../session/server/application/SessionCommandServiceDefaultRoleTest.java`, `CatalogRoleLoginGateTest.java`; update tests using upper-case role literals.

**Interfaces:**
- Consumes: `ApplicationRoles`, `IUserCommandServiceClient.updateUser`.
- Produces: `CatalogRoleLoginGate(ApplicationRoles)` implementing `ILoginGate` — permits when any authority `isRole`. `assignDefaultRole(LoginContext)`: only for a validated password login whose validated authorities are empty → `updateUser` with `expand({defaultRole})`; success → validated authorities = those roles; failure → context rejected (`"user unavailable"` path of `buildReply`). Access-token logins are untouched.

- [ ] **Step 1: Write failing tests:**
  - `firstPasswordLoginGetsDefaultRole` (Poplar catalog → updateUser called with `{ROLE_PromotionsReviewer}`, reply success, JWT authorities contain it).
  - `existingRolesUntouched` (no updateUser call).
  - `defaultRoleWriteFailureRefuses`.
  - `invalidCredentialsNeverAssign` (no updateUser call).
  - `renewalPicksUpRenamedAuthorities`: session row with `{ROLE_ADMIN}`, user now `{ROLE_Admin}`, renewal → session authorities `{ROLE_Admin}`.
  - `CatalogRoleLoginGateTest`: permits `{ROLE_User}`, refuses `{}` and `{SCOPE_internal}`.
- [ ] **Step 2: Run** `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command, then `./gradlew :Granite.Session:Applications:Granite.Session.CommandHost:test :Granite.Session:Applications:Granite.Session.QueryHost:test --rerun` → PASS.
- [ ] **Step 5: Commit** `feat(session): application default role on first password login; catalog login gate`.

---

### Task 6: FrontEndController — abstract configuration, context, generic controller, role catalog output

**Files:**
- Modify: `Microservices/Granite.FrontEndController/Components/Granite.FrontEndController.Server/build.gradle` (add `api "granite.foundation:granite-foundation-core:$system_version"` exactly as in Granite.User.Server; the root composite build substitutes it, no settings change).
- Create: `.../frontendcontroller/server/application/GraniteFrontEndControllerContext.java`
- Create: `.../server/configuration/AbstractGraniteFrontEndControllerConfiguration.java`
- Modify: `.../server/configuration/GraniteFrontEndControllerConfiguration.java` (extends the abstract base), `GraniteFrontEndControllerServiceFactory.java` (builds the context), `.../application/GraniteFrontEndControllerService.java` (constructor `(GraniteFrontEndControllerContext)`), `.../platform/GraniteFrontEndControllerServiceController.java` (generic)
- Create (service contract): `Components/Granite.FrontEndController.Service/src/main/java/granite/frontendcontroller/service/requestreply/RoleData.java` + `src/main/typescript/RequestReply/RoleData.ts`; add `roles` to `UsersOutput.java`/`UsersOutput.ts`, export in `index.ts`.
- Modify: `.../control/user/InitializeUsersView.java` (fills `roles`)
- Test: `Microservices/Granite.FrontEndController/Tests/Granite.FrontEndController.ServerTest/src/test/java/granite/frontendcontroller/server/configuration/ExtendedFrontEndControllerConfigurationTest.java` (+ test-only `ExtendedFrontEndControllerService`, `IExtendedFrontEndControllerService`, `ExtendedFrontEndControllerServiceController` in the same test package); update `GraniteControllerMappingTest`, `GraniteFrontEndControllerConfigurationTest`, Users/Session service tests for the new constructor.

**Interfaces:**
- Consumes: `ApplicationRoles` (Task 1).
- Produces:
  - `GraniteFrontEndControllerContext` — immutable, getters for `ModelMapper`, `Executor`, `IUserQueryServiceClient`, `IUserCommandServiceClient`, `ISessionCommandServiceClient`, `ISessionQueryServiceClient`, `SessionRequestContext`, `SessionLoginSettings`, `ApplicationRoles`; built with a fluent builder or all-args constructor (builder's choice).
  - `GraniteFrontEndControllerService(GraniteFrontEndControllerContext)`; protected `getContext()`.
  - `GraniteFrontEndControllerServiceController<S extends IGraniteFrontEndControllerService>` extends `AbstractFrontEndControllerServiceController<S>`; Granite's 4 `@PostMapping`s stay on it. It carries no `@RestController`/`@RequestMapping`; Granite's binding is a new `@RestController @CrossOrigin(origins="*") @RequestMapping("${granite.frontendcontroller.service-path}")` class `GraniteApplicationFrontEndControllerServiceController extends GraniteFrontEndControllerServiceController<IGraniteFrontEndControllerService>`.
  - `AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>`: `@Import` ApplicationConfiguration + health; beans `frontEndControllerExecutor()`, `applicationRoles()` (Granite catalog, overridable), request-scoped `@Named("Granite") S frontEndControllerService(...)` = `createService(createContext(mapper,executor,roles))`, request-scoped controller = `createController(service)`, `responseCookieAdvice()`. Protected: `GraniteFrontEndControllerContext createContext(ModelMapper,Executor,ApplicationRoles)`, abstract `S createService(GraniteFrontEndControllerContext)`, abstract `Object createController(S)`, plus protected client factories `createUserQueryClient()`, `createUserCommandClient()`, `createSessionCommandClient()`, `createSessionQueryClient()`, and `createClientConfiguration(String baseUrlProperty)` for application clients.
  - `RoleData {String name; String label; List<String> impliedRoles}`; `UsersOutput.roles: List<RoleData>` (TS `roles: Array<RoleData>`).

- [ ] **Step 1: Write failing tests:**
  - `ExtendedFrontEndControllerConfigurationTest.extendedServiceInheritsGraniteOperations`: a test configuration extending the abstract base with `S = IExtendedFrontEndControllerService` (adds `getEchoOutput`) → MockMvc/mapping fixture finds `/get-session-output`, `/handle-session-input`, `/get-users-output`, `/handle-users-input` **and** `/get-echo-output`.
  - `extendedConfigurationOverridesCatalog`: override `applicationRoles()` → `getUsersOutput` reply's `roles` lists `ROLE_Admin` then the override's roles.
  - `graniteUsersOutputListsGraniteCatalog`: names `[ROLE_Admin,ROLE_Auditor,ROLE_User]`.
  - `FrontEndJsonSerializationTest.usersOutputSerializesRoles`.
- [ ] **Step 2: Run** `./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test --rerun` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command, then `./gradlew :Granite.FrontEndController:Applications:Granite.FrontEndController.UserHost:compileJava :Granite.FrontEndController:Applications:Granite.FrontEndController.AdminHost:compileJava`, then in `Microservices/Granite.FrontEndController` `yarn run build-granite-frontendcontroller-service` → all PASS (registry must point at Verdaccio; if it doesn't, stop and ask the user).
- [ ] **Step 5: Commit** `feat(frontendcontroller): extendable configuration, context and generic controller; role catalog in UsersOutput`.

---

### Task 7: Gateway — core rules in base, application hooks, Granite application rules

**Files:**
- Modify: `Microservices/Granite.Gateway/Components/Granite.Gateway.Server/src/main/java/granite/gateway/server/configuration/GraniteGatewayServerConfiguration.java`
- Modify: `.../GraniteRoleRulesGatewayServerConfiguration.java` (becomes the Granite application rules: overrides `getUsersReaderAuthorities()` to add `ROLE_Auditor`)
- Test: `Microservices/Granite.Gateway/Tests/Granite.Gateway.ServerTest/src/test/java/granite/gateway/server/configuration/GatewayApplicationHooksTest.java` (+ `ApplicationHooksGatewayTestConfiguration.java`); update `GatewayRoleRulesTest`, `GatewayAuthorizationTest` and helpers for `ROLE_Admin`/`ROLE_Auditor`.

**Interfaces:**
- Produces on `GraniteGatewayServerConfiguration`:
  - `configureRoutes` calls `configureApplicationRoutes(Builder,Environment)` (default: returns routes) after Granite's routes, in every profile.
  - `configureAuthorization(AuthorizeExchangeSpec,Environment)` becomes the template: when `granite.gateway.authorization.role-rules.enabled` is true → Granite users rules (`POST /frontendcontroller-{user,admin}-service/get-users-output`, `/user-query-service/**`, `/session-query-service/**`, `POST /session-command-service/logout` → `getUsersReaderAuthorities()`), then `configureApplicationAuthorization(...)`, then `/**` → `ROLE_Admin`,`SCOPE_internal`. When false → `configureApplicationAuthorization(...)` only (then the existing `anyExchange().authenticated()`).
  - `protected List<String> getUsersReaderAuthorities()` default `[ROLE_Admin,SCOPE_internal]`.
  - `protected AuthorizeExchangeSpec configureApplicationAuthorization(AuthorizeExchangeSpec,Environment)` default returns its argument.
  - The denyAll/public/current-session matchers stay ahead of everything (unchanged).
- `GraniteRoleRulesGatewayServerConfiguration.getUsersReaderAuthorities()` → `[ROLE_Admin,ROLE_Auditor,SCOPE_internal]`; it no longer overrides `configureAuthorization`.

- [ ] **Step 1: Write failing tests** (`role-rules.enabled=true`, application test config adds route `/promotion-query-service/**` and rule `POST /frontendcontroller-user-service/get-promotions-output` → `ROLE_PromotionsReviewer`):
  - `applicationRuleGrantsApplicationRole`: `ROLE_PromotionsReviewer` JWT → not 403 on that path.
  - `applicationRoleDeniedElsewhere`: same JWT → 403 on `/frontendcontroller-user-service/handle-users-input`.
  - `adminCatchAllStillApplies`: `ROLE_Admin` → allowed on unmatched path; `ROLE_PromotionsReviewer` → 403.
  - `applicationRulesCannotReopenDeniedPaths`: app rule permitAll on `/session-command-service/resolve-session` → still 403.
  - `applicationRouteIsRegistered`: route locator contains the application route id.
  - Updated `GatewayRoleRulesTest`: `ROLE_Auditor` reads users; base (non-Granite) config with `ROLE_Auditor` → 403.
- [ ] **Step 2: Run** `./gradlew :Granite.Gateway:Tests:Granite.Gateway.ServerTest:test --rerun` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command and `./gradlew :Granite.Gateway:Applications:Granite.Gateway.ServiceHost:compileJava` → PASS.
- [ ] **Step 5: Commit** `feat(gateway): application route and authorization hooks; Auditor rules move to Granite application`.

---

### Task 8: Web — `ROLE_Admin` rename

**Files:**
- Modify: `FrontEnds/Granite.Web/Components/Granite.Web.Authentication/src/main/typescript/Authentication/Authorities.ts` (`"ROLE_Admin"`; add exported `hasRole(state,role: string): boolean` if not already generic)
- Modify: any Web tests/fixtures using `ROLE_ADMIN`/`ROLE_USER`/`ROLE_AUDITOR` (grep `FrontEnds/Granite.Web/Components`).
- Modify: `Microservices/Granite.FrontEndController/Components/Granite.FrontEndController.Client` only if it references the literals.

- [ ] **Step 1: Update tests** to mixed case and add `isAdminRecognisesMixedCase` (`["ROLE_Admin"]` → true; `["ROLE_ADMIN"]` → false).
- [ ] **Step 2: Run** in `FrontEnds/Granite.Web`: `yarn run test-granite-web-authentication` → FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `yarn run test-granite-web-authentication`, then from the repo root `yarn run build-granite-web` → PASS.
- [ ] **Step 5: Commit** `feat(web): mixed-case ROLE_Admin`.

---

### Task 9: Documentation and full verification

**Files:**
- Modify: `Microservices/Granite.FrontEndController/CLAUDE.md` and `.github/copilot-instructions.md` (identical), `Microservices/Granite.Gateway/CLAUDE.md`, `Microservices/Granite.User/CLAUDE.md`, `Microservices/Granite.Session/CLAUDE.md`, `FrontEnds/Granite.Web/CLAUDE.md`, root `CLAUDE.md` — extension points, role catalog, renames, Okta properties (only what changed).

- [ ] **Step 1: Update docs.**
- [ ] **Step 2: Run** every suite touched: Foundation CoreTest, User ServerTest + CommandHost, Session ServerTest + both hosts, FEC ServerTest + both hosts compile, Gateway ServerTest + ServiceHost compile, `test-granite-web-authentication`, `build-granite-web` → all PASS; record counts.
- [ ] **Step 3: Commit** `docs: Granite framework extension points (phase 1)`.
- [ ] **Step 4: Notify the user** for build and E2E; do not merge.
