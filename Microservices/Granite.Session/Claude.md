# Granite.Session Microservice

## Overview

Granite.Session is a Spring Boot microservice that manages authentication and user-activity sessions within the Granite system. A Session entity is created when a user authenticates (password login or enterprise Okta login) and holds the issued access token (a signed JWT), device identifier, lifetime, and arbitrary user-activity state. Sessions are closed (with a `closedReason`) on logout, replacement, displacement, expiry, refresh-token reuse or admin logout. The microservice exposes command and query REST APIs and emits domain events (SessionCreated, SessionUpdated, SessionDestroyed) as session state changes occur. Events are propagated to external consumers via the Transactional Outbox pattern and Kafka (topic `granite.session.events`).

Granite.Session mirrors the architecture, build layout, event delivery, and coding style of Granite.User. It depends on Granite.User.Client to authenticate passwords (`authenticateUser`), look up Okta identities (`findUsersByExternalIdentity`) and create first-time Okta users (`createUser`).

## Technology Stack

- **Language**: Java 23 (Amazon Corretto), TypeScript
- **Build**: Gradle (multi-project), Yarn (TypeScript components)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Persistence**: JPA / Hibernate 6.4, PostgreSQL (production), H2 (testing)
- **Object Mapping**: ModelMapper 3.2
- **Eventing**: Kafka, Transactional Outbox (strata.outbox)
- **Security**: spring-security-oauth2-jose (Nimbus JOSE JWT) for RS256 token minting, the Okta `private_key_jwt` client assertion and Okta ID token validation; Spring `RestClient` for the Okta discovery and token endpoints
- **Containerization**: Google Jib (OCI images)
- **Testing**: JUnit Jupiter 5, WireMock (`FakeOkta`: discovery, token and JWKS endpoints)
- **Foundation Libraries**: Strata Framework Set (strata.foundation.core, strata.server.core, strata.client.core, strata.outbox.core)

## Project Structure

```
Granite.Session/
├── Applications/
│   ├── Granite.Session.CommandHost/    # Spring Boot app for command operations (port 8088)
│   └── Granite.Session.QueryHost/      # Spring Boot app for query operations (port 8089)
├── Components/
│   ├── Granite.Session.Service/        # Shared interfaces, DTOs, and events (Java + TypeScript)
│   ├── Granite.Session.Server/         # Server-side implementation (domain, application, platform)
│   └── Granite.Session.Client/         # REST client and Kafka event receiver (Java + TypeScript)
├── Tests/
│   ├── Granite.Session.ServiceTest/    # Test fixtures for service interfaces
│   ├── Granite.Session.ServerTest/     # Server unit/integration tests
│   ├── Granite.Session.ClientTest/     # Client integration tests
│   └── Granite.Session.TestSuite/      # Aggregated test suite
├── build.gradle                                # Root build with dependency versions
├── settings.gradle                             # Multi-project module declarations
└── package.json                                # Root Yarn workspace for TypeScript builds
```

## Architecture

The microservice follows a strict layered architecture with CQRS (Command Query Responsibility Segregation). Commands and queries are separated into distinct host applications, service interfaces, and controllers.

### Layers (Granite.Session.Server)

Each layer is organized into a dedicated Java package (`granite.session.server.*`):

- **`server.platform`** — Spring REST controllers, Spring configuration, JPA/Hibernate unit-of-work wiring, session event sender implementations. Depends on `server.application` and `server.domain`.
- **`server.application`** — Service implementations (`SessionCommandService`, `SessionQueryService`), context objects, JWT minting, the Okta client, ModelMapper configuration. Depends on `server.domain`.
- **`server.domain`** — Domain entity (`Session`), repository interface/implementation, domain event types. No upward dependencies.

### Domain Entity

`Session` extends `AbstractEntityAndDomainEventSource`:

