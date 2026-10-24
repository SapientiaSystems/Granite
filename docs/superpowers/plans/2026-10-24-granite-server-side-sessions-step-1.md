# Granite Server-Side Sessions, Step 1: Session Handle and `resolveSession` Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give every Session an opaque handle (only its hash stored) and add `resolveSession`, which swaps a handle for `SessionData` plus a current JWT, renewing the JWT inside the renewal margin.

**Architecture:** All work is in Granite.Session. The handle reuses `IRefreshTokenService` (256-bit SecureRandom base64url, SHA-256 hex hash). `resolveSession` is a command-side pipeline in `SessionCommandService` (validate, map, load, check lifetime, renew, record activity, save, build reply). The current JWT's `exp` is read through a new `IJwtTokenService.getExpiration(String)`; renewal recalculates authorities through `IUserQueryServiceClient.findUsersByKey(UserKey.ofUserId(..))`.

**Tech Stack:** Java 23, Spring 6.2, JPA/Hibernate (orm.xml), JUnit 5, Mockito, Nimbus JOSE, TypeScript.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Step 1, "New Session fields", "`resolveSession` contract", Configuration).

## Global Constraints

- Java style per `Microservices/Granite.Session/Claude.md`: `////` header and footer, `public` on its own line then `class X`, return type on its own line, Allman braces, fluent setters, `(a,b)` with no space after commas, column-aligned fields.
- No static inner classes, no explanatory comments. TS uses `Array<T>`.
- Never log or `toString()` a session handle, an access token or a refresh token.
- No `@PreAuthorize`. `/resolve-session` is not a public path; the caller (Gateway) authenticates with the service token.
- Never run `jib`, `publish*`, `publishToMavenLocal`, or the Yarn `build`/`publish-package` scripts (they publish to Verdaccio and bump versions).
- Java tests run through the root composite build (the per-microservice build resolves a stale `granite-foundation-core` from `~/.m2`):
  `cd C:/Development/Granite && ./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test :Granite.Session:Tests:Granite.Session.ServiceTest:test :Granite.Session:Tests:Granite.Session.ClientTest:test`
- TS is checked with the installed compiler, no emit: `node_modules/.bin/tsc --noEmit -p .` in `Components/Granite.Session.Service`; the Client is checked with a scratchpad tsconfig that extends the Client's and maps `granite.session.service/*` to the local Service sources.
- Failure message for every `resolveSession` rejection: exactly `invalid session`.
- `granite.session.token.renewal-margin` default `PT2M`. `lastActivity` write interval: 1 minute.

## Review Focus

1. Handle present in a log line (debug included) during login or resolve: no log line may contain it. Test: `ResolveSessionTest.resolveNeverLogsHandleOrToken`.
2. Session whose `created + max-lifetime` has passed while its JWT is still unexpired: must fail and close as `EXPIRED`. Test: `ResolveSessionTest.resolvePastMaxLifetimeClosesAsExpired`.
3. User deleted (lookup returns no user) at renewal time: must fail with `invalid session`, no token minted. Test: `ResolveSessionTest.resolveFailsWhenUserMissingAtRenewal`.
4. Already-closed session presented again: fails, is not re-closed or re-saved. Test: `ResolveSessionTest.resolveClosedSessionFails`.
5. Unparseable stored access token: treated as due for renewal, not as a crash. Test: `ResolveSessionTest.resolveRenewsWhenStoredTokenExpirationUnknown`.

---

### Task 1: Session domain fields, closure and lookup by hashed handle

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/domain/SessionClosedReason.java`
- Modify: `.../server/domain/Session.java`, `.../server/domain/ISessionRepository.java`, `.../server/domain/SessionRepository.java`, `Components/Granite.Session.Server/src/main/resources/Session.orm.xml`
- Test: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/domain/SessionTest.java`, `.../domain/SessionRepositoryTest.java`

**Interfaces:**
- Produces: `enum SessionClosedReason { EXPIRED }` (Step 2 adds the other values); on `Session`: `setHashedHandle(String)`/`getHashedHandle()`, `setLastActivity(Instant)`/`getLastActivity()`, `setClosedReason(SessionClosedReason)`/`getClosedReason()`, `close(SessionClosedReason)` (sets reason, fires `SessionUpdated`), `isClosed()`; `ISessionRepository.findByHashedHandle(String hash): Optional<Session>`.
- ORM: `hashedHandle` `VARCHAR(64)` unique, nullable; `lastActivity` timestamp, nullable; `closedReason` `<enumerated>STRING</enumerated>` `VARCHAR(32)`, nullable.

