# Granite Framework Phase 2 — Preferences Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Server-side, per-user preferences stored in the User `values` map, declared by Granite (theme) and extendable by applications, with a base `PreferencesView` that applications extend with their own sections.

**Architecture:** User gains a values-merge command. FEC gains a Preferences operation pair that always acts on the caller (from the forwarded JWT), validates against declared preference definitions (overridable on the abstract FEC configuration) and stores under `preferences.<key>`. Gateway lets any authenticated user call the Preferences pair. Web gets a `PreferencesProvider` + `usePreference(key)` hook and a `PreferencesView` with child sections; the theme is server-backed with a local cache.

**Tech Stack:** Java 23 / Spring Boot / Strata, WebFlux Gateway, React 19 / TypeScript / Mantine / Vitest.

**Spec:** `docs/superpowers/specs/2026-10-26-granite-framework-extensibility-design.md` (Preferences sections).

## Global Constraints

- Branch `feature/granite-preferences` (from `development` 5d018a9).
- Storage: `values["preferences.<key>"]`; Granite's key `granite.theme` (ENUM `light|dark`, default `light`) is stored as `preferences.granite.theme`. Reserved value-key prefixes: `preferences.` and `granite.` (e.g. `granite.rolesAssigned`).
- Preference types: `STRING`, `BOOLEAN`, `NUMBER`, `ENUM` (with allowed values). Undeclared keys or wrong types on save → failure, nothing stored. A stored value that no longer validates is reported as the default.
- Preferences always act on the caller's own user; a request `userId` is ignored.
- Saving is per change (no Save button).
- Users admin screen shows reserved keys read-only and admin saves never change them.
- Theme: server is the source of truth; the browser caches the last theme in `localStorage` (key `granite.theme`) only to avoid a flash and on the login page; every storage access wrapped in try/catch.
- Role names `ROLE_` + UPPER_SNAKE; login/logout wording.
- Build/test only with existing Gradle tasks (`--rerun` after each task name) and yarn scripts; registry is whatever yarn is configured with (never hard-code or change it). No daemon start/stop, no jib/deploy.
- No hand-written DDL; style per CLAUDE.md files; no explanatory comments; no static inner classes; TS tests in `Tests/<Component>Test/src/test/typescript/<Subject>Test.ts(x)` importing published packages.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; commit explicit paths only (never `.idea/**`, `dist/**`, `.gradle`, snapshot bumps).

## Review Focus

1. A caller sends `handlePreferencesInput` with another user's `userId` → only the caller's own values change (Task 3 test `ignoresRequestUserId`).
2. A save of `granite.theme=purple` or of an undeclared key → failure reply, nothing stored (Task 3 tests).
3. An admin saves a user in the Users screen with edited/removed `preferences.*` / `granite.*` keys → stored reserved keys unchanged (Task 3 test `adminSavePreservesReservedKeys`).
4. Two concurrent merges of different keys → both kept (Task 1 test `mergeKeepsOtherKeys`).
5. `localStorage` throws (private mode) → theme still loads from the server and the page renders (Task 5 test `themeWorksWithoutLocalStorage`).

---

### Task 1: User values-merge command

**Files:** `Microservices/Granite.User/Components/Granite.User.Service` (Java `requestreply/MergeUserValuesRequest`, `MergeUserValuesReply`; `IUserCommandService.mergeUserValues`; TS mirrors + `index.ts`), `Granite.User.Server` (`UserCommandService.mergeUserValues`, `UserCommandServiceController` `POST /merge-user-values`), `Granite.User.Client` (Java `IUserCommandServiceClient`/`UserCommandServiceClient.mergeUserValues`; TS client mirror), tests in `Granite.User.ServerTest`, User CLAUDE.md.

**Interfaces:**
- `MergeUserValuesRequest { Long userId; Map<String,Object> values }`; `MergeUserValuesReply { UserData mergedUser }` (standard reply success/failure).
- Semantics: for each entry, non-null value → put; null value → remove; keys not in the request untouched; authorities untouched; unknown user → `success=false`. Fires `UserUpdated`.
- Gateway: `/user-command-service/**` is already admin/internal-only.

- [ ] Tests: `mergeAddsAndReplacesKeys`, `mergeRemovesNullKeys`, `mergeKeepsOtherKeys`, `mergeLeavesAuthoritiesUnchanged`, `mergeUnknownUserFails`, controller mapping test for `/merge-user-values`.
- [ ] Run `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test --rerun` → FAIL; implement; → PASS. Also `:Granite.User:Tests:Granite.User.ClientTest:compileTestJava`, `yarn run build-granite-user` (root).
- [ ] Commit `feat(user): merge-user-values command`.

### Task 2: Session first-login write uses merge

**Files:** `Microservices/Granite.Session/.../application/SessionCommandService.java` (`assignDefaultRole`), Session tests, Session Claude.md.

**Interfaces:** first password login with no roles and no `granite.rolesAssigned`: `updateUser` with authorities only (no `values`), then `mergeUserValues({granite.rolesAssigned:true})`; either failing refuses the login. Okta creation is unchanged (new user, `CreateUserData.values`).

- [ ] Tests: update `firstLoginSetsMarkerAndKeepsOtherValues` to assert `updateUser` carries no values and `mergeUserValues` carries only the marker; `markerMergeFailureRefuses`.
- [ ] Run `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun` → FAIL; implement; → PASS.
- [ ] Commit `fix(session): store roles-assigned marker via merge-user-values`.

### Task 3: FEC Preferences operations and reserved-key protection

