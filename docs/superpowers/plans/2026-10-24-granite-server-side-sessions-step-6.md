# Granite Server-Side Sessions, Step 6: Web Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** The SPA holds no tokens. Session status, login (password and Okta), re-authentication and logout all go through the FrontEndController (FEC) client, and the browser carries only the HttpOnly `GRANITE_SID` and `GRANITE_DID` cookies plus the readable `XSRF-TOKEN`.

**Architecture:** `Granite.Web.Authentication` gets a shared fetch layer (`CsrfFetch`: `X-XSRF-TOKEN` on unsafe methods and one retry after a 403; `SessionFetch`: one retry after a 401 once the session is confirmed or re-established) and an FEC client subclass that sends every call through it. `AuthenticationManager` is rewritten on an `ISessionService` (the FEC `Session.*` inputs). `AuthenticationProvider` keeps its name, exposes `createFrontEndClient`, and renders a re-authentication dialog while the status is `reauthenticating`. The Navigator creates its clients from the context and no longer sets `Authorization`.

**Tech Stack:** React 19, TypeScript, Mantine, react-router-dom 7, Vitest with jsdom and Testing Library, Strata `AbstractRestClient` and `FrontEndControllerServiceClient`.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Decisions, Cookies, "What services and the SPA must do for continuity", "FrontEndController inputs and outputs", Step 6) and the Step 3 to 5 plans for the wire contract.

## Global Constraints

- TypeScript uses `Array<T>`, never `T[]`. No explanatory comments. Follow the existing Web style (`//////` header and footer, aligned imports, Allman braces, `(a,b)` call style).
- Build and test only with existing yarn scripts. No new third-party libraries, no build tooling changes. Never run `containerize-*`, `push-*`, jib or Gradle publish tasks.
- The SPA never reads, sends or stores a token. `GRANITE_SID` is HttpOnly and unreadable. The `localStorage` device key is gone (`GRANITE_DID` replaces it).
- CSRF: header `X-XSRF-TOKEN` carrying the raw `XSRF-TOKEN` cookie on POST, PUT, PATCH and DELETE; on 403, re-read the cookie and retry once. The first request, with no cookies, sends none.
- FEC keys: `Session.New->LoginView` (get-output, `userId` 0), `Session.New->Login->LoginView`, `Session.New->StartOktaLogin->LoginView`, `Session.New->CompleteOktaLogin->LoginView`, `Session.Current->SessionView` (get-output), `Session.Current->Logout->LoginView` (`logout:true`).
- Do not commit `FrontEnds/Granite.Web/Applications/Granite.Web.AppHost/dist/**`.

## Decisions taken in this step

| Topic | Decision |
|---|---|
| 401 flow | `GRANITE_SID` cannot be read, so after a 401 the manager asks `SessionView`. AUTHENTICATED (another tab re-logged in): retry the request once. Otherwise status becomes `reauthenticating`, the dialog opens, and the request is retried once after a successful sign-in. Cancelling the dialog fails the request and signs the SPA out locally. Concurrent 401s share one prompt. |
| Okta re-authentication | The dialog offers the enabled methods. Okta starts `StartOktaLogin` with `returnTo` = the current local path, then navigates to the returned URL. |
| Where the 401 hook lives | Only app clients (`createFrontEndClient`) use `SessionFetch`. The session service uses plain `CsrfFetch`, so `SessionView` 401s at start-up are not re-authentication events. |
| User id | `SessionOutput` carries no user id. The Users screen derives the caller from `state.userName` matched against the loaded users. `IAuthenticationState.userId` stays optional and unset. |
| Removed | `SessionApi`, `ISessionApi`, `TokenStorage`, `JwtClaims`, the refresh scheduler and timer, `GoogleSignInButton`, `googleClientId` (config, `config.js`, template, entrypoint `envsubst`, Dockerfile env), the `granite.session.*` and `granite.user.service` dependencies of Authentication. |
| Status values | `restoring`, `anonymous`, `authenticated`, `reauthenticating` (forms keep their own busy flag, so there is no `authenticating`). `restoring` is the initial status because the cookie is unreadable. |