- [ ] **Step 1: Failing tests.** `SessionTest.closeSetsReasonAndFiresUpdated` (attach `CapturingObserver`, `close(EXPIRED)`, assert `isClosed()`, `getClosedReason()==EXPIRED`, one `SessionUpdated`). `SessionRepositoryTest.findByHashedHandleReturnsSession` (save with `hashedHandle="ab..."`, `lastActivity`, `closedReason=EXPIRED`; `findByHashedHandle` returns it with all three round-tripped) and `findByHashedHandleUnknownReturnsEmpty`.
- [ ] **Step 2: Run ServerTest, expect compile failure.**
- [ ] **Step 3: Implement** fields, ORM mapping, named query `findByHashedHandle` = `select e from Session e where e.hashedHandle = :hash`.
- [ ] **Step 4: Run ServerTest, expect PASS.**
- [ ] **Step 5: Commit** `feat(session): add hashedHandle, lastActivity and closedReason to Session`.

### Task 2: Login issues the session handle

**Files:**
- Modify: `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/LoginReply.java`, `LoginWithOauth2Reply.java`; TS `LoginReply.ts`, `LoginWithOauth2Reply.ts`; `.../server/application/LoginContext.java`, `LoginWithOauth2Context.java`, `SessionCommandService.java`
- Test: `.../application/LoginTest.java`, `LoginWithOauth2Test.java`; fixture `Tests/Granite.Session.ServiceTest/src/testFixtures/.../SessionCommandServiceTest.java` (`testLogin` asserts `getSessionHandle()` non-blank)

**Interfaces:**
- Produces: `LoginReply.setSessionHandle(String)`/`getSessionHandle()` and the same on `LoginWithOauth2Reply`; TS `sessionHandle?: string`. Decision: the raw handle is returned once as a new reply property; the FEC (Step 4) moves it into `GRANITE_SID`.
- `createSession`/`createOauth2Session` set `hashedHandle = refreshTokenService.hash(handle)` with `handle = refreshTokenService.generate()`, and `lastActivity = now`.

- [ ] **Step 1: Failing tests.** `LoginTest.loginIssuesSessionHandleAndStoresOnlyItsHash`: handle non-blank, base64url 43 chars (`[A-Za-z0-9_-]{43}`), saved `hashedHandle == RefreshTokenService.hash(handle)`, differs from handle and from refresh token; `loginSetsLastActivity` (`== NOW`). `LoginWithOauth2Test.loginWithOauth2IssuesSessionHandle`.
- [ ] **Step 2: Run, expect failure.**
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run, expect PASS.**
- [ ] **Step 5: Commit** `feat(session): login returns an opaque session handle and stores its hash`.

### Task 3: `ResolveSessionRequest` / `ResolveSessionReply` (Java and TS)

**Files:**
- Create: `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/ResolveSessionRequest.java`, `ResolveSessionReply.java`; TS `ResolveSessionRequest.ts`, `ResolveSessionReply.ts`; export both in TS `index.ts`
- Test: `Tests/Granite.Session.ServiceTest/src/test/java/granite/session/service/requestreply/ResolveSessionRequestTest.java`, `ResolveSessionReplyTest.java` (tag `CommitStage`)

**Interfaces:**
- `ResolveSessionRequest extends AbstractServiceRequest`: `setHandle(String)`/`getHandle()`, fluent overrides like `RefreshRequest`, `toString()` = `ResolveSessionRequest{requestId=<id>}`.
- `ResolveSessionReply extends AbstractServiceReply`: `setSession(SessionData)`/`getSession()`, `setAccessToken(String)`/`getAccessToken()`, fluent overrides like `RefreshReply`, `toString()` = `ResolveSessionReply{replyId=..,success=..,session=<sessionId or null>}`.
- TS: `ResolveSessionRequest { handle?: string }`, `ResolveSessionReply { session?: SessionData; accessToken?: string }`.

- [ ] **Step 1: Failing tests.** `toStringNeverContainsHandle` (handle `"secret-handle-value"` absent, requestId present); `toStringNeverContainsAccessToken`; fluent setters return same instance.
- [ ] **Step 2: Run ServiceTest, expect compile failure.**
- [ ] **Step 3: Implement.** Run `tsc --noEmit` in the Service component.
- [ ] **Step 4: Run ServiceTest, expect PASS.**
- [ ] **Step 5: Commit** `feat(session): add ResolveSessionRequest and ResolveSessionReply`.

### Task 4: Read a JWT's expiration

**Files:** Modify `.../application/IJwtTokenService.java`, `JwtTokenService.java`; Test `.../application/JwtTokenServiceTest.java`

