# Granite Server-Side Sessions, Step 3: Gateway Cookie-to-Token Filter Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a browser that holds only `GRANITE_SID` and `GRANITE_DID` reach the FEC through the Gateway: the Gateway swaps the handle for the session's JWT (via Session `resolveSession`), protects cookie requests with a double-submit CSRF token, keeps the login inputs public, and exposes only the FEC routes in production.

**Architecture:** All work is in Granite.Gateway.Server. New reactive `WebFilter`s are added *inside* the existing `SecurityWebFilterChain` (never as Spring beans, so they cannot run outside the chain): device cookie, FEC-input classification, CSRF cookie, Spring's `CsrfWebFilter`, then the session-cookie filter just before `SecurityWebFiltersOrder.AUTHENTICATION`. The session-cookie filter injects `Authorization: Bearer <jwt>`, so the existing bearer authentication, `jti` revocation check and role rules run unchanged on the injected token. Session is called with a `WebClient`, the same mechanism the `jti` check uses.

**Tech Stack:** Java 23, Spring Boot 3.4, Spring Security 6.4 (reactive), Spring Cloud Gateway 4.2, Jackson, Nimbus JOSE (already on the classpath), JUnit 5, `WebTestClient`, the JDK `HttpServer` test backend.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Decisions, Cookies, `resolveSession` contract, Configuration, Step 3, Open item 2). Builds on the Step 1 and Step 2 plans.

## Global Constraints

- Java style per `Microservices/Granite.Gateway/CLAUDE.md`: `////` header and footer, `public` on its own line then `class X`, return type on its own line, Allman braces, fluent setters, `(a,b)` with no space after commas, column-aligned fields.
- No static inner classes (records included), no explanatory comments.
- Never log or `toString()` the handle, an access token, the service token or the CSRF token.
- Authorization rules stay in the Gateway chain. Change only Granite.Gateway and docs. No new third-party libraries, no build-tool changes.
- Never run `jib`, `publish*`, `publishToMavenLocal`.
- Tests run through the root composite build: `cd C:/Development/Granite && ./gradlew :Granite.Gateway:Tests:Granite.Gateway.ServerTest:test --rerun`. Every test class is tagged `CommitStage`.
- The existing tests (`GatewayDefaultSecurityTest`, `GatewayRevocationTest`, `GatewayRoleRulesTest`, `GatewayRoleRulesDisabledTest`, `GatewayAuthorizationTest`, `GraniteGatewayServerConfigurationTest`, `ExtendedGatewayServerConfigurationTest`; 31 tests) keep their assertions unchanged. `GatewayTestBackend` may gain helpers.
- Cookie names exactly: `GRANITE_SID`, `GRANITE_DID`, `XSRF-TOKEN`; CSRF header exactly `X-XSRF-TOKEN`.
- `granite.gateway.session-cache-ttl` default `PT60S`. Cache is TTL-only; close events do not evict (Open item 2).
- Every resolution failure (Session `success=false`, missing token, HTTP error, timeout, missing service token) is a 401 and is not cached.

## Decisions taken in this step

