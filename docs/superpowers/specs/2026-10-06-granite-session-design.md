# Granite.Session Microservice — Design Spec

**Date:** 2026-10-06
**Status:** Draft — awaiting user review
**Scope:** Introduce `Granite.Session`, add supporting changes to `Granite.User`, and extend `Granite.Gateway` routing.

---

## 1. Overview and Goals

Granite.Session is a new Spring Boot microservice that manages authentication and user-activity sessions in the Granite system. A `Session` entity is created when a user authenticates (via credential-based login or OAuth2 federated login) and holds the issued access token, device identifier, lifetime, and arbitrary user-activity state. Sessions are revoked on logout. Session state changes emit domain events (`SessionCreated`, `SessionUpdated`, `SessionDestroyed`) through the Transactional Outbox pattern and Kafka.

The microservice follows the `Granite.User` template exactly in architecture, build layout, event delivery, and coding style. Deviations from the template are called out explicitly in each section.

**In scope**

- New microservice `Granite.Session` with full parity to `Granite.User` (two host apps, three components, four test subprojects, Java + TypeScript mirrors).
- Granite.User additions: `password` field, `externalIdentity` field, `authenticateUser` command, `findUsersByExternalIdentity` query, `password` on `CreateUserData` / `UpdateUserData`.
- Granite.Gateway additions: two routes for Session, host properties per profile, updated route table in `CLAUDE.md`.

**Explicitly out of scope (follow-up specs)**

- Admin / self-service flow to issue and change user passwords (this spec adds the field and the `validate` operation; no `createCredential` / `changePassword` command is added).
- Security-code issuance and validation (`SECURITY_CODE` enum value exists; `login` returns an unsupported-credential failure for v1).
- Public JWKS endpoint on Session for external JWT consumers.
- Scheduled cleanup of expired-but-not-logged-out session rows.
- Front-end OIDC client wiring (belongs under `Granite.Web.Account`).

---

## 2. Technology Choices

Identical to `Granite.User`:

- Java 23 (Amazon Corretto), TypeScript.
- Gradle multi-project, Yarn workspace for TypeScript components.
- Spring Boot 3.4, Spring 6.2.
- JPA / Hibernate 6.4, PostgreSQL (prod), H2 (test).
- ModelMapper 3.2.
- Kafka + Transactional Outbox (strata.outbox).
- Google Jib OCI images.
- JUnit Jupiter 5.
- Strata Framework Set (foundation, server, client, outbox).

Session-specific additions:

- **`spring-security-oauth2-jose`** (Nimbus JOSE JWT). Used for both minting Granite access tokens (RS256) and validating OAuth2 provider ID tokens on `loginWithOauth2`.
- **BCrypt** (`spring-security-crypto`, already transitive via Spring Security). Used in Granite.User for `password` hashing.

---

## 3. Project Layout

Mirrors `Granite.User` exactly:

```
Granite.Session/
├── Applications/
│   ├── Granite.Session.CommandHost/       # Spring Boot — command endpoints, port 8088
│   └── Granite.Session.QueryHost/         # Spring Boot — query endpoints, port 8089
├── Components/
│   ├── Granite.Session.Service/           # Interfaces, DTOs, events (Java + TypeScript)
│   ├── Granite.Session.Server/            # Domain, application, platform
│   └── Granite.Session.Client/            # REST client, Kafka receiver (Java + TypeScript)
├── Tests/
│   ├── Granite.Session.ServiceTest/
│   ├── Granite.Session.ServerTest/
│   ├── Granite.Session.ClientTest/
│   └── Granite.Session.TestSuite/
├── build.gradle
├── settings.gradle
└── package.json
```

Package naming: `granite.session.service.*`, `granite.session.server.{platform,application,domain}`, `granite.session.client.*`.

---

## 4. Domain Entity

```java
public class Session
    extends AbstractEntityAndDomainEventSource
{
    private Long               sessionId;      // PK, auto-generated
    private Long               userId;         // Logical FK to User (stored as id; no JPA relation)
    private String             accessToken;    // JWT, see §7
    private Instant            created;        // Set at construction
    private Instant            expires;        // Set at login / refresh = now + configured TTL
    private String             deviceId;
    private Map<String,Object> values;         // User-activity state
}
```

**Schema notes**