## Review Focus

1. Concurrent 401s open one dialog and all retry after one sign-in. Test: `AuthenticationManager.test.ts` "shares one prompt between concurrent 401s".
2. Okta callback delivered twice (React StrictMode): the code is exchanged once. Test: `LoginCallbackView.test.tsx` "completes once under StrictMode".
3. Off-site `returnTo` from the server: ignored, `/` used. Test: `AuthenticationManager.test.ts` "ignores a non-local returnTo".
4. A failed logout keeps the signed-in state. Test: "keeps the session when logout fails".
5. Both methods disabled: a message, no form. Test: `SignInForm.test.tsx` "tells the user when no method is enabled".

---

### Task 1: Shared fetch layer and FEC client

**Files:** Create under `Components/Granite.Web.Authentication/src/main/typescript/Http/`: `Cookies.ts`, `CsrfFetch.ts`, `SessionFetch.ts`, `GraniteFrontEndControllerServiceClient.ts`, `index.ts`. Modify the component and `src/main/typescript/package.json` manifests (dependencies: add `granite.frontendcontroller.client|service`, `strata.frontendcontroller.client|service`; remove `granite.session.client|service`, `granite.user.service`). Tests: `src/test/typescript/CsrfFetch.test.ts`, `SessionFetch.test.ts`, `FrontEndClient.test.ts`.

**Interfaces:** Produces:
- `type FetchFunction = (input: RequestInfo | URL,init?: RequestInit) => Promise<Response>`
- `readCookie(name: string,source?: string): string | undefined` (raw value)
- `class CsrfFetch { constructor(fetchFunction?: FetchFunction,cookieReader?: (name: string) => string | undefined); readonly fetch: FetchFunction }`
- `type UnauthorizedHandler = () => Promise<boolean>`; `class SessionFetch { constructor(csrf: FetchFunction); setUnauthorizedHandler(handler: UnauthorizedHandler): void; readonly fetch: FetchFunction }`
- `class GraniteFrontEndControllerServiceClient extends FrontEndControllerServiceClient { constructor(baseUrl: string,fetchFunction: FetchFunction) }` overriding `doPost`.

- [x] **Step 1: Failing tests.**
  - `CsrfFetch`: POST with cookie sends `X-XSRF-TOKEN: <raw>`; GET sends none; POST with no cookie sends none; 403 then a new cookie value gives exactly one retry with the new header; 403 with an unchanged cookie is returned without retry; a second 403 is returned.
  - `SessionFetch`: 401 with handler returning true retries once; handler false returns the 401; no handler returns the 401; a second 401 after the retry is returned; 200 never calls the handler.
  - `FrontEndClient`: a `getOutput` posts JSON to `<base>/get-output` with `Content-Type: application/json`, no `Authorization` header and `X-XSRF-TOKEN` from the cookie; 200 and 500 resolve with the entity; 401 rejects with a `ServiceError` whose `getStatus()` is 401.
- [x] **Step 2: Run** `cd FrontEnds/Granite.Web && yarn run test-granite-web-authentication`, expect FAIL.
- [x] **Step 3: Implement** the files above. The client builds `Headers` synchronously from `getHeaderKeys()`/`getHeader()` before the first `await` (callers `close()` right after), and reuses Strata `StandardResponse` and `StandardResponseProcessor`.
- [x] **Step 4: Run, expect PASS.**
- [x] **Step 5: Commit** `feat(web): CSRF and 401-aware fetch layer for FrontEndController clients`.

### Task 2: Session service, manager, provider

