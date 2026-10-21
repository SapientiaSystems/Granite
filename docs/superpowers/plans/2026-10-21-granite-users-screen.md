# Granite Users Screen Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A real Users screen: every user in an AG Grid with value-map detail rows, admins can create/edit/delete, all I/O through the user FrontEndController to the User service.

**Architecture:** The User service gains `findAllUsers`, a masked `UserData.password`, and `values` on create/update. The FrontEndController registers its `FrontEndInput`/`FrontEndOutput` subtypes on every Jackson mapper (Strata now uses `@JsonTypeInfo(DEDUCTION)`), and its `UserRequestProcessor` becomes a `DefaultRequestProcessor` with Initialize/Save/Delete actions. The web grid gets pure change-tracking logic (unit-tested), a master/detail AG Grid modelled on Mahogany's `PurchaseOrdersEditableView`, and `UsersView` switches to the real Strata client.

**Tech Stack:** Java 23, Spring Boot 3.4, ModelMapper, Jackson, JUnit 5 + Mockito; React 19, TypeScript, AG Grid Community 33, Mantine 7, vitest.

**Spec:** `docs/superpowers/specs/2026-10-21-granite-users-screen-design.md`

## Global Constraints

- Masked password value: exactly `"********"` (8 asterisks) when the stored password is non-blank, else `null`. The hash never leaves the User service.
- `UpdateUserData.values`: `null` = unchanged; non-null = replace the whole value map.
- FrontEndController keys (`KeyBuilder.build(source,command,destination)`): load `("User.All", null, "UsersEditableView")`, save `("User.All","Save","UsersEditableView")`, delete `("User.All","Delete","UsersEditableView")`.
- `UsersInput` fields: `usersToCreate: List<CreateUserData>`, `usersToUpdate: List<UpdateUserData>`, `userIdsToDelete: List<Long>` (all default to empty lists; TS always sends all three). `UsersOutput` fields: `users: List<UserData>`, `failureMessages: List<String>`.
- Subtype registration: one Jackson module listing Granite's subtypes, registered on the Spring MVC mapper and the springdoc `StrataObjectMapperProvider` mappers (json + yaml) in **both** FrontEndController hosts, and on every mapper the FrontEndController tests build.
- Java style per component CLAUDE.md: Allman braces, return type on its own line, fluent setters, `////` header/footer, no static inner classes, no explanatory comments. TS: `//////` header/footer, named function declarations, explicit types, `Array<T>`.
- Java tests run from the repo root: `./gradlew :Granite.<Service>:Tests:<TestProject>:test [--tests "<Class>"]` (root composite build supplies Foundation from source). Strata `1.0-SNAPSHOT` with commit `e796b50` is in mavenLocal.
- Never stage `**/.gradle/**` files. No jib, no remote publish. Don't start/stop Gradle daemons or Verdaccio. Web components that depend on unpublished sibling packages are verified by the user's `cd FrontEnds/Granite.Web && yarn run build` (needs Verdaccio running); tasks verify what they can with `npx tsc --noEmit -p .` and component tests.
- Each task updates the CLAUDE.md of the component it changes (User: `Microservices/Granite.User/CLAUDE.md`; FrontEndController: replace the stale template content in `Microservices/Granite.FrontEndController/CLAUDE.md` with the real request/reply design).

## Review Focus

1. **A save whose create batch succeeds but whose update batch fails (exception → rollback)** → reply lists all users incl. the new ones, `failureMessages` non-empty, UI keeps the failed updates pending. Test: `UpdateUsersViewTest.updateFailureAfterCreatesReturnsRefreshedListAndMessages` (Task 5) + `UsersChangeSetTest.mergeKeepsUnacceptedEdits` (Task 6).
2. **Existing rows always carry `password = "********"`** → never sent back. Test: `UsersChangeSetTest.updatesNeverIncludePassword` (Task 6).
3. **A Delete `UsersInput` with empty create/update lists** deserializes as `UsersInput` (deduction needs the properties present). Test: `FrontEndJacksonModuleTest.deleteShapedJsonDeserializesAsUsersInput` (Task 4).
4. **Stored values containing `null`** (legacy rows) don't crash create/update or display. Test: `UserValuesCommandTest.nullValueEntriesAreSkipped` (Task 3) + `UsersChangeSetTest.nullValuesDisplayAsEmptyString` (Task 6).
5. **Authority input with spaces or duplicates (`" ROLE_ADMIN"`, `"ROLE_ADMIN"`)** → stored once, trimmed. Test: `UsersChangeSetTest.normalizesAuthorities` (Task 6).

