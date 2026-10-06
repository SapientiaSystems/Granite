# Granite.Gateway Microservice

## Overview

Granite.Gateway is a Spring Cloud Gateway microservice that serves as the API gateway for the Granite system. It routes incoming HTTP requests to the appropriate backend microservice hosts (FrontEndController, User, Outbox) based on URL path prefixes. The gateway runs on the reactive WebFlux stack and does not manage any domain entities, persistence, or business logic.

## Technology Stack

- **Language**: Java 23 (Amazon Corretto)
- **Build**: Gradle (multi-project)
- **Framework**: Spring Boot 3.4, Spring Cloud Gateway 4.3
- **Web Stack**: Spring WebFlux (reactive Netty — not servlet-based)
- **Security**: Spring Security (reactive `ServerHttpSecurity`)
- **Containerization**: Google Jib (OCI images)
- **Testing**: JUnit Jupiter 5
- **Foundation Libraries**: Strata Framework Set (strata.foundation.spring)

## Project Structure

```
Granite.Gateway/
├── Applications/
│   └── Granite.Gateway.ServiceHost/    # Spring Boot application that hosts the gateway
├── Components/
│   └── Granite.Gateway.Server/         # Route definitions and server-side configuration
├── Tests/
│   └── Granite.Gateway.ServerTest/     # Server tests
├── build.gradle                         # Root build with dependency versions
└── settings.gradle                      # Multi-project module declarations
```

## Architecture

The gateway follows a simplified two-layer architecture. There is no domain, application, or platform layer — the gateway's sole responsibility is routing HTTP requests to backend microservices.

### Component Responsibilities

#### Granite.Gateway.Server

Contains the route definitions:

- **`server.route`**:
  - `RouteConfiguration` — Spring `@Configuration` class that defines a `RouteLocator` bean. Each route maps a URL path prefix to a backend microservice host URI. Host URIs are injected via `@Value` from profile-specific properties, enabling different targets per environment (development, test, production).

#### Granite.Gateway.ServiceHost

The Spring Boot application entry point:

- `GatewayServiceHost` — `@SpringBootApplication` that excludes `DataSourceAutoConfiguration` and `HibernateJpaAutoConfiguration` (no database needed). Sets the "production" profile by default.
- `HostConfiguration` — `@Configuration` with `@Import({RouteConfiguration.class})` to bring in route definitions from the Server component (required because `RouteConfiguration` is outside the default component scan scope). Also defines a `SecurityWebFilterChain` bean that disables CSRF and permits all exchanges.

## Routes

Routes are defined in `RouteConfiguration` and map path prefixes to backend microservice hosts:

| Route ID | Path Prefix | Backend Service |
|---|---|---|
| `frontendcontroller-user-route` | `/frontendcontroller-user-service/**` | FrontEndController UserHost |
| `frontendcontroller-admin-route` | `/frontendcontroller-admin-service/**` | FrontEndController AdminHost |
| `user-command-route` | `/user-command-service/**` | User CommandHost |
| `user-query-route` | `/user-query-service/**` | User QueryHost |
| `outbox-route` | `/outbox-service/**` | Outbox ServiceHost |
| `useradapter-command-route` | `/useradapter-command-service/**` | UserAdapter CommandHost |
| `useradapter-query-route` | `/useradapter-query-service/**` | UserAdapter QueryHost |

No path stripping is applied — backend controllers are mounted at these exact path prefixes.

### Route Host Configuration

Backend host URIs are parameterized via Spring properties (`granite.<service>.host`) and vary by profile:

| Property | Development | Test / Production |
|---|---|---|
| `granite.frontendcontroller-user-service.host` | `https://localhost:8081/frontendcontroller-user-service` | `https://granite-frontendcontroller-userhost:443` |
| `granite.frontendcontroller-admin-service.host` | `https://localhost:8082/frontendcontroller-admin-service` | `https://granite-frontendcontroller-adminhost:443` |
| `granite.user-command-service.host` | `https://localhost:8083/user-command-service` | `https://granite-user-commandhost:443` |
| `granite.user-query-service.host` | `https://localhost:8084/user-query-service` | `https://granite-user-queryhost:443` |
| `granite.outbox-service.host` | `https://localhost:8085/outbox-service` | `https://granite-outbox-servicehost:443` |
| `granite.useradapter-command-service.host` | `https://localhost:8086/useradapter-command-service` | `https://granite-useradapter-commandhost:443` |
| `granite.useradapter-query-service.host` | `https://localhost:8087/useradapter-query-service` | `https://granite-useradapter-queryhost:443` |

## Commonly Used Design Patterns

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. `HostConfiguration` at the application level imports `RouteConfiguration` from the Server component, following the same pattern used by all Granite host applications.

### Reactive Security

The gateway uses Spring Security's reactive `ServerHttpSecurity` (not the servlet `HttpSecurity`). The `SecurityWebFilterChain` bean in `HostConfiguration` disables CSRF and permits all exchanges, matching the security posture of the other Granite microservices.

### Profile-Based Configuration

Route target URIs are externalized into profile-specific properties files (`development.properties`, `test.properties`, `production.properties`), allowing the same gateway code to route to localhost during development and to Kubernetes service DNS names in deployed environments.

## Key Dependency Note

The gateway uses `strata-foundation-spring` (not `strata-server-spring`) to avoid pulling Spring MVC / servlet dependencies onto the classpath. Spring Cloud Gateway requires the reactive WebFlux stack and cannot coexist with Spring MVC.

## Preferred Coding Style and Idioms

### Interface Naming

All interfaces are prefixed with `I`.

### Class and Method Formatting

The `class` keyword and the `implements`/`extends` clause each start on their own line. Opening braces are on their own line (Allman style). Method return types are on a separate line from the method name. Example:

```java
//////////////////////////////////////////////////////////////////////////////
// RouteConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.route;

@Configuration
public
class RouteConfiguration
{
    @Bean
    public RouteLocator
    routeLocator(RouteLocatorBuilder builder)
    {
        return
            builder
                .routes()
                .route(
                    "frontendcontroller-user-route",
                    route ->
                        route
                            .path("/frontendcontroller-user-service/**")
                            .uri(frontendcontrollerCommandServiceHost))
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
```

Key formatting rules:

- **File header/footer**: Every Java file begins and ends with a `//////////////////////////////////////////////////////////////////////////////` comment. The second line contains the filename as a comment.
- **`class` on its own line**: The access modifier (`public`) is on one line, and `class ClassName` is on the next line. `extends` and `implements` clauses are indented on subsequent lines.
- **Method return type on its own line**: The access modifier and return type (e.g., `public RouteLocator`) are on one line, and the method name with parameters (e.g., `routeLocator(RouteLocatorBuilder builder)`) is on the next line.
- **Allman brace style**: Opening braces `{` are on their own line for classes, methods, constructors, and control structures.
- **Column alignment**: Related field declarations are column-aligned by type and name.