- Primary key: `sessionId` (BIGINT).
- Unique index: `accessToken`.
- Non-unique index: `userId`.
- Composite non-unique index: `(userId, deviceId)` — supports `findSessionByUserIdAndDeviceId` and the implicit "replace existing session for this device" convention.
- `values` is persisted via a `@ElementCollection` into a `session_values` table (matching how User persists `values`).
- No JPA relation to User — `userId` is stored as a plain `BIGINT`. Cross-service referential integrity is enforced in the application layer during `login` and `loginWithOauth2` by consulting the User service.

**Domain events**

- `SessionCreated` — fired on `login`, `loginWithOauth2`.
- `SessionUpdated` — fired on `refresh` and on any mutation of `values`.
- `SessionDestroyed` — fired on `logout` and once per session destroyed by `logoutAllForUser`.

All three extend `AbstractSessionEvent`. The observer wiring (`ISessionEventObserver`, `ISessionEventSource`, `ISessionEventSender`, `ISessionEventReceiver`, `ISessionEventListener`) mirrors User.

---

## 5. Service Interfaces

### 5.1 `ISessionCommandService`

| Operation | Request | Reply | Emits |
|---|---|---|---|
| `login` | `LoginRequest { userName, credential, credentialKind, deviceId }` | `LoginReply { session: SessionData, accessToken: String }` | `SessionCreated` |
| `loginWithOauth2` | `LoginWithOauth2Request { provider, idToken, deviceId }` | `LoginWithOauth2Reply { session: SessionData, accessToken: String }` | `SessionCreated` (+ possibly `UserCreated` upstream for JIT) |
| `logout` | `LogoutRequest { sessionId }` | `LogoutReply` | `SessionDestroyed` |
| `logoutAllForUser` | `LogoutAllForUserRequest { userId }` | `LogoutAllForUserReply { destroyed: Set<Long> }` | `SessionDestroyed` × N |
| `refresh` | `RefreshRequest { sessionId }` | `RefreshReply { session: SessionData, accessToken: String }` | `SessionUpdated` |

### 5.2 `ISessionQueryService`

| Operation | Request | Reply |
|---|---|---|
| `findSessionsByKey` | `FindSessionsByKeyRequest { key: SessionKey, returnIfFound }` | `FindSessionsByKeyReply` |
| `findSessionsByKeySet` | `FindSessionsByKeySetRequest { keySet: SessionKeySet }` | `FindSessionsByKeySetReply` |
| `findSessionByUserIdAndDeviceId` | `FindSessionByUserIdAndDeviceIdRequest { userId, deviceId }` | `FindSessionByUserIdAndDeviceIdReply` |

**Credential representation**: credentials are carried as two sibling scalar fields on every request that needs them. There is no `Credential` DTO. `credential: String` holds the opaque value (password, previously-issued access token, or security code) and `credentialKind: CredentialKind` identifies the kind. The `CredentialKind` enum lives in `Granite.User.Service` and Session references it via the `Granite.User.Service` project dependency.

```java
public enum CredentialKind { PASSWORD, ACCESS_TOKEN, SECURITY_CODE }
```

**`SessionKey` / `SessionKeySet`**

Follows the `UserKey` / `UserKeySet` convention: a `(name, value)` or `(name, values)` pair with a fixed set of valid name constants.

- `SessionKey` names: `SESSION_ID`, `ACCESS_TOKEN`, `USER_ID`.
- `SessionKeySet` names: `SESSION_IDS`, `ACCESS_TOKENS`, `USER_IDS`.

**Divergence from User:** composite lookups (userId + deviceId) do not fit the single-value `UserKey` pattern cleanly. A dedicated `findSessionByUserIdAndDeviceId` query handles this, keeping the key abstraction clean.

### 5.3 REST paths

- `POST /session-command-service/login`
- `POST /session-command-service/login-with-oauth2`
- `POST /session-command-service/logout`
- `POST /session-command-service/logout-all-for-user`
- `POST /session-command-service/refresh`
- `POST /session-query-service/find-sessions-by-key`
- `POST /session-query-service/find-sessions-by-keyset`
- `POST /session-query-service/find-session-by-user-id-and-device-id`

---

## 6. Login Dispatch

### 6.1 `login` (credential-based)

`SessionCommandService.login` dispatches on `credentialKind`:

