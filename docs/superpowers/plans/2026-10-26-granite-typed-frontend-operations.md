# Granite: migrate to Strata.FrontEndController typed operations

**Date:** 2026-10-26
**Branch:** `feature/typed-frontend-operations` (from `development`)
**Strata design:** `StrataFrameworkSet/Strata.FrontEndController/docs/superpowers/specs/2026-10-26-typed-frontend-operations-design.md`

The context is Granite's FrontEndController, Gateway and Web, which are built on Strata's shared `handle-input`/`get-output` endpoints, `IRequestProcessor` routing and `DEDUCTION`. Strata has replaced these with typed, per-subject operations. Strata now provides an abstract `FrontEndControllerService` pipeline, an abstract controller and abstract clients; it no longer ships the User/Admin configurations or the concrete clients. My goal is to move Granite onto the new Strata version without changing user-visible behaviour.

## Decisions (made by the user)

| # | Decision |
|---|---|
| 1 | **Session** is one operation pair: `getSessionOutput` / `handleSessionInput`. The service dispatches on the key. |
| 2 | **Users** is `getUsersOutput` / `handleUsersInput`. Save and Delete stay as the `command` and the service dispatches on it. |
| 3 | **Both hosts serve both subjects.** UserHost serves `/frontendcontroller-user-service` (8081) and AdminHost serves `/frontendcontroller-admin-service` (8082), as today. |
| 4 | **Home** stays TS-only, as a typed fake client (`getHomeOutput`). |
| 5 | **Gateway:** keep the body-key filter and the Public/CurrentSession matchers, but only on the two session paths of each host. The Users paths become plain path rules. |
| 6 | **Session payloads are flat:** `SessionInput {userName, password (write-only), returnTo, code, state}`; `SessionOutput {status, userName, authorities, returnTo, passwordEnabled, oktaEnabled, redirectUrl, loggedOut, endSessionUrl}`. |

## Contract

