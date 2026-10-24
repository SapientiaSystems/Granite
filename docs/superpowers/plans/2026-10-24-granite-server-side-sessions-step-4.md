# Granite Server-Side Sessions, Step 4: FEC Session Processor (Password) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make password login, session status and logout FrontEndController inputs: the FEC calls Session with concrete clients, sets and clears `GRANITE_SID` on the HTTP response, and never returns a token to the browser.

**Architecture:** A `SessionRequestProcessor` (Strata `DefaultRequestProcessor`) owns the four Session actions. Each host's request processor becomes a `PrefixRoutingRequestProcessor` (`Session.` goes to the Session processor, everything else to `UserRequestProcessor`), because Strata wires exactly one `IRequestProcessor` per host. A per-request `SessionRequestContext`, captured on the request thread when the processor is built, carries the inbound cookies (`GRANITE_DID`), the caller's session id (the `jti` of the bearer the Gateway injected) and a list of `Set-Cookie` values. A `ResponseCookieAdvice` (`ResponseBodyAdvice`) writes those values onto the HTTP response on the container thread, so no `HttpServletResponse` is touched from continuation threads. All Session and User clients are concrete instances built by one factory, never the request-scoped proxies (the Users Save bug).

**Tech Stack:** Java 23, Spring Boot 3.4 (MVC, `ResponseBodyAdvice`, `ResponseCookie`), Strata FrontEndController, Granite Session and User clients (RESTEasy), Jackson, JUnit 5, Mockito, MockMvc, the JDK `HttpServer`; TypeScript mirrors built through yarn.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Decisions, Cookies, FrontEndController inputs and outputs, Configuration, Step 4, Interactions with earlier work). Builds on the Step 1 to 3 plans.

## Global Constraints

- Java style per `Microservices/Granite.FrontEndController/CLAUDE.md`: `////` header and footer, `public` on its own line then `class X`, return type on its own line, Allman braces, fluent setters, `(a,b)` with no space after commas, column-aligned fields. No static inner classes, no explanatory comments. TS uses `Array<T>`.
- Never log or `toString()` the password, the session handle or any token.
- Keys exactly: `Session.New->LoginView` (get-output), `Session.New->Login->LoginView`, `Session.Current->SessionView` (get-output), `Session.Current->Logout->LoginView`. Cookie `GRANITE_SID=<handle>; HttpOnly; Secure; SameSite=Strict; Path=/`. Device cookie read from the `Cookie` header: `GRANITE_DID`.
- Properties: `granite.session.login.password.enabled` (default `true`), `granite.session.login.okta.enabled` (default `false`).
- Change only Granite.FrontEndController and docs. No new third-party libraries. No `jib`, `publish*`. No build tooling changes.
- Tests: `cd C:/Development/Granite && ./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test --rerun`. Every test class is tagged `CommitStage`. Existing FEC tests keep passing.

## Decisions taken in this step

| Topic | Decision |
|---|---|
| Strata limits | Replies' `cookies` never reach HTTP, and a host has one `IRequestProcessor`. Hence the request context, the advice, and the routing processor. |
| Request scope | The Save fix is not in the repository: `userRequestProcessor()` passes request-scoped client proxies. This step builds concrete clients for both processors through `GraniteRequestProcessorFactory`; the existing `userQueryServiceClient()`/`userCommandServiceClient()` beans stay and delegate to it. |
| Failures | Login failure, disabled password login, a blank user name or password and a missing `GRANITE_DID` all give `SessionOutput{status:"UNAUTHENTICATED", userName:null, authorities:[]}` and no cookie. The reply never says which. |
| `LogoutInput` | Spec says `{}`, but Jackson deduction needs a property no sibling has, so it carries `logout: boolean` (default `true`). `LogoutOutput` carries `loggedOut` as well as `endSessionUrl`, so deduction still works when `endSessionUrl` is null and omitted. |
| Logout | Session id is the `jti` of the forwarded bearer (unverified parse; Session validates the same bearer on the forwarded `logout` call). Cookie is cleared (`Max-Age=0`) when Session reports success or when there is no session id; on failure `loggedOut=false` and the cookie stays so the user can retry. `endSessionUrl` is `null` until Step 5. |
| `SessionView` | Session id from the bearer `jti`; session via Session query `findSessionsByKey(SessionKey.ofSessionId)`; `userName` via User query `findUsersByKey(UserKey.ofUserId)`; `authorities` from `SessionData`. Open and unexpired gives `AUTHENTICATED`, else `UNAUTHENTICATED`. |
| Cookie lifetime | `Max-Age` = seconds from now until `LoginReply.sessionData.expires` (minimum 1); no `Max-Age` (session cookie) when `expires` is absent. |
| Okta types | `StartOktaLoginInput{returnTo}`, `RedirectOutput{url}`, `CompleteOktaLoginInput{code,state}` are defined and registered (no behavior); their keys have no actions until Step 5. |
| Request logging | `CommonsRequestLoggingFilter` in both `HostConfiguration`s logged payloads and headers (passwords, `Cookie`, `Authorization`) at DEBUG. Payload and header logging are turned off. |
| Public get-output | Strata validates a non-null `userId` on `get-output`, so the SPA must send a placeholder `userId` (e.g. 0) for the public `Session.New->LoginView`. |