| `kind` | Behavior |
|---|---|
| `PASSWORD` | Calls `IUserCommandServiceClient.authenticateUser(userName, credential, credentialKind)`. On `valid=true`, mints a Session for the returned `userId`. On `valid=false`, returns `LoginReply` with `success=false` and failure message. |
| `ACCESS_TOKEN` | Session validates the supplied `credential` string against its own session store (`findSessionsByKey(SessionKey.ofAccessToken(credential))`). If an unexpired session matches, mint a new Session for the same `userId` and `deviceId`. Does **not** destroy the original session. Does not call Granite.User. |
| `SECURITY_CODE` | v1 returns `LoginReply { success=false, failureMessage="SECURITY_CODE credentials not yet supported" }`. Follow-up spec will introduce storage and validation. |

### 6.2 `loginWithOauth2`

1. Validate the supplied `idToken` using `NimbusJwtDecoder` configured against the configured provider's JWKS URI, verifying signature, `iss`, `aud`, `exp`, and `nonce` (if present).
2. Extract `sub` (and, for logging, `email`) claims.
3. Call `IUserQueryServiceClient.findUsersByExternalIdentity(provider, sub)`.
4. If a user is found, mint a Session for that `userId`. If not, call `IUserCommandServiceClient.createUser` with the OAuth2 identity to JIT-provision, then mint the Session.
5. Reply with `LoginWithOauth2Reply { session, accessToken }`.

Provider configuration is externalized under `granite.session.oauth2.providers.<name>.{issuer-uri, audience, jwks-uri}` in each profile's properties.

### 6.3 `refresh`

Looks up the Session by `sessionId`, verifies it is unexpired (not revoked), mints a new JWT with a new `exp`, updates `expires` on the row, and fires `SessionUpdated`.

### 6.4 `logout` and `logoutAllForUser`

`logout` destroys the session by `sessionId`, firing one `SessionDestroyed`.

`logoutAllForUser` loads all sessions for the given `userId` within a single transaction, destroys each, and emits one `SessionDestroyed` per destroyed session.

---

## 7. JWT

**Algorithm:** RS256 (asymmetric). Signing key material is a PKCS#8 PEM private key loaded via Spring configuration (`granite.session.jwt.private-key-location`). The corresponding public key is served at `granite.session.jwt.public-key-location` for consumer-side validation.

Dev profile loads a committed test keypair from `src/main/resources/`. Test and production profiles load from externally mounted locations.

**Claims**

| Claim | Value |
|---|---|
| `iss` | `granite-session` |
| `sub` | String form of `userId` |
| `jti` | String form of `sessionId` |
| `iat` | Issue time |
| `exp` | Expiration time (matches `session.expires`) |
| `aud` | Configurable list of audience ids (`granite.session.jwt.audience`) |
| `did` | `deviceId` (custom claim) |

**Validation convention for consumer services (Gateway, FrontEndController, future services)**

1. Decode the JWT with the Session public key and verify signature + `exp` + `iss` + `aud` locally.
2. Call `ISessionQueryServiceClient.findSessionsByKey(SessionKey.ofSessionId(jti))` to check for revocation. Absent row or `expires` in the past means revoked.

Step 1 is cheap and offline-capable; step 2 is one RPC per inbound request (equivalent to opaque-token lookup) but can be skipped by trusted internal services during degraded operation. This design keeps logout semantics immediate while preserving signature-based identity.

**Why RS256, not HS256:** HS256 requires every validating service to hold the signing secret. RS256 lets Session publish the public key and keep the private key confined to the two Session hosts. This matters as new services join the system.

**Why not stateless short-lived JWT + refresh token:** The system requires immediate logout. Short-lived access tokens would delay revocation by up to the TTL. Carrying `jti=sessionId` and consulting the session row preserves immediate revocation at the cost of one lookup per request — the same cost profile as opaque tokens.

---

## 8. Events and Outbox

Follows the User template exactly.

- Service-level events: `SessionCreated`, `SessionUpdated`, `SessionDestroyed`, each extending `AbstractSessionEvent` and carrying a `SessionData` snapshot (plus the triggering request id for traceability).
- Senders: `MockSessionEventSender` (test), `OnCommitSessionEventSender` (post-commit, no outbox), `OutboxEventSessionEventSender` (persists to outbox table within the command transaction; a separate relay publishes to Kafka).
- `CommandPlatformConfiguration` wires `OutboxEventSessionEventSender`; `QueryPlatformConfiguration` wires nothing (query side emits no events).

