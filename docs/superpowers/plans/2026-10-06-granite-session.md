# Granite.Session Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce a new `Granite.Session` microservice that owns authentication and user-activity sessions, add supporting changes to `Granite.User` (password, externalIdentity, authenticateUser, findUsersByExternalIdentity), and extend `Granite.Gateway` to route to the two Session hosts.

**Architecture:** Session mirrors the Granite.User template (CQRS with CommandHost/QueryHost, layered platform/application/domain packages, Transactional Outbox + Kafka events, JPA/Hibernate with H2 for test). Access tokens are RS256 JWTs signed by Session with `jti=sessionId`; revocation is instant via a Session query. User gains two nullable columns (`password` bcrypt-hashed, `externalIdentity`) and two new query operations. Gateway adds two routes.

**Tech Stack:** Java 23, Spring Boot 3.4, Spring 6.2, JPA/Hibernate 6.4, PostgreSQL (prod) / H2 (test), ModelMapper 3.2, Kafka, Strata framework set (foundation/server/client/outbox), `spring-security-oauth2-jose` (Nimbus JOSE JWT), BCrypt, Gradle multi-project, Yarn workspace, TypeScript, JUnit Jupiter 5, Google Jib.

**Spec:** [`docs/superpowers/specs/2026-10-06-granite-session-design.md`](../specs/2026-10-06-granite-session-design.md)

## Global Constraints

- Java 23 (Amazon Corretto); Spring Boot 3.4.1; Spring 6.2.2; Hibernate 6.4.4.
- All Java interfaces prefixed `I`.
- File header/footer banner comments; `class` on its own line; Allman braces; method return type on separate line from method name; fluent setters returning `this`; column-aligned fields; compact single-line simple getters (per `Granite.User` CLAUDE.md).
- Follow the pipeline shape `validate → mapToContext → operation → buildReply` for every service method, returning `CompletionStage<Reply>`.
- No new entity in `Granite.User` beyond the existing `User`; add two columns only (`password`, `externalIdentity`).
- Session's access token is an RS256 JWT with claims `iss=granite-session`, `sub=userId`, `jti=sessionId`, `iat`, `exp`, `aud`, `did=deviceId`.
- Session ports: CommandHost 8088, QueryHost 8089.
- Transactional Outbox is the default event sender wiring on CommandHost; `OnCommitSessionEventSender` and `MockSessionEventSender` exist for alternative wiring and tests.
- TypeScript uses `Array<Type>` syntax, not `Type[]`.
- Any new Gateway route is added to all three profiles (`development`, `test`, `production`) and reflected in `Granite.Gateway/CLAUDE.md`.
- Never commit secrets. The dev-profile JWT keypair is a test keypair under `src/main/resources`; test/prod profiles externalize the key path via properties.
- Follow user's global rule: do not modify project build tooling (Gradle wrapper version, verdaccio, Yarn version, npm registry) without explicit permission; use existing scripts and daemons.

## Review Focus

- **Null credential or null credentialKind in `LoginRequest`**: `validate` must reject `credential == null`, `credential.isBlank()`, and `credentialKind == null` with a clear `IllegalArgumentException` message before any dispatch; otherwise users get an NPE from a downstream call. Covered by Task 17.
- **Expired JWT presented to `refresh`**: `refresh` must look up the session row and reject when `expires < now()` with `success=false` and no new JWT minted; otherwise expired sessions can be silently extended. Covered by Task 19.
- **OAuth2 `idToken` with wrong `aud` or wrong `iss`**: `Oauth2IdTokenValidator` must reject with an explicit audience/issuer failure message, not a generic signature error, so that misconfigured providers are diagnosable. Covered by Task 17.
- **Expired-but-not-logged-out session appearing in revocation lookup**: `findSessionsByKey(SessionKey.ofSessionId(jti))` returns a row where `expires < now()` must be treated as revoked by consumer convention; `SessionData` must expose `expires` and consumers must honor it (and the sample consumer-side helper in Granite.Session.Client must demonstrate this check). Covered by Task 22.
- **JIT provisioning race on `loginWithOauth2`**: two concurrent logins for the same new `externalIdentity` must not create two Users. Resolve by relying on a DB unique constraint on `user.external_identity` and recovering with a lookup on `createUser` failure. Covered by Task 17.

---

## Phase A — Granite.User foundation

These tasks must land first because Granite.Session.Server depends on them at compile time.

### Task 1: Add `CredentialKind` enum to Granite.User.Service

**Model:** Haiku 4.5

**Files:**
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/CredentialKind.java`
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/typescript/requestreply/CredentialKind.ts`

**Interfaces produced:**
- `enum CredentialKind { PASSWORD, ACCESS_TOKEN, SECURITY_CODE }` — used by `LoginRequest` and `AuthenticateUserRequest` alongside a sibling `String credential` field. **There is no Credential DTO**; credentials are always carried as the two-field pair `(credential: String, credentialKind: CredentialKind)` on whichever request needs them.

- [ ] **Step 1: Implement `CredentialKind` enum**

```java
//////////////////////////////////////////////////////////////////////////////
// CredentialKind.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

public
enum CredentialKind
{
    PASSWORD,
    ACCESS_TOKEN,
    SECURITY_CODE
}

//////////////////////////////////////////////////////////////////////////////
```

- [ ] **Step 2: Mirror in TypeScript**

Create `CredentialKind.ts` as a string enum (or discriminated union of literal types) with values `"PASSWORD" | "ACCESS_TOKEN" | "SECURITY_CODE"`. Export from `src/main/typescript/index.ts`.

- [ ] **Step 3: Compile check**

