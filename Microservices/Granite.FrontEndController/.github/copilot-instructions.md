# Granite.FrontEndController Microservice

## Overview

Granite.FrontEndController is a Spring Boot microservice that manages the FrontEndController entity within the Granite system. The microservice exposes command and query REST APIs for CRUD operations on FrontEndController entities and emits domain events (FrontEndControllerCreated, FrontEndControllerUpdated, FrontEndControllerDestroyed) as frontendcontroller state changes occur. Events are propagated to external consumers via the Transactional Outbox pattern and Kafka.

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
Granite.FrontEndController/
├── Applications/
│   ├── Granite.FrontEndController.CommandHost/    # Spring Boot app for command operations
│   └── Granite.FrontEndController.AdminHost/      # Spring Boot app for query operations
├── Components/
│   ├── Granite.FrontEndController.Service/        # Shared interfaces, DTOs, and events (Java + TypeScript)
│   ├── Granite.FrontEndController.Server/         # Server-side implementation (domain, application, platform)
│   └── Granite.FrontEndController.Client/         # REST client and Kafka event receiver (Java + TypeScript)
├── Tests/
│   ├── Granite.FrontEndController.ServiceTest/    # Test fixtures for service interfaces
│   ├── Granite.FrontEndController.ServerTest/     # Server unit/integration tests
│   ├── Granite.FrontEndController.ClientTest/     # Client integration tests
│   └── Granite.FrontEndController.TestSuite/      # Aggregated test suite
├── build.gradle                              # Root build with dependency versions
├── settings.gradle                           # Multi-project module declarations
└── package.json                              # Root Yarn workspace for TypeScript builds
```

## Architecture

The microservice follows a strict layered architecture with CQRS (Command Query Responsibility Segregation). Commands and queries are separated into distinct host applications, service interfaces, and controllers.

### Layers (Granite.FrontEndController.Server)

Each layer is organized into a dedicated Java package:

- **`server.platform`** — Spring REST controllers, Spring configuration, JPA/Hibernate unit-of-work wiring, frontendcontroller event sender implementations. Depends on `server.application` and `server.domain`.
- **`server.application`** — Service implementations (`FrontEndControllerCommandService`, `FrontEndControllerQueryService`), context objects, ModelMapper configuration. Depends on `server.domain`.
- **`server.domain`** — Domain frontendcontroller (`FrontEndController`), repository interface/implementation, domain event types. No upward dependencies.

### Component Responsibilities

#### Granite.FrontEndController.Service

The shared contract component. Contains no implementation logic. Defines:

- **Service interfaces**: `IFrontEndControllerCommandService`, `IFrontEndControllerQueryService`
- **Request/Reply DTOs**: `CreateFrontEndControllerRequest`/`Reply`, `CreateFrontEndControllersRequest`/`Reply`, `UpdateFrontEndControllerRequest`/`Reply`, `DestroyFrontEndControllerRequest`/`Reply`, `DestroyFrontEndControllersRequest`/`Reply`, `FindFrontEndControllersByKeyRequest`/`Reply`, `FindFrontEndControllersByKeySetRequest`/`Reply`
- **Data transfer objects**: `CreateFrontEndControllerData`, `UpdateFrontEndControllerData`, `FrontEndControllerData`
- **Key types**: `FrontEndControllerKey` (single-value lookup key), `FrontEndControllerKeySet` (multi-value lookup key set)
- **Event interfaces**: `IFrontEndControllerEventSender`, `IFrontEndControllerEventReceiver`, `IFrontEndControllerEventListener`
- **TypeScript mirror**: All interfaces and DTOs are also defined in TypeScript under `src/main/typescript/`

#### Granite.FrontEndController.Server

The server-side implementation:

- **Domain layer** (`server.domain`):
  - `FrontEndController` — JPA frontendcontroller extending `AbstractEntityAndDomainEventSource`; fires domain events on create/update/destroy
  - `IFrontEndControllerRepository` / `FrontEndControllerRepository` — Repository interface and JPA implementation with `findAllByIdIn`, `findAllByFoo`, `findAllByFooIn` queries
  - Domain events: `FrontEndControllerCreated`, `FrontEndControllerUpdated`, `FrontEndControllerDestroyed` extending `AbstractFrontEndControllerEvent`
  - Observer pattern: `IFrontEndControllerEventObserver`, `IFrontEndControllerEventSource`

- **Application layer** (`server.application`):
  - `FrontEndControllerCommandService` — Implements `IFrontEndControllerCommandService` and `IFrontEndControllerEventObserver`. Provides create, create-batch, update, destroy, and destroy-batch operations. Translates domain events into service-level frontendcontroller events via `IFrontEndControllerEventSender`.
  - `FrontEndControllerQueryService` — Implements `IFrontEndControllerQueryService`. Provides find-by-key and find-by-key-set operations.
  - Context objects: `CreateFrontEndControllerContext`, `CreateFrontEndControllersContext`, `UpdateFrontEndControllerContext`, `DestroyFrontEndControllerContext`, `DestroyFrontEndControllersContext`, `FindFrontEndControllersByKeyContext`, `FindFrontEndControllersByKeySetContext`
  - `MappingModule` — ModelMapper type mappings between FrontEndController domain objects and FrontEndController DTOs
  - `ApplicationConfiguration` — Spring `@Configuration` that wires frontendcontroller services with request scope

- **Platform layer** (`server.platform`):
  - `FrontEndControllerCommandServiceController` — Spring `@RestController` exposing command endpoints under `/frontendcontroller-command-service/`
  - `FrontEndControllerQueryServiceController` — Spring `@RestController` exposing query endpoints under `/frontendcontroller-admin-service/`
  - `PlatformConfiguration` — Base Spring configuration for JPA EntityManagerFactory, UnitOfWork, and synchronization manager
  - `CommandPlatformConfiguration` — Extends `PlatformConfiguration`; wires command controller and frontendcontroller event sender with Outbox support
  - `QueryPlatformConfiguration` — Extends `PlatformConfiguration`; wires query controller
  - Event senders: `OnCommitFrontEndControllerEventSender` (sends after transaction commit), `OutboxEventFrontEndControllerEventSender` (persists to outbox table), `MockFrontEndControllerEventSender` (testing)

#### Granite.FrontEndController.Client

REST client and event receiver implementations:

- **Request/Reply clients** (Java): `FrontEndControllerCommandServiceClient`, `FrontEndControllerQueryServiceClient` extending `AbstractRestClient`
- **Client interfaces** (Java): `IFrontEndControllerCommandServiceClient`, `IFrontEndControllerQueryServiceClient`
- **Event receiver**: `FrontEndControllerKafkaEventReceiver`, `FrontEndControllerKafkaAvroEventReceiver` — Kafka consumers for FrontEndController events
- **TypeScript mirror**: `FrontEndControllerCommandServiceClient`, `FrontEndControllerQueryServiceClient` with corresponding interfaces

### Applications

- **FrontEndControllerCommandHost** — Spring Boot application that assembles the command-side stack: `HostConfiguration` imports `ApplicationConfiguration`, `DomainConfiguration`, `CommandPlatformConfiguration`, and `ServiceConfiguration`. Listens on port 8081.
- **FrontEndControllerAdminHost** — Spring Boot application that assembles the query-side stack: imports `ApplicationConfiguration`, `DomainConfiguration`, `QueryPlatformConfiguration`, and `ServiceConfiguration`.

## Commonly Used Design Patterns

### CQRS (Command Query Responsibility Segregation)

Commands (create, update, destroy) and queries (find) are separated into distinct service interfaces (`IFrontEndControllerCommandService`, `IFrontEndControllerQueryService`), distinct controllers (`FrontEndControllerCommandServiceController`, `FrontEndControllerQueryServiceController`), and distinct host applications (`FrontEndControllerCommandHost`, `FrontEndControllerAdminHost`). This allows independent scaling of command and query workloads.

### Request-Reply

All service operations accept a typed request DTO and return a typed reply DTO. Requests extend `AbstractServiceRequest` (with `requestId`, `timestamp`, headers, cookies). Replies extend `AbstractServiceReply` (with `replyId`, `originatingRequestId`, `success`, `successMessage`, `failureMessage`, `exception`). Each operation has its own request/reply pair (e.g., `CreateFrontEndControllerRequest` → `CreateFrontEndControllerReply`).

### Pipeline (Chain of Responsibility)

All service methods follow an asynchronous pipeline pattern using `CompletionStage`:

```
validate → mapToContext → [operation steps] → buildReply
                                                  ↓ (on failure)
                                             buildReply(exception)