**Files:** Create `Authentication/ISessionService.ts`, `Authentication/FrontEndSessionService.ts`, `Authentication/createAuthentication.ts`. Rewrite `AuthenticationManager.ts`, `AuthenticationContext.tsx`, `IAuthenticationState.ts`, `Authorities.ts`, `RequireAuthentication.tsx` (preserve `from` with search, keep children mounted while `reauthenticating`), `RuntimeConfig.ts` (drop `googleClientId`), `Authentication/index.ts`. Delete `SessionApi.ts`, `ISessionApi.ts`, `TokenStorage.ts`, `JwtClaims.ts` and their tests. Tests: `FrontEndSessionService.test.ts`, `AuthenticationManager.test.ts` (rewritten), `Authorities.test.ts` (updated), `NoTokenStorage.test.ts`, test support `TestSupport.ts` (fake FEC client scripted per key, fake `INavigator`, fake fetch).

**Interfaces:** Produces:
- `ISessionService { getLoginMethods(): Promise<ILoginMethods>; login(userName: string,password: string): Promise<ISessionStatus>; startOktaLogin(returnTo: string): Promise<string | undefined>; completeOktaLogin(code: string,state: string): Promise<ISessionStatus>; getCurrent(): Promise<ISessionStatus>; logout(): Promise<ILogoutResult> }`, `ILoginMethods {passwordEnabled,oktaEnabled}`, `ISessionStatus {authenticated: boolean; userName?: string; authorities: Array<string>; returnTo?: string}`, `ILogoutResult {loggedOut: boolean; endSessionUrl?: string}`.
- `FrontEndSessionService(createClient: () => IFrontEndControllerServiceClient)`.
- `INavigator { assign(url: string): void }`, `browserNavigator`.
- `AuthenticationManager(service: ISessionService,navigator?: INavigator)` with `getState`, `subscribe`, `restore()`, `getLoginMethods()`, `login()`, `startOktaLogin(returnTo)`, `completeOktaLogin(code,state): Promise<string>` (local `returnTo` or `/`), `logout(): Promise<boolean>`, `handleUnauthorized(): Promise<boolean>`, `cancelReauthentication()`.
- `createAuthentication(options?: {apiBaseUrl?,fetchFunction?,navigator?,service?}): IAuthentication {manager; createFrontEndClient(service: string): IFrontEndControllerServiceClient}`; wires `SessionFetch.setUnauthorizedHandler(() => manager.handleUnauthorized())`.
- `useAuthentication(): {state; login; logout; startOktaLogin; completeOktaLogin; getLoginMethods; cancelReauthentication; createFrontEndClient}`; `AuthenticationProvider({children,authentication?})`.

- [x] **Step 1: Failing tests.**
  - Service: each operation sends the exact key and fields (`userId:0`, `Login` with `userName`/`password`, `StartOktaLogin` with `returnTo`, `CompleteOktaLogin` with `code`/`state`, `Logout` with `logout:true`); a `SessionOutput` UNAUTHENTICATED start maps to `undefined`; `getCurrent` maps a 401 `ServiceError` to unauthenticated; `success:false` rejects with the failure message.
  - Manager: restore authenticated and anonymous; password login success and refusal (`error`, thrown); Okta start navigates to the redirect URL and a refusal throws; callback returns `returnTo`; "ignores a non-local returnTo" (`https://evil`, `//evil`, `/\evil`); logout clears state and navigates to `endSessionUrl`; "keeps the session when logout fails"; `handleUnauthorized`: SessionView AUTHENTICATED resolves true without a dialog; otherwise `reauthenticating` then a successful `login` resolves true; `cancelReauthentication` resolves false and goes anonymous; "shares one prompt between concurrent 401s"; anonymous caller gets false.
  - `NoTokenStorage`: after password login, restore, re-authentication and logout, `localStorage.length` and `sessionStorage.length` are 0 and no stored value contains the fake token strings the fake FEC would never send; no `granite.auth.deviceId` key.
- [x] **Step 2: Run, expect FAIL.**
- [x] **Step 3: Implement.** `restoring` is the initial status; `hasAuthority` accepts `authenticated` and `reauthenticating`.
- [x] **Step 4: Run, expect PASS.**
- [x] **Step 5: Commit** `feat(web): session-status manager and provider on the FEC SessionView`.

