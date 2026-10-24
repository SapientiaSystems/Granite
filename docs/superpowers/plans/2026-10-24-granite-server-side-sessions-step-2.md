# Granite Server-Side Sessions, Step 2: Session Lifecycle and Continuity Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every login apply the session lifecycle table (replace, displace, start from latest, new), carry a `continuityId` across replacements, record why and how sessions were closed, and guarantee one active session per (user, device) even when logins race.

**Architecture:** All work is in Granite.Session. Sessions are no longer deleted by logout or refresh-token reuse; they are *closed* (`closedReason` set, `expires` truncated to the close time, `activeMarker` cleared). A unique index on `(userId, deviceId, activeMarker)` (marker `TRUE` while open, `NULL` once closed) enforces one active session per pair portably on H2 and PostgreSQL. `version` gives optimistic locking. A `ConcurrencyRetryingSessionCommandService` decorator, wired in front of the transactional service by the controller bean, retries a conflicting login once in a new transaction, so the retry replaces (copies from) the winner. Lifetime is governed by `max-lifetime` only: `expires = created + max-lifetime`, never slid.

**Tech Stack:** Java 23, Spring 6.2, JPA/Hibernate 6 (orm.xml), H2 (tests), Avro, JUnit 5, Mockito, Nimbus JOSE, TypeScript.

**Spec:** `docs/superpowers/plans/2026-10-23-granite-server-side-sessions.md` (Decisions, "Session lifecycle", Step 2). Builds on `2026-10-24-granite-server-side-sessions-step-1.md`.

## Global Constraints

- Java style per `Microservices/Granite.Session/Claude.md`: `////` header and footer, `public` on its own line then `class X`, return type on its own line, Allman braces, fluent setters, `(a,b)` with no space after commas, column-aligned fields.
- No static inner classes, no explanatory comments. TS uses `Array<T>`.
- Never log or `toString()` a session handle, an access token or a refresh token.
- No `@PreAuthorize`. Change only Granite.Session and docs. No build-file changes.
- Never run `jib`, `publish*`, `publishToMavenLocal`, or Yarn `build`/`test`/`publish-package`.
- Java tests run through the root composite build:
  `cd C:/Development/Granite && ./gradlew :Granite.Session:Tests:Granite.Session.ServiceTest:test :Granite.Session:Tests:Granite.Session.ServerTest:test :Granite.Session:Tests:Granite.Session.ClientTest:test --rerun`
- TS type-check: `node_modules/.bin/tsc --noEmit -p .` in `Components/Granite.Session.Service` (Client via a scratchpad tsconfig, as in Step 1).
- `closedReason` values exactly: `LOGOUT, EXPIRED, REPLACED, DISPLACED, REVOKED_SECURITY, ADMIN`.
- JWT claim name for the continuity ID: `cid`.
- Authorities are never copied between sessions; they come from User (password, Okta/OAuth2) or are re-read through `IUserQueryServiceClient.findUsersByKey` (access-token login).

## Decisions taken in this step

| Topic | Decision |
|---|---|
| Lifetime | `expires = created + max-lifetime` at login; never slid by `refresh` or `resolveSession`. `granite.session.session-ttl` is no longer read (constructor parameter removed). The purge job is unchanged: it deletes rows with `expires < now`, which now means past max-lifetime or closed. |
| Closing | `Session.close(reason,at)`: sets `closedReason`, clears `activeMarker`, sets `expires = min(expires,at)`, fires one `SessionUpdated`. Rows stay until purged. |
| Revocation on the query side | Closing truncates `expires`, so the Gateway's existing check (`sessions` non-empty and `expires > now`) treats a closed session as revoked with no Gateway change. `SessionData` also exposes `closedReason`; `SessionJwtValidator` rejects a closed session explicitly. |
| `SessionClosedReason` | Moved to `granite.session.service.requestreply` (Service component) with a TS mirror, so `SessionData`, events and the domain share it. |
| Events | Close and revocation events are `UPDATED` service events with the new Avro field `closedReason` (`["null","string"]`, default `null`). |
| Logout paths | `logout` closes as `LOGOUT` (already-closed: success, no change). `logoutAllForUser` closes every open session as `ADMIN`. Refresh-token reuse closes as `REVOKED_SECURITY`. |
| Uniqueness | Column `activeMarker BOOLEAN` + unique index `ux_session_active_user_device (userId, deviceId, activeMarker)` in `schema.sql`. |
| Retry | Login pipelines rethrow persistence/concurrency failures (exceptionally completed stage, which Spring rolls back); commit failures throw from the proxy. `ConcurrencyRetryingSessionCommandService` retries `login`/`loginWithOauth2` once on either; a second failure becomes `success=false`, `Unable to log in`. |
| Access-token login | Rejects a closed session; authorities are re-read from User (no longer copied). The lifecycle then applies like any other login. |

