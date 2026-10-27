# Granite Framework Clean-up Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Uppercase role names, consistent login/logout wording, and TypeScript tests in `Tests/<Component>Test` projects.

**Architecture:** Three independent clean-ups on `feature/granite-framework-extensibility` (phase 1 of the framework work, unmerged).

**Tech Stack:** Java 23 / Spring Boot, React 19 / TypeScript / Vitest, Gradle composite build, yarn + local Verdaccio.

**Spec:** `docs/superpowers/specs/2026-10-26-granite-framework-extensibility-design.md` (role-name casing decision is superseded by this plan: uppercase is the standard).

## Global Constraints

- Role names are `ROLE_` + UPPER_SNAKE: `ROLE_ADMIN`, `ROLE_AUDITOR`, `ROLE_USER`; application example `ROLE_PROMOTIONS_REVIEWER`, `ROLE_PROMOTIONS_PLANNER`, `ROLE_PROMOTIONS_APPROVER`. `ApplicationRoles.ADMIN = "ROLE_ADMIN"`.
- Wording is login/logout everywhere (UI, code, paths, configuration, docs). "JWT signing" etc. is unrelated and unchanged.
- Build/test only with existing Gradle tasks (`./gradlew <task> --rerun`, `--rerun` after each task name) and yarn scripts; yarn registry must be `http://localhost:4873/` (never change it). No daemon start/stop, no `publish*`/jib/deploy scripts beyond the local Verdaccio publish the component build scripts already do.
- No hand-written DDL; data fixes are Java code.
- Style: `//////` headers/footers, Granite formatting (Java and TS per CLAUDE.md files), no explanatory comments, no static inner classes; TS `Array<T>`.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; never commit `.idea/**`, `dist/**`, `.gradle` files or `src/main/typescript/package.json` snapshot bumps.

## Review Focus

1. A user record still holding `ROLE_Admin` after upgrade → converted to `ROLE_ADMIN` at User startup (Task 1 test `renamesMixedCaseToUpper`).
2. An application catalog with a role named `ROLE_PromotionsReviewer` → rejected at construction (Task 1 test `rejectsNonUpperSnakeName`).
3. Okta group mapping values in mixed case → ignored with the startup warning (unchanged behaviour; Task 1 keeps `OktaGroupMappingCheck` tests green).
4. A published Authentication package missing an export a moved test needs → the test project fails to compile (Task 3 runs every moved test).
5. An open session at upgrade → `loginMethod` null; Okta end-session URL absent but logout still succeeds (Task 2 test `logoutWithoutLoginMethodSucceeds`).

---

### Task 1: Uppercase role names

**Files:** every main/test/resource/doc file under `Microservices/`, `Shared/`, `FrontEnds/`, root `CLAUDE.md`, and the spec that contains `ROLE_Admin`, `ROLE_Auditor`, `ROLE_User` or mixed-case application role names (`ROLE_Promotions*`); `Shared/Granite.Foundation/.../security/ApplicationRoles.java`, `GraniteApplicationRoles.java`; `Microservices/Granite.User/.../configuration/GraniteUserCommandServerConfiguration.java` (`legacyAuthorityRenames()`).

**Interfaces:**
- `ApplicationRoles.ADMIN = "ROLE_ADMIN"`; `GraniteApplicationRoles.AUDITOR = "ROLE_AUDITOR"`, `USER = "ROLE_USER"`.
- `ApplicationRoles` constructor rejects (`IllegalArgumentException`) any role name not matching `ROLE_[A-Z0-9_]+`.
- `legacyAuthorityRenames()` returns exactly `ROLE_Admin→ROLE_ADMIN`, `ROLE_Auditor→ROLE_AUDITOR`, `ROLE_User→ROLE_USER` (one-time fix, idempotent).
- Web `Authorities.canEdit` checks `"ROLE_ADMIN"`.

- [ ] Step 1: Update tests first: `ApplicationRolesTest.rejectsNonUpperSnakeName` (`ROLE_PromotionsReviewer`, `ROLE_promotions`, `ADMIN` rejected); `AuthorityRenamerTest.renamesMixedCaseToUpper` (user `{ROLE_Admin,ROLE_User}` → `{ROLE_ADMIN,ROLE_USER}`), idempotence and expansion tests with uppercase Poplar roles; web `isAdminRecognisesUppercase` (`["ROLE_ADMIN"]` true, `["ROLE_Admin"]` false); every other test literal converted.
- [ ] Step 2: Run the affected suites → FAIL.
- [ ] Step 3: Implement the renames and the name check.
- [ ] Step 4: Run Foundation CoreTest, User ServerTest, Session ServerTest + CommandHost + QueryHost tests, FEC ServerTest + host compiles, Gateway ServerTest + ServiceHost compile, `yarn run test-granite-web-authentication` → PASS. `git grep -n -E "ROLE_(Admin|Auditor|User)\b|ROLE_Promotions"` outside `docs/superpowers/plans` returns only the rename map, its tests, the web negative assertion and docs describing the rename.
- [ ] Step 5: Commit `refactor: uppercase role names (ROLE_ADMIN, ROLE_AUDITOR, ROLE_USER)`.