**Interfaces:**
- Produces: `Instant getExpiration(String jwt)` – parses without verifying (the token comes from Session's own store) and returns `exp`, or `null` when blank, unparseable or missing `exp`.

- [ ] **Step 1: Failing tests.** `getExpirationReturnsExpOfMintedToken` (equals `verifySignatureAndExp(..).getExpiresAt()`); `getExpirationReturnsNullForGarbage` (`"not.a.jwt"`, `""`).
- [ ] **Step 2–4: Run fail, implement, run pass.**
- [ ] **Step 5: Commit** `feat(session): expose access token expiration from IJwtTokenService`.

### Task 5: `resolveSession` end to end

**Files:**
- Create: `.../application/ResolveSessionContext.java`; TS client and interface changes
- Modify: `ISessionCommandService.java` (+ TS), `SessionCommandService.java`, `ApplicationConfiguration.java`, `SessionCommandServiceController.java`, `SessionCommandServiceClient.java` (+ TS), all `new SessionCommandService(..)` call sites in ServerTest (add the margin argument after max-lifetime)
- Test: Create `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/ResolveSessionTest.java`; fixture `SessionCommandServiceTest` gains `testResolveSession`, `testResolveSessionWithUnknownHandleFails`; `SessionCommandServiceClientTest` overrides them (IntegrationStage)

**Interfaces:**
- Consumes: Task 1 (`findByHashedHandle`, `close`, `isClosed`, `lastActivity`), Task 2 (handle from login), Task 3 DTOs, Task 4 `getExpiration`.
- Produces: `CompletionStage<ResolveSessionReply> resolveSession(ResolveSessionRequest)` on `ISessionCommandService`; TS `resolveSession(request: ResolveSessionRequest): ICompletionStage<ResolveSessionReply>`; endpoint `POST /session-command-service/resolve-session`; Java and TS clients post to `resolve-session`.
- `SessionCommandService` constructor gains `Duration renewalMargin` after `maxLifetime`; `ApplicationConfiguration` binds `@Value("${granite.session.token.renewal-margin:PT2M}")`.

Pipeline (each step a protected method):
1. `validateResolve`: non-null request; null or blank handle throws `IllegalArgumentException`.
2. `loadForResolve`: `findByHashedHandle(refreshTokenService.hash(handle))`; missing → rejected.
3. `checkLifetime`: closed → rejected. `expires <= now` or `created + maxLifetime <= now` → `attach(this)`, `close(EXPIRED)`, mark closed and rejected.
4. `renew`: when `getExpiration(accessToken)` is null or `exp - renewalMargin <= now`: look up the user through `findUsersByKey(UserKey.ofUserId(userId))`; none → rejected; else `attach(this)`, `setAuthorities(user.authorities)`, `setExpires(capExpires(created,now))`, mint, `setAccessToken`.
5. `recordActivity`: when `lastActivity` is null or at least 1 minute old → `setLastActivity(now)`.
6. `saveResolved`: save when closed, renewed or activity recorded.
7. `buildResolveReply`: success → `session` (mapped `SessionData`) and `accessToken`; any rejection or exception → `success=false`, `failureMessage="invalid session"`, no session, no token. Log only the session ID, never the handle or token.

- [ ] **Step 1: Failing tests (`ResolveSessionTest`, fixed clock, mocked repository/JWT/User):**
  - `resolveValidHandleReturnsSessionAndAccessToken` (token outside margin: no mint, returns stored token)
  - `resolveBlankHandleFails`, `resolveNullHandleFails`, `resolveUnknownHandleFails`, `resolveClosedSessionFails` (no save, no event)
  - `resolveExpiredSessionClosesAsExpired` (`expires` passed), `resolvePastMaxLifetimeClosesAsExpired`: reply `invalid session`, `closedReason==EXPIRED`, saved once, no mint
  - `resolveInsideMarginRenewsToken` (exp = now+1m with margin 2m → mint once, reply carries new token, `expires == now+TTL`)
  - `resolveOutsideMarginDoesNotRenew` (exp = now+3m → no mint, no User call)
  - `resolveRenewalNeverExceedsMaxLifetime` (created = now − maxLifetime + 30s → minted session `expires == created + maxLifetime`)
  - `resolveRenewalRecalculatesAuthoritiesFromUser` (stored `{ROLE_USER}`, User returns `{ROLE_USER,ROLE_AUDITOR}` → minted session and reply have the new set)
  - `resolveFailsWhenUserMissingAtRenewal`, `resolveRenewsWhenStoredTokenExpirationUnknown`
  - `resolveRecordsLastActivityWhenOlderThanAMinute`, `resolveDoesNotRecordLastActivityWithinAMinute` (no save when nothing changed)
  - `resolveNeverLogsHandleOrToken` (logback `ListAppender` on `granite.session` at DEBUG across login-free resolve with renewal; no formatted message contains handle, stored token or minted token)
  - All failures assert message exactly `invalid session`.
- [ ] **Step 2: Run, expect compile failure.**
- [ ] **Step 3: Implement** interface, context, pipeline, config binding, controller, Java client, TS interface/client; update constructor call sites.
- [ ] **Step 4: Run ServerTest, ServiceTest, ClientTest, expect PASS** (fixture `testResolveSession` runs in `SessionCommandServiceServerTest` against H2: login → resolve with `sessionHandle` → success, `session.sessionId` equals login's, `accessToken` non-blank; unknown handle → `invalid session`). Run TS checks.
- [ ] **Step 5: Commit** `feat(session): add resolveSession command`.

### Task 6: Documentation

**Files:** Modify `Microservices/Granite.Session/Claude.md` (entity fields, DTO list, endpoint table, login section, configuration of `granite.session.token.renewal-margin`).

- [ ] **Step 1: Update docs. Step 2: Commit** `docs(session): document session handle and resolveSession`.