## Review Focus

1. U's open session on D has passed max-lifetime but was never resolved: login must close it as `EXPIRED`, must not copy from it, and must not trip the unique index. Test: `LoginLifecycleTest.expiredOpenSessionOnDeviceIsClosedNotCopied`.
2. Start from latest when U's most recent session elsewhere is closed and an older one is open: copy from the open one. Test: `LoginLifecycleTest.startFromLatestIgnoresClosedSessions`.
3. Copy must be independent: changing the new session's `values` must not alter the source's map. Test: `LoginLifecycleTest.copiedValuesAreIndependent`.
4. Refresh on a replaced (closed) session must fail even with a current refresh token. Test: `RefreshTest.refreshOnClosedSessionFails`.
5. Replaying an old refresh token of an already-closed session must not overwrite its reason. Test: `RefreshTest.reuseOnClosedSessionKeepsOriginalReason`.

---

### Task 1: Shared contract — `SessionClosedReason`, `SessionData` fields, event field

**Files:**
- Create: `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/SessionClosedReason.java`, `.../typescript/RequestReply/SessionClosedReason.ts`
- Delete: `Components/Granite.Session.Server/src/main/java/granite/session/server/domain/SessionClosedReason.java` (update imports in `Session`, `SessionCommandService`, tests)
- Modify: `SessionData.java`, `SessionData.ts`, `RequestReply/index.ts`, `src/main/schema/avro/SessionEvent.avsc`, `server/application/MappingModule.java`
- Test: `Tests/Granite.Session.ServerTest/.../application/MappingModuleTest.java`

**Interfaces:**
- Produces: `enum SessionClosedReason { LOGOUT, EXPIRED, REPLACED, DISPLACED, REVOKED_SECURITY, ADMIN }`; TS `type SessionClosedReason = "LOGOUT" | ...`; `SessionData.set/getContinuityId(UUID)`, `set/getClosedReason(SessionClosedReason)`, `set/getReplacedBy(Long)`, `set/getCopiedFrom(Long)`; TS `continuityId?: string; closedReason?: SessionClosedReason; replacedBy?: number; copiedFrom?: number`; Avro `SessionEvent.closedReason: String` (nullable).

- [ ] Step 1: Failing `MappingModuleTest.mapsLifecycleFields` (Session with continuityId, closedReason `REPLACED`, replacedBy 9, copiedFrom 7 → all four on `SessionData`). Depends on Task 2's Session fields, so Tasks 1 and 2 land in one commit if executed separately.
- [ ] Step 2: Implement; add Avro field `{"name":"closedReason","type":["null","string"],"default":null}`.
- [ ] Step 3: Run ServiceTest + ServerTest, `tsc --noEmit`; commit `feat(session): share SessionClosedReason and expose lifecycle fields in SessionData`.

### Task 2: Domain — lifecycle fields, close, uniqueness, open-session queries

**Files:**
- Modify: `server/domain/Session.java`, `ISessionRepository.java`, `SessionRepository.java`, `Components/Granite.Session.Server/src/main/resources/Session.orm.xml`, `.../resources/schema.sql`, `Tests/Granite.Session.ServerTest/src/test/resources/schema.sql`
- Test: `domain/SessionTest.java`, `domain/SessionRepositoryTest.java`

