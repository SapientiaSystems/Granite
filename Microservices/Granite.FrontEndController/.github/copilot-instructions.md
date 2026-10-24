# Granite.FrontEndController Microservice

## Overview

Granite.FrontEndController is a Spring Boot microservice built on the Strata FrontEndController framework. The web application sends a `FrontEndInput` to `handle-input` and reads a `FrontEndOutput` from `get-output` or `handle-input`. The service holds no persistent state of its own: request processors translate inputs into calls on the Granite.User query and command services, forwarding the caller's bearer token, and wrap the results in outputs.

## Technology Stack

- **Language**: Java 23 (Amazon Corretto), TypeScript
- **Build**: Gradle (multi-project), Yarn (TypeScript components)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Serialization**: Jackson (subtype deduction for `FrontEndInput`/`FrontEndOutput`)
- **Containerization**: Google Jib (OCI images)
- **Testing**: JUnit Jupiter 5
- **Foundation Libraries**: Strata Framework Set (strata.foundation.core, strata.server.core, strata.frontendcontroller)

## Project Structure

```
Granite.FrontEndController/
├── Applications/
│   ├── Granite.FrontEndController.UserHost/       # Spring Boot app serving /frontendcontroller-user-service/
│   └── Granite.FrontEndController.AdminHost/      # Spring Boot app serving /frontendcontroller-admin-service/
├── Components/
│   ├── Granite.FrontEndController.Service/        # Input/output types (Java + TypeScript), FrontEndJacksonModule
│   ├── Granite.FrontEndController.Server/         # Request processors, view actions, Spring configuration
│   └── Granite.FrontEndController.Client/         # REST client (Java + TypeScript)
├── Tests/
│   ├── Granite.FrontEndController.ServiceTest/
│   ├── Granite.FrontEndController.ServerTest/     # Plain JUnit unit tests
│   ├── Granite.FrontEndController.ClientTest/
│   └── Granite.FrontEndController.TestSuite/
├── build.gradle
├── settings.gradle
└── package.json
```

## Architecture

### Request/Reply with FrontEndInput and FrontEndOutput

Each screen interaction is a `FrontEndInput` identified by its key `source->command->destination` (for example `User.All->Save->UsersEditableView`). The request processor routes an input to an `IHandleInputAction` and a view request to an `IGetOutputAction`. Both return a `FrontEndOutput`.

#### Granite.FrontEndController.Service

- `UsersInput`: `usersToCreate: List<CreateUserData>`, `usersToUpdate: List<UpdateUserData>`, `userIdsToDelete: List<Long>`; all default to empty lists.
- `UsersOutput`: `users: List<UserData>`, `failureMessages: List<String>`.
- `FrontEndJacksonModule`: Jackson module that registers every Granite subtype of `FrontEndInput` and `FrontEndOutput`.
- TypeScript mirrors of each type live under `src/main/typescript/RequestReply/` and are exported from `index.ts`.

#### Granite.FrontEndController.Server

- `server.control.user`: `UserRequestProcessor` (extends Strata `DefaultRequestProcessor`, constructed with `IUserQueryServiceClient` and `IUserCommandServiceClient`) and its three actions, registered under these keys:

| Key | Action | Behavior |
|---|---|---|
| `User.All->UsersEditableView` (get-output) | `InitializeUsersView` | `findAllUsers`; returns `UsersOutput` with all users |
| `User.All->Save->UsersEditableView` (handle-input) | `UpdateUsersView` | `createUsers` then `updateUsers` (each skipped when its list is empty), then re-queries all users |
| `User.All->Delete->UsersEditableView` (handle-input) | `DeleteUsersView` | refuses the caller's own `userId`, `destroyUsers` for the rest, then re-queries all users |

  Every output copies `source`/`command`/`destination` from the request. Save and delete report failed users and failed batches as `failureMessages` (`<userName or id>: <reason>`) alongside the refreshed `users` list. An unknown key fails with `IllegalArgumentException("No action found for key: ...")`.
