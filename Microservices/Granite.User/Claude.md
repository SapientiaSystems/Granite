# Granite.User Microservice

## Overview

Granite.User is a Spring Boot microservice that manages the User entity within the Granite system. The microservice exposes command and query REST APIs for CRUD operations on User entities and emits domain events (UserCreated, UserUpdated, UserDestroyed) as user state changes occur. Events are propagated to external consumers via the Transactional Outbox pattern and Kafka.

## Technology Stack

- **Language**: Java 22 (Amazon Corretto), TypeScript
- **Build**: Gradle (multi-project), Yarn (TypeScript components)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Persistence**: JPA / Hibernate 6.4, PostgreSQL (production), H2 (testing)
- **Object Mapping**: ModelMapper 3.2
- **Eventing**: Kafka, Transactional Outbox (strata.outbox)
- **Containerization**: Google Jib (OCI images)
- **Testing**: JUnit Jupiter 5
- **Foundation Libraries**: Strata Framework Set (strata.foundation.core, strata.server.core, strata.client.core, strata.outbox.core)

## Project Structure

```
Granite.User/
├── Applications/
│   ├── Granite.User.CommandHost/    # Spring Boot app for command operations
│   └── Granite.User.QueryHost/      # Spring Boot app for query operations
├── Components/
│   ├── Granite.User.Service/        # Shared interfaces, DTOs, and events (Java + TypeScript)
│   ├── Granite.User.Server/         # Server-side implementation (domain, application, platform)
│   └── Granite.User.Client/         # REST client and Kafka event receiver (Java + TypeScript)
├── Tests/
│   ├── Granite.User.ServiceTest/    # Test fixtures for service interfaces
│   ├── Granite.User.ServerTest/     # Server unit/integration tests
│   ├── Granite.User.ClientTest/     # Client integration tests
│   └── Granite.User.TestSuite/      # Aggregated test suite
├── build.gradle                              # Root build with dependency versions
├── settings.gradle                           # Multi-project module declarations
└── package.json                              # Root Yarn workspace for TypeScript builds
```

## Architecture

The microservice follows a strict layered architecture with CQRS (Command Query Responsibility Segregation). Commands and queries are separated into distinct host applications, service interfaces, and controllers.

### Layers (Granite.User.Server)

Each layer is organized into a dedicated Java package:

- **`server.platform`** — Spring REST controllers, Spring configuration, JPA/Hibernate unit-of-work wiring, user event sender implementations. Depends on `server.application` and `server.domain`.
- **`server.application`** — Service implementations (`UserCommandService`, `UserQueryService`), context objects, ModelMapper configuration. Depends on `server.domain`.
- **`server.domain`** — Domain user (`User`), repository interface/implementation, domain event types. No upward dependencies.

### Component Responsibilities

#### Granite.User.Service

The shared contract component. Contains no implementation logic. Defines:

- **Service interfaces**: `IUserCommandService`, `IUserQueryService`
- **Request/Reply DTOs**: `CreateUserRequest`/`Reply`, `CreateUsersRequest`/`Reply`, `UpdateUserRequest`/`Reply`, `DestroyUserRequest`/`Reply`, `DestroyUsersRequest`/`Reply`, `FindUsersByKeyRequest`/`Reply`, `FindUsersByKeySetRequest`/`Reply`
- **Data transfer objects**: `CreateUserData`, `UpdateUserData`, `UserData`
- **Key types**: `UserKey` (single-value lookup key), `UserKeySet` (multi-value lookup key set)
- **Event interfaces**: `IUserEventSender`, `IUserEventReceiver`, `IUserEventListener`
- **TypeScript mirror**: All interfaces and DTOs are also defined in TypeScript under `src/main/typescript/`

#### Granite.User.Server

The server-side implementation:

- **Domain layer** (`server.domain`):
  - `User` — JPA user extending `AbstractEntityAndDomainEventSource`; fires domain events on create/update/destroy
  - `IUserRepository` / `UserRepository` — Repository interface and JPA implementation with `findAllByIdIn`, `findAllByFoo`, `findAllByFooIn` queries
  - Domain events: `UserCreated`, `UserUpdated`, `UserDestroyed` extending `AbstractUserEvent`
  - Observer pattern: `IUserEventObserver`, `IUserEventSource`