| Field | Type | Notes |
|---|---|---|
| `sessionId` | `Long` | Primary key, auto-generated |
| `userId` | `Long` | Logical reference to User (plain BIGINT, no JPA relation); indexed |
| `accessToken` | `String` | Signed JWT (RS256); unique index; never returned in `SessionData` |
| `refreshTokenHash` | `String` | SHA-256 hex of the current refresh token; unique. The raw token is never stored |
| `previousRefreshTokenHash` | `String` | Hash of the token that was just rotated out; used for reuse detection; indexed |
| `created` | `Instant` | Set at construction |
| `expires` | `Instant` | Set once at login to `created + granite.session.max-lifetime` (default `PT24H`) and never slid. Closing a session truncates it to the close time |
| `deviceId` | `String` | Composite index with `userId` |
| `values` | `Map<String,Object>` | User-activity state; persisted as a JSON blob in the `value_map` column via `ValueMapConverter` |
| `hashedHandle` | `String` | SHA-256 hex of the opaque session handle (the future `GRANITE_SID` cookie value), hashed like `refreshTokenHash`; unique. The raw handle is never stored |
| `lastActivity` | `Instant` | Set at login; updated by `resolveSession` at most once a minute |
| `closedReason` | `SessionClosedReason` | `null` while open. One of `LOGOUT`, `EXPIRED`, `REPLACED`, `DISPLACED`, `REVOKED_SECURITY`, `ADMIN` (enum in Granite.Session.Service, `granite.session.service.requestreply`) |
| `activeMarker` | `Boolean` | `TRUE` while open, `NULL` once closed. Unique index `ux_session_active_user_device (userId, deviceId, activeMarker)` (a `unique-constraint` in `Session.orm.xml`) allows one active session per (user, device) |
| `continuityId` | `UUID` | Stays the same across replacements; exposed in `SessionData` and as the JWT claim `cid` |
| `replacedBy`, `copiedFrom` | `Long` | Session links for audit and support (`replacedBy` on the replaced session, `copiedFrom` on the session whose `values` were copied) |
| `loginMethod` | `LoginMethod` | `PASSWORD`, `ACCESS_TOKEN` or `OKTA` (`server.domain`); `null` on rows from before the rename (logout still succeeds, without `endSessionUrl`) |
| `oktaIdToken` | `String` (TEXT) | The Okta `id_token` of an Okta session, kept for the logout `id_token_hint`; cleared when the session closes; never in `SessionData` |
| `oktaGroups` | `Set<String>` | Groups from this session's own Okta ID token; used to recalculate authorities at re-mint; never copied to another session |
| `version` | `Long` | Optimistic locking |

Closing (`Session.close(reason,at)`) sets `closedReason`, clears `activeMarker`, clears `oktaIdToken`, sets `expires = min(expires,at)` and fires one `SessionUpdated`. Closed rows stay until `SessionPurgeJob` deletes them (their `expires` is in the past). Because `expires` is truncated, the Gateway's `jti` revocation check (`sessions` non-empty and `expires > now`) treats a closed session as revoked; `SessionJwtValidator` also rejects a non-null `closedReason`.

`OktaLoginTransaction` extends `AbstractEntity` (table `OktaLoginTransaction`, mapped in `Session.orm.xml`; the schema comes from the ORM mapping, `schema.sql` is not involved): `hashedTransactionId` (SHA-256 hex of the random transaction id, unique; the raw id lives only in the `GRANITE_LOGIN` cookie), `state`, `nonce`, `codeVerifier` (PKCE), `returnTo`, `deviceId`, `expires` (`created + granite.session.okta.transaction-ttl`, `PT5M`). A row is deleted by the first completion attempt that finds it, and `SessionPurgeJob` deletes expired rows. Repository: `IOktaLoginTransactionRepository` (`findByHashedTransactionId`, `findExpiredBefore`).

### Component Responsibilities

#### Granite.Session.Service

The shared contract component. Contains no implementation logic. Defines:

- **Service interfaces**: `ISessionCommandService`, `ISessionQueryService`
- **Request/Reply DTOs**: `LoginRequest`/`Reply`, `StartOktaLoginRequest`/`Reply`, `CompleteOktaLoginRequest`/`Reply`, `LogoutRequest`/`Reply` (the reply carries `endSessionUrl`), `LogoutAllForUserRequest`/`Reply`, `RefreshRequest`/`Reply`, `ResolveSessionRequest`/`Reply`, `FindSessionsByKeyRequest`/`Reply`, `FindSessionsByKeySetRequest`/`Reply`, `FindSessionByUserIdAndDeviceIdRequest`/`Reply`
- **Data transfer objects**: `SessionData`
- **Key types**: `SessionKey` (names `SESSION_ID`, `USER_ID`), `SessionKeySet` (names `SESSION_IDS`, `USER_IDS`). Lookup by access token is deliberately not offered, so tokens never appear in query requests.
- **Credentials**: carried as sibling scalar fields `credential: String` and `credentialKind: CredentialKind`. `CredentialKind` (`PASSWORD`, `ACCESS_TOKEN`, `SECURITY_CODE`) lives in Granite.User.Service.
- **Event interfaces**: `ISessionEventSender`, `ISessionEventReceiver`, `ISessionEventListener`
- **TypeScript mirror**: All interfaces and DTOs are also defined in TypeScript under `src/main/typescript/`

#### Granite.Session.Server

- **Domain layer** (`server.domain`):
  - `Session` — JPA entity; fires domain events on create/update/destroy
  - `ISessionRepository` / `SessionRepository` — Repository interface and JPA implementation
  - Domain events: `SessionCreated`, `SessionUpdated`, `SessionDestroyed` extending `AbstractSessionEvent`
  - Observer pattern: `ISessionEventObserver`, `ISessionEventSource`

- **Application layer** (`server.application`):
  - `SessionCommandService` — Implements `ISessionCommandService` and `ISessionEventObserver`. Provides login, startOktaLogin, completeOktaLogin, logout, logoutAllForUser, refresh and resolveSession. Translates domain events into service-level session events via `ISessionEventSender`.
  - `SessionQueryService` — Implements `ISessionQueryService`. Provides find-by-key, find-by-key-set, and find-by-user-id-and-device-id (open session only) operations.
  - Context objects for each operation
  - `MappingModule` — ModelMapper type mappings between Session domain objects and Session DTOs
  - `ApplicationConfiguration` — Spring `@Configuration` that wires session services with request scope

