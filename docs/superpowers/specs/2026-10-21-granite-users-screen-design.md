# Granite Users Screen — Design Spec

**Date:** 2026-10-21
**Status:** Draft — awaiting user review
**Scope:** `Granite.User` (query + commands), `Granite.FrontEndController` (user FrontEndController), `Granite.Web` (Users view and grid).
**Builds on:** `2026-10-21-granite-login-roles-design.md` (merged): only `ROLE_ADMIN` / `ROLE_AUDITOR` can log in; the Gateway allows auditors `get-output` but not `handle-input`; `UsersView` already passes `show*` flags from `canEdit(state)`.
**Mockup:** `.superpowers/brainstorm/46176-1792604918/content/users-grid.html` (approved, with User Name editable on new rows).

---

## 1. Intent

An administrator needs one screen to see every Granite user, create users, change a user's authorities and value map, and delete users, with every change saved to the database. Auditors see the same screen read-only. Today the Users screen is a demo: two columns, canned data from `FakeFrontEndControllerServiceClient`, and a Save that only changes what's on screen. The FrontEndController handlers behind it are stubs, and the User service can neither list all users nor write the value map.

## 2. Goals

1. The grid shows every user with all `UserData` properties except the value map.
2. Each user's value map appears as key/value detail rows that drop down under the user's row.
3. Admins can create users, edit authorities and value maps, and delete users; Save persists all of it.
4. All UI reads and writes go through the user FrontEndController (`get-output` / `handle-input`), which calls `IUserQueryServiceClient` and `IUserCommandServiceClient`.

## 3. Non-goals and anti-goals

**Non-goals**

- Moving the screen to the admin FrontEndController (may be copied later).
- Changing User Name, External Identity or Password of an existing user.
- Paging or server-side filtering of the user list (all users are loaded at once).
- A catalog of valid authorities.
- Replacing the fake client in `HomeView`.
- Typed values in the value map (everything edited as text, see §4.4).
- Server-side role checks in the FrontEndController or User service. Write access is controlled by the Gateway role rules from the login-roles spec; where those rules are off (test, production until roles are assigned), any logged-in user can save, exactly as for every other endpoint today.

**Anti-goals**