- **Application layer** (`server.application`):
  - `UserCommandService` — Implements `IUserCommandService` and `IUserEventObserver`. Provides create, create-batch, update, destroy, and destroy-batch operations. Translates domain events into service-level user events via `IUserEventSender`.
  - `UserQueryService` — Implements `IUserQueryService`. Provides find-by-key and find-by-key-set operations.
  - Context objects: `CreateUserContext`, `CreateUsersContext`, `UpdateUserContext`, `DestroyUserContext`, `DestroyUsersContext`, `FindUsersByKeyContext`, `FindUsersByKeySetContext`
  - `MappingModule` — ModelMapper type mappings between User domain objects and User DTOs
  - `ApplicationConfiguration` — Spring `@Configuration` that wires user services with request scope

- **Platform layer** (`server.platform`):
  - `UserCommandServiceController` — Spring `@RestController` exposing command endpoints under `/user-command-service/`
  - `UserQueryServiceController` — Spring `@RestController` exposing query endpoints under `/user-query-service/`
  - `PlatformConfiguration` — Base Spring configuration for JPA EntityManagerFactory, UnitOfWork, and synchronization manager
  - `CommandPlatformConfiguration` — Extends `PlatformConfiguration`; wires the command controller and imports the outbox repository configuration
  - `QueryPlatformConfiguration` — Extends `PlatformConfiguration`; wires query controller
  - Event senders: `OnCommitUserEventSender` (sends after transaction commit), `OutboxEventUserEventSender` (persists to outbox table), `MockUserEventSender` (testing)

- **Configuration layer** (`server.configuration`): the entry configurations that hosts extend, following the Strata.Outbox → Granite.Outbox pattern.
  - `AbstractGraniteUserServerConfiguration` — imports `ApplicationConfiguration`, `DomainConfiguration`, `ServiceConfiguration`, and springdoc support. It declares the shared seam beans and the host plumbing: SecurityContextHolder strategy, request-scope registration, and the springdoc `ObjectMapperProvider`/`SpringDocConfigProperties`.
  - `GraniteUserCommandServerConfiguration` — adds `CommandPlatformConfiguration` and the `userEventSender` seam.
  - `GraniteUserQueryServerConfiguration` — adds `QueryPlatformConfiguration`.

#### Granite.User.Client

REST client and event receiver implementations:

- **Request/Reply clients** (Java): `UserCommandServiceClient`, `UserQueryServiceClient` extending `AbstractRestClient`
- **Client interfaces** (Java): `IUserCommandServiceClient`, `IUserQueryServiceClient`
- **Event receiver**: `UserKafkaEventReceiver`, `UserKafkaAvroEventReceiver` — Kafka consumers for User events
- **TypeScript mirror**: `UserCommandServiceClient`, `UserQueryServiceClient` with corresponding interfaces

### Applications

Both hosts are thin subclasses of the entry configurations. They contain no bean definitions of their own.

- **UserCommandHost** — `HostConfiguration extends GraniteUserCommandServerConfiguration`. Listens on port 8081.
- **UserQueryHost** — `HostConfiguration extends GraniteUserQueryServerConfiguration`.

Configuration values come from `SpringConfigurationProvider(environment)`, which loads `application.properties` and `application-<DEPLOY_ENV>.properties`, resolves `${...}` placeholders, and decrypts `ENC(...)`. `spring.jpa.mapping-resources` must include `OutboxEvent.orm.xml` because the default event sender writes to the outbox table.

## Extending Granite.User

Downstream application systems reuse Granite.User by depending on `granite-user-server` and subclassing an entry configuration in their own host. A seam is replaced by overriding its `@Bean` method, which must be re-annotated with `@Bean`. Declaring a bean with the same name in a separate configuration does not work, because Spring Boot rejects bean definition overriding.

| Seam | Declared in | Default |
|---|---|---|
| `configuration(Environment)` | Abstract | `SpringConfigurationProvider(environment).get()` |
| `mapper()` / `configureMapper(ModelMapper)` | Abstract | registers `MappingModule`; override `configureMapper`, call `super`, then add modules |
| `passwordEncoder()` | Abstract | `BCryptPasswordEncoder(12)` |
| `requestLoggingFilter()` | Abstract | query string, payload (10000), headers |
| `openApi()` | Abstract | group `User`, service path + `/health/**` |
| `userEventSender(IOutboxEventRepository)` | Command | `OutboxEventUserEventSender` |

Domain extension is done through the `User` values map (`setValue`/`getValue`) together with `User.getRegistry()` (`StringMapperRegistry`), not by subclassing the entity.