```

Each step receives and returns a typed context object that accumulates state through the pipeline. The pattern ensures consistent validation, error handling, and reply construction across all operations.

### Observer (Domain Events)

The `FrontEndController` domain object extends `AbstractEntityAndDomainEventSource` and fires domain events (`FrontEndControllerCreated`, `FrontEndControllerUpdated`, `FrontEndControllerDestroyed`) via the observer pattern. The `FrontEndControllerCommandService` implements `IFrontEndControllerEventObserver` and attaches itself to each `FrontEndController` it retrieves from the repository. When the `FrontEndController` notifies a state change, the service translates the domain event into a service-level `FrontEndControllerEvent` and dispatches it through `IFrontEndControllerEventSender`.

### Transactional Outbox

Event delivery uses the Transactional Outbox pattern to guarantee at-least-once delivery. `OnCommitFrontEndControllerEventSender` defers sending until the database transaction commits. `OutboxEventFrontEndControllerEventSender` serializes the event into an outbox table within the same transaction as the frontendcontroller state change, and a separate process polls the outbox and publishes to Kafka.

### Repository

`IFrontEndControllerRepository` extends the Strata `IRepository<Long,FrontEndController>` interface providing standard `findById`, `save`, `delete` operations. The concrete `FrontEndControllerRepository` extends `AbstractRepository` and registers named JPA queries for `findAllByIdIn`, `findAllByFoo`, and `findAllByFooIn`.

### Context Object

Each service pipeline uses a typed context object (e.g., `CreateFrontEndControllerContext`, `DestroyFrontEndControllersContext`) that carries the request, intermediate FrontEndController domain objects, and results through the pipeline steps. Context objects use fluent setters and provide convenience getters that delegate to the underlying request.

### Strategy (Event Sending)

The `IFrontEndControllerEventSender` interface has multiple implementations (`MockFrontEndControllerEventSender`, `OnCommitFrontEndControllerEventSender`, `OutboxEventFrontEndControllerEventSender`) that are selected via Spring configuration, allowing the event delivery strategy to be swapped without changing service code.

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. A base `PlatformConfiguration` provides shared JPA/UnitOfWork beans, and `CommandPlatformConfiguration` / `QueryPlatformConfiguration` extend it with command- or query-specific beans. `HostConfiguration` at the application level imports and assembles all required configurations.

## REST Endpoints

### FrontEndController Command Service (`/frontendcontroller-command-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /create-frontendcontroller` | `CreateFrontEndControllerRequest` | `CreateFrontEndControllerReply` |
| `POST /create-frontendcontrollers` | `CreateFrontEndControllersRequest` | `CreateFrontEndControllersReply` |
| `POST /update-frontendcontroller` | `UpdateFrontEndControllerRequest` | `UpdateFrontEndControllerReply` |
| `POST /destroy-frontendcontroller` | `DestroyFrontEndControllerRequest` | `DestroyFrontEndControllerReply` |
| `POST /destroy-frontendcontrollers` | `DestroyFrontEndControllersRequest` | `DestroyFrontEndControllersReply` |

### FrontEndController Query Service (`/frontendcontroller-admin-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /find-frontendcontrollers-by-key` | `FindFrontEndControllersByKeyRequest` | `FindFrontEndControllersByKeyReply` |
| `POST /find-frontendcontrollers-by-keyset` | `FindFrontEndControllersByKeySetRequest` | `FindFrontEndControllersByKeySetReply` |

## Preferred Coding Style and Idioms

### Interface Naming

All interfaces are prefixed with `I`. Examples: `IFrontEndControllerRepository`, `IFrontEndControllerCommandService`, `IFrontEndControllerEventSender`, `IFrontEndControllerEventObserver`, `IFrontEndControllerCommandServiceClient`.

### Class and Method Formatting

The `class` keyword and the `implements`/`extends` clause each start on their own line. Opening braces are on their own line (Allman style). Method return types are on a separate line from the method name. Example:

```java
//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;