- A password hash, or anything derived from it, ever leaves the User service.
- Edits in the detail rows are lost on Save (the bug in Mahogany's `PurchaseOrdersEditableView`).
- A failed or refused save looks like it succeeded.
- An admin can lock everyone out by deleting their own account from this screen.

## 4. Design

### 4.1 Screen

Columns, left to right: expand toggle (▶/▼), selection checkbox, **Id**, **User Name**, **External Identity**, **Password**, **Authorities**.

| Column | Existing row | New row |
|---|---|---|
| Id | read-only | blank until saved |
| User Name | read-only | editable, required |
| External Identity | read-only | read-only, blank |
| Password | read-only, always `********` | editable, required, masked while typing |
| Authorities | editable in edit mode | editable |

- **Authorities** is a chip list of that user's own authority strings. In edit mode each chip has a remove ×, and typing a value and pressing Enter adds it. No catalog, no suggestions beyond what's typed. Duplicates and blank entries are ignored.
- **Value map**: expanding a user inserts a full-width detail row under it containing a nested grid with **Key** and **Value** columns, plus Add value / Delete value actions. Only one user is expanded at a time. Keys and values are editable while the parent row is in edit mode (or is new).
- **Toolbar** (admins only, via the existing `show*` flags): Create, Edit, Save, Undo, Delete, with the existing keyboard shortcuts. The Approve and Cancel placeholder buttons are removed from `UsersEditableView` *(my call: they do nothing and have no meaning for users)*.
- **Delete** opens a Mantine confirmation modal listing the user names to be deleted; Confirm deletes them on the server immediately (not deferred to Save) *(my call)*.
- **Validation before Save** (client side, blocks Save and highlights the row): new rows need a non-blank User Name and Password; value-map keys must be non-blank and unique within a user.
- **Self-delete guard**: the Delete action is disabled when the selection includes the logged-in user (`state.userId`) *(my call, from the anti-goal)*.
- **Errors**: any failed load or save, including a 403, shows a red Mantine alert above the grid with the failure message; nothing is applied locally that the server rejected. This also discharges the 403-display item deferred from the login-roles spec.

The master/detail mechanism follows Mahogany's `PurchaseOrdersEditableView` (AG Grid Community: synthetic `isDetailRow` rows, `isFullWidthRow` + `fullWidthCellRenderer`, `getRowHeight`, `postSortRows` keeping the detail under its parent). Granite must not copy its three duplicated grid configurations or its detail-local state: detail edits write straight back into the parent user's `values` in the master row data.

### 4.2 Load and save flow

**Load** — `get-output` with `source="User.All"`, `destination="UsersEditableView"`, no command (as `UsersView` sends today). The FrontEndController's `InitializeUsersView` calls the new User query (§4.3) and returns `UsersOutput { users: List<UserData> }`.

**Save** — one `handle-input` carrying a `UsersInput` with `source="User.All"`, `command="Save"`, `destination="UsersEditableView"`:

```
UsersInput
  usersToCreate : List<CreateUserData>   // userName, password, authorities, values
  usersToUpdate : List<UpdateUserData>   // userId, authorities, values
```

**Delete** — a separate `handle-input`, `command="Delete"`, `UsersInput.userIdsToDelete : List<Long>`.

The FrontEndController's `UpdateUsersView` (Save) calls `createUsers` then `updateUsers`; `DeleteUsersView` (Delete) calls `destroyUsers`. Each returns a `UsersOutput` holding the **full, freshly re-queried** user list plus `failureMessages : List<String>` (one per failed user, empty on success). Only changed rows are sent: new rows go to `usersToCreate`; existing rows whose authorities or values differ from the last loaded copy go to `usersToUpdate`.

**Partial failure** *(my call)*. Each User command request runs in one transaction (`ITransactionalUserCommandService` is `@Transactional`): an exception rolls back the whole batch, while "soft" failures (e.g. an update for an id that no longer exists) are reported per user in `failedUsers` / `failedUserIds`. Because Save sends creates and updates as two requests, creates can commit while the update batch fails. The UI takes the refreshed list as the new baseline, then re-applies its own pending changes for every row the server did not accept (failed creates, failed updates, and rows not yet saved), so nothing the admin typed is lost. Delete works the same way: other rows' pending edits survive a delete.

**Unsaved rows in a delete** *(my call)*. Selected rows that were never saved are removed locally and are not sent in `userIdsToDelete`.

**Self-delete on the server** *(my call)*. `DeleteUsersView` drops the caller's own user id from `userIdsToDelete` and reports it in `failureMessages`, so the guard does not rely on the UI's disabled button alone. The caller's id is `UsersInput.userId`, which the UI fills from `state.userId`; the FrontEndController does not decode the JWT. This guards against accidents, not against a malicious admin, who can call the User service directly anyway.

**FrontEndController wiring**

- `UserRequestProcessor` becomes a `DefaultRequestProcessor` with actions keyed `KeyBuilder.build(source,command,destination)`: `("User.All", null, "UsersEditableView")` → `InitializeUsersView`, `("User.All","Save","UsersEditableView")` → `UpdateUsersView`, `("User.All","Delete","UsersEditableView")` → `DeleteUsersView`. Whatever `HomeView` needs today keeps working.
- The `IUserQueryServiceClient` / `IUserCommandServiceClient` beans already exist in `GraniteUserFrontEndControllerConfiguration` and forward the caller's bearer token.
- `UsersInput` and `UsersOutput` gain TypeScript mirrors matching the Java fields (there is no TS `UsersInput` today).
- **Polymorphic JSON.** Strata (commit `e796b50`, "Added polymorphic JSON serialization/deserialization") now annotates `FrontEndInput` and `FrontEndOutput` with `@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)` and `@JsonIgnoreProperties(ignoreUnknown = true)`. Each application registers its concrete subtypes on its Jackson mappers (`registerSubtypes`). Granite therefore:
  - keeps one list of its FrontEndController subtypes (`UsersInput`, `UsersOutput`) in the FrontEndController Service component and registers it on **every** ObjectMapper in both FrontEndController hosts: the Spring MVC mapper that binds `handle-input`/`get-output` bodies, the springdoc `ObjectMapperProvider` mapper, and any mapper in the FrontEndController tests (user decision);
  - relies on deduction, so every registered input subtype must have a property set no other registered input subtype has (same for outputs), and the TypeScript side must always send every `UsersInput` field (empty arrays rather than omitted fields) so the distinguishing properties are present;
  - proves the round trip (TS-shaped JSON → `UsersInput`, `UsersOutput` → JSON → `UsersOutput`) in a test before building on it.
- **Strata executor beans.** The same Strata change makes `FrontEndControllerService` take an `Executor` from an application-overridden `@Named("User")` / `@Named("Admin")` bean. Granite's configurations already override both (`BoundedExecutor(200, 10s)`), so no change is needed.

**Web client**

- `UsersView` switches from `FakeFrontEndControllerServiceClient` to the real `FrontEndControllerServiceClient` (`strata.frontendcontroller.client`, constructor `(baseUrl)`), calling `setAuthorization(accessToken)` with the current token from `useAuthentication()`. The base URL is `getRuntimeConfig().apiBaseUrl + "/frontendcontroller-user-service"`, the same pattern `SessionApi` uses; the path is proxied by both the Vite dev server and the AppHost's `nginx.conf`. Navigator gains a dependency on `strata.frontendcontroller.client`.
- `UsersEditableView` gains callbacks so the host performs the I/O: `onSave(creates, updates)` and `onDelete(userIds)`, each returning a promise of the refreshed list and failure messages.

### 4.3 User service changes

**New query** — `findAllUsers`: `FindAllUsersRequest` (no fields beyond the standard request headers) → `FindAllUsersReply { users: List<UserData> }`, ordered by `userId`. Endpoint `POST /user-query-service/find-all-users`. Added to `IUserQueryService`, `UserQueryService`, the controller, `UserQueryServiceClient`, and the TypeScript mirrors, following the existing `findUsersByKeySet` pattern. The Gateway already lets admins and auditors call `/user-query-service/**`.

**`UserData.password`** — new `String password` field. Every `UserData` the User service returns (queries and command replies) carries exactly `"********"` (eight asterisks) when the user has a non-blank stored password and `null` otherwise (e.g. OAuth2-only users, or a null/blank hash). The hash is never mapped into `UserData`. TS mirror updated.

**`values` on create and update**

- `CreateUserData.values : Map<String,Object>` — stored via `User.setValue` for each entry.
- `UpdateUserData.values : Map<String,Object>` — `null` means unchanged; a non-null map **replaces** the user's whole value map (keys absent from it are removed) *(my call: the UI always sends the complete map, so replace is the simplest correct rule)*.
- TS mirrors updated. The leftover template field `foo` on `UpdateUserRequest` is removed.

### 4.4 Value map as text

`values` is `Map<String,Object>`; existing entries may be strings or numbers. The grid shows every value with `String(value)` and sends every edited value back as a string. A numeric value that is opened and saved unchanged is still sent as its original value, because unchanged rows are not sent at all *(my call)*.

## 5. Constraints

- Follow component CLAUDE.md conventions (Java: Allman braces, return type on its own line, fluent setters, `////` header/footer, no static inner classes, no explanatory comments; TS: `//////` header/footer, named function declarations, explicit types, `Array<T>`).
- Java tests run from the repo root composite build (`./gradlew :Granite.<Service>:Tests:…:test`), which builds `Shared/Granite.Foundation` from source.
- Web builds use the existing scripts (`yarn run build` in `FrontEnds/Granite.Web`, which publishes to the local Verdaccio); don't change tooling, don't start/stop Verdaccio or Gradle daemons, never run jib/publish to remote registries.
- Update `Granite.User/CLAUDE.md`, `Granite.FrontEndController/CLAUDE.md` (its current content is a stale template describing a non-existent entity; replace the relevant parts with the real request/reply design) and any web docs.

## 6. Resolved questions

- Polymorphic `FrontEndInput`/`FrontEndOutput` JSON: fixed in Strata (`@JsonTypeInfo(DEDUCTION)`); Granite registers its subtypes on every ObjectMapper (§4.2).

## 7. Left to the builder

- Names of new Java/TS classes beyond those fixed above, and how `UsersEditableView` is split into files (the master grid, detail grid and authorities cell editor are natural units).
- The authorities cell editor component (e.g. Mantine `TagsInput` inside an AG Grid cell editor).
- How the AppHost's access token reaches the FrontEndController client.
- Test design, provided it covers: `findAllUsers`; password masking on every `UserData`-returning path; `values` on create and replace-on-update; the three FrontEndController actions including partial failures; `UsersInput`/`UsersOutput` JSON round-trip; and the grid's change detection (only changed rows sent, detail edits included).
