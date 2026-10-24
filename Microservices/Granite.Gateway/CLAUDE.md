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

Published as `granite.gateway:granite-gateway-server`. Contains the gateway's entry configuration and its health endpoint:

- **`server.configuration`**:
  - `GraniteGatewayServerConfiguration` — the entry configuration that hosts extend, following the Strata.Outbox → Granite.Outbox pattern. It defines the `RouteLocator`, the reactive `SecurityWebFilterChain`, the health controller, and the SecurityContextHolder strategy.
- **`server.health`**:
  - `GatewayHealthCheckController` — reactive liveness/readiness/startup endpoints under `${strata.health.base-path:/health}`.

#### Granite.Gateway.ServiceHost

The Spring Boot application entry point:

- `GatewayServiceHost` — `@SpringBootApplication` that excludes `DataSourceAutoConfiguration`, servlet `SecurityAutoConfiguration`, and `KafkaAutoConfiguration`. The active profile comes from `DEPLOY_ENV` (default `local`); the only environments are `local`, `development`, `test` and `production`.
- `HostConfiguration` — `HostConfiguration extends GraniteRoleRulesGatewayServerConfiguration`; contains no bean definitions of its own.

## Routes

Routes are defined in `GraniteGatewayServerConfiguration.configureRoutes(...)` and map path prefixes to backend microservice hosts:

| Route ID | Path Prefix | Backend Service |
|---|---|---|
| `frontendcontroller-user-route` | `/frontendcontroller-user-service/**` | FrontEndController UserHost |
| `frontendcontroller-admin-route` | `/frontendcontroller-admin-service/**` | FrontEndController AdminHost |
| `user-command-route` | `/user-command-service/**` | User CommandHost |
| `user-query-route` | `/user-query-service/**` | User QueryHost |
| `session-command-route` | `/session-command-service/**` | Session CommandHost |
| `session-query-route` | `/session-query-service/**` | Session QueryHost |
| `outbox-route` | `/outbox-service/**` | Outbox ServiceHost |

No path stripping is applied — backend controllers are mounted at these exact path prefixes.

When the `production` profile is active, `configureRoutes` adds only `frontendcontroller-user-route` and `frontendcontroller-admin-route`; `local`, `development` and `test` keep all seven.

### Route Host Configuration

Backend host URIs are parameterized via Spring properties (`granite.<service>.host`). Following `Templates/__System__`, every environment (`local` = docker-compose network, `development`/`test`/`production` = cluster) uses the same service names:

| Property | Value |
|---|---|
| `granite.frontendcontroller-user-service.host` | `https://granite-frontendcontroller-userhost:443` |
| `granite.frontendcontroller-admin-service.host` | `https://granite-frontendcontroller-adminhost:443` |
| `granite.user-command-service.host` | `https://granite-user-commandhost:443` |
| `granite.user-query-service.host` | `https://granite-user-queryhost:443` |
| `granite.session-command-service.host` | `https://granite-session-commandhost:443` |
| `granite.session-query-service.host` | `https://granite-session-queryhost:443` |
| `granite.outbox-service.host` | `https://granite-outbox-servicehost:443` |

Each property is required. A missing host fails startup, because routes resolve it with `Environment.getRequiredProperty`.

## Extending Granite.Gateway

Downstream application systems reuse the gateway by depending on `granite-gateway-server` and subclassing `GraniteGatewayServerConfiguration` in their own host. A seam is replaced by overriding its `@Bean` method, which must be re-annotated with `@Bean`. Defining a same-named bean elsewhere does not work, because Spring Boot rejects bean definition overriding. Gateway has no `IConfiguration`; all values are plain Spring properties.