**Files:** FEC Service (Java + TS): `PreferencesInput { Map<String,Object> values }`, `PreferencesOutput { Map<String,Object> values; List<PreferenceDefinitionData> definitions; List<String> failureMessages }`, `PreferenceDefinitionData { key; type; defaultValue; allowedValues }`, `Get/HandlePreferences{Output,Input}{Request,Reply}`, `IGraniteFrontEndControllerService.getPreferencesOutput/handlePreferencesInput`; FEC Server: `application/PreferenceDefinition` + `PreferenceType`, `control/preference/{InitializePreferencesView,SavePreferencesView}`, `GraniteFrontEndControllerContext` gains `List<PreferenceDefinition>`, `AbstractGraniteFrontEndControllerConfiguration.preferenceDefinitions()` (protected, overridable; default `[granite.theme ENUM light|dark default light]`), service dispatch maps, controller `POST /get-preferences-output` and `/handle-preferences-input`; `control/user/UpdateUsersView` preserves reserved keys; FEC Client TS methods; tests; FEC CLAUDE.md + `.github/copilot-instructions.md` (identical).

**Interfaces:**
- Keys: get `Preferences.Current->PreferencesView`; handle `Preferences.Current->Save->PreferencesView`.
- Caller from `SessionRequestContext.getCallerUserId()`; absent → failure `not authenticated`.
- Output `values` holds every declared key (stored value if valid, else default), keyed without the `preferences.` prefix; `definitions` lists all declared definitions.
- Save validates every key first, then one `mergeUserValues` with `preferences.<key>` entries; reply returns the refreshed output.
- `UpdateUsersView`: for each updated user, entries with reserved prefixes are taken from the stored user (incoming reserved entries ignored).
- Applications extend by overriding `preferenceDefinitions()` and calling `super`.

- [ ] Tests: `getReturnsDefaultsWhenUnset`, `getReportsInvalidStoredValueAsDefault`, `saveStoresUnderPrefix`, `saveRejectsUndeclaredKey`, `saveRejectsWrongType` (`granite.theme=purple`), `ignoresRequestUserId`, `unauthenticatedFails`, `extendedConfigurationAddsDefinition`, `adminSavePreservesReservedKeys`; MVC context test asserts the two new paths for Granite and Extended configurations; JSON serialization test for `PreferencesOutput`.
- [ ] Run FEC ServerTest → FAIL; implement; → PASS; host compiles; `yarn run build-granite-frontendcontroller` (root).
- [ ] Commit `feat(frontendcontroller): Preferences operations with declared definitions`.

### Task 4: Gateway rule for Preferences

**Files:** `GraniteGatewayServerConfiguration.configureAuthorization`, Gateway tests, Gateway CLAUDE.md.

**Interfaces:** `POST /frontendcontroller-{user,admin}-service/{get-preferences-output,handle-preferences-input}` → `authenticated()`, registered before the admin-only paths and the application hook (so it cannot be loosened to anonymous or tightened away by accident).

- [ ] Tests: `ROLE_USER` JWT allowed on both paths (not 403); anonymous → 401; `ROLE_USER` still 403 on `handle-users-input`; CSRF still required on `handle-preferences-input`.
- [ ] Run Gateway ServerTest → FAIL; implement; → PASS.
- [ ] Commit `feat(gateway): authenticated access to Preferences operations`.

### Task 5: Web preferences provider, hook, view, theme, Users screen read-only keys

**Files:** `FrontEnds/Granite.Web/Components/Granite.Web.Account/src/main/typescript/Preference/{PreferencesProvider.tsx,usePreference.ts,PreferencesView.tsx,IPreferencesViewProps.ts,ThemeCache.ts,index.ts}` (+ component package.json deps), `Granite.Web.Navigator/.../Account/AccountView.tsx` and AppHost wiring, `Granite.Web.User/.../EditableView` (reserved keys read-only), new test project `FrontEnds/Granite.Web/Tests/Granite.Web.AccountTest`, `UserTest` additions, scripts `test-granite-web-account` (included in `test-granite-web`), Web CLAUDE.md.

**Interfaces:**
- `PreferencesProvider` (inside `AuthenticationProvider`): when authenticated, loads `getPreferencesOutput` through `useAuthentication().createFrontEndClient(...)`; exposes `{ values, definitions, status, error }`.
- `usePreference<T>(key): [T, (value: T) => Promise<void>, { saving: boolean; error: string | null }]` — setter calls `handlePreferencesInput({ [key]: value })` and updates state from the reply.
- `PreferencesView({ userName, userRole, children })`: user card + theme switch (`granite.theme` via `usePreference`, applied to Mantine color scheme, cached in `localStorage` `granite.theme`) + `children` sections.
- Before preferences load (and on the login page) the cached theme applies; with no cache, `light`.
- Users screen: values entries whose key starts with `preferences.` or `granite.` render read-only.

- [ ] Tests (AccountTest): `loadsPreferencesWhenAuthenticated`, `themeToggleSavesAndAppliesScheme`, `saveFailureShowsErrorAndRevertsValue`, `childSectionUsesUsePreference`, `cachedThemeAppliesBeforeLoad`, `themeWorksWithoutLocalStorage`; (UserTest) `reservedValueKeysAreReadOnly`.
- [ ] Run (FrontEnds/Granite.Web) `yarn run test-granite-web-account`, `test-granite-web-user`, `test-granite-web-authentication`; root `yarn run build-granite-web` → PASS.
- [ ] Commit `feat(web): server-backed preferences with extendable PreferencesView`.

### Task 6: Docs and full verification

- [ ] Update root CLAUDE.md "Extending Granite" (how an application adds preferences: override `preferenceDefinitions()`, add a section component using `usePreference`), spec status, memory.
- [ ] Run all Gradle suites touched (Foundation, User, Session + hosts, FEC + host compiles, Gateway + ServiceHost compile) and root `yarn run test-granite`; record counts.
- [ ] Commit `docs: preferences extension recipe`.