Kafka topic: `granite.session.events`. Avro schemas live in `Granite.Session.Service` alongside the plain Java event classes, following the pattern in `Granite.User.Client`'s `UserKafkaAvroEventReceiver`.

---

## 9. Granite.User Changes (bundled in this spec)

### 9.1 Entity

Two new nullable columns on `User`:

```java
private String password;           // bcrypt hash; nullable (OAuth2-only users have no password)
private String externalIdentity;   // "provider:sub" composite; nullable; indexed
```

No new entities. No Credential entity.

### 9.2 DTOs

- `CreateUserData` and `UpdateUserData` gain `password` (plain-text in the DTO; hashed before persistence in `UserCommandService`) and `externalIdentity`.
- `UserData` gains `externalIdentity` only (never includes `password` on reply).

### 9.3 Service

One new operation added to `IUserCommandService`:

| Operation | Request | Reply |
|---|---|---|
| `authenticateUser` | `AuthenticateUserRequest { userName, credential, credentialKind }` | `AuthenticateUserReply { valid: boolean, userId?: Long }` |

One new operation added to `IUserQueryService`:

| Operation | Request | Reply |
|---|---|---|
| `findUsersByExternalIdentity` | `FindUsersByExternalIdentityRequest { provider, externalId }` | `FindUsersByExternalIdentityReply` |

`authenticateUser` v1 handles only `credentialKind=PASSWORD`:

- Looks up User by `userName`.
- If present and `user.password != null`, verifies the submitted `credential` string against the stored bcrypt hash.
- Returns `valid=true` with `userId` on match; `valid=false` otherwise.
- For `credentialKind=ACCESS_TOKEN` and `credentialKind=SECURITY_CODE`, returns `valid=false` with a failure message indicating the kind is not handled by User (Session dispatches ACCESS_TOKEN to its own store; SECURITY_CODE is unsupported in v1).

### 9.4 Platform

`UserCommandServiceController` gains one endpoint:

- `POST /user-command-service/authenticate-user`

`UserQueryServiceController` gains one endpoint:

- `POST /user-query-service/find-users-by-external-identity`

`UserCommandService` hashes `createUser.password` and `updateUser.password` with BCrypt (strength 12) before persisting.

### 9.5 Client

`UserCommandServiceClient` gains the `authenticateUser` method; `UserQueryServiceClient` gains `findUsersByExternalIdentity`. TypeScript mirror updates in `Granite.User.Service` (DTOs and `CredentialKind` — there is no `Credential` DTO) and `Granite.User.Client` (client methods).

---

## 10. Granite.Gateway Changes

### 10.1 `RouteConfiguration.java`

Two new `@Value` fields:

```java
@Value("${granite.session-command-service.host}")
private String sessionCommandServiceHost;

@Value("${granite.session-query-service.host}")
private String sessionQueryServiceHost;
```

Two new routes appended to the `RouteLocator`:

```java
.route(
    "session-command-route",
    route ->
        route
            .path("/session-command-service/**")
            .uri(sessionCommandServiceHost))
.route(
    "session-query-route",
    route ->
        route
            .path("/session-query-service/**")
            .uri(sessionQueryServiceHost))
```

### 10.2 Profile properties

Add two properties to each of `application-development.properties`, `application-test.properties`, `application-production.properties`:

| Property | Development | Test / Production |
|---|---|---|
| `granite.session-command-service.host` | `https://localhost:8088/session-command-service` | `https://granite-session-commandhost:443` |
| `granite.session-query-service.host` | `https://localhost:8089/session-query-service` | `https://granite-session-queryhost:443` |

### 10.3 Documentation

`Microservices/Granite.Gateway/CLAUDE.md` route table and host-configuration table extended with Session rows.

---

## 11. Cross-Service Dependencies

- `Granite.Session.Server` depends on `Granite.User.Client` for:
  - `IUserCommandServiceClient.authenticateUser(...)` — PASSWORD login path.
  - `IUserQueryServiceClient.findUsersByExternalIdentity(...)` — OAuth2 lookup path.
  - `IUserCommandServiceClient.createUser(...)` — OAuth2 JIT provisioning path.
- Build order (orchestrated in root `package.json` and the Microservices-level Gradle composite build): `Granite.User` publishes before `Granite.Session`. Root `package.json` scripts gain `clean-granite-session`, `deep-clean-granite-session`, `build-granite-session` and include them in the composed `clean-microservices` / `deep-clean-microservices` / `build-microservices` after User.