| Subject | Operation | Path (under each host's service path) | Request / Reply | Keys handled |
|---|---|---|---|---|
| Users | `getUsersOutput` | `POST /get-users-output` | `GetUsersOutputRequest` / `GetUsersOutputReply` (`UsersOutput`) | `User.All->UsersEditableView` |
| Users | `handleUsersInput` | `POST /handle-users-input` | `HandleUsersInputRequest` (`UsersInput`) / `HandleUsersInputReply` | `User.All->Save->UsersEditableView`, `User.All->Delete->UsersEditableView` |
| Session | `getSessionOutput` | `POST /get-session-output` | `GetSessionOutputRequest` / `GetSessionOutputReply` (`SessionOutput`) | `Session.New->LoginView`, `Session.Current->SessionView` |
| Session | `handleSessionInput` | `POST /handle-session-input` | `HandleSessionInputRequest` (`SessionInput`) / `HandleSessionInputReply` | `Session.New->{Login,StartOktaLogin,CompleteOktaLogin}->LoginView`, `Session.Current->Logout->LoginView` |
| Home (TS fake) | `getHomeOutput` | — | `GetHomeOutputRequest` / `GetHomeOutputReply` (`HomeOutput`) | `Home->HomeView` |

- An unknown key fails the reply with `IllegalArgumentException("No action found for key: …")`, as today.
- `userId` is no longer required, so the SPA stops sending the `userId: 0` placeholder.
- The refused Okta start becomes `SessionOutput` with status `UNAUTHENTICATED` and no `redirectUrl`.

## Steps

The agent does the steps in order. Steps 2, 4 and 5–6 depend only on step 1, so they can run in parallel.

### 1. Service contract (Java + TS): `Granite.FrontEndController.Service`

- Delete `LoginInput`, `LogoutInput`, `StartOktaLoginInput`, `CompleteOktaLoginInput`, `LoginMethodsOutput`, `LogoutOutput`, `RedirectOutput` and `FrontEndJacksonModule`, in both Java and TS.
- Add `SessionInput` and the flat `SessionOutput`. Keep `UsersInput` and `UsersOutput`.
- Add the request/reply pairs and the `IUsersFrontEndControllerService` and `ISessionFrontEndControllerService` interfaces, in Java and TS. Add the TS-only Home types and `IHomeFrontEndControllerService`.
- **Acceptance:** the Service module compiles, and a JSON round-trip test binds the concrete input and output with no type property. `password` is not serialized.

### 2. Server: `Granite.FrontEndController.Server`

- Add `UsersFrontEndControllerService` and `SessionFrontEndControllerService`. Each extends Strata `FrontEndControllerService` and dispatches by key to its existing action classes. The action classes are adapted to Granite-local typed action interfaces; their logic is unchanged.
- Add `UsersFrontEndControllerServiceController` and `SessionFrontEndControllerServiceController`, mapped at `${granite.frontendcontroller.service-path}`.
- Replace `GraniteUser/AdminFrontEndControllerConfiguration` with one `GraniteFrontEndControllerConfiguration`, which provides the executor, clients, request-scoped services and controllers, and `ResponseCookieAdvice`. `GraniteRequestProcessorFactory` becomes a factory for the services.
- Delete `PrefixRoutingRequestProcessor`, `UserRequestProcessor` and `SessionRequestProcessor`.
- Port the tests, and add a mapping-completeness test per subject that extends Strata `AbstractControllerMappingTest`.
- **Acceptance:** `Granite.FrontEndController.ServerTest` passes, including the cookie, MockMvc and session-leak tests on the new paths.

### 3. Hosts: UserHost and AdminHost

- Both `HostConfiguration`s import `GraniteFrontEndControllerConfiguration` and stop registering `FrontEndJacksonModule`.
- Set `granite.frontendcontroller.service-path` in each host's `application.properties`.
- **Acceptance:** both hosts compile, and their OpenAPI groups still cover their service path.

### 4. Gateway

- `FrontEndInputKeyWebFilter.PATHS` becomes the 4 session paths: `get-session-output` and `handle-session-input` on each host.
- The Public and CurrentSession matchers match the same keys on those paths.
- Role rules become `POST …/get-users-output` for ADMIN, AUDITOR and `SCOPE_internal`. `handle-users-input` falls through to the ADMIN catch-all.
- Update the tests.
- **Acceptance:** `Granite.Gateway.ServerTest` passes.

### 5. TS client: `Granite.FrontEndController.Client`

- Add `SessionFrontEndControllerServiceClient` and `UsersFrontEndControllerServiceClient`. They extend Strata `AbstractFrontEndControllerServiceClient` through a Granite base that accepts an optional injected `fetch`. That is the doPost override that lives in Web today.
- Replace `FakeFrontEndControllerServiceClient` with `FakeHomeFrontEndControllerServiceClient`.
- **Acceptance:** the package builds.

### 6. Web

- `FrontEndSessionService` uses the session client.
- `createAuthentication` builds typed clients over the CSRF and session fetches. `createFrontEndClient` takes a client factory.
- Update `UsersView` and `HomeView`. Delete `GraniteFrontEndControllerServiceClient`.
- Update the tests.
- **Acceptance:** the Web vitest suites pass and the AppHost builds.

### 7. Docs

- Update the `CLAUDE.md` and `.github/copilot-instructions.md` files for FrontEndController, Gateway and Web to describe the typed operations.

### 8. Full verification

- Run the root Gradle tests for FrontEndController, Gateway, Session and User, and the root yarn builds for the FEC packages and Granite.Web.
- **Acceptance:** everything is green. Then notify the user for E2E. Do not merge.

## Constraints

- Build only through the existing root scripts and tasks. No jib, `publish*` or `publishToMavenLocal`. Don't start or stop daemons, and don't change tooling.
- Never log handles, tokens, Okta codes or PKCE verifiers. `SessionInput.password` stays write-only.
- Follow the project formatting conventions. Don't add explanatory comments or static inner classes.