```java
@Configuration
public
class HostConfiguration
    extends GraniteUserCommandServerConfiguration
{
    @Override
    protected ModelMapper
    configureMapper(ModelMapper mapper)
    {
        return super.configureMapper(mapper).registerModule(new RetailMappingModule());
    }
}
```

Plain (non-Boot) Spring contexts, such as `AnnotationConfigApplicationContext` in tests, must supply a Jackson `ObjectMapper` bean because springdoc's `StrataModelResolver` requires one. Boot hosts get it from Jackson auto-configuration.

## Commonly Used Design Patterns

### CQRS (Command Query Responsibility Segregation)

Commands (create, update, destroy) and queries (find) are separated into distinct service interfaces (`IUserCommandService`, `IUserQueryService`), distinct controllers (`UserCommandServiceController`, `UserQueryServiceController`), and distinct host applications (`UserCommandHost`, `UserQueryHost`). This allows independent scaling of command and query workloads.

### Request-Reply

All service operations accept a typed request DTO and return a typed reply DTO. Requests extend `AbstractServiceRequest` (with `requestId`, `timestamp`, headers, cookies). Replies extend `AbstractServiceReply` (with `replyId`, `originatingRequestId`, `success`, `successMessage`, `failureMessage`, `exception`). Each operation has its own request/reply pair (e.g., `CreateUserRequest` → `CreateUserReply`).

### Pipeline (Chain of Responsibility)

All service methods follow an asynchronous pipeline pattern using `CompletionStage`:

```
validate → mapToContext → [operation steps] → buildReply
                                                  ↓ (on failure)
                                             buildReply(exception)
```

Each step receives and returns a typed context object that accumulates state through the pipeline. The pattern ensures consistent validation, error handling, and reply construction across all operations.

### Observer (Domain Events)

The `User` domain object extends `AbstractEntityAndDomainEventSource` and fires domain events (`UserCreated`, `UserUpdated`, `UserDestroyed`) via the observer pattern. The `UserCommandService` implements `IUserEventObserver` and attaches itself to each `User` it retrieves from the repository. When the `User` notifies a state change, the service translates the domain event into a service-level `UserEvent` and dispatches it through `IUserEventSender`.

### Transactional Outbox

Event delivery uses the Transactional Outbox pattern to guarantee at-least-once delivery. `OnCommitUserEventSender` defers sending until the database transaction commits. `OutboxEventUserEventSender` serializes the event into an outbox table within the same transaction as the user state change, and a separate process polls the outbox and publishes to Kafka.

### Repository

`IUserRepository` extends the Strata `IRepository<Long,User>` interface providing standard `findById`, `save`, `delete` operations. The concrete `UserRepository` extends `AbstractRepository` and registers named JPA queries for `findAllByIdIn`, `findAllByFoo`, and `findAllByFooIn`.

### Context Object

Each service pipeline uses a typed context object (e.g., `CreateUserContext`, `DestroyUsersContext`) that carries the request, intermediate User domain objects, and results through the pipeline steps. Context objects use fluent setters and provide convenience getters that delegate to the underlying request.

### Strategy (Event Sending)

The `IUserEventSender` interface has multiple implementations (`MockUserEventSender`, `OnCommitUserEventSender`, `OutboxEventUserEventSender`) that are selected via Spring configuration, allowing the event delivery strategy to be swapped without changing service code.

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. A base `PlatformConfiguration` provides shared JPA/UnitOfWork beans, and `CommandPlatformConfiguration` / `QueryPlatformConfiguration` extend it with command- or query-specific beans. `HostConfiguration` at the application level imports and assembles all required configurations.

## REST Endpoints

### User Command Service (`/user-command-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /create-user` | `CreateUserRequest` | `CreateUserReply` |
| `POST /create-users` | `CreateUsersRequest` | `CreateUsersReply` |
| `POST /update-user` | `UpdateUserRequest` | `UpdateUserReply` |
| `POST /destroy-user` | `DestroyUserRequest` | `DestroyUserReply` |
| `POST /destroy-users` | `DestroyUsersRequest` | `DestroyUsersReply` |

### User Query Service (`/user-query-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /find-users-by-key` | `FindUsersByKeyRequest` | `FindUsersByKeyReply` |
| `POST /find-users-by-keyset` | `FindUsersByKeySetRequest` | `FindUsersByKeySetReply` |

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
