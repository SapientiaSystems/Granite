# Granite Framework Phase 3 — UI Packaging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Granite's web pieces become building blocks an application with its own frame (Poplar) can use: a reusable Users view with a role-catalog pick list, and a user menu; Granite's Navigator becomes Granite's own frame built on them.

**Architecture:** `UsersView` moves from `Granite.Web.Navigator` to `Granite.Web.User` (new subpath `granite.web.user/Users`); the authorities editor becomes a Mantine `MultiSelect` over the role catalog (`UsersOutput.roles`) with hierarchy cascade implemented as pure functions; a `UserMenu` component is exported from `granite.web.authentication/Login`; `MenuBar` uses it.

**Tech Stack:** React 19, TypeScript, Mantine 7, AG Grid 33, Vitest.

**Spec:** `docs/superpowers/specs/2026-10-26-granite-framework-extensibility-design.md` (Web, phase 3).

## Global Constraints

- Branch `feature/granite-ui-packaging` (from `development` fefc313).
- Role cascade uses the catalog's `impliedRoles`: selecting a role adds its transitive implied roles; removing a role removes every role that (transitively) implies it. `ROLE_ADMIN` implies nothing and is implied by nothing. Roles not in the catalog that a user already holds are shown and kept (not removable via cascade side effects; removable explicitly).
- User menu decisions (user): shows the user name or "Log in"; Log out / Log in actions; a Preferences item pointing at a path the app passes; optional app-supplied extra items. Login/logout wording only.
- Pick list decision (user): multi-select with chips, labels from the catalog.
- Navigator decision (user): stays Granite's own frame, rebuilt on the building blocks.
- `Granite.Web.User` gains dependencies on `granite.web.authentication`, `granite.frontendcontroller.client`, `granite.frontendcontroller.service`; build order in `FrontEnds/Granite.Web/package.json` `build-components` becomes authentication → user → account → navigator.
- Web style per `FrontEnds/Granite.Web/CLAUDE.md`; TS tests in `Tests/<Component>Test/src/test/typescript/<Subject>Test.ts(x)` importing published subpaths; component deps declared in both package.json files (never commit version snapshot bumps; restore package.json files rewritten by `yarn upgrade`).
- Registry is whatever yarn is configured with; never change it; wait for `npm view <pkg> dist-tags` to show a fresh snapshot before rerunning a dependent test project.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; explicit paths only.

## Review Focus

1. Removing a role that a selected role implies (e.g. remove Reviewer while Approver selected) → Approver and Planner removed too (Task 2 test `removeCascadesToDependents`).
2. A user holding a non-catalog role (`ROLE_LEGACY`) is edited → the value stays unless explicitly removed (Task 2 test `keepsNonCatalogRoles`).
3. Users output without `roles` (older FEC) → pick list falls back to the user's current authorities, no crash (Task 2 test `missingCatalogFallsBack`).
4. `UserMenu` while `reauthenticating` → shows the user name, no Log in item (Task 3 test).
5. Application frame without a Preferences path → the Preferences item is hidden (Task 3 test).

---

### Task 1: Move `UsersView` into `Granite.Web.User`

**Files:** move `Components/Granite.Web.Navigator/src/main/typescript/User/UsersView.tsx` → `Components/Granite.Web.User/src/main/typescript/Users/{UsersView.tsx,index.ts}`; `Granite.Web.User` package.json (both) deps; `Granite.Web.Navigator` (remove `User/`, AppHost/Navigator imports `granite.web.user/Users`); `FrontEnds/Granite.Web/package.json` build order; Web CLAUDE.md.

**Interfaces:** `UsersView(props: { service?: string })` — `service` default `"frontendcontroller-user-service"`; behaviour unchanged; it passes `output.roles` (catalog) to the editable view (consumed in Task 2; Task 1 may pass it unused).

- [ ] Move with `git mv`, update imports/exports and build order; existing tests and `build-granite-web` pass; commit `refactor(web): reusable UsersView in granite.web.user`.

### Task 2: Role-catalog pick list with cascade

**Files:** `Granite.Web.User/.../EditableView/{RoleCascade.ts,AuthoritiesCellEditor.tsx,AuthoritiesCellRenderer.tsx,UsersEditableView.tsx,index.ts}`, `Tests/Granite.Web.UserTest/src/test/typescript/RoleCascadeTest.ts`.

**Interfaces:**
- `RoleCascade.ts`: `selectRole(selected: Array<string>, role: string, catalog: Array<RoleData>): Array<string>`, `deselectRole(selected, role, catalog): Array<string>`, `applySelection(previous: Array<string>, next: Array<string>, catalog): Array<string>` (diffs MultiSelect output into select/deselect calls), `roleOptions(catalog, current): Array<{ value: string; label: string }>` (catalog roles plus any held non-catalog roles labelled by name).
- Editor: Mantine `MultiSelect` (chips) using `roleOptions` and `applySelection`; renderer shows labels.
- Catalog provided through the existing editable-view context/props from `UsersView`.

- [ ] Tests first: `selectAddsImpliedRoles`, `removeCascadesToDependents`, `adminIsIndependent`, `keepsNonCatalogRoles`, `missingCatalogFallsBack`, `applySelectionHandlesAddAndRemove`; implement; `yarn run test-granite-web-user`; `build-granite-web`; commit `feat(web): role-catalog pick list with hierarchy cascade`.

### Task 3: `UserMenu` building block; Navigator uses it

**Files:** `Granite.Web.Authentication/.../Login/{UserMenu.tsx,index.ts}`, `Granite.Web.Navigator/.../Navigation/MenuBar.tsx`, `Tests/Granite.Web.AuthenticationTest/src/test/typescript/UserMenuTest.tsx`, Web CLAUDE.md.

**Interfaces:** `UserMenu(props: { preferencesPath?: string; loginPath?: string /* default "/login" */; items?: Array<{ label: string; onClick: () => void }> })` — uses `useAuthentication()` and `useNavigate()`; authenticated or reauthenticating → user name label, items: Preferences (if path), extra items, Log out (navigates to `loginPath` after a successful logout); anonymous → "Log in" label and a Log in item.

- [ ] Tests first: `anonymousShowsLogIn`, `authenticatedShowsNameAndLogout`, `reauthenticatingStillShowsName`, `preferencesHiddenWithoutPath`, `extraItemsRendered`, `logoutNavigatesToLogin`; implement; MenuBar uses `<UserMenu preferencesPath="/user" />`; `test-granite-web-authentication`; `build-granite-web`; commit `feat(web): reusable UserMenu`.

### Task 4: Docs and verification

- [ ] Root CLAUDE.md "Extending Granite": UI building blocks list (`UsersView`, `UserMenu`, `LoginView`, `LoginCallbackView`, `RequireAuthentication`, `PreferencesProvider`/`PreferencesView`/`usePreference`) and a short Poplar-frame example; spec phase 3 status note; Web CLAUDE.md.
- [ ] Run root `yarn run test-granite` and `yarn run build-granite-web`; record counts; commit `docs: UI building blocks`.
