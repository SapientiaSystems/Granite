# Password Login Toggle and Change Password Implementation Plan

**Goal:** Make username/password login a real toggle like Okta (enforced in Session, not only hidden in the UI), and add a self-service Change Password dialog on the User menu, shown only when password login is enabled and the signed-in user has a password.

**Architecture:** Session owns login methods and sessions, so the Change Password path is Web → FEC (`handle-session-input`, verified caller) → Session Command `change-password` (toggle check, open session) → User Command `change-user-password` (lockout, BCrypt, policy) → Session closes the user's other open sessions. User gets a password policy (`granite.user.password.min-length`) applied to every new password.

**Tech Stack:** Spring Boot (Java), Strata typed FEC operations, React/Mantine (`granite.web.authentication`), yarn against the public npmjs registry.

## Decisions (from the interview)

- `granite.session.login.password.enabled=false` is enforced in Session (password `login` and `change-password` refused) and hides the form and the menu item. Session and FEC read the same property (default `true`). `ACCESS_TOKEN` login is unaffected.
- Dialog: current password, new password, confirm. New must differ from current and be at least `granite.user.password.min-length` (default **8**) and at most 128 characters. A wrong current password counts toward the existing lockout (`granite.user.lockout.*`).
- Min length applies to every new password: Change Password, admin create, admin update. Stored passwords keep working. The bootstrap admin (`GRANITE_BOOTSTRAP_ADMIN_PASSWORD`) is exempt. The masked `********` value sent back by the Users grid is not a new password and is not checked.
- After a successful change the current session stays; every other open session of the user is closed with the new reason `PASSWORD_CHANGED`.
- Menu item "Change Password" is visible when password login is enabled and the user has a password (Okta-only users don't see it). It appears in AppHost and ClientHost (shared `UserMenu`).
- When password login is disabled, the admin Users grid still lets admins set passwords.

## Global Constraints

- Before any TypeScript build: `yarn run remote-registry-config` (registry `https://registry.npmjs.org/`); check with `yarn config get registry`. Never Verdaccio.
- Passwords, current or new, are never logged or included in any `toString`.
- Every refusal of change-password is generic to the browser: `success=false` with one of `password change is not available`, `invalid credentials` (wrong current, locked, unknown), or the policy message (`password must be at least N characters`, `new password must differ from the current one`).
- Gateway: Session Command `change-password` is `SCOPE_internal` only in every environment (only the FEC calls it, with its service token).
- Style: Granite conventions (Allman braces, `//////` header/footer, fluent setters, no explanatory comments, no static inner classes, Obsidian method order).
- Branch `feature/password-login-toggle` from `development`; commit after each task; build with the project's existing scripts only.

## Tasks

### Task 1: User password policy and `change-user-password`
- User.Service (Java + TS mirror): `ChangeUserPasswordRequest {userId, currentPassword, newPassword}` → `ChangeUserPasswordReply {success, failureMessage}`; `IUserCommandService.changeUserPassword`; `toString` omits both passwords.
- User.Server: `PasswordPolicy` (`granite.user.password.min-length` default 8, max 128) as a `@Bean` seam `passwordPolicy(Environment)` on `AbstractGraniteUserServerConfiguration`; applied in create, update (skips null/blank/`********`), and change; bootstrap exempt. `ChangeUserPasswordContext` pipeline: lookup → checkLockout → checkCredential (same dummy-hash timing as `authenticateUser`) → recordOutcome → policy → must differ → set hash.
- Controller `POST /user-command-service/change-user-password`; User.Client Java + TS method.
- Tests: wrong current counts toward lockout; locked account refuses even with correct current; short/too long/same password refused; admin create/update short password refused; masked value on update accepted; bootstrap admin with short password still created.

### Task 2: Session toggle enforcement and `change-password`
- Session reads `granite.session.login.password.enabled` (default `true`); password `login` refused with `invalid credentials`-style generic reply (`Password login is not available`, mirroring Okta) when disabled.
- Session.Service (Java + TS): `ChangePasswordRequest {sessionId, currentPassword, newPassword}` → `ChangePasswordReply {success, failureMessage}`; `SessionClosedReason.PASSWORD_CHANGED` (Java + TS).
- `SessionCommandService.changePassword`: toggle → open, unexpired session → User `changeUserPassword(session.userId, …)` → close the user's other open sessions (`PASSWORD_CHANGED`, events as for `ADMIN` closes). Controller `POST /session-command-service/change-password`; Session.Client Java + TS method.
- Tests: disabled → refused for login and change; closed session refused; success closes others and keeps current; User failure passes through its message.

### Task 3: Gateway rule
- `POST /session-command-service/change-password` → `SCOPE_internal` only in every environment. Test: admin user token 403, service token allowed.

### Task 4: FEC
- `SessionOutput` (Java + TS) gains `passwordSet`; `SessionStatusView` sets it from the found User (`password != null`) and also sets `passwordEnabled` from `SessionLoginSettings`.
- New input action `Session.Current->ChangePassword->…` on `handle-session-input` (new `SessionInput` fields `currentPassword`, `newPassword`): refuses with `not authenticated` without a verified caller session and with `password change is not available` when disabled; otherwise calls Session `change-password` with the caller's session id. ClientHost already reaches `handle-session-input`, so no confinement change.
- Tests: no caller → refused, no Session call; disabled → refused; success/failure messages relayed; passwords never logged.

### Task 5: Web
- `granite.web.authentication`: `ISessionService.changePassword(current, next)`; `AuthenticationContext` exposes `changePassword` and `state.passwordEnabled` / `state.passwordSet`.
- `ChangePasswordDialog` (`granite.web.authentication/Login`): three password fields, confirm must match, Save disabled until filled, server message shown inline, success notice then close.
- `UserMenu` shows "Change Password" when `passwordEnabled && passwordSet` and opens the dialog.
- Tests in `Granite.Web.AuthenticationTest`: menu item hidden/shown by the two flags; mismatch blocks submit; server failure shown.

### Task 6: Docs, build and images
- Update root `CLAUDE.md` (Web applications, UI building blocks), Session `Claude.md`, User `Claude.md`, FEC and Gateway `CLAUDE.md`.
- `yarn run remote-registry-config`; top-down Java and TS build of changed components (User, Session, FEC, Gateway, web authentication, AppHost, ClientHost), npmjs version checks before containerizing; build and push images for changed pods: user-commandhost, session-commandhost, frontendcontroller, gateway, web-apphost, web-clienthost.

## Review Focus

- The masked `********` round-trip from the Users grid must not trip the min-length check or overwrite the hash.
- A Change Password from a session closed elsewhere (between menu open and submit) returns a generic failure, not a stack trace.
- Lockout: repeated wrong current passwords lock the account for login too.
- Okta-only users never see the menu item even when password login is enabled.
- Password values never appear in FEC, Session or User logs (request logging filters, `toString`).