## Review Focus

1. Login with no `GRANITE_DID`: refused without calling Session. Test: `LoginSessionTest.missingDeviceCookieIsRefused`.
2. Password or handle in a log line or `toString`: none. Test: `SessionLeakTest.noSecretInToStringOrOutput`.
3. Continuation threads: a client call made off the request thread still forwards the bearer. Test: `GraniteRequestProcessorFactoryTest.clientsWorkOffTheRequestThread`.
4. Logout of a session Session can no longer close: cookie retained, `loggedOut=false`. Test: `LogoutSessionTest.failedLogoutKeepsCookie`.
5. Jackson deduction ambiguity among the new subtypes. Test: `FrontEndJacksonModuleTest` additions.

---

### Task 1: Input and output types, Jackson registration, TS mirrors

**Files:** Create in `Components/Granite.FrontEndController.Service/src/main/java/granite/frontendcontroller/service/requestreply/`: `LoginMethodsOutput`, `LoginInput`, `SessionOutput`, `LogoutInput`, `LogoutOutput`, `StartOktaLoginInput`, `RedirectOutput`, `CompleteOktaLoginInput`. Modify `FrontEndJacksonModule`. Create the TS mirrors under `src/main/typescript/RequestReply/` and export them from `index.ts`. Test: modify `Tests/.../ServerTest/.../configuration/FrontEndJacksonModuleTest.java`.

**Interfaces:** Produces (all extend `FrontEndInput`/`FrontEndOutput`, with narrowed fluent overrides of `setUserId/setSource/setCommand/setDestination`):
- `LoginMethodsOutput{passwordEnabled:boolean, oktaEnabled:boolean}`, `SessionOutput{userName:String, authorities:Set<String>, status:String}`, `LogoutOutput{loggedOut:boolean, endSessionUrl:String}`, `RedirectOutput{url:String}`.
- `LoginInput{userName,password:String}`, `LogoutInput{logout:boolean}`, `StartOktaLoginInput{returnTo:String}`, `CompleteOktaLoginInput{code,state:String}`.

- [ ] **Step 1: Failing tests** in `FrontEndJacksonModuleTest`: each input shaped JSON deserializes to its own class through `FrontEndInput` (login, logout with `logout:true`, start/complete Okta); each output round-trips through `FrontEndOutput` to its own class including `LogoutOutput` with `endSessionUrl` null and `SessionOutput` with empty authorities; `LoginInput.toString()` does not contain the password.
- [ ] **Step 2: Run, expect FAIL** (classes missing).
- [ ] **Step 3: Implement the eight classes**, register them in `registerSubtypes`, add the TS interfaces. `LoginInput` overrides `toString()` to omit the password.
- [ ] **Step 4: Run ServerTest, expect PASS.**
- [ ] **Step 5: Commit** `feat(frontendcontroller): session input and output types`.

### Task 2: Request context, response cookies and the advice

**Files:** Create in `Components/Granite.FrontEndController.Server/src/main/java/granite/frontendcontroller/server/platform/`: `SessionRequestContext`, `ResponseCookieAdvice`, `SessionCookies`. Tests: `ServerTest/.../platform/SessionRequestContextTest`, `ResponseCookieAdviceTest`.

**Interfaces:** Produces:
- `SessionRequestContext.fromCurrentRequest()`: captures cookies, the bearer's `jti`/`sub` and registers itself as request attribute `granite.session-request-context`; a detached empty context outside a web request. Methods `Optional<String> getCookie(String name)`, `Optional<Long> getSessionId()`, `Optional<Long> getCallerUserId()`, `void addSetCookie(String headerValue)`, `List<String> getSetCookies()` (thread-safe).
- `SessionCookies.issue(String handle,Instant expires,Instant now)` and `SessionCookies.clear()` return `Set-Cookie` header strings; constants `SESSION_COOKIE="GRANITE_SID"`, `DEVICE_COOKIE="GRANITE_DID"`.
- `ResponseCookieAdvice` (`@ControllerAdvice`, `ResponseBodyAdvice<Object>`): copies the context's values to `Set-Cookie` response headers and adds `Cache-Control: no-store` when there are any.

