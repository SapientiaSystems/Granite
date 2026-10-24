# Granite.Session Microservice

## Overview

Granite.Session is a Spring Boot microservice that manages authentication and user-activity sessions within the Granite system. A Session entity is created when a user authenticates (credential-based login or OAuth2 federated login) and holds the issued access token (a signed JWT), device identifier, lifetime, and arbitrary user-activity state. Sessions are revoked on logout. The microservice exposes command and query REST APIs and emits domain events (SessionCreated, SessionUpdated, SessionDestroyed) as session state changes occur. Events are propagated to external consumers via the Transactional Outbox pattern and Kafka (topic `granite.session.events`).

Granite.Session mirrors the architecture, build layout, event delivery, and coding style of Granite.User. It depends on Granite.User.Client to authenticate passwords (`authenticateUser`) and look up OAuth2 identities (`findUsersByExternalIdentity`).

## Technology Stack

- **Language**: Java 23 (Amazon Corretto), TypeScript
- **Build**: Gradle (multi-project), Yarn (TypeScript components)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Persistence**: JPA / Hibernate 6.4, PostgreSQL (production), H2 (testing)
- **Object Mapping**: ModelMapper 3.2
- **Eventing**: Kafka, Transactional Outbox (strata.outbox)
- **Security**: spring-security-oauth2-jose (Nimbus JOSE JWT) for RS256 token minting and OAuth2 ID token validation
- **Containerization**: Google Jib (OCI images)
- **Testing**: JUnit Jupiter 5, WireMock (OAuth2 JWKS fixture)
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
- **`server.application`** — Service implementations (`SessionCommandService`, `SessionQueryService`), context objects, JWT minting, OAuth2 ID token validation, ModelMapper configuration. Depends on `server.domain`.
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
| `expires` | `Instant` | Set at login/refresh to now + configured TTL |
| `deviceId` | `String` | Composite index with `userId` |
| `values` | `Map<String,Object>` | User-activity state; persisted as a JSON blob in the `value_map` column via `ValueMapConverter` |
| `hashedHandle` | `String` | SHA-256 hex of the opaque session handle (the future `GRANITE_SID` cookie value), hashed like `refreshTokenHash`; unique. The raw handle is never stored |
| `lastActivity` | `Instant` | Set at login; updated by `resolveSession` at most once a minute |
| `closedReason` | `SessionClosedReason` | `null` while open. `resolveSession` sets `EXPIRED` when the session is past `expires` or `max-lifetime` (Step 2 of the server-side sessions roadmap adds the other reasons) |

### Component Responsibilities

#### Granite.Session.Service

The shared contract component. Contains no implementation logic. Defines:

- **Service interfaces**: `ISessionCommandService`, `ISessionQueryService`
- **Request/Reply DTOs**: `LoginRequest`/`Reply`, `LoginWithOauth2Request`/`Reply`, `LogoutRequest`/`Reply`, `LogoutAllForUserRequest`/`Reply`, `RefreshRequest`/`Reply`, `ResolveSessionRequest`/`Reply`, `FindSessionsByKeyRequest`/`Reply`, `FindSessionsByKeySetRequest`/`Reply`, `FindSessionByUserIdAndDeviceIdRequest`/`Reply`
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
  - `SessionCommandService` — Implements `ISessionCommandService` and `ISessionEventObserver`. Provides login, loginWithOauth2, logout, logoutAllForUser, and refresh. Translates domain events into service-level session events via `ISessionEventSender`.
  - `SessionQueryService` — Implements `ISessionQueryService`. Provides find-by-key, find-by-key-set, and find-by-user-id-and-device-id operations.
  - Context objects for each operation
  - `MappingModule` — ModelMapper type mappings between Session domain objects and Session DTOs
  - `ApplicationConfiguration` — Spring `@Configuration` that wires session services with request scope

- **Platform layer** (`server.platform`):
  - `SessionCommandServiceController` — Spring `@RestController` exposing command endpoints under `/session-command-service/`
  - `SessionQueryServiceController` — Spring `@RestController` exposing query endpoints under `/session-query-service/`
  - `PlatformConfiguration` (JPA/UnitOfWork, health), `CommandPlatformConfiguration` (command controller, outbox repository, `Oauth2ProvidersProperties`), `QueryPlatformConfiguration` (query controller)
  - `JwtKeyLoader` — loads the RSA key pair from configured locations; classpath keys are allowed only in the `local` and `development` environments (and never with `test`/`production`)
  - Event senders: `OnCommitSessionEventSender`, `OutboxEventSessionEventSender`, `MockSessionEventSender` (testing)