---

### Task 1: User service `findAllUsers`

**Files** (U = `Microservices/Granite.User/Components`):
- Create: `U/Granite.User.Service/src/main/java/granite/user/service/requestreply/FindAllUsersRequest.java`, `FindAllUsersReply.java`; TS `U/Granite.User.Service/src/main/typescript/RequestReply/FindAllUsersRequest.ts`, `FindAllUsersReply.ts`, `FindAllUsersRequestBuilder.ts`; export in `index.ts`.
- Modify: `IUserQueryService.java` (+ TS `IUserQueryService.ts`), `U/Granite.User.Server/.../application/UserQueryService.java`, `ITransactionalUserQueryService.java` if it re-declares methods, `U/Granite.User.Server/.../platform/UserQueryServiceController.java`, `U/Granite.User.Client/.../UserQueryServiceClient.java` (+ `IUserQueryServiceClient.java`), TS `U/Granite.User.Client/src/main/typescript/RequestReply/UserQueryServiceClient.ts` (+ interface).
- Test: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/application/FindAllUsersTest.java` (template: `FindUsersByExternalIdentityTest`).

**Interfaces:**
- Produces: `CompletionStage<FindAllUsersReply> findAllUsers(FindAllUsersRequest request)`; `FindAllUsersReply.getUsers(): List<UserData>` ordered by `userId` ascending; REST `POST /user-query-service/find-all-users`; client path `"find-all-users"`.

- [ ] **Step 1: Failing tests** — `findAllUsersReturnsEveryUserOrderedById` (save 3 users out of id order → reply `isSuccess()`, ids ascending, size 3); `findAllUsersOnEmptyRepositoryReturnsEmptyList`.
- [ ] **Step 2: Run, expect FAIL** — `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test --tests "*FindAllUsersTest"`
- [ ] **Step 3: Implement** following the `findUsersByKeySet` pipeline (validate → context → find → buildReply/exceptionally) and the existing ModelMapper `TypeToken<List<UserData>>` mapping. Add the controller mapping, Java + TS client methods and TS mirrors. Document the query in User CLAUDE.md.
- [ ] **Step 4: Run ServerTest suite, expect PASS.**
- [ ] **Step 5: Commit** — `feat(user): add findAllUsers query`

---

### Task 2: Masked `UserData.password`

**Files:**
- Modify: `U/Granite.User.Service/.../UserData.java` (+ `UserData.ts`), `U/Granite.User.Server/.../application/MappingModule.java`.
- Test: `.../server/application/UserPasswordMaskingTest.java`; extend `ModelMapperTest` if that's where mapping tests live.

**Interfaces:**
- Produces: `UserData.getPassword(): String` / fluent `setPassword(String)`; TS `password?: string`.

- [ ] **Step 1: Failing tests** — `mapsNonBlankPasswordToMask` (User with hash → `"********"`); `mapsNullOrBlankPasswordToNull`; `createUserReplyIsMasked`, `updateUserReplyIsMasked`, `findAllUsersIsMasked`, `authenticateUserReplyIsMasked` (each asserts `"********"` and that the reply JSON contains no `$2` / hash prefix).
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement** — in `MappingModule`'s explicit `User → UserData` type map add a converter mapping `User::getPassword` → `UserData::setPassword` that yields the mask or null. Every `UserData`-returning path goes through this map, so no per-path code. Document in User CLAUDE.md.
- [ ] **Step 4: Run ServerTest suite, expect PASS.**
- [ ] **Step 5: Commit** — `feat(user): expose masked password on UserData`

---

### Task 3: `values` on create and update

**Files:**
- Modify: `CreateUserData.java`, `UpdateUserData.java` (+ TS mirrors), `UserCommandService.java` (`mapToUser` ~L718, `applyUpdate` ~L727), `UpdateUserRequest.java` (remove `foo`, L18/L25), TS `UpdateUserRequest.ts` (rename `user` → `userToUpdate` to match Java), TS `UserData.ts` (`values?: Record<string, unknown>` instead of `Map`).
- Test: `.../server/application/UserValuesCommandTest.java`.

**Interfaces:**
- Produces: `CreateUserData.getValues(): Map<String,Object>` (default empty) + fluent setter; `UpdateUserData.getValues(): Optional<Map<String,Object>>` + fluent setter.

- [ ] **Step 1: Failing tests** — `createStoresValues` (`{"theme":"dark","pageSize":"50"}` → stored and returned); `updateWithValuesReplacesWholeMap` (existing `{a:1,b:2}`, update `{b:"3",c:"4"}` → `{b:"3",c:"4"}`); `updateWithoutValuesLeavesMapUnchanged`; `updateWithEmptyValuesClearsMap`; `nullValueEntriesAreSkipped` (`{"a":null,"b":"x"}` → `{b:"x"}`, no exception).
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement** — keep `values` out of the ModelMapper `CreateUserData → User` map (skip it) and apply entries with `User.setValue` in `mapToUser`; in `applyUpdate`, when present: `user.getValues().clear()` then `setValue` per non-null entry. Document in User CLAUDE.md.
- [ ] **Step 4: Run ServerTest suite, expect PASS.**
- [ ] **Step 5: Commit** — `feat(user): create and replace user value maps`

---

### Task 4: FrontEndController subtypes and Jackson registration

**Files** (FC = `Microservices/Granite.FrontEndController`):
- Modify: `FC/Components/Granite.FrontEndController.Service/src/main/java/granite/frontendcontroller/service/requestreply/UsersInput.java`, `UsersOutput.java` (fields per Global Constraints; fix their stale file-header names); TS `.../src/main/typescript/RequestReply/UsersOutput.ts`; create `UsersInput.ts`; export from `index.ts`.
- Create: `FC/Components/Granite.FrontEndController.Service/src/main/java/granite/frontendcontroller/service/requestreply/FrontEndJacksonModule.java`.
- Modify: `FC/Components/Granite.FrontEndController.Server/.../configuration/GraniteUserFrontEndControllerConfiguration.java` and `GraniteAdminFrontEndControllerConfiguration.java` (add a `com.fasterxml.jackson.databind.Module` bean returning `new FrontEndJacksonModule()`; Spring Boot registers Module beans on its MVC mapper); `FC/Applications/Granite.FrontEndController.UserHost/.../HostConfiguration.java` and `AdminHost/.../HostConfiguration.java` (`objectMapperProvider`: register the module on `jsonMapper()` and `yamlMapper()` of the `StrataObjectMapperProvider`).
- Test: `FC/Tests/Granite.FrontEndController.ServerTest/src/test/java/granite/frontendcontroller/server/configuration/FrontEndJacksonModuleTest.java`.

**Interfaces:**
- Consumes: `CreateUserData`/`UpdateUserData` with `values` (Task 3), `UserData.password` (Task 2).
- Produces: `public class FrontEndJacksonModule extends SimpleModule` whose constructor calls `registerSubtypes(UsersInput.class, UsersOutput.class)`. Future FrontEndController subtypes are added here only. Bean method name `frontEndJacksonModule()` in both configurations.

- [ ] **Step 1: Failing tests** (mapper = `new ObjectMapperSupplier().get().registerModule(new FrontEndJacksonModule())`, as Strata's `FrontEndJsonSerializationTest`):
  - `saveShapedJsonDeserializesAsUsersInput` — literal TS-shaped JSON `{"userId":7,"source":"User.All","command":"Save","destination":"UsersEditableView","usersToCreate":[{"userName":"amy","password":"pw","authorities":["ROLE_AUDITOR"],"values":{"theme":"dark"}}],"usersToUpdate":[],"userIdsToDelete":[]}` read as `FrontEndInput` → `UsersInput`, key `"User.All->Save->UsersEditableView"`.
  - `deleteShapedJsonDeserializesAsUsersInput` — empty create/update lists, `userIdsToDelete:[3]`.
  - `handleInputRequestBodyDeserializes` — the same JSON wrapped as `{"input":{...}}` read as `HandleInputRequest`.
  - `usersOutputRoundTrips` — `UsersOutput` → JSON → `FrontEndOutput` → `UsersOutput` with users and failureMessages intact; JSON contains no type id property.
  - `configurationsExposeModuleBean` — both configurations' `frontEndJacksonModule()` return a `FrontEndJacksonModule`.
- [ ] **Step 2: Run, expect FAIL** — `./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test --tests "*FrontEndJacksonModuleTest"`. (If it fails because `FrontEndInput` lacks `@JsonTypeInfo`, the resolved Strata is stale: stop and report.)
- [ ] **Step 3: Implement** the types, module, beans and host registrations. Update FrontEndController CLAUDE.md (subtype registration rule: add every new subtype to `FrontEndJacksonModule`; distinct property sets; TS sends all fields).
- [ ] **Step 4: Run ServerTest suite and compile both hosts, expect PASS** — `./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test :Granite.FrontEndController:Applications:Granite.FrontEndController.UserHost:compileJava :Granite.FrontEndController:Applications:Granite.FrontEndController.AdminHost:compileJava`
- [ ] **Step 5: Commit** — `feat(frontendcontroller): users input/output and Jackson subtype registration`

---

### Task 5: FrontEndController user actions

**Files:**
- Modify: `FC/Components/Granite.FrontEndController.Server/.../control/user/UserRequestProcessor.java`, `InitializeUsersView.java`, `UpdateUsersView.java`; create `DeleteUsersView.java`.
- Modify: `GraniteUserFrontEndControllerConfiguration.userRequestProcessor()` and `GraniteAdminFrontEndControllerConfiguration.adminRequestProcessor()` (Admin gains an `IUserCommandServiceClient` bean like User's so it can construct the processor).
- Test: `FC/Tests/Granite.FrontEndController.ServerTest/src/test/java/granite/frontendcontroller/server/control/user/InitializeUsersViewTest.java`, `UpdateUsersViewTest.java`, `DeleteUsersViewTest.java`, `UserRequestProcessorTest.java` (Mockito client mocks, plain unit tests like the existing ServerTest classes).

**Interfaces:**
- Consumes: `findAllUsers` (Task 1), `createUsers`/`updateUsers`/`destroyUsers` (existing), `UsersInput`/`UsersOutput` (Task 4).
- Produces: `public class UserRequestProcessor extends DefaultRequestProcessor` with constructor `UserRequestProcessor(IUserQueryServiceClient queryClient, IUserCommandServiceClient commandClient)` registering the three actions under the Global Constraints keys. Each action returns `UsersOutput` with `source/command/destination` copied from the request and `users` from a fresh `findAllUsers`.
- New actions rely on `ForwardedBearerClientRequestFilter` for the Authorization header; they don't add body headers.

- [ ] **Step 1: Failing tests**
  - `InitializeUsersViewTest.returnsAllUsers` — query mock returns 2 users → output users size 2, `failureMessages` empty.
  - `UpdateUsersViewTest.createsThenUpdatesThenRequeries` — verifies `createUsers` called with the input's creates, then `updateUsers` with its updates, then `findAllUsers`; output = re-queried list.
  - `UpdateUsersViewTest.skipsEmptyBatches` — no creates → `createUsers` never called.
  - `UpdateUsersViewTest.reportsFailedUsers` — create reply with one `failedUsers` entry → one failure message naming that userName.
  - `UpdateUsersViewTest.updateFailureAfterCreatesReturnsRefreshedListAndMessages` — update reply `success=false` with a failure message → output has re-queried users and that message.
  - `DeleteUsersViewTest.refusesCallersOwnId` — input `userId=7`, `userIdsToDelete=[7,8]` → `destroyUsers` called with `{8}` only, one failure message.
  - `DeleteUsersViewTest.reportsFailedUserIds`.
  - `UserRequestProcessorTest.routesLoadSaveDelete` — the three keys reach the three actions; an unknown key fails with "No action found for key".
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement.** Failure message text: `"<userName or id>: <reply failureMessage or 'not saved'>"` *(builder may refine wording)*. Remove the leftover `getBuyerIds`/hard-coded ids. Update FrontEndController CLAUDE.md (keys, actions).
- [ ] **Step 4: Run ServerTest suite + both host compiles, expect PASS.**
- [ ] **Step 5: Commit** — `feat(frontendcontroller): load, save and delete users`

---

### Task 6: Grid change tracking (pure logic) with tests

**Files:**
- Modify (tooling, approved by user 2026-10-21): `FrontEnds/Granite.Web/Components/Granite.Web.User/package.json` (add `"test": "vitest run"` script and the same `vitest`/`jsdom` devDependency versions as `Granite.Web.Authentication`), create `vitest.config.ts` mirroring Authentication's.
- Create: `.../Granite.Web.User/src/main/typescript/EditableView/UsersChangeSet.ts`; test `.../Granite.Web.User/src/test/typescript/UsersChangeSet.test.ts`.

**Interfaces:**
- Produces (exported):
  - `interface IUserRow extends UserData { rowKey: string; isNew: boolean; isDetailRow: boolean }` (`rowKey` = `"user-<id>"` or `"new-<n>"`).
  - `function toRows(users: Array<UserData>): Array<IUserRow>`
  - `function normalizeAuthorities(values: Array<string>): Array<string>` — trim, drop blanks, dedupe, keep first-seen order.
  - `function displayValue(value: unknown): string` — `null`/`undefined` → `""`, else `String(value)`.
  - `function validateRows(rows: Array<IUserRow>): Map<string, Array<string>>` — rowKey → messages (new rows: blank userName/password; any row: blank or duplicate value keys).
  - `function computeChanges(baseline: Array<IUserRow>, rows: Array<IUserRow>): { usersToCreate: Array<CreateUserData>; usersToUpdate: Array<UpdateUserData> }` — new rows → creates (userName, password, authorities, values); existing rows whose authorities or values differ from baseline → updates (userId, authorities, values only).
  - `function mergePending(refreshed: Array<UserData>, rows: Array<IUserRow>, baseline: Array<IUserRow>): Array<IUserRow>` — refreshed list as base, then re-apply pending edits for rows whose server state still differs from what was sent, and keep unsaved new rows whose userName isn't in the refreshed list.

- [ ] **Step 1: Failing tests** — `normalizesAuthorities`; `nullValuesDisplayAsEmptyString`; `validateFlagsBlankNewUserFields`; `validateFlagsDuplicateAndBlankKeys`; `newRowsBecomeCreates`; `changedAuthoritiesOrValuesBecomeUpdates`; `unchangedRowsAreNotSent`; `updatesNeverIncludePassword`; `mergeKeepsUnacceptedEdits`; `mergeDropsAcceptedEdits`; `mergeKeepsUnsavedNewRowsNotCreated`.
- [ ] **Step 2: Run, expect FAIL** — `cd FrontEnds/Granite.Web/Components/Granite.Web.User && yarn test`
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run, expect PASS; `npx tsc --noEmit -p .` clean.**
- [ ] **Step 5: Commit** — `feat(web): users grid change tracking`

---

### Task 7: Users grid UI (master/detail)

**Files** (all in `FrontEnds/Granite.Web/Components/Granite.Web.User/src/main/typescript/EditableView/`):
- Modify: `UsersEditableView.tsx`, `IUsersEditableViewProps.ts`, `index.ts`.
- Create: `UserValuesDetail.tsx` (nested Key/Value grid, full-width renderer), `AuthoritiesCellEditor.tsx` + `AuthoritiesCellRenderer.tsx` (chips; Mantine `TagsInput` in the editor), `ExpandCellRenderer.tsx`.

**Interfaces:**
- Consumes: Task 6 functions.
- Produces props on `IUsersEditableViewProps`: `currentUserId?: number`; `onSave?: (changes: { usersToCreate: Array<CreateUserData>; usersToUpdate: Array<UpdateUserData> }) => Promise<IUsersResult>`; `onDelete?: (userIds: Array<number>) => Promise<IUsersResult>`; `export interface IUsersResult { users: Array<UserData>; failureMessages: Array<string> }`. Remove `showApprove`/`showCancel` and their buttons/handlers.

- [ ] **Step 1: Implement** per spec §4.1:
  - Columns expand, select, Id, User Name, External Identity, Password, Authorities with the edit rules in the spec table; password editor is a masked text input on new rows.
  - Master/detail exactly as Mahogany's `PurchaseOrdersEditableView` mechanism (synthetic `isDetailRow` rows in a `displayData` memo, `isFullWidthRow`, `fullWidthCellRenderer={UserValuesDetail}`, `getRowHeight`, `postSortRows`, `getRowId` = rowKey / `"detail-"+rowKey`, selection ignores detail rows), one grid configuration (not Mahogany's three copies), one expanded user at a time.
  - Detail edits update the parent row's `values` in the master `rowData` state.
  - Save: `validateRows` → show messages and stop if any; else `onSave(computeChanges(baseline,rows))` → on resolve `mergePending`, set baseline to the refreshed list, show `failureMessages` in a red Mantine `Alert` above the grid; on reject show the error message in the alert and keep everything.
  - Delete: Mantine confirm modal listing user names; unsaved new rows removed locally; saved ids → `onDelete`; then merge as for Save. Delete disabled when selection includes `currentUserId`.
- [ ] **Step 2: Verify** — `cd FrontEnds/Granite.Web/Components/Granite.Web.User && npx tsc --noEmit -p . && yarn test`, both clean.
- [ ] **Step 3: Commit** — `feat(web): users master/detail grid`

---

### Task 8: Web: UsersView uses the real FrontEndController client

**Files:**
- Modify: `FrontEnds/Granite.Web/Components/Granite.Web.Navigator/src/main/typescript/User/UsersView.tsx`; `FrontEnds/Granite.Web/Components/Granite.Web.Navigator/package.json` (add `"strata.frontendcontroller.client": "0.0.0-snapshot"`, matching the other Strata entries).

**Interfaces:**
- Consumes: `FrontEndControllerServiceClient(baseUrl)`, `.setAuthorization(token)`, `.getOutput`, `.handleInput` (`strata.frontendcontroller.client/RequestReply`); `HandleInputRequestBuilder.newBuilder().setInput(input).build()`; `getRuntimeConfig().apiBaseUrl`; `useAuthentication().state.{accessToken,userId}`; Task 4 TS `UsersInput`/`UsersOutput`; Task 7 props.

- [ ] **Step 1: Implement** — client base URL `getRuntimeConfig().apiBaseUrl + "/frontendcontroller-user-service"`; load with `GetOutputRequestBuilder` (`userId = state.userId`, source/destination per Global Constraints); `onSave`/`onDelete` build a `UsersInput` with **all three arrays present**, `userId = state.userId`, the right `command`, send via `handleInput`, map the reply's output to `IUsersResult`, reject on `success=false`; show load errors in a red `Alert`; pass `currentUserId={state.userId}`. Remove the fake client import from this view (leave `FakeFrontEndControllerServiceClient` itself in place for `HomeView`).
- [ ] **Step 2: Self-check** imports and types against the Task 4/7 exports (Navigator can't be type-checked until the sibling packages are published).
- [ ] **Step 3: Commit** — `feat(web): users view uses the real FrontEndController`
- [ ] **Step 4: User verification (after merge, Verdaccio running):** `cd FrontEnds/Granite.Web && yarn run build`; redeploy User, FrontEndController, web; as admin: load, create user with authorities + values, edit values in detail rows, save, delete with confirm, self-delete disabled; as auditor: grid and detail rows visible, no actions.

---

## Self-review notes

- Spec §4.1 → Task 7; §4.2 load/save/delete, partial failure, self-delete → Tasks 4, 5, 6, 7, 8; polymorphic JSON + all mappers → Task 4; executor beans need no change (already overridden); §4.3 → Tasks 1–3; §4.4 → Tasks 3, 6; §5 docs → Tasks 1–5.
- Task 6 changes `Granite.Web.User/package.json` and adds a vitest config — a tooling change that needs the user's approval before execution.