### Task 3: Login views and re-authentication dialog

**Files:** Create `Login/SignInForm.tsx`, `Login/LoginCallbackView.tsx`, `Login/ReauthenticationDialog.tsx`; rewrite `Login/LoginView.tsx`; delete `Login/GoogleSignInButton.tsx`; update `Login/index.ts`; the provider renders the dialog while `reauthenticating`. Tests: `SignInForm.test.tsx`, `LoginView.test.tsx`, `LoginCallbackView.test.tsx`, `Reauthentication.test.tsx`; `AuthenticationComponents.test.tsx` updated.

**Interfaces:** `SignInForm({returnTo: string,onSignedIn?: () => void})`; `LoginView()`; `LoginCallbackView()` (reads `code`, `state`, `error` from the query); `ReauthenticationDialog()`.

- [x] **Step 1: Failing tests.** Password only shows the form and no Okta button; Okta only shows the button and no form; "tells the user when no method is enabled"; both shows both; login success navigates to the protected route; refusal shows an alert; Okta button calls start with `returnTo` of the target and navigates; callback with `code` and `state` navigates to the returned local path; "completes once under StrictMode"; callback with `error` or without `code` shows an alert and never calls the service; re-authentication: an app client call returns 401 then 200, the dialog appears, signing in in the dialog completes the call (fetch called twice, no `Authorization`); cancel fails the call and redirects to login; Okta in the dialog uses the current path as `returnTo`.
- [x] **Step 2: Run, expect FAIL.**
- [x] **Step 3: Implement** with Mantine; the dialog is a non-dismissable-by-backdrop `Modal` with `transitionProps={{duration:0}}`.
- [x] **Step 4: Run, expect PASS.**
- [x] **Step 5: Commit** `feat(web): login view, Okta callback and re-authentication dialog`.

### Task 4: Navigator, AppHost, removals

**Files:** Modify `Navigator/.../User/UsersView.tsx` (clients from `createFrontEndClient("frontendcontroller-user-service")`, no `setAuthorization`, current user derived from `state.userName`), `Navigation/MenuBar.tsx` (logout navigates only on success), Navigator `package.json` files if a dependency becomes unused (none expected); AppHost `src/App.tsx` (route `login/callback`), `config.js.template`, `public/config.js`, `docker-entrypoint.sh`, `Dockerfile` (remove the Google entries).

- [x] **Step 1:** `yarn run build-granite-web` from the repo root builds Authentication, Navigator and AppHost (type-checks everything); fix errors until it passes.
- [x] **Step 2:** `yarn run test-granite-web-authentication` and the User component tests pass.
- [x] **Step 3: Commit** the Navigator and AppHost changes, then the snapshot version bumps and lockfiles separately (never `dist/**`).

### Task 5: Docs

**Files:** Modify `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (tick Step 6), `Microservices/Granite.FrontEndController/CLAUDE.md` Step 6 notes only if they reference removed behavior; add `FrontEnds/Granite.Web/CLAUDE.md` describing the Authentication architecture.

- [x] Commit `docs(web): document the cookie session client and tick Step 6`.

## Acceptance Criteria Map

| Criterion | Proving tests |
|---|---|
| No tokens in browser storage | `NoTokenStorage.test.ts` (password, restore, re-authentication, logout), `FrontEndClient.test.ts` (no `Authorization`) |
| Password, Okta callback, re-authentication, logout flows through a faked FEC client | `AuthenticationManager.test.ts`, `LoginView.test.tsx`, `LoginCallbackView.test.tsx`, `Reauthentication.test.tsx` |
| Disabled methods not shown | `SignInForm.test.tsx` |
| Users-screen tests pass | Granite.Web.User component tests (`UsersChangeSet`, `UsersValueEntries`) and a type-checked build of `UsersView` |