- **Configuration layer** (`server.configuration`): the entry configurations that hosts extend, following the Strata.Outbox → Granite.Outbox pattern.
  - `AbstractGraniteSessionServerConfiguration` — imports `ApplicationConfiguration`, `DomainConfiguration`, `ServiceConfiguration`, and springdoc support. It declares the shared seam beans and the host plumbing: SecurityContextHolder strategy, request-scope registration, and the springdoc `ObjectMapperProvider`/`SpringDocConfigProperties`.
  - `GraniteSessionCommandServerConfiguration` — adds `CommandPlatformConfiguration` and the command seams: event sender, JWT, OAuth2, User clients.
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
| `clock()` | Abstract | `Clock.systemUTC()` |
| `schemaInitializer(EntityManagerFactory)` | Abstract | runs `schema.sql` against the persistence unit's DataSource |
| `requestLoggingFilter()` | Abstract | query string only |
| `openApi()` | Abstract | group `Session`, service path + `/health/**` |
| `securityFilterChain(HttpSecurity,JwtDecoder,GraniteJwtAuthenticationConverter,Environment)` | Abstract | stateless JWT resource server; public paths come from `getPublicPathPatterns(Environment)`: `OPTIONS`, `${strata.health.base-path}/**`, `/error` (and the springdoc paths outside `production`), plus on the Command side `login`, `login-with-oauth2`, `refresh` and `/.well-known/jwks.json` |
| `jwtDecoder(Environment,ObjectProvider<IJwkSetProvider>)` | Abstract | CommandHost validates in-process against its own `IJwkSetProvider`. QueryHost (or any host with `granite.security.jwt.jwk-set-uri` set) fetches the JWKS remotely |
| `jwtAuthenticationConverter()` | Abstract | `GraniteJwtAuthenticationConverter` (`sub` principal, `authorities` claim) |
| `sessionEventSender(IOutboxEventRepository)` | Command | `OutboxEventSessionEventSender` |
| `jwtTokenService(Environment,ResourceLoader)` | Command | `JwtTokenService` with keys from `JwtKeyLoader` and `granite.session.jwt.*` |
| `jwkSetProvider(Environment,ResourceLoader)` | Command | `JwkSetProvider` with the public key at `granite.session.jwt.public-key-location`, plus `granite.session.jwt.previous-public-key-location` when set (key rotation) |
| `refreshTokenService()` | Command | `RefreshTokenService`: 256-bit SecureRandom base64url tokens, SHA-256 hex hashes |
| `oauth2IdTokenValidator(Oauth2ProvidersProperties)` | Command | `Oauth2IdTokenValidator` |
| `sessionPurgeJob(ObjectProvider<ISessionRepository>,ObjectProvider<ISpringUnitOfWorkManager>,Clock,Environment)` | Command | `SessionPurgeJob` (`SmartLifecycle`): every `granite.session.purge.interval` (`PT15M`) it deletes up to `granite.session.purge.batch-size` (500) expired sessions, oldest first, in its own request scope and transaction, without firing `SessionDestroyed`. `granite.session.purge.enabled=false` disables the schedule; `purgeExpired()` can still be called directly |
| `serviceTokenProvider(IJwtTokenService,Clock,Environment)` | Command | `ServiceTokenProvider`: a cached service JWT (`granite.session.service-token.subject` default `service:granite-session`, `.authorities` default `SCOPE_internal`, `.ttl` default `PT5M`), renewed a minute before expiry |
| `loginGate(Environment)` | Command | `RequiredAuthoritiesLoginGate` bound to `granite.session.login.required-authorities` (comma-separated). A login is permitted only when the user holds at least one listed authority; unset or empty permits everyone. Set to `ROLE_ADMIN,ROLE_AUDITOR` in the CommandHost `local` and `development` profiles only |
| `userCommandServiceClient(...)` / `userQueryServiceClient(...)` | Command | REST clients to `granite.user-{command,query}-service.host`, with optional SSL bundle `granite.session.user-client.ssl-bundle`. `createClientBuilder(sslBundles,environment,serviceTokenProvider)` is a protected hook; it registers `ServiceTokenClientRequestFilter`, so every User call carries `Authorization: Bearer <service token>` |

Security: Session only authenticates. Authorization rules belong to the Gateway's `securityWebFilterChain`. The one authorization-style decision made outside the Gateway is the login gate (`ILoginGate`): it is applied at login only, to `login` (PASSWORD and ACCESS_TOKEN) and `loginWithOauth2`, and refresh is not re-gated. Switching the login gate on does not evict live sessions: `refresh` is a public Gateway path, so sessions minted before the switch keep refreshing until `granite.session.max-lifetime` (24h default) unless ended with `logout-all-for-user`. Every refused login replies `success=false` with `invalid credentials`, revealing nothing about roles or account existence. `SecurityAutoConfiguration` stays excluded in the host mains, because the entry configuration enables web security itself. The SecurityContextHolder strategy is set in `beanFactoryPostProcessor()`, before any security filter captures it.