public
class FrontEndControllerData
    implements Serializable
{
    private Long   frontendcontrollerId;
    private String foo;

    public
    FrontEndControllerData()
    {
        frontendcontrollerId = null;
        foo = null;
    }

    public FrontEndControllerData
    setFrontEndControllerId(Long id)
    {
        frontendcontrollerId = id;
        return this;
    }

    public FrontEndControllerData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public Long
    getFrontEndControllerId() { return frontendcontrollerId; }

    public String
    getFoo() { return foo; }
}

//////////////////////////////////////////////////////////////////////////////
```

Key formatting rules:

- **File header/footer**: Every Java file begins and ends with a `//////////////////////////////////////////////////////////////////////////////` comment. The second line contains the filename as a comment.
- **`class` on its own line**: The access modifier (`public`) is on one line, and `class ClassName` is on the next line. `extends` and `implements` clauses are indented on subsequent lines.
- **Method return type on its own line**: The access modifier and return type (e.g., `public FrontEndControllerData`) are on one line, and the method name with parameters (e.g., `setFoo(String f)`) is on the next line.
- **Allman brace style**: Opening braces `{` are on their own line for classes, methods, constructors, and control structures.
- **Compact getters**: Simple getters that return a single field use an inline body on the same line as the method name: `getFoo() { return foo; }`.
- **Column alignment**: Related field declarations are column-aligned by type and name (e.g., `private final IFrontEndControllerRepository  repository;` and `private final ModelMapper            mapper;`).
- **Constructor field alignment**: Assignment statements in constructors are column-aligned on the `=` sign.
- **Minimal space after commas**: Method calls and declarations use `(param1,param2)` with no space after commas in most cases, or a single space after commas when it improves readability.