- **Platform layer** (`server.platform`):
  - `SessionCommandServiceController` — Spring `@RestController` exposing command endpoints under `/session-command-service/`
  - `SessionQueryServiceController` — Spring `@RestController` exposing query endpoints under `/session-query-service/`
  - `PlatformConfiguration` (JPA/UnitOfWork, health), `CommandPlatformConfiguration` (command controller, outbox repository), `QueryPlatformConfiguration` (query controller)
  - `OktaKeyLoader` — loads the Okta client-assertion key from `granite.session.okta.keystore.*`; a classpath key store is allowed only in `local` and `development`
  - `JwtKeyLoader` — loads the RSA key pair from configured locations; classpath keys are allowed only in the `local` and `development` environments (and never with `test`/`production`)
  - Event senders: `OnCommitSessionEventSender`, `OutboxEventSessionEventSender`, `MockSessionEventSender` (testing)

- **Configuration layer** (`server.configuration`): the entry configurations that hosts extend, following the Strata.Outbox → Granite.Outbox pattern.
  - `AbstractGraniteSessionServerConfiguration` — imports `ApplicationConfiguration`, `DomainConfiguration`, `ServiceConfiguration`, and springdoc support. It declares the shared seam beans and the host plumbing: SecurityContextHolder strategy, request-scope registration, and the springdoc `ObjectMapperProvider`/`SpringDocConfigProperties`.
  - `GraniteSessionCommandServerConfiguration` — adds `CommandPlatformConfiguration` and the command seams: event sender, JWT, Okta, User clients.
  - `GraniteSessionQueryServerConfiguration` — adds `QueryPlatformConfiguration`.

#### Granite.Session.Client

- **Request/Reply clients** (Java): `SessionCommandServiceClient`, `SessionQueryServiceClient` extending `AbstractRestClient`
- **Client interfaces** (Java): `ISessionCommandServiceClient`, `ISessionQueryServiceClient`
- **Event receiver**: `SessionKafkaEventReceiver`, `SessionKafkaAvroEventReceiver`
- **TypeScript mirror**: `SessionCommandServiceClient`, `SessionQueryServiceClient` with corresponding interfaces

### Applications

Both hosts are thin subclasses of the entry configurations. They contain no bean definitions of their own.

- **SessionCommandHost** — `HostConfiguration extends GraniteSessionCommandServerConfiguration`. Listens on port 8088.
- **SessionQueryHost** — `HostConfiguration extends GraniteSessionQueryServerConfiguration`. Listens on port 8089.

`main()` reads `DEPLOY_ENV` (default `local`), activates it as the only Spring profile, and publishes it as a default property. The only environments are `local`, `development`, `test` and `production`; each host has `application.properties` plus one `application-<env>.properties` per environment, following `Templates/__System__` (`local`: docker-host Postgres; `development`/`test`/`production`: cluster Postgres. `local` and `development` update the schema automatically (`ddl-auto=update`); `test` and `production` use `validate` until Flyway is introduced). Configuration comes from two places:
- **`IConfiguration`** (JPA and datasource settings) comes from `SpringConfigurationProvider(environment)`, which loads `application.properties` plus `application-<DEPLOY_ENV>.properties`, resolves `${...}` placeholders (e.g. `${DB_PASSWORD}`), and decrypts `ENC(...)`.
- **Spring-bound settings** (`granite.session.*`, User service hosts) come from the same profile file, which Boot loads natively.

`spring.jpa.mapping-resources` must include `OutboxEvent.orm.xml` for the outbox event sender.

TLS: in every environment, both hosts serve HTTPS with the `granite-services` SSL bundle (`server.ssl.bundle`), like the Gateway, FrontEndController, Outbox and User. The keystore and truststore files (`granite-services.p12`, `granite-truststore.p12`) are supplied outside the repository. In `production`, their locations and passwords come from `${GRANITE_KEYSTORE_*}` / `${GRANITE_TRUSTSTORE_*}`.
- The CommandHost's User clients trust the bundle through `granite.session.user-client.ssl-bundle`.
- The QueryHost fetches the JWKS through `granite.security.jwt.ssl-bundle`.
- The smoke tests run under `local` with a test-resource `application-local.properties` (H2, plain HTTP) that shadows the main one.

## Extending Granite.Session

Downstream application systems reuse Granite.Session by depending on `granite-session-server` and subclassing an entry configuration in their own host. A seam is replaced by overriding its `@Bean` method, which must be re-annotated with `@Bean`. Declaring a bean with the same name elsewhere does not work, because Spring Boot rejects bean definition overriding. Seam signatures take only injectable types (`Environment`, `ResourceLoader`, `ObjectProvider<SslBundles>`, ...) and never `@Value String` parameters, so overrides stay simple.