**Interfaces:**
- Produces on `Session`: `set/getContinuityId(UUID)`, `set/getReplacedBy(Long)`, `set/getCopiedFrom(Long)`, `set/getActiveMarker(Boolean)` (`TRUE` on construction), `setValues(Map<String,Object>)` (copies into a new `LinkedHashMap`), `close(SessionClosedReason reason,Instant at)` (replaces Step 1's `close(reason)`); `setClosedReason` keeps `activeMarker` in sync.
- Repository: `List<Session> findOpenByDeviceId(String deviceId)`, `List<Session> findOpenByUserId(Long userId)`, `Optional<Session> findOpenByUserIdAndDeviceId(Long userId,String deviceId)`; `findByUserIdAndDeviceId` now returns only the open session (`closedReason is null`).
- ORM: `continuityId` (Hibernate UUID mapping), `replacedBy`/`copiedFrom` BIGINT, `activeMarker` BOOLEAN; all nullable.
- schema.sql: `CREATE UNIQUE INDEX IF NOT EXISTS ux_session_active_user_device ON Session (userId, deviceId, activeMarker);`

- [ ] Step 1: Failing tests: `SessionTest.closeSetsReasonClearsMarkerTruncatesExpiresAndFiresOneUpdate`, `closeKeepsEarlierExpires`, `newSessionIsActive`, `setValuesCopiesMap`; `SessionRepositoryTest.secondActiveSessionForSamePairIsRejected` (second save throws inside the transaction), `closedSessionDoesNotBlockNewActiveSession`, `findOpenQueriesExcludeClosed`, `lifecycleFieldsRoundTrip`.
- [ ] Step 2: Implement; run ServerTest; commit `feat(session): lifecycle fields, close semantics and active-pair uniqueness`.

### Task 3: `cid` claim

**Files:** Modify `server/application/JwtTokenService.java`; Test `application/JwtTokenServiceTest.java`.

- [ ] Step 1: Failing `mintedJwtCarriesContinuityId` (`claims.getStringClaim("cid")` equals the session's UUID string) and `mintedJwtOmitsCidWhenAbsent`.
- [ ] Step 2: Add `CONTINUITY_ID_CLAIM = "cid"`, set only when non-null. Commit `feat(session): mint cid claim from continuityId`.

### Task 4: Lifetime governed by `max-lifetime`; every path rejects closed sessions

**Files:** Modify `SessionCommandService.java`, `ApplicationConfiguration.java`, `RefreshContext.java`; Tests `LoginTest`, `LoginWithOauth2Test`, `RefreshTest`, `ResolveSessionTest`, `LogoutTest`, `LogoutAllForUserTest` (constructor drops the `sessionTtl` argument).

**Interfaces:** `SessionCommandService(repository,jwtTokenService,refreshTokenService,userCommandServiceClient,userQueryServiceClient,oauth2Validator,loginGate,sender,mapper,Duration maxLifetime,Duration renewalMargin,Clock clock)`.

- [ ] Step 1: Tests: login `expires == NOW + maxLifetime`; `RefreshTest.refreshDoesNotSlideExpires`, `refreshOnClosedSessionFails`; `ResolveSessionTest` renewal keeps `expires` and still fires one `UPDATED`; `LoginTest.loginWithClosedSessionAccessTokenFails`, `loginWithAccessTokenRecalculatesAuthoritiesFromUser`.
- [ ] Step 2: Implement; remove `capExpires` sliding, `session-ttl` `@Value`. Commit `feat(session): govern session validity by max-lifetime only and reject closed sessions`.

### Task 5: Close instead of delete, with reasons on events

**Files:** Modify `SessionCommandService.java` (`destroy`→`closeForLogout`, `destroyAll`, `revokeOnReuse`, `toServiceEvent`), `Client/.../SessionJwtValidator.java`; Tests `LogoutTest`, `LogoutAllForUserTest`, `RefreshTest`, `ResolveSessionTest`, `ClientTest/.../SessionJwtValidatorTest`.

- [ ] Step 1: Tests: logout closes as `LOGOUT`, never deletes, emits one `UPDATED` with `closedReason="LOGOUT"`; already-closed logout succeeds without an event; logoutAll closes open sessions as `ADMIN`; reuse closes as `REVOKED_SECURITY` (`reuseOnClosedSessionKeepsOriginalReason`); expiry close event carries `EXPIRED`; validator rejects a session with `closedReason`.
- [ ] Step 2: Implement; commit `feat(session): close sessions with reasons and carry closedReason on events`.

### Task 6: Login lifecycle (all login paths)

**Files:** Create `server/application/LoginLifecycle.java` (holds `Session source`, `boolean replacing`); Modify `LoginContext.java`, `LoginWithOauth2Context.java`, `SessionCommandService.java`; Test `application/LoginLifecycleTest.java`, `LoginWithOauth2Test.java`.

**Pipeline:** `... applyLoginGate → applyLifecycle → createSession → mintJwt → linkReplaced → save → buildReply` (OAuth2 mirrors it).

**Algorithm (`prepareLifecycle(userId,deviceId,now)`):**
1. For each `findOpenByDeviceId(deviceId)`: not live (`expires <= now` or past `created + maxLifetime`) → `close(EXPIRED)`; other user → `close(DISPLACED)`; same user → it is the replace source, `close(REPLACED)`.
2. No replace source → source = live session from `findOpenByUserId(userId)` with the greatest `lastActivity` (nulls last).
3. `findOpenByUserIdAndDeviceId(userId,deviceId)` (flushes the closes before the new row is inserted); a hit means a concurrent winner → throw `ConcurrentLoginException`.
- New session: `values` copied from source (if any); `continuityId` = source's when replacing, else `UUID.randomUUID()`; `copiedFrom` = source ID; authorities from the login only. After the insert, `linkReplaced` sets `source.replacedBy`.

- [ ] Step 1: Failing tests in `LoginLifecycleTest`: `reloginOnSameDeviceReplaces` (same cid, values copied, old `REPLACED`, `replacedBy`/`copiedFrom` set), `otherUserOnDeviceIsDisplacedAndNothingPasses` (new cid, empty values, V's session `DISPLACED`, authorities U's), `firstSessionOnNewDeviceStartsFromLatest` (copies most recent by lastActivity, new cid), `startFromLatestIgnoresClosedSessions`, `noSessionAnywhereStartsEmpty`, `authoritiesAreNeverCopied`, `expiredOpenSessionOnDeviceIsClosedNotCopied`, `copiedValuesAreIndependent`, `accessTokenLoginAppliesLifecycle`; `LoginWithOauth2Test.oauth2LoginReplacesExistingSession`.
- [ ] Step 2: Implement; commit `feat(session): apply the login lifecycle table on every login path`.

### Task 7: Concurrency — retry once, racing logins

**Files:** Create `server/application/ConcurrentLoginException.java`, `server/application/ConcurrencyRetryingSessionCommandService.java`; Modify `SessionCommandService.java` (`buildReply(Throwable,..)` rethrows conflicts), `platform/CommandPlatformConfiguration.java` (controller wraps the service); Tests `application/ConcurrencyRetryingSessionCommandServiceTest.java`, `application/SessionCommandServiceServerTest.java`.

**Interfaces:** `ConcurrencyRetryingSessionCommandService(ISessionCommandService delegate) implements ISessionCommandService`; `static`-free helper `isConflict(Throwable)` walks causes for `ConcurrentLoginException`, `jakarta.persistence.PersistenceException`, `org.springframework.dao.DataAccessException`, `org.springframework.transaction.TransactionException`.

- [ ] Step 1: Failing unit tests: retries once after a synchronous conflict, retries once after an exceptionally completed stage, no retry for a non-conflict reply, second conflict → `success=false` `Unable to log in`, non-login operations pass through. H2 tests: `testReloginReplacesAndKeepsContinuity`, `testOtherUserDisplaces`, `testRacingLoginsLeaveExactlyOneActiveSession` (two threads, own request scopes, start latch, same user and device, repeated 5 rounds; both succeed, exactly one open row for the pair, both replies share one `continuityId`).
- [ ] Step 2: Implement; commit `feat(session): retry conflicting logins once so racing logins leave one active session`.

### Task 8: Documentation

- [ ] Update `Microservices/Granite.Session/Claude.md` (fields, lifecycle, closing, events, lifetime, retry, schema); commit `docs(session): document session lifecycle and continuity`.