---

## 12. Testing Strategy

Mirrors `Granite.User`:

- **`Granite.Session.ServiceTest`** — JUnit fixtures for `ISessionCommandService` and `ISessionQueryService`; reused by Server and Client tests.
- **`Granite.Session.ServerTest`** — Service layer against H2. Mocks `IUserCommandServiceClient` (stub `authenticateUser` and `createUser` to canned outcomes) and `IUserQueryServiceClient` (stub `findUsersByExternalIdentity`). Uses `MockSessionEventSender`. Covers JWT minting, revocation lookup, all five commands, all three queries, and expiry rejection.
- **`Granite.Session.ClientTest`** — `SessionCommandServiceClient` / `SessionQueryServiceClient` against a live Spring Boot test context.
- **`Granite.Session.TestSuite`** — aggregated.
- **OAuth2 fixtures** — a self-signed RSA keypair acts as the test issuer; test JWTs are minted with the private key, served by a `WireMock`-hosted JWKS endpoint, and the Session's `NimbusJwtDecoder` validates them against that URI.

Granite.User gains tests for `authenticateUser` (password matches / mismatches / absent-password / absent-user) and `findUsersByExternalIdentity` (hit / miss).

Granite.Gateway gains a route-table assertion test that both session routes resolve.

---

## 13. Coding Style and Conventions

Follows Granite.User's conventions verbatim:

- Interface naming `I*`.
- File header/footer banner comment with filename.
- `class` keyword on its own line; `extends` / `implements` indented on subsequent lines.
- Allman braces.
- Return type on a separate line from method name.
- Fluent setters returning `this`; `@Override` setters narrowed via cast.
- Compact single-line getters.
- Column-aligned field declarations and constructor assignments.
- `Array<Type>` syntax in TypeScript.
- Request/Reply/Context pipeline shape: `validate → mapToContext → operation → buildReply`.
- Singular and plural operations where applicable. Granite.Session's commands are auth-flavored rather than CRUD, so there is no `createSessions` / `destroySessions` pair; instead `logoutAllForUser` is the plural-flavored destructor. Pipeline shape, context objects, and reply population still follow the User template.

---

## 14. Open Items (follow-up specs)

- Admin / self-service `changePassword` and `setExternalIdentity` command operations on Granite.User.
- `SecurityCode` entity and `issueSecurityCode` command on Granite.Session (or Granite.User — to be decided).
- Public JWKS endpoint on `Granite.Session.QueryHost`.
- Scheduled task on `Granite.Session.CommandHost` to delete sessions where `expires < now()` by more than a configured grace period.
- Front-end OIDC integration in `Granite.Web.Account` (React `oidc-client-ts` + token forwarding to Session).
- Granite.Session's own Granite.Session.Client being added as an outer dependency on consumer services that call revocation lookups.

---

## 15. Acceptance Criteria

- `yarn run build` from the Granite repo root publishes `granite.session.service`, `granite.session.client` (TypeScript) and builds all Session Gradle subprojects without errors, in an order where User's new DTOs are published before Session consumes them.
- `Granite.Session.CommandHost` starts on port 8088 and `Granite.Session.QueryHost` starts on port 8089, both reachable through Granite.Gateway using the paths in §5.3.
- `login` with a valid password returns a Session row, a signed JWT whose `jti` matches the sessionId, and emits a single `SessionCreated` through the outbox.
- `login` with an invalid password returns `success=false` with no Session row written and no event emitted.
- `loginWithOauth2` against the test issuer JWKS fixture creates a Session (and provisions a User if absent).
- `logout(sessionId)` destroys the row, emits `SessionDestroyed`, and subsequent revocation lookups return absent.
- `logoutAllForUser(userId)` destroys every active session for the user in a single transaction and emits one `SessionDestroyed` per session.
- `refresh(sessionId)` returns a new JWT with a later `exp`, updates `expires` on the row, emits `SessionUpdated`.
- The three `findSessions*` queries return matches by sessionId, accessToken, userId, and (userId + deviceId) respectively.
- Granite.User's `authenticateUser` returns `valid=true` for a correct password, `valid=false` otherwise, and `valid=false` with a kind-not-supported message for ACCESS_TOKEN and SECURITY_CODE.
- Granite.Gateway routes `/session-command-service/**` and `/session-query-service/**` to the configured hosts in all three profiles.
