# Granite Server-Side Sessions, Step 5: Okta Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let a browser sign in with enterprise Okta through the FEC: Session runs a server-side authorization-code flow with PKCE `S256` and a `private_key_jwt` client, maps `okta:{sub}` to a Granite User (created with `ROLE_USER` when missing), applies the login gate and the Step 2 lifecycle, keeps the Okta `id_token` for RP-initiated logout, and replaces the Google `loginWithOauth2` path.

**Architecture:** Session gains two public command operations, `startOktaLogin` and `completeOktaLogin`. Start stores an `OktaLoginTransaction` row (state, nonce, PKCE verifier, `returnTo`, device, 5-minute expiry), keyed by the SHA-256 hash of a random transaction id that is returned once and that the FEC puts in `GRANITE_LOGIN`. Complete consumes the row (deleted on every attempt), exchanges the code through `IOktaClient` (discovery cached, Nimbus JWKS validation, RS256 client assertion), then reuses `prepareLifecycle`/`newSession` and the retry wrapper. Authorities come from `OktaAuthorities` (`USER`, `OKTA_GROUPS`, `BOTH` plus a group map), recalculated at login and at `resolveSession` re-mint from the groups stored on that session. The FEC adds two actions around `GRANITE_LOGIN`; the Gateway denies the two new Session paths externally.

**Tech Stack:** Java 23, Spring 6.2 (`RestClient`, `UriComponentsBuilder`), Spring Security OAuth2 JOSE (`NimbusJwtDecoder`), Nimbus JOSE (client assertion), JPA/Hibernate orm.xml, H2, WireMock 3.9, JUnit 5, Mockito, TypeScript.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Decisions, Cookies, FEC inputs and outputs, Configuration, Step 5, Interactions, Open item 1 decided: keep the gate for Okta). Builds on the Step 1 to 4 plans.

## Global Constraints

- Java style per each component's `CLAUDE.md`: `////` header and footer, `public` on its own line then `class X`, return type on its own line, Allman braces, fluent setters, `(a,b)`, column-aligned fields. No static inner classes, no explanatory comments, no `@PreAuthorize`. TS uses `Array<T>`.
- Never log or `toString()` an Okta code, an Okta token, the PKCE verifier, the transaction id, the session handle or a client assertion.
- Schema only from the ORM mapping (`ddl-auto=update`): no hand-written DDL, no `schema.sql` edits.
- Only libraries already on the classpath. No `jib`, `publish*`, `publishToMavenLocal`; no build-tooling changes.
- Change only Granite.Session, Granite.FrontEndController, the Gateway (deny rule and removing `login-with-oauth2`), and docs. Granite.User already supports `externalIdentity` on create (`CreateUserData`, TS mirror, `MappingModule`, `UserPasswordHashingTest.createUserCopiesExternalIdentity`), so it is unchanged.
- Java tests via the root composite build, for example `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun`. Every test class is tagged `CommitStage`.
- TS via `yarn run build-granite-session` then `yarn run build-granite-frontendcontroller` (local Verdaccio only); commit the snapshot bumps.
- Okta scope exactly `openid profile email groups`; PKCE `S256`; transaction TTL `PT5M`; `GRANITE_LOGIN` is `HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=300`.
- Every Okta rejection replies `success=false` with `invalid credentials`; an unexpected error replies `Unable to log in`.

## Decisions taken in this step