### Fluent Setters and Fluent Methods

All setters return `this` (or the concrete subclass type) to enable method chaining. This applies to request, reply, context, and data classes. When overriding base class setters, the return type is narrowed to the concrete class via a cast:

```java
@Override
public CreateFrontEndControllerReply
setSuccess(boolean success)
{
    return (CreateFrontEndControllerReply)super.setSuccess(success);
}
```

Fluent style is also used for context mutation methods and builder patterns:

```java
return
    new CreateFrontEndControllerReply(context.getRequest())
        .setSuccess(true)
        .setSuccessMessage("FrontEndController: " + created.getPrimaryId() + " created")
        .setCreatedFrontEndController(mapper.map(created,FrontEndControllerData.class));
```

### Singular and Plural Operations

Command operations come in singular and plural variants:
- **Singular**: `createFrontEndController`, `updateFrontEndController`, `destroyFrontEndController` — operate on a single frontendcontroller
- **Plural**: `createFrontEndControllers`, `destroyFrontEndControllers` — operate on a batch of frontendcontroller instances with independent success/failure tracking per frontendcontroller

Plural replies include `requested`, `failed`, and `succeeded` collections to report partial success.

### Method Organization

Within a class, methods are grouped by access level: public methods first, then protected, then private. Within each group, methods follow the declaration order of the interface or base class they implement. For service implementations, the order is: public service methods, then `onEvent`/`onException`, then protected pipeline steps (`validate`, `mapToContext`, operation methods, `save`, `buildReply`), then private helper methods.

### Stream vs. Loop Style

Prefer explicit `for` loops for simple iteration. Use streams with `Collectors` for set-difference, mapping, and collecting operations. Never use side effects inside stream `map` or `filter` operations — derive intermediate collections in separate stream pipelines instead.

### Validation

Each pipeline starts with a `validate` step. Use `Objects.requireNonNull` for required fields and throw `IllegalArgumentException` for empty collections.

### TypeScript Array Types

Use `Array<Type>` syntax rather than `Type[]` in all TypeScript code.