- [ ] **Step 1: Failing tests.** `SessionRequestContextTest`: reads `GRANITE_DID` among several cookies; parses `jti` and `sub` from a bearer; malformed or absent bearer gives empties; `issue` yields exactly `GRANITE_SID=<handle>; Max-Age=N; Expires=...; Path=/; Secure; HttpOnly; SameSite=Strict` attributes (assert each attribute present, `Max-Age` equal to remaining seconds, none when `expires` null, minimum 1); `clear` has `Max-Age=0`. `ResponseCookieAdviceTest` (MockMvc standalone, Strata's `AbstractFrontEndControllerServiceController` subclass over a stub service completing on another thread after adding a cookie to the context; `asyncDispatch`): response has the `Set-Cookie` header and `Cache-Control: no-store`; a request with no cookies has neither.
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement.** Bearer parsed by base64url-decoding the payload with Jackson; no signature check (documented in the design table). Cookies via `ResponseCookie`.
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** `feat(frontendcontroller): per-request session context and Set-Cookie advice`.

### Task 3: Session actions and processor

**Files:** Create in `Components/Granite.FrontEndController.Server/src/main/java/granite/frontendcontroller/server/control/session/`: `SessionLoginSettings`, `LoginMethodsView`, `LoginView`, `SessionStatusView`, `LogoutView`, `SessionRequestProcessor`. Tests in `ServerTest/.../control/session/`: `LoginMethodsViewTest`, `LoginSessionTest`, `SessionStatusViewTest`, `LogoutSessionTest`, `SessionLeakTest`, `SessionRequestProcessorTest`.

**Interfaces:** Consumes Task 1 and 2 types, `ISessionCommandServiceClient`, `ISessionQueryServiceClient`, `IUserQueryServiceClient`.
- `SessionLoginSettings(boolean passwordEnabled,boolean oktaEnabled)`, `static fromConfiguration(IConfiguration)`.
- `LoginMethodsView(SessionLoginSettings)` is an `IGetOutputAction`; `LoginView(ISessionCommandServiceClient,SessionRequestContext,SessionLoginSettings)`, `SessionStatusView(ISessionQueryServiceClient,IUserQueryServiceClient,SessionRequestContext)` (get-output) and `LogoutView(ISessionCommandServiceClient,SessionRequestContext)` are as named. Every output copies `source/command/destination/userId` from the request.
- `SessionRequestProcessor(commandClient,queryClient,userQueryClient,context,settings)` registers the four keys.

- [ ] **Step 1: Failing tests** (mocked clients, `SessionRequestContext` built by hand):
  - `LoginMethodsViewTest`: `passwordEnabled=false` and `oktaEnabled` flags pass through; `fromConfiguration` defaults are `true`/`false`.
  - `LoginSessionTest`: success sets exactly one `GRANITE_SID` cookie holding the handle with `Max-Age` from `expires`, `LoginRequest` has `credentialKind=PASSWORD` and `deviceId` from `GRANITE_DID`, output is `AUTHENTICATED` with user name and authorities, serialized output contains neither the handle nor `accessToken`/`refreshToken`; failed login, disabled password login (Session never called), blank fields, `missingDeviceCookieIsRefused` give `UNAUTHENTICATED` and no cookie.
  - `SessionStatusViewTest`: open session gives `AUTHENTICATED` with `userName`, `authorities`; closed, expired, missing session, no session id and missing user give `UNAUTHENTICATED`.
  - `LogoutSessionTest`: success calls `logout` with the `jti` session id and clears the cookie; no session id clears the cookie without calling Session; `failedLogoutKeepsCookie`.
  - `SessionLeakTest.noSecretInToStringOrOutput`: password, handle and tokens appear in no `toString` of `LoginInput`, `SessionOutput`, outputs.
  - `SessionRequestProcessorTest`: the four keys route; the Okta keys and unknown keys fail with `No action found for key`.
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement.** Pipelines use `thenApply`/`thenCompose`; no request-scoped proxies.
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** `feat(frontendcontroller): session login, status and logout actions`.

### Task 4: Wiring on both hosts

**Files:** Create `server/configuration/GraniteRequestProcessorFactory`, `server/control/PrefixRoutingRequestProcessor`. Modify `GraniteUserFrontEndControllerConfiguration`, `GraniteAdminFrontEndControllerConfiguration` (processors via the factory; `ResponseCookieAdvice` bean; existing client beans delegate to the factory), `Components/Granite.FrontEndController.Server/build.gradle` (add `api "granite.session:granite-session-client:$system_version"`), both hosts' `HostConfiguration` (request-logging filter) and all ten `application-*.properties` (`session-command-service.base-url=https://granite-session-commandhost:443`, `session-query-service.base-url=https://granite-session-queryhost:443`). Tests: `ServerTest/.../configuration/GraniteRequestProcessorFactoryTest`, `PrefixRoutingRequestProcessorTest`, `SessionHttpTest`; existing `GraniteUserFrontEndControllerConfigurationTest` unchanged.

**Interfaces:** `GraniteRequestProcessorFactory(IConfiguration,SslBundles)` with `IRequestProcessor create()` and `userQueryClient()`, `userCommandClient()`, `sessionCommandClient()`, `sessionQueryClient()` (each a new concrete client registering `ForwardedBearerClientRequestFilter.fromCurrentRequest()` and the `granite-services` bundle). `PrefixRoutingRequestProcessor(Map<String,IRequestProcessor> routes,IRequestProcessor fallback)` routes on the source prefix.

- [ ] **Step 1: Failing tests.** `PrefixRoutingRequestProcessorTest`: `Session.` source goes to the session delegate for both request kinds, others to the fallback. `GraniteRequestProcessorFactoryTest.clientsWorkOffTheRequestThread`: a fake HTTP server standing in for Session and User; processor built on a thread with a request carrying `Authorization` and `Cookie: GRANITE_DID=dev-1`, then a login and a session-view driven from a different thread; asserts the right paths (`/session-command-service/login`, `/session-query-service/find-sessions-by-key`, `/user-query-service/find-users-by-key`), the forwarded bearer, `deviceId`, and the captured `Set-Cookie`. `SessionHttpTest`: the Task 2 MockMvc rig with the real processor against the fake servers: the HTTP `handle-input` response for a good login carries `Set-Cookie: GRANITE_SID=...` with `HttpOnly`, `Secure`, `SameSite=Strict`, `Path=/` and a body without the handle or tokens; a failed login has no `Set-Cookie`; `Session.Current->Logout->LoginView` has `Max-Age=0`.
- [ ] **Step 2: Run, expect FAIL.**
- [ ] **Step 3: Implement.** Existing User actions are unchanged. Add the session base URLs to the property files; turn off payload and header logging.
- [ ] **Step 4: Run the full ServerTest, expect PASS** (all existing tests included).
- [ ] **Step 5: Commit** `feat(frontendcontroller): route Session inputs and use concrete clients on both hosts`.

### Task 5: Password switch, TS build, docs

**Files:** Modify `Components/Granite.FrontEndController.Client/src/main/typescript/RequestReply/FakeFrontEndControllerServiceClient.ts` (fake `Session.New` and `Session.Current` outputs), `Microservices/Granite.FrontEndController/CLAUDE.md`. Test additions in `LoginSessionTest` / `LoginMethodsViewTest` if not already covering the switch.

- [ ] **Step 1: Build TS.** From the repo root: `yarn run build-granite-session`, then `yarn run build-granite-frontendcontroller`. Expected: both finish and publish only to the local Verdaccio.
- [ ] **Step 2: Update CLAUDE.md** (session actions table, request context and advice, routing processor, config properties, new types, Step 6 notes).
- [ ] **Step 3: Run the full ServerTest once more**, expect PASS.
- [ ] **Step 4: Commit** the docs, fake client and snapshot version bumps.

## Acceptance Criteria Map

| Criterion | Proving tests |
|---|---|
| Password login sets `GRANITE_SID`, reply has no token | `LoginSessionTest` (success, no secrets), `SessionHttpTest` (header and body) |
| `SessionView` reflects the session | `SessionStatusViewTest` |
| Logout closes as LOGOUT and clears the cookie | `LogoutSessionTest`, `SessionHttpTest` (Session itself closes as `LOGOUT`: Session Step 2 tests) |
| Disabled password login refused and missing from `LoginMethodsOutput` | `LoginSessionTest`, `LoginMethodsViewTest` |
| `Set-Cookie` emitted on the HTTP response | `ResponseCookieAdviceTest`, `SessionHttpTest` |