Run: `./gradlew :Microservices:Granite.User:Components:Granite.User.Service:compileJava`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.User/Components/Granite.User.Service
git commit -m "feat(user): add CredentialKind enum"
```

---

### Task 2: Add `password` and `externalIdentity` columns to the `User` entity

**Model:** Opus 4.6 (database and migration decisions are higher-stakes)

**Files:**
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/domain/User.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/CreateUserData.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/UpdateUserData.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/UserData.java`
- Modify TypeScript mirrors of the above three DTOs.
- Create: `Microservices/Granite.User/Components/Granite.User.Server/src/main/resources/db/migration/V2__add_user_password_and_external_identity.sql` (or extend existing schema setup if Flyway/Liquibase isn't used — check and fold into whichever bootstrap mechanism User currently uses).
- Modify: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/domain/UserTest.java` (add tests).

**Interfaces produced:**
- `User.setPassword(String hashed): User`, `User.getPassword(): String`.
- `User.setExternalIdentity(String): User`, `User.getExternalIdentity(): String`.
- `CreateUserData` / `UpdateUserData` carry **plain-text** `password` (hashed by command service before persistence). `UserData` carries `externalIdentity` but **never** `password`.

- [ ] **Step 1: Inspect current User persistence bootstrap**

Open `User.java` and the Server's `src/main/resources/` directory. Determine whether schema is managed via Flyway, Liquibase, or Hibernate `ddl-auto`. Record the finding — this drives whether Step 4 is a migration file or an `@Entity` change alone.

- [ ] **Step 2: Write failing entity test**

Add tests to `UserTest.java` asserting fluent `setPassword("hash") → getPassword() == "hash"` and `setExternalIdentity("google:abc") → getExternalIdentity() == "google:abc"`; both default to `null` on a fresh entity.

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :Microservices:Granite.User:Tests:Granite.User.ServerTest:test --tests "*UserTest*"`
Expected: FAIL — methods not found.

- [ ] **Step 4: Add fields to `User.java`**

```java
@Column(name = "password", nullable = true)
private String password;

@Column(name = "external_identity", nullable = true, unique = true)
private String externalIdentity;
```

Add fluent setters returning `User` and compact getters following the existing style in `User.java`. Add a unique index on `external_identity` (via `@Table(indexes = ...)` or in the migration file depending on Step 1 finding).

- [ ] **Step 5: Add `password` and `externalIdentity` to DTOs**

- `CreateUserData`: add private fields `password` and `externalIdentity`, fluent setters returning `CreateUserData`, getters.
- `UpdateUserData`: same.
- `UserData`: add `externalIdentity` only (no `password`). Confirm Jackson serialization excludes any accidental `password` property.

Mirror in TypeScript: `CreateUserData.ts`, `UpdateUserData.ts`, `UserData.ts`. Both plain-text `password?` on create/update data; `UserData` has `externalIdentity?` only.

- [ ] **Step 6: Add server-test coverage for DTOs**

Assert that serialization of `UserData` to JSON contains no `password` key even when the field is accidentally added to the backing map. Guard against regression.

- [ ] **Step 7: Run full User test suite**

Run: `./gradlew :Microservices:Granite.User:Tests:Granite.User.ServerTest:test :Microservices:Granite.User:Tests:Granite.User.ServiceTest:test`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add Microservices/Granite.User/Components/Granite.User.Server Microservices/Granite.User/Components/Granite.User.Service Microservices/Granite.User/Tests
git commit -m "feat(user): add password and externalIdentity columns to User"
```

---

### Task 3: BCrypt-hash passwords in `UserCommandService.create/update`

**Model:** Sonnet

**Files:**
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/application/UserCommandService.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/application/ApplicationConfiguration.java`
- Create: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/application/UserPasswordHashingTest.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/build.gradle` (add `spring-security-crypto` dependency).

**Interfaces produced:**
- `UserCommandService` constructor-injects a `PasswordEncoder` (BCrypt, strength 12).
- `createUser`/`updateUser` hash `CreateUserData.password` / `UpdateUserData.password` before persisting; a null or blank password passes through as-is (null-only in User.password column).

- [ ] **Step 1: Add `spring-security-crypto` dependency**

Add to Server's `build.gradle`:
```groovy
implementation "org.springframework.security:spring-security-crypto:${spring_security_version}"
```

- [ ] **Step 2: Write failing test**

```java
@Test
public void
createUserHashesPasswordBeforePersist()
{
    CreateUserRequest req = new CreateUserRequest()
        .setData(new CreateUserData().setUserName("alice").setPassword("hunter2"));

    CreateUserReply reply = service.createUser(req).toCompletableFuture().join();

    assertTrue(reply.isSuccess());
    User persisted = repository.findById(reply.getCreatedUser().getUserId()).orElseThrow();
    assertNotEquals("hunter2", persisted.getPassword());
    assertTrue(encoder.matches("hunter2", persisted.getPassword()));
}

@Test
public void
createUserWithNullPasswordPersistsNullPassword()
{
    CreateUserRequest req = new CreateUserRequest()
        .setData(new CreateUserData().setUserName("bob"));

    CreateUserReply reply = service.createUser(req).toCompletableFuture().join();

    assertTrue(reply.isSuccess());
    User persisted = repository.findById(reply.getCreatedUser().getUserId()).orElseThrow();
    assertNull(persisted.getPassword());
}
```

- [ ] **Step 3: Run test to verify it fails**

Run: `./gradlew :Microservices:Granite.User:Tests:Granite.User.ServerTest:test --tests "*UserPasswordHashingTest*"`
Expected: FAIL.

- [ ] **Step 4: Wire `PasswordEncoder` in `ApplicationConfiguration`**

```java
@Bean
public PasswordEncoder
passwordEncoder()
{
    return new BCryptPasswordEncoder(12);
}
```

- [ ] **Step 5: Constructor-inject `PasswordEncoder` into `UserCommandService` and hash in the `create` / `update` pipeline steps**

In the `mapToContext` or persistence step: if `data.getPassword() != null && !data.getPassword().isBlank()`, replace with `encoder.encode(data.getPassword())`. Otherwise pass through unchanged (so update does not clear an existing password).

- [ ] **Step 6: Run tests to verify they pass**

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add Microservices/Granite.User
git commit -m "feat(user): bcrypt-hash passwords in UserCommandService"
```

---

### Task 4: Add `authenticateUser` command operation to Granite.User

**Model:** Opus 4.6 (new RPC-facing operation; security-sensitive)

**Files:**
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/AuthenticateUserRequest.java`
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/AuthenticateUserReply.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/IUserCommandService.java` (add method)
- Create: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/application/AuthenticateUserContext.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/application/UserCommandService.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/platform/UserCommandServiceController.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Client/src/main/java/granite/user/client/UserCommandServiceClient.java`
- Modify: `Microservices/Granite.User/Components/Granite.User.Client/src/main/java/granite/user/client/IUserCommandServiceClient.java`
- Mirror in TypeScript (Service DTOs/interface, Client method).
- Create: `Microservices/Granite.User/Tests/Granite.User.ServerTest/src/test/java/granite/user/server/application/AuthenticateUserTest.java`

**Interfaces produced:**
- Request: `AuthenticateUserRequest extends AbstractServiceRequest { String userName; String credential; CredentialKind credentialKind; }` — three top-level scalar fields, no nested DTO.
- Reply: `AuthenticateUserReply extends AbstractServiceReply { boolean valid; Long userId; }` (userId null when invalid).
- `IUserCommandService.authenticateUser(AuthenticateUserRequest): CompletionStage<AuthenticateUserReply>`.
- `IUserCommandServiceClient.authenticateUser(...)` client-side equivalent.
- REST path: `POST /user-command-service/authenticate-user`.

- [ ] **Step 1: Write failing server-test**

Six scenarios:
```java
@Test void returnsValidTrueWithUserIdForCorrectPassword()
@Test void returnsValidFalseForWrongPassword()
@Test void returnsValidFalseForAbsentUser()
@Test void returnsValidFalseWithUnsupportedKindMessageForAccessToken()
@Test void returnsValidFalseWithUnsupportedKindMessageForSecurityCode()
@Test void returnsValidFalseForUserWithNullPassword()
```

Each creates a User via the repository, then calls `service.authenticateUser(req)` and asserts on the reply.

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :Microservices:Granite.User:Tests:Granite.User.ServerTest:test --tests "*AuthenticateUserTest*"`
Expected: FAIL (method not defined).

- [ ] **Step 3: Create request/reply DTOs**

Follow `FindUsersByKeyRequest.java` as the template for `AuthenticateUserRequest` (fluent setter overrides for `requestId`, `timestamp`, `addHeader`, etc.) with three top-level fields `userName`, `credential` (String), `credentialKind` (CredentialKind) — no nested Credential object. Follow `FindUsersByKeyReply.java` as the template for `AuthenticateUserReply` with fields `valid` (boolean) and `userId` (nullable Long).

- [ ] **Step 4: Add method to `IUserCommandService` and TypeScript mirror**

```java
CompletionStage<AuthenticateUserReply>
authenticateUser(AuthenticateUserRequest request);
```

- [ ] **Step 5: Create `AuthenticateUserContext`**

Follow `FindUsersByKeyContext.java`. Fields: `request`, `user` (nullable), `valid`.

- [ ] **Step 6: Implement in `UserCommandService`**

Pipeline:
- `validate`: `Objects.requireNonNull(request, "request")`; `Objects.requireNonNull(request.getUserName(), "userName")`; `Objects.requireNonNull(request.getCredential(), "credential")`; reject `request.getCredential().isBlank()`; `Objects.requireNonNull(request.getCredentialKind(), "credentialKind")`.
- `mapToContext`: build `AuthenticateUserContext`.
- `lookupUser`: `repository.findByUserName(request.getUserName())` → set `context.setUser(...)`.
- `check`: dispatch on `request.getCredentialKind()`:
  - PASSWORD: `encoder.matches(request.getCredential(), user.getPassword())` only when `user != null && user.getPassword() != null`.
  - ACCESS_TOKEN or SECURITY_CODE: `valid = false`, failureMessage=`"CredentialKind " + kind + " is not supported by Granite.User.authenticateUser"`.
- `buildReply`: `valid=true` reply carries `userId`; `valid=false` reply has `userId=null`.

Note: if `findByUserName` is not yet on `IUserRepository`, add it with a named JPA query `findByUserName`.

- [ ] **Step 7: Add REST endpoint**

```java
@PostMapping("/authenticate-user")
public AuthenticateUserReply
authenticateUser(@RequestBody AuthenticateUserRequest request)
{
    return service.authenticateUser(request).toCompletableFuture().join();
}
```

- [ ] **Step 8: Add client method (Java + TypeScript) + ClientTest**

Follow `UserCommandServiceClient.createUser` as the template for the Java side. Add TypeScript equivalent.

- [ ] **Step 9: Run tests**

Expected: PASS.

- [ ] **Step 10: Commit**

```bash
git add Microservices/Granite.User
git commit -m "feat(user): add authenticateUser command operation"
```

---

### Task 5: Add `findUsersByExternalIdentity` query operation to Granite.User

**Model:** Sonnet

**Files:**
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/FindUsersByExternalIdentityRequest.java`
- Create: `Microservices/Granite.User/Components/Granite.User.Service/src/main/java/granite/user/service/requestreply/FindUsersByExternalIdentityReply.java`
- Modify: `IUserQueryService.java` (add method)
- Create: `Microservices/Granite.User/Components/Granite.User.Server/src/main/java/granite/user/server/application/FindUsersByExternalIdentityContext.java`
- Modify: `UserQueryService.java`, `UserQueryServiceController.java`, `UserQueryServiceClient.java`, `IUserQueryServiceClient.java`.
- Mirror in TypeScript.
- Create: server-test and client-test for the new operation.

**Interfaces produced:**
- Request: `FindUsersByExternalIdentityRequest { String provider; String externalId; }` — `provider` is the OAuth2 provider name (e.g., `"google"`), `externalId` is the `sub` claim.
- Reply: `FindUsersByExternalIdentityReply { List<UserData> users; }` — zero or one.
- `IUserRepository` gains `findByExternalIdentity(String composite)`.
- REST path: `POST /user-query-service/find-users-by-external-identity`.

- [ ] **Step 1: Write failing server-test**

Three scenarios: user with matching externalIdentity returns one; no match returns empty; null externalIdentity in request fails validation.

- [ ] **Step 2: Run to verify failure**

- [ ] **Step 3: Create DTOs, interface method, context, repository method, service pipeline, REST endpoint, client method, and TypeScript mirrors**

Follow the same pattern as `findUsersByKey`. The repository composite stored in `user.external_identity` is `provider + ":" + externalId`.

- [ ] **Step 4: Run tests**

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.User
git commit -m "feat(user): add findUsersByExternalIdentity query operation"
```

---

## Phase B — Granite.Session scaffolding and Service contracts

### Task 6: Create Granite.Session root project scaffolding

**Model:** Sonnet (file layout is mechanical but spans many files)

**Files (all under `Microservices/Granite.Session/`):**
- Create: `settings.gradle` (copied and renamed from Granite.User's)
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `gradlew`, `gradlew.bat`, `.gradle/wrapper/*` (copy from Granite.User)
- Create: `.gitignore`, `.gitlab-ci.yml`
- Create: `package.json`
- Create: `CLAUDE.md` (describes architecture — adapt from Granite.User's CLAUDE.md and the design spec)

**Interfaces produced:** none at code level — subproject identifiers for subsequent tasks:
- `:Microservices:Granite.Session:Components:Granite.Session.Service`
- `:Microservices:Granite.Session:Components:Granite.Session.Server`
- `:Microservices:Granite.Session:Components:Granite.Session.Client`
- `:Microservices:Granite.Session:Applications:Granite.Session.CommandHost`
- `:Microservices:Granite.Session:Applications:Granite.Session.QueryHost`
- `:Microservices:Granite.Session:Tests:Granite.Session.{ServiceTest,ServerTest,ClientTest,TestSuite}`

- [ ] **Step 1: Copy Granite.User's `settings.gradle`, replace all `User` with `Session`**

Use Granite.User/settings.gradle as source of truth; replace `rootProject.name = 'Granite.User'` with `'Granite.Session'`; replace all `Granite.User.*` include paths with `Granite.Session.*`. Keep the Strata and Confluent repositories.

- [ ] **Step 2: Copy and adapt `build.gradle`**

Keep version/plugin configuration identical; change `group 'granite.user'` → `group 'granite.session'`. Keep all `ext { ... }` version numbers.

- [ ] **Step 3: Create `gradle.properties`, copy Gradle wrapper files from Granite.User unchanged**

- [ ] **Step 4: Create `package.json`**

Copy from `Granite.User/package.json`, replace `Granite.User` with `Granite.Session`, rename script keys (`clean-granite-user-service` → `clean-granite-session-service`, etc.). Scripts to include: `clean-granite-session-service`, `clean-granite-session-client`, `clean-components`, `clean`, deep-clean variants, `build-granite-session-service`, `build-granite-session-client`, `build-components`, `build`, `test`.

- [ ] **Step 5: Create `.gitignore`, `.gitlab-ci.yml`**

Copy from Granite.User unchanged.

- [ ] **Step 6: Create `CLAUDE.md`**

Adapt Granite.User's CLAUDE.md to describe Granite.Session: entity (per spec §4), operations (per spec §5), REST endpoints, event flow, package structure. Preserve the Coding Style section verbatim (identical rules apply).

- [ ] **Step 7: Verify project loads**

Run: `./gradlew :Microservices:Granite.Session:projects`
Expected: lists all 11 subprojects without error. (Subprojects are empty at this point — a later task adds a build.gradle to each.)

- [ ] **Step 8: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "chore(session): scaffold Granite.Session root project"
```

---

### Task 7: Create empty subproject skeletons for Granite.Session

**Model:** Haiku 4.5

**Files:** per subproject, create a minimal `build.gradle` that compiles and runs tests (empty source trees). Follow the equivalent file under `Granite.User`.

- `Applications/Granite.Session.CommandHost/build.gradle`
- `Applications/Granite.Session.QueryHost/build.gradle`
- `Components/Granite.Session.Service/build.gradle`, `package.json`, `tsconfig.json`, `src/main/typescript/package.json`
- `Components/Granite.Session.Server/build.gradle`
- `Components/Granite.Session.Client/build.gradle`, `package.json`, `tsconfig.json`, `src/main/typescript/package.json`
- `Tests/Granite.Session.ServiceTest/build.gradle`
- `Tests/Granite.Session.ServerTest/build.gradle`
- `Tests/Granite.Session.ClientTest/build.gradle`
- `Tests/Granite.Session.TestSuite/build.gradle`

**Interfaces produced:** every subproject's dependency identifier is now a Gradle project reference.

- [ ] **Step 1: Create each build.gradle by copying the Granite.User sibling and rewriting `user` → `session` throughout**

For instance, `Granite.Session.Server/build.gradle` is `Granite.User.Server/build.gradle` with all `granite.user.*` package references → `granite.session.*` and all project dependencies `':Microservices:Granite.User:Components:*'` → `':Microservices:Granite.Session:Components:*'`.

- [ ] **Step 2: Copy and adapt TypeScript package.json / tsconfig.json for Service and Client**

Name becomes `granite.session.service` and `granite.session.client`. Scripts identical. TypeScript `tsconfig.json` identical.

- [ ] **Step 3: Verify empty build succeeds**

Run: `./gradlew :Microservices:Granite.Session:build -x test`
Expected: PASS (nothing to compile, but all projects evaluate).

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "chore(session): add empty subproject build files"
```

---

### Task 8: Session.Service — core value types (`SessionData`, `SessionKey`, `SessionKeySet`)

**Model:** Sonnet

**Files:**
- Create: `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/SessionData.java`
- Create: `.../SessionKey.java`
- Create: `.../SessionKeySet.java`
- Create: TypeScript mirrors.
- Create: `Tests/Granite.Session.ServiceTest/src/test/java/granite/session/service/requestreply/SessionKeyTest.java`
- Create: `.../SessionKeySetTest.java`

**Interfaces produced:**
- `SessionData { Long sessionId; Long userId; String accessToken; Instant created; Instant expires; String deviceId; Map<String,Object> values; }` with fluent setters and getters.
- `SessionKey` modeled after `UserKey`: `(name, value)` pair with name constants `SESSION_ID`, `ACCESS_TOKEN`, `USER_ID`. Static factories `ofSessionId(Long)`, `ofAccessToken(String)`, `ofUserId(Long)`. `getValueAsLong()` helper.
- `SessionKeySet` modeled after `UserKeySet`: name constants `SESSION_IDS`, `ACCESS_TOKENS`, `USER_IDS`. Static factories `ofSessionIds(Set<Long>)`, `ofAccessTokens(Set<String>)`, `ofUserIds(Set<Long>)`, `empty(name)`.

- [ ] **Step 1: Write failing tests**

```java
@Test void sessionKeyAcceptsOnlyKnownNames()
@Test void sessionKeyFactoriesSetCorrectNameAndStringValue()
@Test void sessionKeyEqualsAndHashCode()
@Test void sessionKeyJsonRoundTrip()
@Test void sessionKeySetValuesAsLongsConverts()
```

- [ ] **Step 2: Run to verify failure**

- [ ] **Step 3: Implement SessionKey**

Copy `UserKey.java`, replace `UserKey → SessionKey`, replace name constants and factories per the Interfaces section above. Keep validate switch covering all three constants.

- [ ] **Step 4: Implement SessionKeySet**

Copy `UserKeySet.java`, replace accordingly.

- [ ] **Step 5: Implement SessionData**

Follow `UserData.java` as the fluent-setter template. Include all seven fields from the Interfaces section.

- [ ] **Step 6: TypeScript mirror**

`SessionData.ts`, `SessionKey.ts`, `SessionKeySet.ts` as interfaces + helper builder modules. Use `Array<Type>` syntax.

- [ ] **Step 7: Run tests**

- [ ] **Step 8: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add SessionData, SessionKey, SessionKeySet value types"
```

---

### Task 9: Session.Service — Command Request/Reply DTOs

**Model:** Sonnet

**Files:** create under `Components/Granite.Session.Service/src/main/java/granite/session/service/requestreply/` and the TypeScript mirror directory:

- `LoginRequest.java` / `LoginReply.java`
- `LoginWithOauth2Request.java` / `LoginWithOauth2Reply.java`
- `LogoutRequest.java` / `LogoutReply.java`
- `LogoutAllForUserRequest.java` / `LogoutAllForUserReply.java`
- `RefreshRequest.java` / `RefreshReply.java`
- TypeScript mirrors.

**Interfaces produced:**
- `LoginRequest extends AbstractServiceRequest { String userName; String credential; CredentialKind credentialKind; String deviceId; }` (reuses `granite.user.service.requestreply.CredentialKind` from Task 1 via `Granite.User.Service` dependency — add `implementation project(':Microservices:Granite.User:Components:Granite.User.Service')` to Session.Service's build.gradle).
- `LoginReply extends AbstractServiceReply { SessionData session; String accessToken; }`
- `LoginWithOauth2Request { String provider; String idToken; String deviceId; }`; `LoginWithOauth2Reply { SessionData session; String accessToken; }`
- `LogoutRequest { Long sessionId; }`; `LogoutReply` (empty payload beyond base).
- `LogoutAllForUserRequest { Long userId; }`; `LogoutAllForUserReply { Set<Long> destroyed; }`
- `RefreshRequest { Long sessionId; }`; `RefreshReply { SessionData session; String accessToken; }`

- [ ] **Step 1: Add `Granite.User.Service` dependency to `Granite.Session.Service` build.gradle**

```groovy
implementation project(':Microservices:Granite.User:Components:Granite.User.Service')
```

- [ ] **Step 2: Create all request/reply classes**

Follow `CreateUserRequest.java` and `CreateUserReply.java` as templates. Each class includes the mandatory fluent-setter overrides for `setRequestId`, `setTimestamp`, `addHeader`, `clearHeaders`, `addCookie`, `clearCookies` (returning the concrete class type via cast).

- [ ] **Step 3: Create TypeScript mirrors**

Each as a TypeScript interface in `src/main/typescript/requestreply/`.

- [ ] **Step 4: Compile check**

Run: `./gradlew :Microservices:Granite.Session:Components:Granite.Session.Service:compileJava`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add command Request/Reply DTOs"
```

---

### Task 10: Session.Service — Query Request/Reply DTOs

**Model:** Sonnet

**Files:**
- `FindSessionsByKeyRequest.java` / `FindSessionsByKeyReply.java`
- `FindSessionsByKeySetRequest.java` / `FindSessionsByKeySetReply.java`
- `FindSessionByUserIdAndDeviceIdRequest.java` / `FindSessionByUserIdAndDeviceIdReply.java`
- TypeScript mirrors.

**Interfaces produced:**
- `FindSessionsByKeyRequest { SessionKey key; boolean returnIfFound; }` → `FindSessionsByKeyReply { List<SessionData> sessions; }`
- `FindSessionsByKeySetRequest { SessionKeySet keySet; }` → `FindSessionsByKeySetReply { List<SessionData> sessions; }`
- `FindSessionByUserIdAndDeviceIdRequest { Long userId; String deviceId; }` → `FindSessionByUserIdAndDeviceIdReply { SessionData session; }` (session may be null if not found).

- [ ] **Step 1: Create classes following `FindUsersByKey*` templates**

- [ ] **Step 2: Create TypeScript mirrors**

- [ ] **Step 3: Compile check**

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add query Request/Reply DTOs"
```

---

### Task 11: Session.Service — Service interfaces and index.ts exports

**Model:** Sonnet

**Files:**
- Create: `Components/Granite.Session.Service/src/main/java/granite/session/service/ISessionCommandService.java`
- Create: `Components/Granite.Session.Service/src/main/java/granite/session/service/ISessionQueryService.java`
- Modify: `src/main/typescript/index.ts` to export all public types.

**Interfaces produced:**
- `ISessionCommandService.login/loginWithOauth2/logout/logoutAllForUser/refresh`, each returning `CompletionStage<Reply>`.
- `ISessionQueryService.findSessionsByKey/findSessionsByKeySet/findSessionByUserIdAndDeviceId`, each returning `CompletionStage<Reply>`.

- [ ] **Step 1: Create `ISessionCommandService`**

Follow `IUserCommandService.java` layout. Five method signatures.

- [ ] **Step 2: Create `ISessionQueryService`**

Follow `IUserQueryService.java`. Three method signatures.

- [ ] **Step 3: Mirror in TypeScript — interfaces for both services, re-exports in `index.ts`**

- [ ] **Step 4: Compile**

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add ISessionCommandService and ISessionQueryService"
```

---

### Task 12: Session.Service — Events and event-sender/receiver interfaces

**Model:** Sonnet

**Files:**
- Create: `.../event/AbstractSessionEvent.java`, `SessionCreated.java`, `SessionUpdated.java`, `SessionDestroyed.java`.
- Create: `.../event/ISessionEventSender.java`, `ISessionEventReceiver.java`, `ISessionEventListener.java`, `ISessionEventObserver.java`, `ISessionEventSource.java`.
- Mirror in TypeScript.

**Interfaces produced:**
- `AbstractSessionEvent` carries `SessionData` snapshot, `UUID originatingRequestId`, `Instant timestamp`. `SessionCreated`, `SessionUpdated`, `SessionDestroyed` are concrete subtypes.
- `ISessionEventSender.send(AbstractSessionEvent event): CompletionStage<Void>`.
- `ISessionEventObserver.onEvent(AbstractSessionEvent): void` and `onException(Throwable)`.
- `ISessionEventReceiver` / `ISessionEventListener` / `ISessionEventSource` mirror the User equivalents.

- [ ] **Step 1: Create event classes following `AbstractUserEvent.java`, `UserCreated.java`, etc.**

- [ ] **Step 2: Create interfaces following `IUserEventSender.java`, etc.**

- [ ] **Step 3: TypeScript mirror of event types.**

- [ ] **Step 4: Compile**

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Session events and event-port interfaces"
```

---

### Task 13: Session.Service — publish the npm package

**Model:** Haiku 4.5

**Files:** none to modify beyond Service build triggers; this task runs the publish and verifies the local Verdaccio registry has `granite.session.service` at `1.0.0-snapshot`.

- [ ] **Step 1: Run publish**

Run: `cd Microservices/Granite.Session/Components/Granite.Session.Service && yarn run publish-package`
Expected: Verdaccio reports a new snapshot version.

- [ ] **Step 2: Verify version exists on local registry**

Run: `curl http://localhost:4873/granite.session.service`
Expected: JSON showing the published versions.

- [ ] **Step 3: No commit** — nothing changed under source control.

---

## Phase C — Granite.Session.Server implementation

### Task 14: Session domain — entity, repository, domain events

**Model:** Opus 4.6

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/domain/Session.java`
- Create: `.../domain/ISessionRepository.java`
- Create: `.../domain/SessionRepository.java`
- Create: `.../domain/SessionCreated.java`, `SessionUpdated.java`, `SessionDestroyed.java` (domain-tier events)
- Create: `.../domain/IDomainSessionEventObserver.java`
- Create: `.../domain/DomainConfiguration.java`
- Create: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/domain/SessionTest.java`
- Create: `.../SessionRepositoryTest.java`

**Interfaces produced:**
- `Session extends AbstractEntityAndDomainEventSource` with fields from spec §4; fluent setters that fire the appropriate domain event on mutation.
- `ISessionRepository extends IRepository<Long,Session>` with `findByAccessToken(String)`, `findAllByUserId(Long)`, `findByUserIdAndDeviceId(Long,String)` and `findAllByIdIn(Collection<Long>)`, `findAllByAccessTokenIn(Collection<String>)`, `findAllByUserIdIn(Collection<Long>)`.
- Domain events are the server-side events `User`/`Session` fire; the application layer translates them to `granite.session.service.event.*` types.

- [ ] **Step 1: Write failing entity test**

Assert: construction with required fields; `destroy()` fires a `SessionDestroyed` domain event; `setExpires(Instant)` fires `SessionUpdated`; `setValues(Map)` fires `SessionUpdated`.

- [ ] **Step 2: Write failing repository test**

Assert: `findByAccessToken`, `findByUserIdAndDeviceId`, `findAllByUserId` round-trip persisted sessions against H2.

- [ ] **Step 3: Implement `Session.java`**

```java
@Entity
@Table(
    name = "sessions",
    indexes = {
        @Index(name = "ux_sessions_access_token", columnList = "access_token", unique = true),
        @Index(name = "ix_sessions_user_id", columnList = "user_id"),
        @Index(name = "ix_sessions_user_id_device_id", columnList = "user_id, device_id")
    })
public
class Session
    extends AbstractEntityAndDomainEventSource
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "access_token", nullable = false, length = 2048)
    private String accessToken;

    @Column(name = "created", nullable = false)
    private Instant created;

    @Column(name = "expires", nullable = false)
    private Instant expires;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @ElementCollection
    @CollectionTable(
        name = "session_values",
        joinColumns = @JoinColumn(name = "session_id"))
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    private Map<String,Object> values;
    // ... fluent setters, getters, destroy(), event firing
}
```

Fire a `granite.session.server.domain.SessionCreated` from a `postPersist` hook (or a factory `create(...)` method), `SessionUpdated` on mutations, `SessionDestroyed` on `destroy()`.

- [ ] **Step 4: Implement `ISessionRepository` + `SessionRepository`**

Follow `UserRepository.java`. Register the named queries listed above.

- [ ] **Step 5: Implement domain-tier event classes and observer interface**

Follow `UserCreated.java` etc. These are the domain-tier events; the application layer will translate them to service-level events.

- [ ] **Step 6: Create `DomainConfiguration`**

Spring `@Configuration` wiring `SessionRepository` as a bean (mirrors Granite.User's domain configuration).

- [ ] **Step 7: Run tests**

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Session domain entity and repository"
```

---

### Task 15: `JwtTokenService` — mint and verify RS256 JWTs

**Model:** Opus 4.6 (crypto, security-sensitive)

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/JwtTokenService.java`
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/IJwtTokenService.java`
- Modify: `Granite.Session.Server/build.gradle` (add `spring-security-oauth2-jose` dependency)
- Create: `src/main/resources/jwt-test-private-key.pem`, `jwt-test-public-key.pem` (test-only RS256 keypair; dev-profile only).
- Create: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/JwtTokenServiceTest.java`

**Interfaces produced:**
- `IJwtTokenService.mint(Session session): String` — returns RS256-signed JWT with claims per spec §7.
- `IJwtTokenService.verifySignatureAndExp(String jwt): JwtVerificationResult` — returns parsed claims on success; `JwtVerificationException` with explicit reason on failure (bad signature, expired, bad issuer, bad audience).

- [ ] **Step 1: Add dependency**

```groovy
implementation "org.springframework.security:spring-security-oauth2-jose:${spring_security_version}"
```

- [ ] **Step 2: Write failing test**

```java
@Test void mintedJwtContainsRequiredClaims()
@Test void verifyAcceptsValidJwt()
@Test void verifyRejectsExpiredJwt()                        // Review Focus #2
@Test void verifyRejectsWrongAudience()                     // Review Focus #3
@Test void verifyRejectsWrongIssuer()
@Test void verifyRejectsTamperedSignature()
@Test void mintedJwtJtiEqualsSessionId()
```

- [ ] **Step 3: Run to verify failure**

- [ ] **Step 4: Generate test keypair**

Run (one-off, outside the plan commits):
```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out jwt-test-private-key.pem
openssl rsa -in jwt-test-private-key.pem -pubout -out jwt-test-public-key.pem
```

Place both PEMs in `src/main/resources/`. Add a comment at the top of each: `# TEST KEY — DO NOT USE OUTSIDE DEV PROFILE`.

- [ ] **Step 5: Implement `JwtTokenService` using Nimbus**

- Construct `RSASSASigner` with the loaded private key.
- `mint`: build JWT claims (`iss`, `sub`, `jti`, `iat`, `exp`, `aud`, `did`), sign with RS256, serialize.
- `verifySignatureAndExp`: parse with `SignedJWT.parse`, verify signature with public key, assert `iss == granite-session`, `aud` contains the configured audience, `exp > now()`.

Key material and audience come from Spring properties:
- `granite.session.jwt.private-key-location`
- `granite.session.jwt.public-key-location`
- `granite.session.jwt.audience`
- `granite.session.jwt.ttl` (Duration)

- [ ] **Step 6: Wire via `@ConfigurationProperties` or `@Value` in `ApplicationConfiguration` (next task)**

- [ ] **Step 7: Run tests**

Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add JwtTokenService for RS256 access tokens"
```

---

### Task 16: `Oauth2IdTokenValidator` — verify provider ID tokens

**Model:** Opus 4.6 (OAuth2 security details)

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/Oauth2IdTokenValidator.java`
- Create: `.../IOauth2IdTokenValidator.java`
- Create: `.../Oauth2ProviderConfig.java` (holds `name`, `issuerUri`, `audience`, `jwksUri`).
- Create: `.../Oauth2ProvidersProperties.java` (`@ConfigurationProperties("granite.session.oauth2")`, holding a `Map<String,Oauth2ProviderConfig> providers`).
- Create: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/Oauth2IdTokenValidatorTest.java`

**Interfaces produced:**
- `IOauth2IdTokenValidator.validate(String provider, String idToken): Oauth2ValidatedIdentity` where `Oauth2ValidatedIdentity { String provider; String sub; Optional<String> email; }` or throws `Oauth2ValidationException` with explicit reason.

- [ ] **Step 1: Write failing test using WireMock-hosted JWKS**

Scenarios:
```java
@Test void acceptsValidIdToken()
@Test void rejectsWrongAudience()                           // Review Focus #3
@Test void rejectsWrongIssuer()
@Test void rejectsExpired()
@Test void rejectsSignatureFromDifferentKey()
@Test void rejectsUnknownProviderName()
```

- [ ] **Step 2: Implement using `NimbusJwtDecoder.withJwkSetUri(providerConfig.jwksUri).build()`**

Validate `iss`, `aud`, `exp`. Extract `sub` and `email`.

- [ ] **Step 3: Run tests**

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Oauth2IdTokenValidator"
```

---

### Task 17: `SessionCommandService` — `login` dispatch (PASSWORD, ACCESS_TOKEN, SECURITY_CODE)

**Model:** Opus 4.6 (service orchestration and multi-path dispatch)

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/SessionCommandService.java` (initial version — login only; later tasks add logout/refresh)
- Create: `.../LoginContext.java`
- Create: `.../MappingModule.java` (ModelMapper configuration)
- Modify: `Granite.Session.Server/build.gradle` to add `implementation project(':Microservices:Granite.User:Components:Granite.User.Client')`
- Create: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/application/LoginTest.java`

**Interfaces produced:**
- `SessionCommandService` constructor-injects `ISessionRepository`, `IJwtTokenService`, `IUserCommandServiceClient` (for `authenticateUser` in login and `createUser` in loginWithOauth2), `IUserQueryServiceClient` (for `findUsersByExternalIdentity` in loginWithOauth2), `ISessionEventSender`, `ModelMapper`, `Clock`, `Duration sessionTtl`.
- Pipeline for `login`: validate → mapToContext → dispatch (PASSWORD / ACCESS_TOKEN / SECURITY_CODE) → createSession → mintJwt → save → buildReply.

- [ ] **Step 1: Write failing tests**

```java
@Test void loginWithValidPasswordMintsSession()
@Test void loginWithWrongPasswordReturnsSuccessFalse()
@Test void loginWithNullCredentialFailsValidation()         // Review Focus #1
@Test void loginWithBlankCredentialFailsValidation()        // Review Focus #1
@Test void loginWithNullCredentialKindFailsValidation()     // Review Focus #1
@Test void loginWithAccessTokenMintsNewSessionForSameUser()
@Test void loginWithAccessTokenDoesNotDestroyOriginalSession()
@Test void loginWithExpiredAccessTokenReturnsSuccessFalse()
@Test void loginWithSecurityCodeReturnsUnsupportedMessage()
@Test void loginFiresSessionCreatedEventOnSuccess()
```

Stub `IUserCommandServiceClient.authenticateUser(...)` to return canned replies. Use `MockSessionEventSender` to capture events.

- [ ] **Step 2: Implement `MappingModule`**

ModelMapper type map `Session → SessionData`.

- [ ] **Step 3: Implement `LoginContext`**

Fields: `request`, `validatedUserId` (nullable Long), `created` (new Session), `jwt` (minted token).

- [ ] **Step 4: Implement `SessionCommandService.login`**

```java
public CompletionStage<LoginReply>
login(LoginRequest request)
{
    return
        validate(request)
            .thenApply(this::mapToContext)
            .thenCompose(this::dispatch)
            .thenApply(this::createSession)
            .thenApply(this::mintJwt)
            .thenApply(this::save)
            .thenApply(this::buildReply)
            .exceptionally(e -> buildReply(request, e));
}
```

`validate` null-checks `request`, `userName`, `credential` (and rejects blank), `credentialKind`, `deviceId`.

`dispatch` on `request.getCredentialKind()`:
- `PASSWORD`: call `userCommandServiceClient.authenticateUser(userName, credential, credentialKind)` → set `validatedUserId` to the reply's userId or leave null on failure.
- `ACCESS_TOKEN`: look up a live session by access token (`repository.findByAccessToken(request.getCredential())` + `expires > now()`). If present, set `validatedUserId` to that session's userId; otherwise leave null.
- `SECURITY_CODE`: set `context.setUnsupportedKind(true)`, leave `validatedUserId` null.

`createSession`: if `validatedUserId == null` short-circuit to a failure reply; otherwise construct a new `Session` with `created=now`, `expires=now+ttl`, `deviceId=request.deviceId`, `accessToken=""` (filled by `mintJwt`).

`mintJwt`: assign sessionId first (via `repository.save(session)`), then set `session.setAccessToken(jwtTokenService.mint(session))`. (Two-pass save because `jti=sessionId` requires the ID.)

`save`: `repository.save(session)` again to persist the token.

`buildReply`: `new LoginReply(request).setSuccess(true).setSession(mapper.map(session,SessionData.class)).setAccessToken(session.getAccessToken())`.

- [ ] **Step 5: Run tests**

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): implement SessionCommandService.login with credential dispatch"
```

---

### Task 18: `SessionCommandService.loginWithOauth2`

**Model:** Opus 4.6

**Files:**
- Modify: `SessionCommandService.java`
- Create: `.../LoginWithOauth2Context.java`
- Create: `Tests/.../application/LoginWithOauth2Test.java`

- [ ] **Step 1: Write failing tests**

```java
@Test void loginWithValidIdTokenForKnownUserMintsSession()
@Test void loginWithValidIdTokenForUnknownUserProvisionsThenMints()
@Test void loginWithWrongAudienceIdTokenReturnsSuccessFalse() // Review Focus #3
@Test void loginWithUnknownProviderReturnsSuccessFalse()
@Test void jitProvisioningRaceResolvesToSameUser()           // Review Focus #5
```

The JIT race test uses two concurrent `loginWithOauth2` calls with the same provider+sub. Stub `IUserCommandServiceClient.createUser` to simulate one succeeding and the other failing with a unique-constraint violation; verify the losing call re-queries by externalIdentity and recovers.

- [ ] **Step 2: Implement**

Pipeline:
- `validate`: null checks on `provider`, `idToken`, `deviceId`.
- `validateIdToken`: `oauth2Validator.validate(provider, idToken)` → `Oauth2ValidatedIdentity`.
- `resolveUser`: `userQueryServiceClient.findUsersByExternalIdentity(provider, sub)`; if a user is found → `validatedUserId`; if not, call `userCommandServiceClient.createUser(...)` with `externalIdentity = provider + ":" + sub`. On `createUser` failure that looks like a unique-constraint violation, re-query `findUsersByExternalIdentity` and use the found userId.
- Remainder is identical to `login`'s create/mint/save/buildReply.

- [ ] **Step 3: Run tests**

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): implement SessionCommandService.loginWithOauth2"
```

---

### Task 19: `SessionCommandService.logout`, `logoutAllForUser`, `refresh`

**Model:** Opus 4.6

**Files:**
- Modify: `SessionCommandService.java`
- Create: `.../LogoutContext.java`, `LogoutAllForUserContext.java`, `RefreshContext.java`
- Create: `Tests/.../application/LogoutTest.java`, `LogoutAllForUserTest.java`, `RefreshTest.java`

- [ ] **Step 1: Write failing tests**

```java
@Test void logoutDestroysSessionAndFiresEvent()
@Test void logoutOfUnknownSessionReturnsSuccessFalse()
@Test void logoutAllForUserDestroysEverySessionForUser()
@Test void logoutAllForUserWhenNoneExistReturnsEmptyDestroyedSet()
@Test void refreshIssuesNewJwtAndUpdatesExpires()
@Test void refreshFiresSessionUpdatedEvent()
@Test void refreshRejectsExpiredSession()                   // Review Focus #2
@Test void refreshOfUnknownSessionReturnsSuccessFalse()
```

- [ ] **Step 2: Implement `logout`**

Load by sessionId, call `session.destroy()`, `repository.delete(session)`. If absent, reply with success=false.

- [ ] **Step 3: Implement `logoutAllForUser`**

Load all by userId, iterate `destroy()` + delete, collect sessionIds into `Set<Long> destroyed`, reply with that set.

- [ ] **Step 4: Implement `refresh`**

Load by sessionId → if absent or `expires < now()` → success=false. Otherwise set new `expires=now+ttl`, re-mint JWT (`jti` stays the same), save, event fires via domain mutation.

- [ ] **Step 5: Run tests**

- [ ] **Step 6: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): implement logout, logoutAllForUser, refresh"
```

---

### Task 20: `SessionQueryService` — all three queries

**Model:** Sonnet

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/SessionQueryService.java`
- Create: `.../FindSessionsByKeyContext.java`, `FindSessionsByKeySetContext.java`, `FindSessionByUserIdAndDeviceIdContext.java`
- Create: `Tests/.../application/SessionQueryServiceTest.java`

- [ ] **Step 1: Write failing tests**

```java
@Test void findSessionsByKeyBySessionIdReturnsMatch()
@Test void findSessionsByKeyByAccessTokenReturnsMatch()
@Test void findSessionsByKeyByUserIdReturnsAllForUser()
@Test void findSessionsByKeySetByUserIdsReturnsAggregate()
@Test void findSessionByUserIdAndDeviceIdReturnsMatchOrNull()
@Test void findReturnsRowEvenWhenExpired()                  // Review Focus #4
```

Review Focus #4: queries do not filter on `expires`; the consumer-side revocation convention treats expired rows as revoked. Session.Service must expose `expires` on `SessionData` so consumers can do this check.

- [ ] **Step 2: Implement `SessionQueryService`**

Pipeline per operation: validate → mapToContext → load → buildReply. Dispatch `SessionKey.name` to the correct repository method.

- [ ] **Step 3: Run tests**

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): implement SessionQueryService"
```

---

### Task 21: Session.Server — Application and Platform configuration + event senders

**Model:** Sonnet

**Files:**
- Create: `Components/Granite.Session.Server/src/main/java/granite/session/server/application/ApplicationConfiguration.java`
- Create: `.../platform/PlatformConfiguration.java`
- Create: `.../platform/CommandPlatformConfiguration.java`
- Create: `.../platform/QueryPlatformConfiguration.java`
- Create: `.../platform/MockSessionEventSender.java`
- Create: `.../platform/OnCommitSessionEventSender.java`
- Create: `.../platform/OutboxEventSessionEventSender.java`
- Create: `.../platform/SessionCommandServiceController.java`
- Create: `.../platform/SessionQueryServiceController.java`

**Interfaces produced:**
- `ApplicationConfiguration` wires `SessionCommandService` and `SessionQueryService` beans with request scope.
- `PlatformConfiguration` holds base JPA EntityManagerFactory, UnitOfWork, synchronization manager (adapt from User's `PlatformConfiguration`).
- `CommandPlatformConfiguration` wires `SessionCommandServiceController` + `OutboxEventSessionEventSender`.
- `QueryPlatformConfiguration` wires `SessionQueryServiceController`.
- Event senders mirror the User equivalents (`OutboxEventUserEventSender` etc.).
- Controllers expose the REST paths per spec §5.3.

- [ ] **Step 1: Implement the three event sender classes**

Follow Granite.User's `MockUserEventSender`, `OnCommitUserEventSender`, `OutboxEventUserEventSender` as templates. The outbox variant serializes the service-level event to the outbox table.

- [ ] **Step 2: Implement the three configuration classes**

Follow User's `ApplicationConfiguration`, `PlatformConfiguration`, `CommandPlatformConfiguration`, `QueryPlatformConfiguration` as templates.

- [ ] **Step 3: Implement the two controllers**

```java
@RestController
@RequestMapping("/session-command-service")
public class SessionCommandServiceController {
    @PostMapping("/login") public LoginReply login(@RequestBody LoginRequest r) { return service.login(r).toCompletableFuture().join(); }
    @PostMapping("/login-with-oauth2") public LoginWithOauth2Reply loginWithOauth2(@RequestBody LoginWithOauth2Request r) { ... }
    @PostMapping("/logout") public LogoutReply logout(@RequestBody LogoutRequest r) { ... }
    @PostMapping("/logout-all-for-user") public LogoutAllForUserReply logoutAllForUser(@RequestBody LogoutAllForUserRequest r) { ... }
    @PostMapping("/refresh") public RefreshReply refresh(@RequestBody RefreshRequest r) { ... }
}
```

Query controller mirrors the three query operations under `/session-query-service/`.

- [ ] **Step 4: Verify Server component compiles**

Run: `./gradlew :Microservices:Granite.Session:Components:Granite.Session.Server:compileJava`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Server application/platform configuration + event senders + controllers"
```

---

## Phase D — Granite.Session hosts

### Task 22: `Granite.Session.CommandHost` Spring Boot application

**Model:** Sonnet

**Files:**
- Create: `Applications/Granite.Session.CommandHost/src/main/java/granite/session/commandhost/main/SessionCommandHost.java`
- Create: `.../main/HostConfiguration.java`
- Create: `src/main/resources/application.properties`, `application-development.properties`, `application-test.properties`, `application-production.properties`
- Copy test keypair PEMs into `src/main/resources/` for the dev profile.

**Interfaces produced:**
- Boot-executable JAR on port 8088.
- `HostConfiguration` `@Import({DomainConfiguration.class, ApplicationConfiguration.class, CommandPlatformConfiguration.class, ServiceConfiguration.class})`.

- [ ] **Step 1: Create `SessionCommandHost.java`**

```java
@SpringBootApplication
public class SessionCommandHost {
    public static void main(String[] args) {
        new SpringApplicationBuilder(SessionCommandHost.class)
            .profiles("production")
            .run(args);
    }
}
```

- [ ] **Step 2: Create `HostConfiguration.java`**

- [ ] **Step 3: Create `application.properties`**

```properties
server.port=8088
spring.application.name=granite-session-commandhost
spring.main.web-application-type=servlet
```

- [ ] **Step 4: Create per-profile properties**

Dev profile sample:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/granite_session
spring.datasource.username=granite
spring.datasource.password=granite
granite.session.jwt.private-key-location=classpath:jwt-test-private-key.pem
granite.session.jwt.public-key-location=classpath:jwt-test-public-key.pem
granite.session.jwt.audience=granite-services
granite.session.jwt.ttl=PT1H
granite.user-query-service.host=https://localhost:8084/user-query-service
granite.user-command-service.host=https://localhost:8083/user-command-service
granite.session.oauth2.providers.google.issuer-uri=https://accounts.google.com
granite.session.oauth2.providers.google.audience=<dev-client-id>
granite.session.oauth2.providers.google.jwks-uri=https://www.googleapis.com/oauth2/v3/certs
```

Test and production profiles: externalize endpoints via Kubernetes service DNS and secret-mounted key paths.

- [ ] **Step 5: Smoke-test boot**

Run: `./gradlew :Microservices:Granite.Session:Applications:Granite.Session.CommandHost:bootRun --args='--spring.profiles.active=development'`
Expected: starts cleanly, logs `Tomcat started on port 8088`.

- [ ] **Step 6: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add SessionCommandHost Spring Boot application"
```

---

### Task 23: `Granite.Session.QueryHost` Spring Boot application

**Model:** Sonnet

**Files:** mirrors Task 22 for the Query side on port 8089 with `QueryPlatformConfiguration`.

- [ ] **Step 1: Create `SessionQueryHost.java` with same shape as `SessionCommandHost.java`**

- [ ] **Step 2: Create `HostConfiguration` importing `QueryPlatformConfiguration`**

- [ ] **Step 3: Create `application.properties` with `server.port=8089`**

- [ ] **Step 4: Create per-profile properties (JWT public-key-location only; QueryHost does not mint)**

- [ ] **Step 5: Smoke-test boot**

Run: `./gradlew :Microservices:Granite.Session:Applications:Granite.Session.QueryHost:bootRun --args='--spring.profiles.active=development'`
Expected: port 8089 up.

- [ ] **Step 6: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add SessionQueryHost Spring Boot application"
```

---

## Phase E — Granite.Session.Client

### Task 24: Session.Client — Java REST clients

**Model:** Sonnet

**Files:**
- Create: `Components/Granite.Session.Client/src/main/java/granite/session/client/ISessionCommandServiceClient.java`
- Create: `.../ISessionQueryServiceClient.java`
- Create: `.../SessionCommandServiceClient.java`
- Create: `.../SessionQueryServiceClient.java`

**Interfaces produced:**
- Both clients mirror the service interfaces (`CompletionStage<Reply>` returns) and extend `AbstractRestClient`.

- [ ] **Step 1: Create interfaces mirroring the service interfaces**

- [ ] **Step 2: Implement clients following `UserCommandServiceClient.java` as the template**

Each client method serializes the request and POSTs to the configured host URL.

- [ ] **Step 3: Compile check**

Run: `./gradlew :Microservices:Granite.Session:Components:Granite.Session.Client:compileJava`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Java REST clients"
```

---

### Task 25: Session.Client — Kafka event receivers (Java) and consumer-side helper for revocation checks

**Model:** Sonnet

**Files:**
- Create: `.../SessionKafkaEventReceiver.java`
- Create: `.../SessionKafkaAvroEventReceiver.java`
- Create: `.../SessionJwtValidator.java` (consumer-side helper: local signature+exp check + revocation check against `ISessionQueryServiceClient`).
- Create: `Tests/Granite.Session.ClientTest/src/test/java/granite/session/client/SessionJwtValidatorTest.java`

**Interfaces produced:**
- `SessionKafkaEventReceiver implements ISessionEventReceiver` consuming from `granite.session.events`.
- `SessionJwtValidator.validate(String jwt): Optional<Long>` returning the authenticated userId when the JWT is signature-valid, unexpired, and the session row is still present and unexpired. Returns empty otherwise.

- [ ] **Step 1: Write failing `SessionJwtValidatorTest`**

Scenarios:
```java
@Test void validReturnsUserId()
@Test void expiredJwtReturnsEmpty()
@Test void tamperedJwtReturnsEmpty()
@Test void jwtWithRevokedSessionReturnsEmpty()
@Test void jwtWithSessionExpiresInPastReturnsEmpty()        // Review Focus #4
```

- [ ] **Step 2: Implement `SessionJwtValidator`**

Local step: `IJwtTokenService.verifySignatureAndExp(jwt)` or an inline Nimbus validator holding only the public key. Remote step: `sessionQueryServiceClient.findSessionsByKey(SessionKey.ofSessionId(jti))`; treat absent or `expires < now()` as revoked.

- [ ] **Step 3: Implement Kafka event receivers following User's templates**

- [ ] **Step 4: Run tests**

- [ ] **Step 5: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add Kafka receivers and consumer-side JWT validator"
```

---

### Task 26: Session.Client — TypeScript mirror

**Model:** Haiku 4.5

**Files:**
- Create: `src/main/typescript/SessionCommandServiceClient.ts`, `SessionQueryServiceClient.ts`
- Create: interface mirrors
- Update: `src/main/typescript/index.ts`

- [ ] **Step 1: Mirror client classes in TypeScript**

Follow `UserCommandServiceClient.ts` as the template.

- [ ] **Step 2: Build**

Run: `cd Microservices/Granite.Session/Components/Granite.Session.Client && yarn run build`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "feat(session): add TypeScript client mirror"
```

---

## Phase F — Session tests

### Task 27: `Granite.Session.ServiceTest` — interface fixtures

**Model:** Sonnet

**Files:**
- Create: `Tests/Granite.Session.ServiceTest/src/testFixtures/java/granite/session/service/ISessionCommandServiceFixture.java`
- Create: `.../ISessionQueryServiceFixture.java`

**Interfaces produced:** reusable fixtures Server and Client tests both drive.

- [ ] **Step 1: Create fixtures that provide canned requests and assert reply invariants** (mirrors Granite.User.ServiceTest patterns)

- [ ] **Step 2: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "test(session): add service-interface fixtures"
```

---

### Task 28: `Granite.Session.ServerTest` — integration against H2

**Model:** Sonnet

**Files:**
- Create: `Tests/Granite.Session.ServerTest/src/test/java/granite/session/server/integration/SessionIntegrationTest.java`
- Create: `.../integration/TestConfiguration.java` (H2 datasource, in-memory JWT keypair, mock User client, wiremock OAuth2 JWKS endpoint).

**Interfaces produced:** a full round-trip test proving login → findBy → logout → findBy returns absent, with event emission observed.

- [ ] **Step 1: Write integration test**

Scenarios:
```java
@Test void endToEndLoginFindRefreshLogoutCycle()
@Test void endToEndOauth2LoginAndFindByUserIdAndDeviceId()
@Test void endToEndLogoutAllForUserDestroysAll()
```

- [ ] **Step 2: Run**

Run: `./gradlew :Microservices:Granite.Session:Tests:Granite.Session.ServerTest:test`
Expected: PASS.

- [ ] **Step 3: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "test(session): add Server integration tests"
```

---

### Task 29: `Granite.Session.ClientTest` and `Granite.Session.TestSuite`

**Model:** Sonnet

**Files:**
- Create: `Tests/Granite.Session.ClientTest/src/test/java/granite/session/client/SessionClientIntegrationTest.java`
- Create: `Tests/Granite.Session.TestSuite/src/test/java/granite/session/testsuite/SessionTestSuite.java`

- [ ] **Step 1: ClientTest hits a running CommandHost/QueryHost (TestContainers or `@SpringBootTest` for the host)**

- [ ] **Step 2: TestSuite aggregates ServiceTest + ServerTest + ClientTest with `@Suite`**

- [ ] **Step 3: Run suite**

Run: `./gradlew :Microservices:Granite.Session:Tests:Granite.Session.TestSuite:test`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add Microservices/Granite.Session
git commit -m "test(session): add ClientTest and aggregated TestSuite"
```

---

## Phase G — Gateway + root orchestration

### Task 30: Extend `Granite.Gateway` with Session routes

**Model:** Haiku 4.5

**Files:**
- Modify: `Microservices/Granite.Gateway/Components/Granite.Gateway.Server/src/main/java/granite/gateway/server/route/RouteConfiguration.java`
- Modify: `Microservices/Granite.Gateway/Applications/Granite.Gateway.ServiceHost/src/main/resources/application-development.properties`
- Modify: `.../application-test.properties`
- Modify: `.../application-production.properties`
- Modify: `Microservices/Granite.Gateway/CLAUDE.md`

**Interfaces produced:** two new routes `session-command-route` and `session-query-route`.

- [ ] **Step 1: Write failing route-table test**

In `Microservices/Granite.Gateway/Tests/Granite.Gateway.ServerTest/`, add a test that asserts the `RouteLocator` resolves `/session-command-service/foo` to the configured host and `/session-query-service/foo` to its configured host.

- [ ] **Step 2: Run to verify failure**

- [ ] **Step 3: Add two `@Value` fields and two `.route(...)` entries to `RouteConfiguration.java`**

Per spec §10.1.

- [ ] **Step 4: Add two properties to each of the three profile properties files**

```
granite.session-command-service.host=https://localhost:8088/session-command-service   # development
granite.session-query-service.host=https://localhost:8089/session-query-service
```
Test / production: `https://granite-session-commandhost:443` / `https://granite-session-queryhost:443`.

- [ ] **Step 5: Update `Granite.Gateway/CLAUDE.md`**

Append two rows to the route table and two rows to the route-host-configuration table (per spec §10).

- [ ] **Step 6: Run tests**

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add Microservices/Granite.Gateway
git commit -m "feat(gateway): route /session-command-service and /session-query-service"
```

---

### Task 31: Update root `package.json` and Microservices orchestration

**Model:** Haiku 4.5

**Files:**
- Modify: `C:\Development\Granite\package.json`

- [ ] **Step 1: Add Session scripts following the User pattern**

```json
"clean-granite-session": "cd Microservices/Granite.Session && yarn run clean",
"deep-clean-granite-session": "cd Microservices/Granite.Session && yarn run deep-clean",
"build-granite-session": "cd Microservices/Granite.Session && yarn run build",
```

And insert `clean-granite-session`, `deep-clean-granite-session`, `build-granite-session` into `clean-microservices`, `deep-clean-microservices`, `build-microservices` **after** the corresponding `-granite-user` entry (because Session depends on User's new DTOs being published first).

- [ ] **Step 2: Verify root `package.json` parses as JSON**

Run: `node -e "JSON.parse(require('fs').readFileSync('package.json','utf8'))"`
Expected: no error.

- [ ] **Step 3: Dry-run top-level build**

Run: `yarn run clean && yarn run build`
Expected: User publishes, then Session consumes User's packages, then builds through to the hosts.

- [ ] **Step 4: Commit**

```bash
git add package.json
git commit -m "chore(root): wire Granite.Session into microservice build orchestration"
```

---

### Task 32: End-to-end verification against Gateway

**Model:** Opus 4.6 (judgement + verification)

- [ ] **Step 1: Start supporting services**

Locally start PostgreSQL and Kafka (or ensure the dev Compose file is up).
Start `Granite.User.CommandHost` and `Granite.User.QueryHost`.
Start `Granite.Session.CommandHost` and `Granite.Session.QueryHost`.
Start `Granite.Gateway.ServiceHost`.

- [ ] **Step 2: Create a test user with a password via Gateway**

```bash
curl -k -X POST https://localhost:8181/user-command-service/create-user \
  -H 'Content-Type: application/json' \
  -d '{"data":{"userName":"alice","password":"hunter2"}}'
```

- [ ] **Step 3: Log alice in via Gateway**

```bash
curl -k -X POST https://localhost:8181/session-command-service/login \
  -H 'Content-Type: application/json' \
  -d '{"userName":"alice","credential":{"kind":"PASSWORD","value":"hunter2"},"deviceId":"test-device-1"}'
```
Expected: 200 with `success=true`, a `session` payload, and an `accessToken` JWT.

- [ ] **Step 4: Verify the session is findable via Gateway**

```bash
curl -k -X POST https://localhost:8181/session-query-service/find-sessions-by-key \
  -H 'Content-Type: application/json' \
  -d '{"key":{"name":"accessToken","value":"<jwt-from-step-3>"}}'
```
Expected: a single session returned.

- [ ] **Step 5: Log out and verify absence**

```bash
curl -k -X POST https://localhost:8181/session-command-service/logout \
  -H 'Content-Type: application/json' \
  -d '{"sessionId":<id-from-step-3>}'
```

Repeat Step 4's query. Expected: empty.

- [ ] **Step 6: If every step passes, no commit (this task is verification only). Record findings in `docs/superpowers/plans/2026-10-06-granite-session-verification.md`.**

---

## Done criteria

- All 32 tasks complete and committed on a feature branch.
- `./gradlew clean build` at the Microservices root succeeds end-to-end.
- `yarn run build` at the Granite root publishes User and Session TypeScript packages and builds every Gradle subproject.
- The end-to-end flow in Task 32 passes against a live Gateway + both User hosts + both Session hosts.
- The spec's 11 acceptance criteria from §15 are each covered by at least one test or by Task 32's verification steps.
