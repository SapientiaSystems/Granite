# Multiple Applications in One Browser Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Several Granite-derived applications run side by side in one browser, sharing Okta single sign-on, with separate sessions, users and authorities that cannot interfere with each other.

**Architecture:** Cookie names come from one per-application prefix and always carry the `__Host-` prefix; the Gateway, the FEC and the web app read the same names. Logout ends only the application's session; Granite never ends the Okta session. Production hosts refuse to start without an explicit JWT issuer and audience. Deployment rules and accepted limits are documented.

**Tech Stack:** Java 23, Spring Boot 3 / Spring Security 6 (servlet hosts, WebFlux Gateway), React 19 / TypeScript / Vitest, nginx AppHost, Kustomize.

**Spec:** `docs/design/2026-10-30-multi-application-browser-analysis.md` (R1, R2, R3, R5, R6; R4 replaced by application-only logout)

## Global Constraints

- Cookie prefix property: `granite.cookies.prefix`, default `GRANITE`, must match `[A-Z0-9_]+` (startup fails otherwise).
- Cookie names (prefix `P`): `__Host-P_SID`, `__Host-P_DID`, `__Host-P_LOGIN`, `__Host-P-XSRF-TOKEN`. Every one is set with `Secure`, `Path=/` and no `Domain`; existing `HttpOnly`/`SameSite`/`Max-Age` attributes are unchanged.
- Logout is application-level only: no Okta end-session call, no end-session URL anywhere, and no setting to enable one.
- JWT: in the `production` profile, `granite.security.jwt.issuer`, `granite.security.jwt.audience` (hosts and Gateway) and `granite.session.jwt.issuer`, `granite.session.jwt.audience` (Session) must be set explicitly; outside production the existing defaults (`granite-session`, `granite-services`) stay.
- Never log cookie values, handles, tokens or secrets.
- Build and test only with existing Gradle tasks (`--rerun`) and yarn scripts; no daemon start/stop, no tool-config changes, no `publish*`, jib, `containerize-*`, `push-*` or deploy scripts. Never `--dry-run` across included builds.
- Style: `//////` headers/footers, Granite formatting (see each service's CLAUDE.md), no explanatory comments, no static inner classes; TypeScript `Array<T>`.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; explicit paths only; never `.idea/**`, `dist/**`, `.gradle`, package.json snapshot bumps.

## Review Focus

1. Gateway and FEC disagree on a name (for example the FEC sets `__Host-GRANITE_SID` but the Gateway reads `GRANITE_SID`): every login silently fails. Covered by Task 2 and Task 3 tests that use `GraniteCookieNames` rather than literals, plus the Task 3 MVC test reading the `Set-Cookie` header.
2. A cookie built without `Path=/` or with a `Domain` is dropped by the browser because of `__Host-`: login works in tests but not in browsers. Task 1 test `everyCookieMeetsHostPrefixRules` builds each cookie and asserts `Secure`, `Path=/`, no `Domain`.
3. The web app reads the wrong CSRF cookie name, so every POST gets 403. Task 4 test `readsPrefixedCsrfCookie` and `defaultsToGranitePrefix`.
4. Logging out and immediately choosing Okta logs the user straight back in, because the Okta session is still alive. This is the intended behaviour; Task 5 test `oktaLogoutClosesOnlyApplicationSession` and the docs state it.
5. A production host started without explicit issuer/audience falls back to Granite's defaults and accepts another application's tokens. Task 6 test `productionRequiresExplicitIssuerAndAudience`.

---

### Task 1: Foundation cookie names

**Files:**
- Create: `Shared/Granite.Foundation/Components/Granite.Foundation.Core/src/main/java/granite/foundation/core/security/GraniteCookieNames.java`
- Test: `Shared/Granite.Foundation/Tests/Granite.Foundation.CoreTest/src/test/java/granite/foundation/core/security/GraniteCookieNamesTest.java`

**Interfaces:**
- Produces: `public final class GraniteCookieNames` with `public static final String PREFIX = "granite.cookies.prefix"`, `public static final String DEFAULT_PREFIX = "GRANITE"`, constructor `GraniteCookieNames(String prefix)` (throws `IllegalArgumentException` unless `[A-Z0-9_]+`), `static GraniteCookieNames from(PropertyResolver properties)`, getters `getSession()`, `getDevice()`, `getLogin()`, `getCsrf()`, and `ResponseCookie.ResponseCookieBuilder hostCookie(String name,String value)` returning a builder with `secure(true)` and `path("/")` and no domain.

- [ ] **Step 1: Write the failing tests**: `defaultNames` (`__Host-GRANITE_SID`, `__Host-GRANITE_DID`, `__Host-GRANITE_LOGIN`, `__Host-GRANITE-XSRF-TOKEN`), `customPrefix` (`POPLAR` → `__Host-POPLAR_SID` …), `rejectsInvalidPrefix` (`poplar`, `PO-PLAR`, blank → `IllegalArgumentException`), `fromProperties` (`MockEnvironment` with and without the property), `everyCookieMeetsHostPrefixRules` (for each name, `hostCookie(name,"v").build().toString()` contains `Secure` and `Path=/` and not `Domain=`).
- [ ] **Step 2: Run** `./gradlew :Granite.Foundation:Tests:Granite.Foundation.CoreTest:test --rerun`; expected: compile failure (class missing).
- [ ] **Step 3: Implement `GraniteCookieNames`.**
- [ ] **Step 4: Run the same command**; expected: pass.
- [ ] **Step 5: Commit** `feat(foundation): per-application __Host- cookie names`.

### Task 2: Gateway uses the configured names

**Files:** `Microservices/Granite.Gateway/Components/Granite.Gateway.Server/src/main/java/granite/gateway/server/security/{SessionCookieWebFilter,DeviceCookieWebFilter}.java`, `.../configuration/GraniteGatewayServerConfiguration.java` (`createCsrfTokenRepository`, filter construction), Gateway tests under `Tests/Granite.Gateway.ServerTest` that use `GRANITE_SID`, `GRANITE_DID` or `XSRF-TOKEN` literals, Gateway `CLAUDE.md`.

**Interfaces:**
- Consumes: `GraniteCookieNames` (Task 1).
- Produces: a `@Bean GraniteCookieNames cookieNames(Environment)` in `GraniteGatewayServerConfiguration` (overridable); `SessionCookieWebFilter(String cookieName,...)` and `DeviceCookieWebFilter(String cookieName,Duration maxAge)`; the public `SESSION_COOKIE`/`DEVICE_COOKIE` constants are removed; the CSRF repository uses `setCookieName(names.getCsrf())`, path `/`, `Secure`, `SameSite=Strict`, script-readable.

- [ ] **Step 1: Update the tests first**: replace literals with `new GraniteCookieNames("GRANITE")` getters; add `GatewaySessionCookieTest.customPrefixReadsPrefixedSessionCookie` (`granite.cookies.prefix=POPLAR`: a request with `__Host-POPLAR_SID` resolves, one with `__Host-GRANITE_SID` does not), `deviceCookieIsHostPrefixed` (new device cookie `Set-Cookie` starts with `__Host-GRANITE_DID=`, has `Secure`, `Path=/`, no `Domain`), `csrfCookieIsHostPrefixed` (same for `__Host-GRANITE-XSRF-TOKEN`).
- [ ] **Step 2: Run** `./gradlew :Granite.Gateway:Tests:Granite.Gateway.ServerTest:test --rerun`; expected: the new tests fail.
- [ ] **Step 3: Implement**; the cookie-stripping before forwarding (`CookieHeaders.strip`) uses the configured name.
- [ ] **Step 4: Run** Gateway ServerTest and `./gradlew :Granite.Gateway:Applications:Granite.Gateway.ServiceHost:compileJava --rerun`; expected: pass.
- [ ] **Step 5: Commit** `feat(gateway): configurable __Host- cookie names`.

### Task 3: FEC uses the configured names

**Files:** `Microservices/Granite.FrontEndController/Components/Granite.FrontEndController.Server/src/main/java/granite/frontendcontroller/server/platform/SessionCookies.java`, `.../control/session/{LoginView,StartOktaLoginView,CompleteOktaLoginView,LogoutView}.java` and any other `SessionCookies` user, `GraniteFrontEndControllerContext`, `AbstractGraniteFrontEndControllerConfiguration`, FEC tests using the literals, FEC `CLAUDE.md` and its identical `.github/copilot-instructions.md`.

**Interfaces:**
- Consumes: `GraniteCookieNames` (Task 1).
- Produces: `SessionCookies` becomes an instance class `SessionCookies(GraniteCookieNames names)` with `issue(handle,expires,now)`, `clear()`, `issueLogin(transactionId)`, `clearLogin()`, `getSessionName()`, `getDeviceName()`, `getLoginName()`; the static constants are removed. `GraniteFrontEndControllerContext` carries a `SessionCookies`; `AbstractGraniteFrontEndControllerConfiguration` has a protected overridable `cookieNames()` returning `GraniteCookieNames.from(environment)` and passes it into the context.

- [ ] **Step 1: Update the tests first**: literals replaced by `GraniteCookieNames` getters; add `SessionCookiesTest.prefixedNamesAndHostRules` (`POPLAR`: `issue` → `__Host-POPLAR_SID=…; Path=/; Secure; HttpOnly; SameSite=Strict`, no `Domain`; `issueLogin` → `__Host-POPLAR_LOGIN`, `SameSite=Lax`) and an MVC/`SessionHttpTest` case where a password login with `__Host-GRANITE_DID` returns `Set-Cookie: __Host-GRANITE_SID=` and a request carrying only `GRANITE_DID` (old name) returns `UNAUTHENTICATED` with no cookie.
- [ ] **Step 2: Run** `./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test --rerun`; expected: the new tests fail.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** FEC ServerTest, `./gradlew :Granite.FrontEndController:Applications:Granite.FrontEndController.UserHost:test --rerun` and AdminHost `compileJava --rerun`; expected: pass.
- [ ] **Step 5: Commit** `feat(frontendcontroller): configurable __Host- cookie names`.

### Task 4: Web app reads the configured CSRF cookie

**Files:** `FrontEnds/Granite.Web/Components/Granite.Web.Authentication/src/main/typescript/{Authentication/RuntimeConfig.ts,Authentication/createAuthentication.ts,Http/CsrfFetch.ts,Http/index.ts}`, `FrontEnds/Granite.Web/Applications/Granite.Web.AppHost/{config.js.template,docker-entrypoint.sh,Dockerfile,public/config.js}`, `Shared/Granite.Kubernetes/base/granite-system.yml` (web AppHost `COOKIE_PREFIX` env, value `GRANITE`), `FrontEnds/Granite.Web/Tests/Granite.Web.AuthenticationTest/src/test/typescript/CsrfFetchTest.ts`, Web `CLAUDE.md`.

**Interfaces:**
- Produces: `IRuntimeConfig.cookiePrefix: string` (default `"GRANITE"`, trimmed; invalid values fall back to the default); `csrfCookieName(prefix: string): string` returns `` `__Host-${prefix}-XSRF-TOKEN` ``; `CSRF_COOKIE` is removed from the exports; `createAuthentication` options gain `cookiePrefix?: string`; `CsrfFetch` takes the cookie name. `config.js.template` adds `cookiePrefix: "${COOKIE_PREFIX}"`; `docker-entrypoint.sh` substitutes `${COOKIE_PREFIX}`; `Dockerfile` sets `ENV COOKIE_PREFIX=GRANITE`; `public/config.js` sets `cookiePrefix: "GRANITE"`.

- [ ] **Step 1: Write the failing tests**: `readsPrefixedCsrfCookie` (prefix `POPLAR`: the header value comes from `__Host-POPLAR-XSRF-TOKEN`, a plain `XSRF-TOKEN` is ignored), `defaultsToGranitePrefix` (no `cookiePrefix` → `__Host-GRANITE-XSRF-TOKEN`), `invalidPrefixFallsBack` (`"po-plar"` → `GRANITE`).
- [ ] **Step 2: Run** `yarn run test-granite-web-authentication` (from `FrontEnds/Granite.Web`); expected: the new tests fail.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `yarn run test-granite-web` and the root `yarn run build-granite-web`; expected: pass. Restore any `package.json` rewritten by the build.
- [ ] **Step 5: Commit** `feat(web): configurable CSRF cookie name`.

### Task 5: Logout ends only the application session

There is no global (Okta) logout and no setting for it. Logout closes the application's session and clears its cookie; the Okta session is never ended by an application.

**Files:**
- Session Server: `application/{IOktaClient,OktaClient,OktaSettings,LogoutContext,SessionCommandService}.java`, `configuration/GraniteSessionCommandServerConfiguration.java`.
- Session Service: `requestreply/LogoutReply.java` and `src/main/typescript/RequestReply/LogoutReply.ts`.
- Session CommandHost `application-{local,development,test,production}.properties` (remove `granite.session.okta.post-logout-redirect-uri`).
- FEC: `Granite.FrontEndController.Service` `SessionOutput.java` and `SessionOutput.ts`; Server `control/session/LogoutView.java`.
- Web: `Granite.Web.Authentication` `Authentication/{ISessionService,FrontEndSessionService,AuthenticationManager}.ts`.
- Tests: Session ServerTest (`LogoutTest`, `OktaClientTest`, `SessionCommandServiceServerTest`, `GraniteSessionCommandServerConfigurationTest`), Session ServiceTest (`OktaRequestReplyTest`), FEC ServerTest (`LogoutSessionTest`, `FrontEndJsonSerializationTest`), Web AuthenticationTest (`FrontEndSessionServiceTest`, `TestSupport`).
- Docs: Session `Claude.md`, FEC `CLAUDE.md` (and identical copilot copy), Web `CLAUDE.md`.

**Interfaces:**
- Removed: `IOktaClient.buildEndSessionUrl`, `OktaSettings.postLogoutRedirectUri` and its property, `LogoutContext.endSessionUrl`, `LogoutReply.endSessionUrl` (Java and TS), `SessionOutput.endSessionUrl` (Java and TS), `ILogoutResult.endSessionUrl` and the web navigation to it.
- Removed: the session's stored Okta ID token (`Session.oktaIdToken`, set at Okta login completion), which existed only to build the end-session URL. The column is no longer mapped; `ddl-auto` leaves the existing column in place.
- Unchanged: the logout path in the FEC (`Session.Current->Logout->LoginView` closes the session, clears `__Host-<PREFIX>_SID`, `loggedOut=true`) and the web app navigating to the login page after logout.

- [ ] **Step 1: Update the tests first**: `oktaLogoutClosesOnlyApplicationSession` (Session: Okta session logout succeeds, session closed, the reply has no end-session field, the Okta client is never called); FEC `LogoutSessionTest` asserts the output has no `endSessionUrl` and JSON round trip has no such property; Web `logoutNavigatesToLoginPage` (an Okta-logged-in user's logout never calls `navigator.assign` with an external URL). Remove the tests of the deleted methods.
- [ ] **Step 2: Run** Session ServerTest and ServiceTest, FEC ServerTest (`--rerun`) and `yarn run test-granite-web-authentication`; expected: compile or assertion failures until the code is removed.
- [ ] **Step 3: Implement** the removals.
- [ ] **Step 4: Run** the same suites plus Session CommandHost test, FEC UserHost test and `yarn run build-granite-web`; expected: pass. `grep -rn "endSessionUrl\|buildEndSessionUrl\|post-logout-redirect-uri\|oktaIdToken"` outside `docs/` finds nothing.
- [ ] **Step 5: Commit** `feat(session)!: logout ends only the application session`.

### Task 6: Explicit JWT issuer and audience in production

**Files:** `Shared/Granite.Foundation/Components/Granite.Foundation.Core/src/main/java/granite/foundation/core/security/GraniteResourceServerSettings.java`, Session `JwtTokenService` settings binding (`GraniteSessionCommandServerConfiguration`), every `application-production.properties` of User, Session, FEC, Outbox and Gateway hosts (set the values explicitly to Granite's current `granite-session` / `granite-services`), tests in Granite.Foundation.CoreTest and Session ServerTest, root `CLAUDE.md`.

**Interfaces:**
- Produces: `GraniteResourceServerSettings.from(Environment)` throws `IllegalStateException("granite.security.jwt.issuer and granite.security.jwt.audience must be set in production")` when the `production` profile is active and either property is absent; the existing `from(PropertyResolver)` keeps its defaults. Session's token settings apply the same rule to `granite.session.jwt.issuer` / `.audience`.

- [ ] **Step 1: Write the failing tests**: `productionRequiresExplicitIssuerAndAudience` (`MockEnvironment` with profile `production`, each property missing in turn → `IllegalStateException`), `productionAcceptsExplicitValues`, `nonProductionKeepsDefaults`; Session equivalents for `granite.session.jwt.*`.
- [ ] **Step 2: Run** Foundation CoreTest and Session ServerTest (`--rerun`); expected: the new tests fail.
- [ ] **Step 3: Implement** and set the values in every production properties file (check each with `grep -L "jwt.issuer"`).
- [ ] **Step 4: Run** Foundation CoreTest, Session ServerTest, User ServerTest, FEC ServerTest, Gateway ServerTest, Outbox ServerTest; expected: pass.
- [ ] **Step 5: Commit** `feat(security): explicit JWT issuer and audience in production`.

### Task 7: Deployment rules, accepted limits and verification

**Files:** root `CLAUDE.md` (new section "Several applications in one browser": R1 one hostname per application including local dev, the cookie names and `granite.cookies.prefix`, why `__Host-`, logout is application-level only (logging out never ends the Okta session, so a following Okta login is silent), issuer/audience per application with Poplar example `poplar-session` / `poplar-services`, R6 accepted limit), Gateway, FEC and Session CLAUDE.md files (any remaining mentions of `GRANITE_SID`, `GRANITE_DID`, `GRANITE_LOGIN`, `XSRF-TOKEN`), Web `CLAUDE.md`, the design doc status line.

- [ ] **Step 1: Update the docs**; `grep -rn "GRANITE_SID\|GRANITE_DID\|GRANITE_LOGIN\|XSRF-TOKEN" --include=*.md` shows only the prefixed names (or the design doc's history).
- [ ] **Step 2: Run** every suite: Foundation CoreTest, User ServerTest + hosts, Session ServerTest + hosts, FEC ServerTest + UserHost test + AdminHost compile, Gateway ServerTest + ServiceHost compile, Outbox ServerTest, root `yarn run test-granite`; record the counts.
- [ ] **Step 3: Commit** `docs: several applications in one browser`; hand over for E2E: log in to Granite and Poplar in the same browser (password and Okta), confirm separate sessions, one Okta prompt, logout of one leaves the other logged in, and the dev tools show only `__Host-` cookies.