Domain extension is done through the `Session` values map (`setValue`/`getValue`) together with `Session.getRegistry()` (`StringMapperRegistry`), not by subclassing the entity.

Plain (non-Boot) Spring contexts, such as the ServerTest configurations, must supply a Jackson `ObjectMapper` bean because springdoc's `StrataModelResolver` requires one. Boot hosts get it from Jackson auto-configuration.

## Login and JWT

- `login` dispatches on `credentialKind`: `PASSWORD` calls `IUserCommandServiceClient.authenticateUser` and mints a Session on success; `ACCESS_TOKEN` validates against Session's own store and mints a new Session for the same user and device (original is not destroyed), carrying the existing session's authorities; `SECURITY_CODE` returns a not-supported failure in v1.
- `loginWithOauth2` validates the ID token with `NimbusJwtDecoder` against the configured provider's JWKS (`granite.session.oauth2.providers.<name>.{issuer-uri,audience,jwks-uri}`), finds the user via `findUsersByExternalIdentity`; an unknown identity is refused with `invalid credentials` and no user is ever created. Otherwise it applies the login gate and mints a Session.
- Every successful `login`/`loginWithOauth2` reply carries `accessToken` and an opaque `refreshToken`. Only the refresh token's SHA-256 hash is stored on the Session.
- Every successful `login`/`loginWithOauth2` reply also carries `sessionHandle`: a 256-bit SecureRandom base64url handle generated by `IRefreshTokenService`. It is returned once; only its hash (`hashedHandle`) is stored.
- `resolveSession` takes `{handle}` and returns `{session, accessToken}`. It hashes the handle and looks up `hashedHandle`; a blank, unknown or closed handle fails, and a session past `expires` or `created + max-lifetime` is closed as `EXPIRED` (fires `SessionUpdated`) and fails. Every failure replies `success=false` with exactly `invalid session`. When the stored JWT expires within `granite.session.token.renewal-margin` (default `PT2M`), or its expiry cannot be read, the user is re-read through `IUserQueryServiceClient.findUsersByKey` (authorities recalculated; a missing user fails), `expires` is set to `min(now + session-ttl, created + max-lifetime)`, a new JWT is minted and `SessionUpdated` fires. `lastActivity` is written at most once a minute (no event). `ResolveSessionRequest.toString()` and `ResolveSessionReply.toString()` never print the handle or the token, and the pipeline logs only session IDs. `/resolve-session` is not a public path: the caller (the Gateway) authenticates with its service token.
- `refresh` takes `{refreshToken}` (not a sessionId). It looks the session up by the token's hash, rejects it if the sliding `expires` or the absolute cap `created + granite.session.max-lifetime` (default `PT24H`) has passed, rotates the refresh token (the current hash becomes `previousRefreshTokenHash`), sets `expires = min(now + session-ttl, created + max-lifetime)`, mints a new JWT, and fires `SessionUpdated`. The reply carries the new `accessToken` and `refreshToken`.
- Presenting a refresh token that matches a session's `previousRefreshTokenHash` is treated as theft: the session is destroyed (`SessionDestroyed`) and the refresh fails. Each refresh token is therefore single-use.
- Access tokens are RS256 JWTs (private key at `granite.session.jwt.private-key-location`). The header carries `typ=JWT` and `kid` (the RFC 7638 thumbprint of the public key). Claims: `iss=granite-session`, `sub=userId`, `jti=sessionId`, `iat`, `exp`, `aud`, `did=deviceId`, `authorities`.
- Access tokens are short-lived: `exp = min(now + granite.session.jwt.ttl, session.expires)`, where `jwt.ttl` defaults to `PT15M`. Clients get a new one via `refresh`. Revocation relies on this short lifetime: after logout, an outstanding access token stays valid until its `exp`.
- Consumers validate the signature/claims locally, then check revocation via `ISessionQueryServiceClient.findSessionsByKey(SessionKey.ofSessionId(jti))`.

## Commonly Used Design Patterns

### CQRS (Command Query Responsibility Segregation)

Commands (login, loginWithOauth2, logout, logoutAllForUser, refresh, resolveSession) and queries (find) are separated into distinct service interfaces (`ISessionCommandService`, `ISessionQueryService`), distinct controllers (`SessionCommandServiceController`, `SessionQueryServiceController`), and distinct host applications (`SessionCommandHost`, `SessionQueryHost`).

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
| `POST /login-with-oauth2` | `LoginWithOauth2Request` | `LoginWithOauth2Reply` |
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