| Seam | Declared in | Default |
|---|---|---|
| `configuration(Environment)` | Abstract | `SpringConfigurationProvider(environment).get()` |
| `mapper()` / `configureMapper(ModelMapper)` | Abstract | registers `MappingModule`; override `configureMapper`, call `super`, then add modules |
| `applicationRoles()` | Abstract | `GraniteApplicationRoles.create()`; used for the default role, Okta role mapping, expansion and the catalog login gate. `applicationRolesStartupLog(ApplicationRoles)` logs the catalog role names once at startup |
| `clock()` | Abstract | `Clock.systemUTC()` |
| `schemaInitializer(EntityManagerFactory)` | Abstract | runs `schema.sql` against the persistence unit's DataSource |
| `requestLoggingFilter()` | Abstract | query string only |
| `openApi()` | Abstract | group `Session`, service path + `/health/**` |
| `securityFilterChain(HttpSecurity,JwtDecoder,GraniteJwtAuthenticationConverter,Environment)` | Abstract | stateless JWT resource server; public paths come from `getPublicPathPatterns(Environment)`: `OPTIONS`, `${strata.health.base-path}/**`, `/error` (and the springdoc paths outside `production`), plus on the Command side `login`, `start-okta-login`, `complete-okta-login`, `refresh`, `resolve-session` and `/.well-known/jwks.json` |
| `jwtDecoder(Environment,ObjectProvider<IJwkSetProvider>)` | Abstract | CommandHost validates in-process against its own `IJwkSetProvider`. QueryHost (or any host with `granite.security.jwt.jwk-set-uri` set) fetches the JWKS remotely |
| `jwtAuthenticationConverter()` | Abstract | `GraniteJwtAuthenticationConverter` (`sub` principal, `authorities` claim) |
| `sessionEventSender(IOutboxEventRepository)` | Command | `OutboxEventSessionEventSender` |
| `jwtTokenService(Environment,ResourceLoader)` | Command | `JwtTokenService` with keys from `JwtKeyLoader` and `granite.session.jwt.*` |
| `jwkSetProvider(Environment,ResourceLoader)` | Command | `JwkSetProvider` with the public key at `granite.session.jwt.public-key-location`, plus `granite.session.jwt.previous-public-key-location` when set (key rotation) |
| `refreshTokenService()` | Command | `RefreshTokenService`: 256-bit SecureRandom base64url tokens, SHA-256 hex hashes |
| `oktaSettings(Environment)` | Command | `OktaSettings` from `granite.session.login.okta.enabled` (default `false`) and `granite.session.okta.*`: `issuer`, `client-id`, `redirect-uri`, `post-logout-redirect-uri`, `role-assignment` (`ON_CREATE` default, `EVERY_LOGIN`), `unmapped-groups` (`DEFAULT_ROLE` default, `REFUSE`), `group-mapping.<group>=ROLE_X` (catalog roles only) (use `group-mapping[Group Name]` for names with spaces), `transaction-ttl` (`PT5M`), `discovery-cache-ttl` (`PT1H`), `clock-skew` (`PT60S`), `http-timeout` (`PT5S`); scope is always `openid profile email groups` |
| `oktaClient(OktaSettings,Environment,ResourceLoader,Clock)` | Command | `OktaClient`; when Okta is enabled the client-assertion key comes from `OktaKeyLoader` (`granite.session.okta.keystore.{location,password,type (PKCS12),alias,key-password,key-id}`; `kid` = `key-id` or the RFC 7638 thumbprint). Disabled: no key store is read |
| `sessionPurgeJob(ObjectProvider<ISessionRepository>,ObjectProvider<IOktaLoginTransactionRepository>,ObjectProvider<ISpringUnitOfWorkManager>,Clock,Environment)` | Command | `SessionPurgeJob` (`SmartLifecycle`): every `granite.session.purge.interval` (`PT15M`) it deletes up to `granite.session.purge.batch-size` (500) expired sessions and, separately, up to the same number of expired Okta login transactions (`purgeExpiredOktaTransactions()`), oldest first, in its own request scope and transaction, without firing `SessionDestroyed`. `granite.session.purge.enabled=false` disables the schedule; `purgeExpired()` can still be called directly |
| `serviceTokenProvider(IJwtTokenService,Clock,Environment)` | Command | `ServiceTokenProvider`: a cached service JWT (`granite.session.service-token.subject` default `service:granite-session`, `.authorities` default `SCOPE_internal`, `.ttl` default `PT5M`), renewed a minute before expiry |
| `loginGate(Environment,ApplicationRoles)` | Command | `RequiredAuthoritiesLoginGate` when `granite.session.login.required-authorities` (comma-separated) is non-empty: a login is permitted only when the user holds at least one listed authority. Otherwise `CatalogRoleLoginGate`: permitted when the user holds at least one catalog role (`ApplicationRoles.isRole`). The CommandHost `local` and `development` profiles set `ROLE_ADMIN,ROLE_AUDITOR` |
| `oktaGroupMappingCheck(OktaSettings,ApplicationRoles)` | Command | `OktaGroupMappingCheck`: once at startup, logs a WARN with the group key of every `group-mapping` entry that is not a catalog role; `getIgnoredGroups()` returns those keys |
| `userCommandServiceClient(...)` / `userQueryServiceClient(...)` | Command | REST clients to `granite.user-{command,query}-service.host`, with optional SSL bundle `granite.session.user-client.ssl-bundle`. `createClientBuilder(sslBundles,environment,serviceTokenProvider)` is a protected hook; it registers `ServiceTokenClientRequestFilter`, so every User call carries `Authorization: Bearer <service token>` |

