# Granite.Web

## Overview

React 19 and TypeScript single-page app, built as yarn components published to the local Verdaccio (`Components/*`) and assembled by the Vite `Applications/Granite.Web.AppHost`. Tests are Vitest with jsdom and Testing Library.

## Commands (from `FrontEnds/Granite.Web`, or the root `build-granite-web`)

- `yarn run test-granite-web`: all test projects; `test-granite-web-authentication` and `test-granite-web-user` run one. Each publishes its component to the local Verdaccio, then runs Vitest in `Tests/Granite.Web.<Component>Test`.
- Test layout: `Tests/Granite.Web.<Component>Test/src/test/typescript/<Subject>Test.ts(x)` (private projects, never published, importing subpaths such as `granite.web.authentication/Authentication`). Export from the component's `index.ts` whatever a test needs. Header name is the test project, for example `Granite.Web.AuthenticationTest`.
- `yarn run build-granite-web-<component>` and `yarn run build`: type-check, bump the snapshot version in each component's `src/main/typescript/package.json`, and publish to the local Verdaccio only. `dist/**` under the AppHost is build output and is not committed.
- `containerize-*` and `push-*` publish images: never run them without approval.

## Style

- `//////` header with the component name and a `//////` footer in every source file; no explanatory comments.
- `Array<T>`, never `T[]`. Allman braces. Call arguments as `(a,b)` with no space after commas. Aligned imports and declarations.
- Each component's `src/main/typescript/package.json` declares the dependencies the published package needs; the component's `package.json` declares the dev and build ones. Keep both in step.

## Authentication (`Granite.Web.Authentication`)

The browser holds no tokens. It carries the HttpOnly cookies `GRANITE_SID` and `GRANITE_DID` (set by the Gateway and the FrontEndController) and the script-readable `XSRF-TOKEN`. The Gateway swaps the cookie for a JWT, so the SPA never sends `Authorization` and never uses `localStorage` or `sessionStorage` for session data.

- `Http/`: `CsrfFetch` adds `X-XSRF-TOKEN` (raw `XSRF-TOKEN` cookie) to POST, PUT, PATCH and DELETE and retries once after a 403 when the cookie changed. `SessionFetch` retries once after a 401 when its handler (the manager) says the session is valid again. The typed FEC client (`GraniteFrontEndControllerServiceClient` from `granite.frontendcontroller.client`, one client for the Users and Session operations) takes an optional fetch function, and Authentication passes these layers to them; they reject non-200/500 responses with a Strata `ServiceError`.
- `Authentication/`: `ISessionService` and `FrontEndSessionService` call `getSessionOutput` and `handleSessionInput` on a `GraniteFrontEndControllerServiceClient` (`/get-session-output`, `/handle-session-input`) with the flat `SessionInput` and `SessionOutput`, keyed by `Session.New->LoginView`, `Session.New->{Login,StartOktaLogin,CompleteOktaLogin}->LoginView`, `Session.Current->SessionView` and `Session.Current->Logout->LoginView`; no `userId` placeholder is sent. `AuthenticationManager` holds the state (`restoring`, `anonymous`, `authenticated`, `reauthenticating`; no tokens), exposes login, Okta start and completion (only a local `returnTo` is honoured), logout (navigates to `endSessionUrl`) and `handleUnauthorized`.
- 401 flow: the app clients (`useAuthentication().createFrontEndClient((url,fetchFunction) => new XClient(url,fetchFunction),service)`, a typed factory over the session fetch) report a 401 to `handleUnauthorized`, which asks `Session.Current->SessionView`. AUTHENTICATED (for example another tab logged in again): the request is retried once. Otherwise the status becomes `reauthenticating`, `ReauthenticationDialog` opens over the page (password form and/or Okta with the current path as `returnTo`) and the request is retried once after login. Cancelling fails the request and logs the SPA out locally. Concurrent 401s share one prompt. The session service itself uses plain `CsrfFetch`, so the start-up `SessionView` 401 is not a re-authentication event.
- `Login/`: `LoginForm` shows only the methods enabled in the `passwordEnabled` and `oktaEnabled` flags of `SessionOutput`; `LoginView`; `LoginCallbackView` (the `/login/callback` route, `code`, `state` and `error` from the query); `ReauthenticationDialog` (rendered by `AuthenticationProvider`).
- `createAuthentication(options)` wires the fetch layers, the session service and the manager; tests pass a fake FEC client, a fake `fetch`, a cookie reader and a navigator. `SessionOutput` carries no user id, so the Users screen finds the caller by `userName` among the loaded users.
- Runtime config is `window.__GRANITE_CONFIG__.apiBaseUrl` only (nginx `config.js`); there is no Google client id.

## Navigator and AppHost

`UsersView` creates `GraniteFrontEndControllerServiceClient` instances with `createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),"frontendcontroller-user-service")` and calls `getUsersOutput` and `handleUsersInput`. `HomeView` uses `FakeHomeFrontEndControllerServiceClient.getHomeOutput` (no server).  The AppHost routes `login` and `login/callback`; the Okta redirect URI configured in Session must be the SPA's `/login/callback`.