---

### Task 2: login/logout wording

**Files:** `FrontEnds/Granite.Web/Components/Granite.Web.Authentication/src/main/typescript/Login/SignInForm.tsx` → `LoginForm.tsx` (`ILoginFormProps`, `LoginForm`, `onLoggedIn`), `Login/index.ts`, `LoginView.tsx`, `ReauthenticationDialog.tsx`, `LoginCallbackView.tsx`, `Authentication/AuthenticationManager.ts`, `Granite.Web.Navigator/.../Navigation/MenuBar.tsx`, their tests; `Microservices/Granite.Session/.../domain/SignInMethod.java` → `LoginMethod.java`, `Session.java` (`loginMethod`, `get/setLoginMethod`), `Session.orm.xml` (`<basic name="loginMethod"><column name="loginMethod" .../>`), `SessionCommandService.java`, Session tests; CLAUDE.md files.

**Interfaces:** UI copy: "Log in", "Log in with Okta", "<user>, log in again to continue.", "Cancel login", "Login failed", "Logout failed", "Login was not completed", "Back to login", "Completing login", "Loading login options", "Unable to load login options", "No login method is enabled.". Error constants `LOGIN_FAILED`, `LOGOUT_FAILED`.

- [ ] Step 1: Update tests (renamed test file for the form, copy assertions, `LoginMethod` in Session tests) and add `logoutWithoutLoginMethodSucceeds` (Session row with null `loginMethod` → logout succeeds, no `endSessionUrl`).
- [ ] Step 2: Run → FAIL.
- [ ] Step 3: Implement the renames (`git mv` for renamed files).
- [ ] Step 4: Run Session ServerTest + CommandHost + QueryHost tests, `yarn run test-granite-web-authentication`, root `yarn run build-granite-web` → PASS. `git grep -n -i -E "sign[- _]?(in|out)"` outside `docs/superpowers` returns nothing except unrelated "signing" matches.
- [ ] Step 5: Commit `refactor: use login/logout wording consistently`.

---

### Task 3: TypeScript tests in `Tests/<Component>Test` projects

**Files:**
- Create: `FrontEnds/Granite.Web/Tests/Granite.Web.AuthenticationTest/{package.json,tsconfig.json,vitest.config.ts,src/test/typescript/*Test.ts(x),src/test/typescript/TestSupport.ts}`
- Create: `FrontEnds/Granite.Web/Tests/Granite.Web.UserTest/{package.json,tsconfig.json,vitest.config.ts,src/test/typescript/UsersChangeSetTest.ts,UsersValueEntriesTest.ts}`
- Delete (`git mv`): `Components/Granite.Web.{Authentication,User}/src/test/typescript/**`; remove vitest/jsdom/testing-library dev dependencies and `test` script/`vitest.config.ts` from the two components.
- Modify: `Components/Granite.Web.Authentication/src/main/typescript/**/index.ts` (and `Granite.Web.User`) to export what the tests import; `FrontEnds/Granite.Web/package.json`; root `package.json`; `FrontEnds/Granite.Web/CLAUDE.md`; root `CLAUDE.md`.

**Interfaces:**
- Test file names: `<Subject>Test.ts` / `<Subject>Test.tsx` (e.g. `AuthenticationManagerTest.ts`, `LoginViewTest.tsx`, `UsersChangeSetTest.ts`); vitest `include` matches `src/test/typescript/**/*Test.ts?(x)`.
- Test projects depend on the published packages (`granite.web.authentication`, `granite.web.user` at `1.0.0-snapshot`) from Verdaccio and import their subpaths; they are `private`, never published.
- `FrontEnds/Granite.Web/package.json` scripts: `test-granite-web-authentication` = publish the Authentication component, then `cd Tests/Granite.Web.AuthenticationTest && yarn run test`; `test-granite-web-user` likewise (publishing Authentication first if User depends on it); `test-granite-web` runs both. Each test project: `build-init` (install + `yarn upgrade --pattern "granite|strata"`), `test` = `yarn run build-init && vitest run`.
- Root `package.json`: `test-granite-web` (`cd FrontEnds/Granite.Web && yarn run test-granite-web`) and `test-granite` (runs every TS test project; today only the web ones).
- CLAUDE.md: the convention (`Tests/<Component>Test/src/test/typescript/<Subject>Test.ts`) applies to microservice TS components too when tests are added.

- [ ] Step 1: Move and rename the tests, create the two test projects, switch imports to the published packages.
- [ ] Step 2: Run `yarn run test-granite-web-authentication` and `yarn run test-granite-web-user` (FrontEnds/Granite.Web) → FAIL where exports are missing.
- [ ] Step 3: Add the missing exports; update scripts and docs.
- [ ] Step 4: Run (FrontEnds/Granite.Web) `yarn run test-granite-web`, then root `yarn run test-granite` and `yarn run build-granite-web` → PASS with the same test count as before the move (93 Authentication + User tests, plus any added in Tasks 1–2).
- [ ] Step 5: Commit `test(web): move TypeScript tests to Tests/<Component>Test projects`.