| Topic | Decision |
|---|---|
| Filter order (inside the security chain) | `DeviceCookieWebFilter` at `FIRST` → `FrontEndInputKeyWebFilter` before `CSRF` → `CsrfCookieWebFilter` before `CSRF` → Spring `CsrfWebFilter` at `CSRF` → `SessionCookieWebFilter` before `AUTHENTICATION` → existing bearer authentication (JWT + `jti`) → authorization (public matcher, role rules, `authenticated()`). CSRF is checked before Session is called. |
| Bearer vs cookie | **A request with an `Authorization` header takes the bearer path, exactly as today.** The cookie is never resolved, but `GRANITE_SID` is still stripped from the forwarded `Cookie` header. No `GRANITE_DID` or CSRF cookie is issued and no CSRF check applies. Safe because a cross-site page cannot attach an `Authorization` header without a CORS preflight, and the cookie confers no authority on that request. |
| Public requests carrying `GRANITE_SID` | Not resolved (a stale or closed handle must not block re-login); `GRANITE_SID` is stripped; the request proceeds anonymously. |
| Cache | `ConcurrentHashMap` keyed by SHA-256 hex of the handle, value = token + `cacheUntil = min(now + session-cache-ttl, token exp)`. Tokens without a readable `exp` are not cached. Expired entries are swept when the map exceeds 10 000 entries. |
| CSRF | `CookieCsrfTokenRepository.withHttpOnlyFalse()` (cookie `XSRF-TOKEN`, header `X-XSRF-TOKEN`, path `/`, `SameSite=Strict`), plain `ServerCsrfTokenRequestAttributeHandler` (the SPA echoes the raw cookie value; no XOR masking). Required when the method is not `GET`/`HEAD`/`TRACE`/`OPTIONS`, **and** the request has no `Authorization` header, **and** it carries at least one cookie. The CSRF cookie is issued (when absent) on every non-bearer request, before the check, so a 403 also hands the SPA a fresh token to retry with. |
| Public FEC inputs | FEC keys are not URL paths: both hosts take every input on `POST /frontendcontroller-{user,admin}-service/handle-input` and every view on `.../get-output`; the key is `source->command->destination` in the JSON body (`get-output`: top-level `source`/`command`/`destination`; `handle-input`: `input.source`/`input.command`/`input.destination`). `FrontEndInputKeyWebFilter` buffers the body of those four paths for requests without an `Authorization` header (limit `granite.gateway.front-end-input.max-bytes`, default `1048576`; larger → 413), records the key in exchange attribute `granite.gateway.front-end-input-key`, and replays the body unchanged. `PublicFrontEndInputMatcher` permits only these pairs: `get-output` + `Session.New->LoginView`; `handle-input` + `Session.New->Login->LoginView`, `Session.New->StartOktaLogin->LoginView`, `Session.New->CompleteOktaLogin->LoginView`. |
| Public paths | Existing `getPublicPathPatterns` (health `/**`, Session `login`/`login-with-oauth2`/`refresh`, swagger outside production) plus the static app (`/`, `/index.html`, `/config.js`, `/favicon.ico`, `/assets/**`; nginx serves the app today, listed so an ingress may route it through the Gateway) plus the FEC inputs above. |
| Service token | **Gap:** the Gateway has no Phase 2 service token today (the `jti` check forwards the caller's own bearer, and only Session can mint service tokens). New seam `IServiceTokenSupplier serviceTokenSupplier(Environment)`; the default fails closed (every cookie request gets 401) until the credential mechanism is decided. Tests override the seam. |
| Production routes | `configureRoutes` adds only `frontendcontroller-user-route` and `frontendcontroller-admin-route` when the `production` profile is active. Other environments keep all seven. |
| `GRANITE_DID` | Issued when absent on non-bearer requests: 256-bit random base64url, `HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=31536000`. Also appended to the forwarded `Cookie` header so the FEC sees it on that first request. |

## Review Focus

1. Login after the session expired: a request with a dead `GRANITE_SID` to `Session.New->Login->LoginView` must reach the FEC (not 401) without `GRANITE_SID`. Test: `GatewaySessionCookieTest.publicInputWithDeadCookieIsForwardedWithoutResolving`.
2. Body of a classified FEC request must reach the FEC byte-for-byte. Test: `GatewayPublicInputTest.inspectedBodyIsForwardedUnchanged`.
3. Session hangs: resolution must fail within the timeout with 401 and not be cached. Test: `GatewaySessionCookieTest.resolveTimeoutIsUnauthorized`.
4. Handle or token in logs: none of the Gateway log lines (DEBUG) contain them on success or failure. Test: `GatewaySessionCookieTest.neverLogsHandleOrToken`.
5. Other cookies must survive stripping (`GRANITE_DID`, `XSRF-TOKEN`, third-party). Test: `GatewaySessionCookieTest.cookieRequestReachesFecWithJwtAndWithoutSid` (asserts the remaining cookies).

---

### Task 1: Production exposes only the FEC routes

**Files:** Modify `Components/Granite.Gateway.Server/.../configuration/GraniteGatewayServerConfiguration.java` (`configureRoutes`). Test: create `Tests/Granite.Gateway.ServerTest/.../configuration/GatewayProductionRoutesTest.java`.

- [ ] **Step 1: Failing test** (`@ActiveProfiles("production")`, `GatewayTestConfiguration`, test backend): `productionExposesOnlyFrontEndControllerRoutes` (route IDs == {`frontendcontroller-user-route`,`frontendcontroller-admin-route`}); `productionDoesNotRouteDirectServicePaths` (admin bearer on `POST /user-query-service/find-users-by-key` → 404 and the backend received nothing).
- [ ] **Step 2: Run, expect FAIL** (7 routes).
- [ ] **Step 3: Implement**: when `environment.acceptsProfiles(Profiles.of("production"))`, add only the two FEC routes.
- [ ] **Step 4: Run all Gateway tests, expect PASS** (existing 7-route test unchanged).
- [ ] **Step 5: Commit** `feat(gateway): expose only the FrontEndController routes in production`.

### Task 2: Session resolver with handle cache

**Files:** Create in `granite.gateway.server.security`: `IServiceTokenSupplier`, `ISessionResolver`, `SessionCommandSessionResolver`, `ResolvedSession`. Modify `GraniteGatewayServerConfiguration` (beans `serviceTokenSupplier`, `sessionResolver`). Test: create `Tests/.../security/SessionCommandSessionResolverTest.java`, `Tests/.../security/MutableClock.java`; extend `GatewayTestBackend` with a resolve endpoint.

**Interfaces:**
- `IServiceTokenSupplier { Mono<String> getToken(); }`
- `ISessionResolver { Mono<String> resolve(String handle); }` — emits the access token, or completes empty on any failure.
- `SessionCommandSessionResolver(WebClient webClient,URI resolveUri,IServiceTokenSupplier tokenSupplier,Duration cacheTtl,Duration timeout,Clock clock)`; package-visible `Set<String> getCacheKeys()`.
- Request body: `{"requestId":<uuid>,"timestamp":<ISO instant>,"handle":<handle>}`, `Authorization: Bearer <service token>`, `POST ${granite.session-command-service.host}/session-command-service/resolve-session`. Success = `success:true` and non-blank `accessToken`.
- Beans: `serviceTokenSupplier(Environment)` → `() -> Mono.error(new IllegalStateException("no Gateway service token is configured"))`; `sessionResolver(ObjectProvider<SslBundles>,Environment,IServiceTokenSupplier)` with `granite.gateway.session-cache-ttl` (`PT60S`), `granite.gateway.session-resolve.timeout` (`PT2S`), WebClient trusting `granite.gateway.session-resolve.ssl-bundle` (default the gateway HTTP client bundle).
- `GatewayTestBackend` gains `registerHandle(String handle,String token)`, `failResolvesFor(String handle)`, `delayResolvesFor(String handle)` (3 s), `getResolveCalls()` (handles), `getResolveAuthorizations()`, `getReceivedCookies()`, `sign(String sessionId,List<String> authorities,Duration lifetime)`, and a multi-threaded executor.

- [ ] **Step 1: Failing tests** (`SessionCommandSessionResolverTest`, mutable clock): `resolvesHandleWithServiceToken` (token returned; backend saw the handle in the body and `Bearer <service token>`); `cachesForConfiguredTtl` (two calls → 1 backend call; +61 s → 2); `cacheIsCappedByTokenExpiry` (token lifetime 10 s, ttl 60 s; +11 s → 2 calls); `cacheKeyIsHashOfHandle` (keys == {sha256 hex of handle}, none equals the handle); `unknownHandleIsEmptyAndNotCached`; `backendErrorIsEmptyAndNotCached`; `timeoutIsEmpty` (timeout 300 ms); `missingServiceTokenIsEmpty`.
- [ ] **Step 2: Run, expect compile failure.**
- [ ] **Step 3: Implement.** `exp` read with `com.nimbusds.jwt.JWTParser` (unverified; the bearer filter verifies it). Errors are logged with the exception class name only.
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** `feat(gateway): resolve session handles through Session with a TTL cache`.

### Task 3: Public FEC inputs and static app paths

**Files:** Create `security/FrontEndInputKeyWebFilter.java`, `security/PublicFrontEndInputMatcher.java`. Modify `GraniteGatewayServerConfiguration` (`getPublicPathPatterns` adds static paths; new hook `ServerWebExchangeMatcher getPublicExchangeMatcher(Environment)` = OR of path patterns and `PublicFrontEndInputMatcher`; chain uses `exchanges.matchers(publicMatcher).permitAll()`). Test: create `GatewayPublicInputTest.java` (role rules on).

**Interfaces:**
- `FrontEndInputKeyWebFilter(int maxBytes)`; constant `KEY_ATTRIBUTE = "granite.gateway.front-end-input-key"`, `PATH_ATTRIBUTE` not needed (matcher reads the path).
- `PublicFrontEndInputMatcher()`; `PUBLIC_OUTPUT_KEYS = Set.of("Session.New->LoginView")`, `PUBLIC_INPUT_KEYS = Set.of("Session.New->Login->LoginView","Session.New->StartOktaLogin->LoginView","Session.New->CompleteOktaLogin->LoginView")`.

- [ ] **Step 1: Failing tests:** `loginViewOutputIsPublicOnBothHosts`; `loginInputsArePublicOnBothHosts` (three keys × two hosts, no token → not 401/403); `otherKeysStayProtected` (`User.All->UsersEditableView` get-output → 401; `Session.New->LoginView` sent to handle-input → 401); `inspectedBodyIsForwardedUnchanged` (backend received the exact bytes); `oversizedBodyIsRejected` (max-bytes 64 → 413); `staticAppPathsArePublic` (`GET /index.html` not 401/403); `testStaticAppPathsArePublic` in a new assertion on `getPublicPathPatterns`.
- [ ] **Step 2–4: Run fail, implement, run pass (whole suite).**
- [ ] **Step 5: Commit** `feat(gateway): make the LoginView FEC inputs and static app public`.

### Task 4: Cookie-to-token filter

**Files:** Create `security/SessionCookieWebFilter.java`, `security/CookieHeaders.java` (cookie-header rewriting helpers: `strip(HttpHeaders,String name)`, `append(HttpHeaders,String name,String value)`). Modify `GraniteGatewayServerConfiguration.securityWebFilterChain(...)` — new parameter `ISessionResolver sessionResolver`; filter added `addFilterBefore(..., SecurityWebFiltersOrder.AUTHENTICATION)`. Test: create `GatewaySessionCookieTest.java` and `SessionCookieGatewayTestConfiguration.java` (extends `GraniteRoleRulesGatewayServerConfiguration`, overrides `serviceTokenSupplier` with property `test.gateway.service-token`).

**Interfaces:** `SessionCookieWebFilter(ISessionResolver resolver,ServerWebExchangeMatcher publicMatcher)`; `SESSION_COOKIE = "GRANITE_SID"`.

- [ ] **Step 1: Failing tests** (role rules on, CSRF header supplied where needed): `cookieRequestReachesFecWithJwtAndWithoutSid` (FEC sees `Bearer <session jwt>`, Cookie has `GRANITE_DID` and `other` but no `GRANITE_SID`); `unknownHandleIsUnauthorized`; `closedOrExpiredHandleIsUnauthorized` (backend `success=false`); `resolveErrorIsUnauthorized`; `resolveTimeoutIsUnauthorized` (timeout PT0.5S); `resolvedJwtStillPassesRoleRules` (ROLE_USER session → 403 on get-output); `resolvedJwtStillPassesRevocationCheck` (revoked `jti` → 401); `bearerTakesPrecedenceOverCookie` (bearer + SID → backend sees caller's bearer, no resolve call, no SID forwarded); `publicInputWithDeadCookieIsForwardedWithoutResolving`; `cachedHandleIsResolvedOnce`; `neverLogsHandleOrToken` (logback `ListAppender` on `granite.gateway` at DEBUG).
- [ ] **Step 2–4: Run fail, implement, run pass (whole suite).**
- [ ] **Step 5: Commit** `feat(gateway): swap GRANITE_SID for the session JWT before authentication`.

### Task 5: `GRANITE_DID` and CSRF double-submit

**Files:** Create `security/DeviceCookieWebFilter.java`, `security/CsrfCookieWebFilter.java`, `security/CookieCsrfProtectionMatcher.java`. Modify `securityWebFilterChain` (replace `csrf.disable()`). Test: create `GatewayCookieCsrfTest.java`.

**Interfaces:** `DeviceCookieWebFilter(Duration maxAge)` (`DEVICE_COOKIE = "GRANITE_DID"`); `CsrfCookieWebFilter(ServerCsrfTokenRepository)`; `CookieCsrfProtectionMatcher()`.

- [ ] **Step 1: Failing tests:** `deviceCookieIssuedWhenMissing` (Set-Cookie `GRANITE_DID`, HttpOnly, Secure, SameSite=Lax, Path=/, Max-Age=31536000, 43-char base64url); `deviceCookieNotReissuedWhenPresent`; `deviceCookieNotIssuedToBearerRequests`; `csrfCookieIssuedReadableByScript` (`XSRF-TOKEN`, not HttpOnly, Path=/); `cookiePostWithoutCsrfTokenIsForbidden` (403, no resolve call); `cookiePostWithWrongCsrfTokenIsForbidden`; `cookiePostWithMatchingCsrfTokenPasses`; `getWithCookiesNeedsNoCsrfToken`; `bearerPostWithCookiesNeedsNoCsrfToken`; `postWithoutCookiesNeedsNoCsrfToken`; `forbiddenResponseCarriesFreshCsrfCookie`.
- [ ] **Step 2–4: Run fail, implement, run pass (whole suite).**
- [ ] **Step 5: Commit** `feat(gateway): issue GRANITE_DID and require a double-submit CSRF token on cookie requests`.

### Task 6: Documentation

- [ ] Update `Microservices/Granite.Gateway/CLAUDE.md` (seams table, cookie path, CSRF, public FEC inputs, production routes, properties, service-token gap). Commit `docs(gateway): document the cookie-to-token filter`.