| Seam | Default |
|---|---|
| `routeLocator(RouteLocatorBuilder,Environment)` → `configureRoutes(Builder,Environment)` | adds the seven Granite routes above |
| `addRoute(Builder,id,pathPattern,hostProperty,Environment)` | helper: one route whose URI comes from the required property `hostProperty` |
| `securityWebFilterChain(ServerHttpSecurity,ReactiveJwtDecoder,GraniteJwtAuthenticationConverter,ISessionResolver,Environment)` | reactive JWT resource server with the cookie path (see "Browser cookies"). `OPTIONS` and `getPublicExchangeMatcher(Environment)` are public. Then come the `configureAuthorization(...)` rules, then `anyExchange().authenticated()` |
| `configureAuthorization(AuthorizeExchangeSpec,Environment)` | hook where an application declares its authorization rules; the default declares none (authentication only) |
| `getPublicPathPatterns(Environment)` | hook: paths reachable without a token: `/health/**`, Session `login`, `login-with-oauth2`, `refresh`, the static app (`/`, `/index.html`, `/config.js`, `/favicon.ico`, `/assets/**`), plus `/*/swagger-ui/**`, `/*/swagger-ui.html` and `/*/v3/api-docs/**` outside `production` |
| `getPublicExchangeMatcher(Environment)` | hook: the public paths OR `PublicFrontEndInputMatcher` (the `LoginView` FEC inputs) |
| `createCsrfTokenRepository()` | hook: `CookieServerCsrfTokenRepository` (`XSRF-TOKEN`, readable by JavaScript, `Path=/`, `SameSite=Strict`) |
| `serviceTokenSupplier(Environment)` | `IServiceTokenSupplier` used to authenticate `resolveSession` calls. **The default fails closed** (no Gateway service token exists yet), so every cookie request gets 401 until a host overrides it |
| `sessionResolver(ObjectProvider<SslBundles>,Environment,IServiceTokenSupplier)` | `SessionCommandSessionResolver`: `POST {granite.session-command-service.host}/session-command-service/resolve-session` with the service token. Cached by SHA-256 of the handle for the smaller of `granite.gateway.session-cache-ttl` (`PT60S`) and the token's remaining life; failures, errors and timeouts (`granite.gateway.session-resolve.timeout`, `PT2S`) are not cached. TTL only: close events do not evict. TLS trust from `granite.gateway.session-resolve.ssl-bundle` (defaults to the gateway HTTP client bundle) via `createSessionResolveWebClient(...)` |
| `jwtDecoder(Environment,ISessionRevocationChecker)` | `RevocationCheckingReactiveJwtDecoder` wrapping `createJwtDecoder(Environment)`. The inner decoder is a `NimbusReactiveJwtDecoder` against `granite.security.jwt.jwk-set-uri` (Session's JWKS) with the Foundation `iss`/`aud`/`exp` validators |
| `revocationChecker(ObjectProvider<SslBundles>,Environment)` | `SessionQueryRevocationChecker`: after signature validation, looks up the token's `jti` (session id) via Session Query's `find-sessions-by-key`, using the caller's own bearer. Live results are cached for `granite.gateway.revocation.cache-ttl` (`PT30S`); revoked results are cached until the token's `exp`. Lookup errors and timeouts (`granite.gateway.revocation.timeout`, `PT2S`) reject the request and are not cached. Tokens whose `sub` starts with `service:` skip the lookup. `granite.gateway.revocation.enabled=false` turns the check off |
| `createRevocationWebClient(ObjectProvider<SslBundles>,Environment)` | hook: the WebClient for lookups. It trusts `granite.gateway.revocation.ssl-bundle`, which defaults to `spring.cloud.gateway.httpclient.ssl.bundle` |
| `createJwtDecoder(ObjectProvider<SslBundles>,Environment)` / `createTrustingWebClient(...)` | hooks: the JWKS is fetched with a WebClient that trusts `granite.security.jwt.ssl-bundle`, which also defaults to the gateway HTTP client bundle |

Production no longer uses `useInsecureTrustManager`. The `granite-services` bundle's truststore comes from `${GRANITE_TRUSTSTORE_LOCATION}` / `${GRANITE_TRUSTSTORE_PASSWORD}`, which must be supplied at deploy time.

After logout, a token is rejected at the Gateway within the live-cache window (30 seconds by default) instead of lasting until its `exp`.
| `jwtAuthenticationConverter()` | `GraniteJwtAuthenticationConverter` (`sub` principal, `authorities` claim) |
| `healthCheckController()` | `GatewayHealthCheckController` |

### Browser cookies (server-side sessions)

The browser holds only `GRANITE_SID` (session handle), `GRANITE_DID` (device) and `XSRF-TOKEN`. Filters run inside the security chain in this order (none is a Spring bean, so none runs outside the chain):

1. `FrontEndInputKeyWebFilter` (before `CSRF`): for `POST /frontendcontroller-{user,admin}-service/{get-output,handle-input}` without an `Authorization` header, buffers the body (`granite.gateway.front-end-input.max-bytes`, default `1048576`; larger is 413), records the FEC key `source->command->destination` (top level for `get-output`, under `input` for `handle-input`) and replays the body unchanged.
2. `CsrfCookieWebFilter` (before `CSRF`): issues `XSRF-TOKEN` when absent, on every non-bearer request (a 403 also carries a fresh one).
3. Spring `CsrfWebFilter`: required by `CookieCsrfProtectionMatcher` = method not `GET`/`HEAD`/`TRACE`/`OPTIONS`, no `Authorization` header, at least one cookie. The SPA echoes the raw `XSRF-TOKEN` cookie value in the `X-XSRF-TOKEN` header (plain `ServerCsrfTokenRequestAttributeHandler`, no masking). Missing or wrong → 403.
4. `DeviceCookieWebFilter` (after `CSRF`): when a non-bearer request has no `GRANITE_DID`, issues 256-bit base64url `GRANITE_DID` (`HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=31536000`) and adds it to the forwarded `Cookie` header.
5. `SessionCookieWebFilter` (before `AUTHENTICATION`): with `GRANITE_SID` present it always strips it from the forwarded `Cookie` header. A request with an `Authorization` header keeps the bearer path unchanged (bearer wins, cookie ignored). A public request is not resolved. Otherwise the handle is resolved through `ISessionResolver` and `Authorization: Bearer <jwt>` is injected; any failure is 401.

The injected JWT then goes through the ordinary bearer authentication, `jti` revocation check and role rules. Public FEC inputs (`PublicFrontEndInputMatcher`), on both hosts: `get-output` with `Session.New->LoginView`; `handle-input` with `Session.New->Login->LoginView`, `Session.New->StartOktaLogin->LoginView`, `Session.New->CompleteOktaLogin->LoginView`. The handle, tokens and CSRF token are never logged.

Authorization rules live here and only here: User and Session only authenticate. Each application that instantiates a Gateway declares its rules by overriding `configureAuthorization`. Rules match on path and method, so they cannot inspect request bodies. The caller's `Authorization` header is relayed to the routed service unchanged.

Granite's own rules ship in `GraniteRoleRulesGatewayServerConfiguration` (`HostConfiguration` extends it) and apply only when `granite.gateway.authorization.role-rules.enabled=true` (default `false`: authentication only). The property is `true` in the ServiceHost `local` and `development` profiles and unset in `test` and `production`. Public paths are permitted before these rules. Every allowed list also includes `SCOPE_internal`. Rules are evaluated in this order:

| Matcher | Authorities |
|---|---|
| `POST /frontendcontroller-user-service/get-output`, `POST /frontendcontroller-admin-service/get-output` | `ROLE_ADMIN`, `ROLE_AUDITOR` |
| `/user-query-service/**` | `ROLE_ADMIN`, `ROLE_AUDITOR` |
| `POST /session-command-service/logout` | `ROLE_ADMIN`, `ROLE_AUDITOR` |
| `/session-query-service/**` | `ROLE_ADMIN`, `ROLE_AUDITOR` |
| `/**` (catch-all, so everything else) | `ROLE_ADMIN` |

The catch-all uses `pathMatchers("/**")`, not `anyExchange()`, because the base chain registers `anyExchange().authenticated()` afterwards.

Example only, not part of Granite: how a downstream application declares its own rules by overriding `configureAuthorization` in its own host, distinct from Granite's built-in `GraniteRoleRulesGatewayServerConfiguration` above. Granite does not ship these rules.

```java
@Override
protected ServerHttpSecurity.AuthorizeExchangeSpec
configureAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges,Environment environment)
{
    return
        super
            .configureAuthorization(exchanges,environment)
            .pathMatchers(HttpMethod.POST,"/user-command-service/**")
                .hasAuthority("ROLE_ADMIN")
            .pathMatchers("/session-query-service/**")
                .hasAnyAuthority("ROLE_ADMIN","SCOPE_internal");
}
```

`AuthorizingGatewayTestConfiguration` and `GatewayAuthorizationTest` in ServerTest show this end to end. `GatewayTestBackend` serves a JWKS and stands in for the routed services.

To add routes, override `configureRoutes`, call `super`, then call `addRoute`. To expose only some Granite routes, override without calling `super` and add the ones you want.

```java
@Configuration
public
class HostConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected RouteLocatorBuilder.Builder
    configureRoutes(RouteLocatorBuilder.Builder routes,Environment environment)
    {
        super.configureRoutes(routes,environment);
        return
            addRoute(
                routes,
                "order-command-route",
                "/order-command-service/**",
                "retail.order-command-service.host",
                environment);
    }
}
```

Spring Cloud Gateway normalizes route URIs, so `http://host` is reported as `http://host:80`.

## Commonly Used Design Patterns

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import` and inheritance. `HostConfiguration` at the application level extends `GraniteGatewayServerConfiguration` from the Server component, following the same entry-configuration pattern used by all Granite host applications.

### Reactive Security

The gateway uses Spring Security's reactive `ServerHttpSecurity` (not the servlet `HttpSecurity`). The `SecurityWebFilterChain` bean in `GraniteGatewayServerConfiguration` validates Session-issued JWTs and enforces the application's authorization rules (see "Extending Granite.Gateway").

### Profile-Based Configuration

Route target URIs are externalized into environment files (`application-local.properties`, `application-development.properties`, `application-test.properties`, `application-production.properties`) that follow `Templates/__System__`.

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