| Topic | Decision |
|---|---|
| Endpoint names | `POST /session-command-service/start-okta-login` (`StartOktaLoginRequest {deviceId, returnTo}` → `StartOktaLoginReply {authorizeUrl, transactionId}`), `POST /session-command-service/complete-okta-login` (`CompleteOktaLoginRequest {code, state, transactionId, deviceId}` → `CompleteOktaLoginReply {sessionData, sessionHandle, userName, returnTo}`). Neither reply carries a Granite access or refresh token. |
| Public / deny | Both paths are in Session's `getPublicPathPatterns` (Command). The Gateway adds them to the existing `denyAll` rule next to `resolve-session`. `login-with-oauth2` is removed from both public lists. |
| Transaction entity | `OktaLoginTransaction` (in `Session.orm.xml`, table `OktaLoginTransaction`): `hashedTransactionId` (unique, VARCHAR(64)), `state`, `nonce`, `codeVerifier` (VARCHAR(128)), `returnTo` (VARCHAR(2048)), `deviceId`, `expires`. Deleted on every completion attempt that finds it; expired rows deleted by `SessionPurgeJob` in the same batch transaction. |
| Device binding | Complete is refused unless its `deviceId` equals the transaction's. |
| `prompt=login` | Added when any live open session exists on the device at start (the Okta user is not known yet, so any active session counts). |
| `returnTo` | Kept only when it is a local path (`/` first, not `//` or `/\`); otherwise `/`. |
| User mapping | `findUsersByExternalIdentity(provider="okta", externalId=sub)`. Missing → `createUser` with `externalIdentity="okta:"+sub`, `authorities={ROLE_USER}`, `userName` = `preferred_username`, else `email`, else `okta:`+sub, no password. A failed create is re-queried once (a concurrent first login), else rejected. |
| Authorities | `OktaAuthorities.calculate(userAuthorities,groups)`: `USER` → user's; `OKTA_GROUPS` → mapped groups; `BOTH` → union. Unmapped groups are dropped. The gate checks the calculated set. |
| Re-mint | `Session` stores `signInMethod` (`PASSWORD`, `ACCESS_TOKEN`, `OKTA`), `oktaIdToken` and `oktaGroups` (from that session's own last ID token). `resolveSession` renewal uses `OktaAuthorities` for `OKTA` sessions, the user's authorities otherwise. `newSession` never copies them. `Session.close` clears `oktaIdToken`. |
| Retry | The command service is request-scoped, so the retry runs on the same instance: the validated identity is remembered per hashed transaction id, and the retry skips the second code exchange (Okta codes are single-use). Rollback of the first attempt restores the transaction row for the retry. |
| Logout | Before closing an open `OKTA` session with an `oktaIdToken`, `LogoutReply.endSessionUrl` = `end_session_endpoint?id_token_hint=…[&post_logout_redirect_uri=…]`. A discovery failure logs out without a URL. `LogoutReply.toString()` omits the URL. |
| Disabled | `granite.session.login.okta.enabled=false` (default) makes start and complete fail without calling Okta; the key store is not loaded. |
| Key | `granite.session.okta.keystore.{location,password,type(PKCS12),alias,key-password,key-id}`; `kid` = `key-id` or the RFC 7638 thumbprint. Classpath locations only in `local`/`development` (like `JwtKeyLoader`). |
| FEC refusal shape | `StartOktaLogin` refused (disabled, no `GRANITE_DID`, Session failure) returns `SessionOutput{status:UNAUTHENTICATED}` instead of a `RedirectOutput` with a null `url` (Jackson deduction cannot match an empty `RedirectOutput`). |
| FEC complete | Always clears `GRANITE_LOGIN`; success also issues `GRANITE_SID` and returns `SessionOutput{userName,authorities,status:AUTHENTICATED,returnTo}`. |

## Review Focus

1. A transaction presented twice (browser back button, replay): the second attempt fails even though the first succeeded. Test: `CompleteOktaLoginTest.reusedTransactionIsRejected`.
2. An ID token signed by a key not in Okta's JWKS but with a matching `kid`: rejected. Test: `OktaClientTest.foreignSignatureIsRejected`.
3. `returnTo` pointing off-site (`https://evil`, `//evil`): replaced by `/`. Test: `StartOktaLoginTest.offSiteReturnToBecomesRoot`.
4. Secrets in `toString`/logs: code, verifier, transaction id, id token, assertion never appear. Tests: `OktaRequestReplyTest.toStringHidesSecrets`, `CompleteOktaLoginTest.neverLogsSecrets`.
5. Okta session re-mint when the user's groups shrink: re-mint uses the session's stored groups and current User authorities only, never another session's. Test: `ResolveSessionTest.oktaRenewalUsesTheSessionsOwnGroups`.

---

### Task 1: Service contract (Java + TS) and removal of `loginWithOauth2`

**Files:** Create in `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/`: `StartOktaLoginRequest`, `StartOktaLoginReply`, `CompleteOktaLoginRequest`, `CompleteOktaLoginReply`; TS mirrors. Modify `ISessionCommandService` (Java, TS), `LogoutReply` (+`endSessionUrl`, Java, TS), `index.ts`. Delete `LoginWithOauth2Request/Reply` (Java, TS). Test: `Tests/Granite.Session.ServiceTest/src/test/java/.../OktaRequestReplyTest.java`.

**Interfaces:** `ISessionCommandService.startOktaLogin(StartOktaLoginRequest)`, `completeOktaLogin(CompleteOktaLoginRequest)` returning `CompletionStage<…Reply>`; TS `ICompletionStage`.

- [x] Step 1: Failing `OktaRequestReplyTest.toStringHidesSecrets` (code, state, transactionId, handle, endSessionUrl absent from `toString`) and JSON round-trip of each type.
- [x] Step 2: Implement; remove `loginWithOauth2` everywhere it no longer compiles (service, controller, retry wrapper, clients, fixtures) as part of Tasks 1-4 landing together.

### Task 2: Domain — transaction entity, Session Okta fields

**Files:** Create `server/domain/OktaLoginTransaction`, `IOktaLoginTransactionRepository`, `OktaLoginTransactionRepository`, `SignInMethod`. Modify `Session` (+`signInMethod`, `oktaIdToken`, `oktaGroups`; `close` clears `oktaIdToken`), `Session.orm.xml`, `DomainConfiguration`. Tests: `domain/OktaLoginTransactionRepositoryTest`, `SessionTest.closeClearsOktaIdToken`, `SessionRepositoryTest.oktaFieldsRoundTrip`.

**Interfaces:** `IOktaLoginTransactionRepository extends IRepository<Long,OktaLoginTransaction>`: `Optional<OktaLoginTransaction> findByHashedTransactionId(String)`, `List<OktaLoginTransaction> findExpiredBefore(Instant)`.

### Task 3: Okta client — discovery, client assertion, token exchange, ID-token validation, end-session URL

**Files:** Create `server/application/`: `OktaSettings`, `OktaAuthoritiesSource`, `OktaAuthorities`, `OktaEndpoints`, `OktaIdentity`, `OktaLoginException`, `IOktaClient`, `OktaClient`, `OktaClientAssertionSigner`; `server/platform/OktaKeyLoader`. Tests: `application/OktaClientTest` (WireMock discovery/token/JWKS), `application/OktaAuthoritiesTest`, `platform/OktaKeyLoaderTest` (PKCS12 generated in `@TempDir`).

**Interfaces:** `IOktaClient`: `String buildAuthorizeUrl(String state,String nonce,String codeChallenge,boolean promptLogin)`, `OktaIdentity exchange(String code,String codeVerifier,String nonce) throws OktaLoginException`, `Optional<String> buildEndSessionUrl(String idToken)`. `OktaAuthorities.calculate(Set<String> userAuthorities,Set<String> groups): Set<String>`.

- [x] Tests: discovery fetched once and cached; issuer mismatch in the discovery document rejected; authorize URL has `response_type=code`, `client_id`, `redirect_uri`, `scope=openid profile email groups`, `state`, `nonce`, `code_challenge`, `code_challenge_method=S256`, and `prompt=login` only when asked; token request is form-encoded with `grant_type=authorization_code`, `code`, `code_verifier`, `redirect_uri`, `client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer` and a client assertion that verifies with the public key (RS256, `iss=sub=clientId`, `aud=token endpoint`, `exp` ≤ 5 min, `jti`); ID-token rejections for wrong `iss`, wrong `aud`, wrong/missing `nonce`, expired, missing `exp`, foreign signature, `alg=none`/HS256, token endpoint error; groups and user name extracted; end-session URL with `id_token_hint` and optional `post_logout_redirect_uri`.

### Task 4: Session operations — start, complete, re-mint authorities, logout URL, purge

**Files:** Create `StartOktaLoginContext`, `CompleteOktaLoginContext`. Modify `SessionCommandService` (constructor drops `IOauth2IdTokenValidator`, adds `IOktaLoginTransactionRepository`, `IOktaClient`, `OktaSettings`), `ConcurrencyRetryingSessionCommandService`, `ApplicationConfiguration`, `SessionCommandServiceController`, `CommandPlatformConfiguration`, `GraniteSessionCommandServerConfiguration` (seams `oktaSettings(Environment)`, `oktaClient(OktaSettings,Environment,ResourceLoader,Clock)`; public paths), `SessionPurgeJob`. Delete `LoginWithOauth2Context`, `Oauth2*`, `IOauth2IdTokenValidator`. Tests: `StartOktaLoginTest`, `CompleteOktaLoginTest` (real `OktaClient` against WireMock, mocked repositories and User clients), `ResolveSessionTest` additions, `LogoutTest` additions, `SessionCommandServiceServerTest` Okta end-to-end on H2, `SessionPurgeJobTest` addition; update the other test constructors; delete `LoginWithOauth2Test`, `Oauth2IdTokenValidatorTest`.

- [x] Tests (each Okta rejection asserts `invalid credentials`, no session saved, no User call where applicable): wrong state, missing state, unknown transaction, expired transaction, reused transaction, device mismatch, wrong nonce, wrong aud, wrong iss, foreign signature, disabled; first login creates the User with exactly `{ROLE_USER}` and `externalIdentity=okta:<sub>`; existing user not re-created; `USER`/`OKTA_GROUPS`/`BOTH`; gate refuses an ungated new user (user still created); lifecycle: re-login replaces with same `continuityId`, another user displaced; retry after a conflict does not exchange the code twice; `prompt=login` when a live session is on the device; logout of an Okta session returns `end_session_endpoint?id_token_hint=<id token>&post_logout_redirect_uri=…` and clears the stored token; password logout has no URL; re-mint of an Okta session under `BOTH` uses the session's stored groups.

### Task 5: Clients (Java + TS), configuration, docs

**Files:** `SessionCommandServiceClient` (Java, TS), `ClientTest`, ServiceTest fixture (`testLoginWithOauth2` removed), all CommandHost `application-*.properties` (+ test resources), `Microservices/Granite.Session/Claude.md`.

- [x] Properties per environment: `granite.session.login.okta.enabled=false`; `granite.session.okta.issuer`, `.client-id`, `.redirect-uri`, `.post-logout-redirect-uri`, `.authorities-source=USER`, `.keystore.*` — `local`/`development` with example placeholders commented by value only (`https://localhost:5173/login/callback`), `test`/`production` as `${OKTA_*}` placeholders. Remove `granite.session.oauth2.providers.*`.

### Task 6: Gateway deny rule

**Files:** `GraniteGatewayServerConfiguration` (deny `start-okta-login`, `complete-okta-login`; drop `login-with-oauth2` public path), `GatewayDefaultSecurityTest`, `GatewaySessionCookieTest`, `Microservices/Granite.Gateway/CLAUDE.md`.

- [x] Tests: both Okta paths → 401 anonymous, 403 with an admin bearer, never reach the backend; `login-with-oauth2` → 401.

### Task 7: FEC actions

**Files:** Create `control/session/StartOktaLoginView`, `CompleteOktaLoginView`. Modify `SessionRequestProcessor`, `LogoutView` (`endSessionUrl` from the reply), `SessionCookies` (`LOGIN_COOKIE="GRANITE_LOGIN"`, `issueLogin(String)`, `clearLogin()`), `SessionOutput` (+`returnTo`, Java + TS), FEC `application-*.properties` (`granite.session.login.okta.enabled=false`), `CLAUDE.md`. Tests: `StartOktaLoginViewTest`, `CompleteOktaLoginViewTest`, `LogoutSessionTest.oktaLogoutCarriesTheEndSessionUrl`, `SessionRequestProcessorTest` (Okta keys route), `SessionHttpTest` (Okta start sets `GRANITE_LOGIN` with `SameSite=Lax`, `Max-Age=300`; complete sets `GRANITE_SID` and `GRANITE_LOGIN=; Max-Age=0`), `FrontEndJacksonModuleTest` (`SessionOutput` with `returnTo`).

### Task 8: TS builds, roadmap tick, final verification

- [x] `yarn run build-granite-session`, `yarn run build-granite-frontendcontroller`; commit bumps. Run Session (Service, Server, Client), FEC ServerTest, Gateway ServerTest. Tick Step 5 in the roadmap.

## Acceptance Criteria Map

| Criterion | Proving tests |
|---|---|
| Wrong/missing `state`, `nonce`, `aud`, `iss`, signature rejected (mocked Okta) | `CompleteOktaLoginTest`, `OktaClientTest` |
| Expired, unknown, reused transaction rejected | `CompleteOktaLoginTest` |
| First login creates the User with only `ROLE_USER` | `CompleteOktaLoginTest.firstLoginCreatesUserWithOnlyRoleUser`, `SessionCommandServiceServerTest.testCompleteOktaLoginEndToEnd` |
| `USER`, `OKTA_GROUPS`, `BOTH` | `OktaAuthoritiesTest`, `CompleteOktaLoginTest` |
| Gate refuses an ungated new user | `CompleteOktaLoginTest.gateRefusesUngatedNewUser` |
| Step 2 lifecycle applies | `CompleteOktaLoginTest.reloginOnTheSameDeviceReplacesAndKeepsContinuity`, `.otherUserOnTheDeviceIsDisplaced`, `SessionCommandServiceServerTest.testOktaReloginReplacesAndKeepsContinuity`, `SessionCommandServiceServerTest` |
| Logout returns end-session URL with `id_token_hint` | `LogoutTest.oktaLogoutReturnsEndSessionUrl`, `LogoutSessionTest`, `SessionCommandServiceServerTest` |
| FEC cookies set/cleared, disabled flag | `StartOktaLoginViewTest`, `CompleteOktaLoginViewTest`, `SessionHttpTest` |
| Gateway denies the Okta paths externally | `GatewaySessionCookieTest.oktaPathsAreDeniedExternallyAndNeverReachTheBackend` |