- `server.configuration`: `GraniteUserFrontEndControllerConfiguration` and `GraniteAdminFrontEndControllerConfiguration` provide the processors, executors, User service clients and the `FrontEndJacksonModule` bean.

### Jackson subtype registration

Strata's abstract `FrontEndInput` and `FrontEndOutput` use `@JsonTypeInfo(use = DEDUCTION)`: no type id is written, and the concrete class is deduced from the set of properties present in the JSON. Each application must register its concrete classes, otherwise deserialization fails.

- Add every new `FrontEndInput` or `FrontEndOutput` subtype to `FrontEndJacksonModule` and nowhere else.
- The module is registered on the Spring MVC mapper through the `frontEndJacksonModule()` bean in both Granite configurations (Spring Boot registers `Module` beans), and on the springdoc `StrataObjectMapperProvider` json and yaml mappers in both `HostConfiguration` classes.
- Any test that builds an `ObjectMapper` for FrontEndController types must register `FrontEndJacksonModule`.
- Deduction needs each subtype to have a distinct set of property names, so give every subtype at least one property no sibling has.
- The TypeScript client must send all fields of an input, including empty arrays, so that deduction can match the intended subtype.

### Calling User with the caller's identity

User requires a valid Session-issued bearer token. The request-scoped `IUserQueryServiceClient` beans and the UserHost's `IUserCommandServiceClient` (`user-command-service.base-url`) in `GraniteUserFrontEndControllerConfiguration` and `GraniteAdminFrontEndControllerConfiguration` register `ForwardedBearerClientRequestFilter`. It captures the inbound `Authorization: Bearer ...` header when the client is created, and adds it as a real HTTP header on outgoing calls. Copying `Authorization` into a request DTO's `headers` does not reach User, because DTO headers are part of the JSON body.

### Applications

- **FrontEndControllerUserHost**: imports `GraniteUserFrontEndControllerConfiguration` and `ServiceConfiguration`; exposes `/frontendcontroller-user-service/`. Listens on port 8081.
- **FrontEndControllerAdminHost**: imports `GraniteAdminFrontEndControllerConfiguration` and `ServiceConfiguration`; exposes `/frontendcontroller-admin-service/`.

## Commonly Used Design Patterns

### Request-Reply

Service operations accept a typed request and return a typed reply. Requests extend `AbstractServiceRequest` (with `requestId`, `timestamp`, headers, cookies). Replies extend `AbstractServiceReply`. `handle-input` wraps a `FrontEndInput` in a `HandleInputRequest`; `get-output` returns a `GetOutputReply` carrying a `FrontEndOutput`.

### Asynchronous Pipeline

Actions return `CompletionStage<FrontEndOutput>` and compose User client calls with `thenApply`/`thenCompose`.

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. `HostConfiguration` at the application level imports and assembles the required configurations.

## REST Endpoints

### FrontEndController User Service (`/frontendcontroller-user-service/`)

| Endpoint | Request | Reply |
|---|---|---|
| `POST /handle-input` | `HandleInputRequest` | `HandleInputReply` |
| `POST /get-output` | `GetOutputRequest` | `GetOutputReply` |

### FrontEndController Admin Service (`/frontendcontroller-admin-service/`)

Configured by `GraniteAdminFrontEndControllerConfiguration`, which extends Strata's `AdminFrontEndControllerConfiguration`.

| Endpoint | Request | Reply |
|---|---|---|
| `POST /handle-input` | `HandleInputRequest` | `HandleInputReply` |
| `POST /get-output` | `GetOutputRequest` | `GetOutputReply` |

Both hosts route requests through the same `UserRequestProcessor`. The Admin host has request-scoped `IUserQueryServiceClient` and `IUserCommandServiceClient` beans (`user-query-service.base-url`, `user-command-service.base-url`).

## Preferred Coding Style and Idioms

### Interface Naming

All interfaces are prefixed with `I`. Examples: `IFrontEndControllerRepository`, `IBusinessPresentersService`, `IFrontEndControllerEventSender`, `IFrontEndControllerEventObserver`, `IBusinessPresentersServiceClient`.

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