Security: Session only authenticates. Authorization rules belong to the Gateway's `securityWebFilterChain`. The one authorization-style decision made outside the Gateway is the login gate (`ILoginGate`): it is applied at login only, to `login` (PASSWORD and ACCESS_TOKEN) and `completeOktaLogin` (on the calculated Okta authorities), and refresh is not re-gated. Switching the login gate on does not evict live sessions: `refresh` is a public Gateway path, so sessions minted before the switch keep refreshing until `granite.session.max-lifetime` (24h default) unless ended with `logout-all-for-user`. Every refused login replies `success=false` with `invalid credentials`, revealing nothing about roles or account existence. `SecurityAutoConfiguration` stays excluded in the host mains, because the entry configuration enables web security itself. The SecurityContextHolder strategy is set in `beanFactoryPostProcessor()`, before any security filter captures it.

Domain extension is done through the `Session` values map (`setValue`/`getValue`) together with `Session.getRegistry()` (`StringMapperRegistry`), not by subclassing the entity.

Plain (non-Boot) Spring contexts, such as the ServerTest configurations, must supply a Jackson `ObjectMapper` bean because springdoc's `StrataModelResolver` requires one. Boot hosts get it from Jackson auto-configuration.

## Login and JWT

- `login` dispatches on `credentialKind`: `PASSWORD` calls `IUserCommandServiceClient.authenticateUser` and mints a Session on success (a user with no authorities whose `values` lack the marker `granite.rolesAssigned` (`SessionCommandService.ROLES_ASSIGNED`) first gets the catalog default role, expanded; the same `updateUser` writes the roles and `values` = the user's existing values from the `authenticateUser` reply plus `granite.rolesAssigned=true`, because `UpdateUserData.values` replaces the whole map; a failed write refuses the login as `invalid credentials`; the role is stored before the login gate runs, so it stays stored even if the gate then refuses, as for auto-created Okta users. Once the marker is set the default is never assigned again, so an Admin who clears a user's roles revokes access (the login gate then refuses); ACCESS_TOKEN logins never assign); `ACCESS_TOKEN` validates against Session's own store (a closed or expired session fails) and mints a new Session for the same user, with authorities re-read through `IUserQueryServiceClient.findUsersByKey` (a missing user fails); `SECURITY_CODE` returns a not-supported failure in v1.
- Every login path that creates a session (`login` with either credential, `completeOktaLogin`) applies the lifecycle for user U on device D after the login gate: open sessions on D that are past `expires`/`max-lifetime` close as `EXPIRED`; another user's session closes as `DISPLACED` (nothing is copied); U's own session closes as `REPLACED` and is the source (new session keeps its `continuityId` and copies `values`; the old one gets `replacedBy`, the new one `copiedFrom`). With nothing to replace, U's most recently active (`lastActivity`) live open session on another device is the source ("start from latest": `values` copied, new `continuityId`); otherwise the session starts empty with a new `continuityId`. Authorities are never copied.
- Concurrency: after the closes, the pipeline re-queries open sessions on D (which also flushes the closes before the new row is inserted); a concurrent winner, a unique-index violation or an optimistic-lock failure completes the login stage exceptionally (rolled back by Spring). `ConcurrencyRetryingSessionCommandService`, wrapped around the service by the controller bean, retries `login`/`completeOktaLogin` once in a new transaction, so the retry replaces the winner; a second failure replies `success=false`, `Unable to log in`.
- Okta login: see "Okta login" below.
- Every successful `login` reply carries `accessToken` and an opaque `refreshToken`. Only the refresh token's SHA-256 hash is stored on the Session.
- Every successful `login` and `completeOktaLogin` reply also carries `sessionHandle`: a 256-bit SecureRandom base64url handle generated by `IRefreshTokenService`. It is returned once; only its hash (`hashedHandle`) is stored.
- `resolveSession` takes `{handle}` and returns `{session, accessToken}`. It hashes the handle and looks up `hashedHandle`; a blank, unknown or closed handle fails, and a session past `expires` or `created + max-lifetime` is closed as `EXPIRED` (fires `SessionUpdated`) and fails. Every failure replies `success=false` with exactly `invalid session`. When the stored JWT expires within `granite.session.token.renewal-margin` (default `PT2M`), or its expiry cannot be read, the user is re-read through `IUserQueryServiceClient.findUsersByKey` (authorities recalculated; for an `OKTA` session the User's stored authorities, for every login method; a missing user fails), a new JWT is minted (never past `expires`) and `SessionUpdated` fires. `lastActivity` is written at most once a minute (no event). `ResolveSessionRequest.toString()` and `ResolveSessionReply.toString()` never print the handle or the token, and the pipeline logs only session IDs. `/resolve-session` is a public path (the handle is the credential, like the refresh token for `refresh`); the Gateway calls it without an `Authorization` header and denies external requests to it.
- `refresh` takes `{refreshToken}` (not a sessionId). It looks the session up by the token's hash, rejects a closed session (`session closed`) or one past `expires` or `created + granite.session.max-lifetime` (default `PT24H`, `session expired`), re-reads the user through `IUserQueryServiceClient.findUsersByKey` (a missing user is rejected with `user unavailable`) and sets the session authorities to the User's stored roles, rotates the refresh token (the current hash becomes `previousRefreshTokenHash`), mints a new JWT, and fires `SessionUpdated`. `expires` does not change. The reply carries the new `accessToken` and `refreshToken`.
- Presenting a refresh token that matches a session's `previousRefreshTokenHash` is treated as theft: the session is closed as `REVOKED_SECURITY` (an already-closed session keeps its reason) and the refresh fails. Each refresh token is therefore single-use.
- `logout` closes the session as `LOGOUT` (already closed: success, no change); for an open `OKTA` session it first builds `LogoutReply.endSessionUrl` = `end_session_endpoint?id_token_hint=<id_token>[&post_logout_redirect_uri=...]` (a discovery failure only omits the URL), and `LogoutReply.toString()` never prints it; `logoutAllForUser` closes every open session of the user as `ADMIN`. Neither deletes rows.
- Close and revocation events are `UPDATED` service events whose Avro field `closedReason` names the reason (`null` on other events).
- Lifetime is `granite.session.max-lifetime` only; there is no idle timeout and `granite.session.session-ttl` is no longer read.
- Access tokens are RS256 JWTs (private key at `granite.session.jwt.private-key-location`). The header carries `typ=JWT` and `kid` (the RFC 7638 thumbprint of the public key). Claims: `iss=granite-session`, `sub=userId`, `jti=sessionId`, `iat`, `exp`, `aud`, `did=deviceId`, `cid=continuityId` (when set), `authorities`.
- Access tokens are short-lived: `exp = min(now + granite.session.jwt.ttl, session.expires)`, where `jwt.ttl` defaults to `PT15M`. Clients get a new one via `refresh`. Revocation relies on this short lifetime: after logout, an outstanding access token stays valid until its `exp`.
- Consumers validate the signature/claims locally, then check revocation via `ISessionQueryServiceClient.findSessionsByKey(SessionKey.ofSessionId(jti))`.

## Okta login

Server-side authorization-code flow with PKCE `S256`; Session is a confidential client using `private_key_jwt`. Okta tokens never leave Session except the `id_token` in the logout `id_token_hint`. The FEC is the only caller: both operations are public at Session (the FEC calls them before there is a session) and denied externally at the Gateway.

- `startOktaLogin({deviceId, returnTo})` → `{authorizeUrl, transactionId}`. Refused (`Okta login is not available`) when `granite.session.login.okta.enabled=false`. Generates a 256-bit transaction id, `state`, `nonce` and PKCE verifier, stores an `OktaLoginTransaction` (hash of the id only, `returnTo` kept only when it is a local path, else `/`), and builds the authorize URL from the discovery document (`response_type=code`, `client_id`, `redirect_uri`, `scope=openid profile email groups`, `state`, `nonce`, `code_challenge`, `code_challenge_method=S256`, plus `prompt=login` when any live session is open on the device, because the logging-in user is not known yet). The raw transaction id is returned once; the FEC puts it in `GRANITE_LOGIN`.
- `completeOktaLogin({code, state, transactionId, deviceId})` → `{sessionData, sessionHandle, userName, returnTo}` (no Granite access or refresh token). Pipeline: consume the transaction (deleted on every attempt that finds it; unknown, expired, other device, wrong or missing `state`, missing `code` → reject) → exchange the code at the token endpoint (`grant_type=authorization_code`, `code_verifier`, `redirect_uri`, `client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer`, RS256 `client_assertion` with `iss=sub=client-id`, `aud=token endpoint`, 5-minute `exp`, random `jti`) → validate the ID token (signature against the discovered JWKS, RS256 only, `iss`, `aud` contains the client id, `exp` present and not past (60 s skew), `nonce` equals the transaction's) → `findUsersByExternalIdentity(provider="okta", externalId=sub)`, creating the User when missing with `externalIdentity=okta:<sub>`, `authorities` = the mapped catalog roles expanded (or the default role), `values` = `{granite.rolesAssigned: true}`, no password and `userName` = `preferred_username`, else `email`, else `sub` (the `externalIdentity` is always `okta:<sub>`) → authorities = the User's stored roles → login gate → Step 2 lifecycle (`prepareLifecycle`/`newSession`) → session with `loginMethod=OKTA`, `oktaIdToken`, `oktaGroups`.
- Every rejection replies `success=false`, `invalid credentials`; an unexpected error replies `Unable to log in` without exception data. A new Okta user refused by the login gate stays created with its assigned roles until an admin grants a gated role.
- Retry: `ConcurrencyRetryingSessionCommandService` retries `completeOktaLogin` once on a conflict. The command service is request-scoped, so the retry runs on the same instance, which remembers the validated identity per hashed transaction id and does not exchange the (single-use) code again; the rollback of the first attempt restores the transaction row.
- Roles (`granite.session.okta.role-assignment`, `unmapped-groups`): `OktaRoles` maps the ID token groups through `group-mapping` (non-catalog values ignored) and expands them over `ApplicationRoles`; no mapped group gives the catalog default role (`DEFAULT_ROLE`) or refuses (`REFUSE`, `user unavailable`). `ON_CREATE` applies the roles only when the User is created, later logins use the stored roles; `EVERY_LOGIN` writes changed roles back with `updateUser` (a failed write refuses) and uses them. Session authorities are always the User's stored roles, including at every `resolveSession` renewal and every `refresh` re-mint.
- Discovery (`<issuer>/.well-known/openid-configuration`) is cached for `discovery-cache-ttl`; a document whose `issuer` differs from the configured one is rejected. JWKS keys are cached by Nimbus and refetched on an unknown `kid`.
- Never logged or in any `toString`: the code, the verifier, `state`, `nonce`, the transaction id, the ID token, the client assertion and the session handle (`CompleteOktaLoginTest.neverLogsSecrets`, `OktaRequestReplyTest`).

## Commonly Used Design Patterns

### CQRS (Command Query Responsibility Segregation)

Commands (login, startOktaLogin, completeOktaLogin, logout, logoutAllForUser, refresh, resolveSession) and queries (find) are separated into distinct service interfaces (`ISessionCommandService`, `ISessionQueryService`), distinct controllers (`SessionCommandServiceController`, `SessionQueryServiceController`), and distinct host applications (`SessionCommandHost`, `SessionQueryHost`).

### Request-Reply

All service operations accept a typed request DTO and return a typed reply DTO. Requests extend `AbstractServiceRequest` (with `requestId`, `timestamp`, headers, cookies). Replies extend `AbstractServiceReply` (with `replyId`, `originatingRequestId`, `success`, `successMessage`, `failureMessage`, `exception`). Each operation has its own request/reply pair (e.g., `LoginRequest` → `LoginReply`).

### Pipeline (Chain of Responsibility)

All service methods follow an asynchronous pipeline pattern using `CompletionStage`:

```
validate → mapToContext → [operation steps] → buildReply
                                                  ↓ (on failure)
                                             buildReply(exception)
```

Each step receives and returns a typed context object that accumulates state through the pipeline.

### Observer (Domain Events)

The `Session` domain object extends `AbstractEntityAndDomainEventSource` and fires domain events (`SessionCreated`, `SessionUpdated`, `SessionDestroyed`). `SessionCommandService` implements `ISessionEventObserver` and attaches itself to each `Session` it creates or retrieves. State changes are translated into service-level `SessionEvent`s and dispatched through `ISessionEventSender`.

### Transactional Outbox

`OnCommitSessionEventSender` defers sending until the database transaction commits. `OutboxEventSessionEventSender` serializes the event into an outbox table within the same transaction as the session state change, and a separate process polls the outbox and publishes to Kafka.

### Repository

`ISessionRepository` extends the Strata `IRepository<Long,Session>` interface. The concrete `SessionRepository` extends `AbstractRepository` and registers named JPA queries (by id set, access token, access token set, user id, user id set, user id and device id).

### Context Object

Each service pipeline uses a typed context object that carries the request, intermediate Session domain objects, and results through the pipeline steps. Context objects use fluent setters and provide convenience getters that delegate to the underlying request.

### Strategy (Event Sending)

`ISessionEventSender` has multiple implementations (`MockSessionEventSender`, `OnCommitSessionEventSender`, `OutboxEventSessionEventSender`) selected via Spring configuration.

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. A base `PlatformConfiguration` provides shared JPA/UnitOfWork beans, and `CommandPlatformConfiguration` / `QueryPlatformConfiguration` extend it. The entry configurations in `server.configuration` import the layer configurations and expose overridable seams. Each host's `HostConfiguration` simply extends the appropriate entry configuration.

## REST Endpoints

### Session Command Service (`/session-command-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /login` | `LoginRequest` | `LoginReply` |
| `POST /start-okta-login` | `StartOktaLoginRequest` | `StartOktaLoginReply` |
| `POST /complete-okta-login` | `CompleteOktaLoginRequest` | `CompleteOktaLoginReply` |
| `POST /logout` | `LogoutRequest` | `LogoutReply` |
| `POST /logout-all-for-user` | `LogoutAllForUserRequest` | `LogoutAllForUserReply` |
| `POST /refresh` | `RefreshRequest` | `RefreshReply` |
| `POST /resolve-session` | `ResolveSessionRequest` | `ResolveSessionReply` |

### JWK Set (CommandHost)

| Endpoint | Reply |
|---|---|
| `GET /.well-known/jwks.json` | Public JWK set (RS256, `use=sig`, thumbprint `kid`), cacheable for 5 minutes. Resource servers validate access tokens against it. To rotate keys, deploy the new key pair and set `previous-public-key-location` to the old public key until outstanding tokens expire. |

### Session Query Service (`/session-query-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /find-sessions-by-key` | `FindSessionsByKeyRequest` | `FindSessionsByKeyReply` |
| `POST /find-sessions-by-keyset` | `FindSessionsByKeySetRequest` | `FindSessionsByKeySetReply` |
| `POST /find-session-by-user-id-and-device-id` | `FindSessionByUserIdAndDeviceIdRequest` | `FindSessionByUserIdAndDeviceIdReply` |

## Preferred Coding Style and Idioms

### Interface Naming

All interfaces are prefixed with `I`. Examples: `IUserRepository`, `IUserCommandService`, `IUserEventSender`, `IUserEventObserver`, `IUserCommandServiceClient`.

### Class and Method Formatting

The `class` keyword and the `implements`/`extends` clause each start on their own line. Opening braces are on their own line (Allman style). Method return types are on a separate line from the method name. Example:

```java
//////////////////////////////////////////////////////////////////////////////
// UserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.io.Serializable;

public
class UserData
    implements Serializable
{
    private Long   userId;
    private String foo;

    public
    UserData()
    {
        userId = null;
        foo = null;
    }

    public UserData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public UserData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getFoo() { return foo; }
}

//////////////////////////////////////////////////////////////////////////////
```

Key formatting rules:

- **File header/footer**: Every Java file begins and ends with a `//////////////////////////////////////////////////////////////////////////////` comment. The second line contains the filename as a comment.
- **`class` on its own line**: The access modifier (`public`) is on one line, and `class ClassName` is on the next line. `extends` and `implements` clauses are indented on subsequent lines.
- **Method return type on its own line**: The access modifier and return type (e.g., `public UserData`) are on one line, and the method name with parameters (e.g., `setFoo(String f)`) is on the next line.
- **Allman brace style**: Opening braces `{` are on their own line for classes, methods, constructors, and control structures.
- **Compact getters**: Simple getters that return a single field use an inline body on the same line as the method name: `getFoo() { return foo; }`.
- **Column alignment**: Related field declarations are column-aligned by type and name (e.g., `private final IUserRepository  repository;` and `private final ModelMapper            mapper;`).
- **Constructor field alignment**: Assignment statements in constructors are column-aligned on the `=` sign.
- **Minimal space after commas**: Method calls and declarations use `(param1,param2)` with no space after commas in most cases, or a single space after commas when it improves readability.

### Fluent Setters and Fluent Methods

All setters return `this` (or the concrete subclass type) to enable method chaining. This applies to request, reply, context, and data classes. When overriding base class setters, the return type is narrowed to the concrete class via a cast:

```java
@Override
public CreateUserReply
setSuccess(boolean success)
{
    return (CreateUserReply)super.setSuccess(success);
}
```

Fluent style is also used for context mutation methods and builder patterns:

```java
return
    new CreateUserReply(context.getRequest())
        .setSuccess(true)
        .setSuccessMessage("User: " + created.getPrimaryId() + " created")
        .setCreatedUser(mapper.map(created,UserData.class));
```

### Singular and Plural Operations

Command operations come in singular and plural variants:
- **Singular**: `createUser`, `updateUser`, `destroyUser` — operate on a single user
- **Plural**: `createUsers`, `destroyUsers` — operate on a batch of user instances with independent success/failure tracking per user

Plural replies include `requested`, `failed`, and `succeeded` collections to report partial success.

### Method Organization

Within a class, methods are grouped by access level: public methods first, then protected, then private. Within each group, methods follow the declaration order of the interface or base class they implement. For service implementations, the order is: public service methods, then `onEvent`/`onException`, then protected pipeline steps (`validate`, `mapToContext`, operation methods, `save`, `buildReply`), then private helper methods.

### Stream vs. Loop Style

Prefer explicit `for` loops for simple iteration. Use streams with `Collectors` for set-difference, mapping, and collecting operations. Never use side effects inside stream `map` or `filter` operations — derive intermediate collections in separate stream pipelines instead.

### Validation

Each pipeline starts with a `validate` step. Use `Objects.requireNonNull` for required fields and throw `IllegalArgumentException` for empty collections.

### TypeScript Array Types

Use `Array<Type>` syntax rather than `Type[]` in all TypeScript code.
