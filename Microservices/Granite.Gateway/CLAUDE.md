# Granite.Gateway Microservice

## Overview

Granite.Gateway is a Spring Cloud Gateway microservice that serves as the API gateway for the Granite system. It routes incoming HTTP requests to the appropriate backend microservice hosts (FrontEndController, User, Session, Outbox) based on URL path prefixes. The gateway runs on the reactive WebFlux stack and does not manage any domain entities, persistence, or business logic.

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
| `feedback-command-route` | `/feedback-command-service/**` | Feedback CommandHost |
| `feedback-query-route` | `/feedback-query-service/**` | Feedback QueryHost |
| `survey-command-route` | `/survey-command-service/**` | Survey CommandHost |
| `survey-query-route` | `/survey-query-service/**` | Survey QueryHost |
| `outbox-route` | `/outbox-service/**` | Outbox ServiceHost |

No path stripping is applied — backend controllers are mounted at these exact path prefixes.

Every profile, including `production`, registers all seven routes. In `production` the User, Session and Outbox routes carry only service-to-service traffic: their paths require `SCOPE_internal` (see the rule table), so browser sessions of any role get 403 there and administer users through the FEC. The production Ingress does not publish these paths at all (the `production` overlay removes them from `granite-ingress`), so they are reachable only from inside the cluster.

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
| `securityWebFilterChain(ServerHttpSecurity,ReactiveJwtDecoder,GraniteJwtAuthenticationConverter,ISessionResolver,GraniteCookieNames,Environment)` | reactive JWT resource server with the cookie path (see "Browser cookies"). First rule: `denyAll` on `/session-command-service/resolve-session`, so no caller, including a service token, reaches it through the Session route (403 when authenticated, 401 when not; the Gateway's own resolver calls Session directly, not through the chain). Second: `/session-command-service/start-okta-login` and `/session-command-service/complete-okta-login` require `SCOPE_internal`, so the FEC reaches them with its service token and browser sessions get 403 (Session lists them as public; the FEC is the only entry for Okta login). `OPTIONS` and `getPublicExchangeMatcher(Environment)` are public. `getAuthenticatedExchangeMatcher(Environment)` then requires only authentication. Then come the `configureAuthorization(...)` rules, then `anyExchange().authenticated()` |
| `configureAuthorization(AuthorizeExchangeSpec,Environment)` | `final` template, always applied: in `production` first the `SCOPE_internal`-only rule for the User, Session and Outbox paths, then the Preferences `authenticated()` rules, Granite's users-reader rules, Granite's admin-only paths, then `configureApplicationAuthorization(...)`, then the `ROLE_ADMIN` catch-all. Not overridable; applications extend through the hooks below |
| `configureApplicationAuthorization(AuthorizeExchangeSpec,Environment)` | hook: application rules, evaluated after Granite's Preferences, users-reader and admin-only rules and before the `ROLE_ADMIN` catch-all; default declares none |
| `getUsersReaderAuthorities(Environment)` | hook: authorities allowed on Granite's users-reader rules; default `ROLE_ADMIN`, `SCOPE_internal` |
| `configureApplicationRoutes(Builder,Environment)` | hook: called after Granite's routes in every profile, including `production`; default adds none |
| `getPublicPathPatterns(Environment)` | hook: paths reachable without a token: `/health/**`, Session `issue-service-token` (rate limited, see `createServiceTokenRateLimitWebFilter`), the static app (`/`, `/index.html`, `/config.js`, `/favicon.ico`, `/assets/**`), plus, outside `production` only, Session `login` and `refresh`, `/*/swagger-ui/**`, `/*/swagger-ui.html` and `/*/v3/api-docs/**`. In `production` `login` and `refresh` fall under the production `SCOPE_internal` rule (401 without a token, 403 for browser sessions), so only the FEC reaches them with its service token; browser login still goes through the public FEC `LoginView` inputs (`GatewayProductionRoutesTest`) |
| `createServiceTokenRateLimitWebFilter(Environment)` | hook: `RateLimitWebFilter` with a `FixedWindowRateLimiter` on `/session-command-service/issue-service-token` only, keyed by the request's remote address (`X-Forwarded-For` is not trusted; the Gateway has no forwarded-header strategy). Over the limit the request gets 429 and never reaches Session. `granite.gateway.issue-service-token.rate-limit.requests` (`10`) per `granite.gateway.issue-service-token.rate-limit.window` (`PT1M`), in memory per Gateway instance; at most `granite.gateway.issue-service-token.rate-limit.max-clients` (`10000`) addresses are tracked, expired windows are evicted when that bound is reached, and a new address is refused (429) while it is still full. Runs before `CSRF`. Deployment invariant: the key is the socket address, so the Gateway must not be exposed directly to untrusted clients and forwarded headers stay untrusted; behind a proxy all callers share one key, and a flood of distinct addresses fills the map and locks out new addresses, including the FEC's token issuance (the FEC falls back to its cached token until it expires) |
| `getPublicExchangeMatcher(Environment)` | hook: the public paths OR `PublicFrontEndInputMatcher` (the `LoginView` FEC inputs) |
| `getAuthenticatedExchangeMatcher(Environment)` | hook: `CurrentSessionFrontEndInputMatcher` (the `Session.Current` FEC inputs), which any authenticated caller reaches regardless of role. It is registered after the public matcher and before `configureAuthorization(...)`, so it always applies |
| `cookieNames(Environment)` | `GraniteCookieNames.from(environment)`: prefix `granite.cookies.prefix` (default `GRANITE`, `[A-Z0-9_]+`); overridable `@Bean` |
| `createCsrfTokenRepository(GraniteCookieNames)` | hook: `CookieServerCsrfTokenRepository` (`__Host-<PREFIX>-XSRF-TOKEN`, readable by JavaScript, `Secure`, `Path=/`, `SameSite=Strict`) |
| `sessionResolver(ObjectProvider<SslBundles>,Environment)` | `SessionCommandSessionResolver`: `POST {granite.session-command-service.host}/session-command-service/resolve-session` with no `Authorization` header (the handle is the credential; Session lists the path as public). Cached by SHA-256 of the handle for the smaller of `granite.gateway.session-cache-ttl` (`PT60S`) and the token's remaining life; failures, errors and timeouts (`granite.gateway.session-resolve.timeout`, `PT2S`) are not cached. TTL only: close events do not evict. TLS trust from `granite.gateway.session-resolve.ssl-bundle` (defaults to the gateway HTTP client bundle) via `createSessionResolveWebClient(...)` |
| `jwtDecoder(Environment,ISessionRevocationChecker)` | `RevocationCheckingReactiveJwtDecoder` wrapping `createJwtDecoder(Environment)`. The inner decoder is a `NimbusReactiveJwtDecoder` against `granite.security.jwt.jwk-set-uri` (Session's JWKS) with the Foundation `iss`/`aud`/`exp` validators |
| `revocationChecker(ObjectProvider<SslBundles>,Environment)` | `SessionQueryRevocationChecker`: after signature validation, looks up the token's `jti` (session id) via Session Query's `find-sessions-by-key`, using the caller's own bearer. Live results are cached for `granite.gateway.revocation.cache-ttl` (`PT30S`); revoked results are cached until the token's `exp`. A lookup that times out (`granite.gateway.revocation.timeout`, `PT2S`) or hits a transport error is retried once after 100 ms; if the retry fails too, or Session answers with an error status (not retried), the request is rejected. Failures are never cached. Service tokens skip the lookup, because no session stands behind them: a `sub` starting with `service:` (Session's own `ServiceTokenProvider` token), or an `authorities` claim containing `SCOPE_internal` without a `did` claim (client-credentials tokens from `issue-service-token`, such as the FEC's `granite-frontendcontroller`; session tokens always carry `did`, so a session token is still checked). `granite.gateway.revocation.enabled=false` turns the check off |
| `createRevocationWebClient(ObjectProvider<SslBundles>,Environment)` | hook: the WebClient for lookups. It trusts `granite.gateway.revocation.ssl-bundle`, which defaults to `spring.cloud.gateway.httpclient.ssl.bundle` |
| `createJwtDecoder(ObjectProvider<SslBundles>,Environment)` / `createTrustingWebClient(...)` | hooks: the JWKS is fetched with a WebClient that trusts `granite.security.jwt.ssl-bundle`, which also defaults to the gateway HTTP client bundle |

Production no longer uses `useInsecureTrustManager`. The `granite-services` bundle's truststore comes from `${GRANITE_TRUSTSTORE_LOCATION}` / `${GRANITE_TRUSTSTORE_PASSWORD}`, which must be supplied at deploy time.

After logout, a token is rejected at the Gateway within the live-cache window (30 seconds by default) instead of lasting until its `exp`.
| `jwtAuthenticationConverter()` | `GraniteJwtAuthenticationConverter` (`sub` principal, `authorities` claim) |
| `healthCheckController()` | `GatewayHealthCheckController` |

### Browser cookies (server-side sessions)

The browser holds only `__Host-<PREFIX>_SID` (session handle), `__Host-<PREFIX>_DID` (device) and `__Host-<PREFIX>-XSRF-TOKEN` (default prefix `GRANITE`, set with `granite.cookies.prefix`; all `Secure`, `Path=/`, no `Domain`). Filters run inside the security chain in this order (none is a Spring bean, so none runs outside the chain):

1. `FrontEndInputKeyWebFilter` (before `CSRF`): for `POST /frontendcontroller-{user,admin}-service/{get-session-output,handle-session-input}` only (Users paths are plain path rules), cookie or bearer, buffers the body (`granite.gateway.front-end-input.max-bytes`, default `1048576`; larger is 413), records the FEC key `source->command->destination` (top level for `get-session-output`, under `input` for `handle-session-input`) and replays the body unchanged. No key is recorded when a part contains `->` or is present but not a string.
2. `CsrfCookieWebFilter` (before `CSRF`): issues the CSRF cookie when absent, on every non-bearer request (a 403 also carries a fresh one).
3. Spring `CsrfWebFilter`: required by `CookieCsrfProtectionMatcher` = method not `GET`/`HEAD`/`TRACE`/`OPTIONS`, no `Authorization` header, at least one cookie. The SPA echoes the raw CSRF cookie value in the `X-XSRF-TOKEN` header (plain `ServerCsrfTokenRequestAttributeHandler`, no masking). Missing or wrong → 403.
4. `DeviceCookieWebFilter` (after `CSRF`): when a non-bearer request has no device cookie, issues a 256-bit base64url device cookie (`HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=31536000`) and adds it to the forwarded `Cookie` header.
5. `SessionCookieWebFilter` (before `AUTHENTICATION`): with the session cookie present it always strips it from the forwarded `Cookie` header. A request with an `Authorization` header keeps the bearer path unchanged (bearer wins, cookie ignored). A public request is not resolved. Otherwise the handle is resolved through `ISessionResolver` and `Authorization: Bearer <jwt>` is injected; any failure is 401.

The injected JWT then goes through the ordinary bearer authentication, `jti` revocation check and role rules. Public FEC inputs (`PublicFrontEndInputMatcher`), on both hosts: `get-session-output` with `Session.New->LoginView`; `handle-session-input` with `Session.New->Login->LoginView`, `Session.New->StartOktaLogin->LoginView`, `Session.New->CompleteOktaLogin->LoginView`. Authenticated-only FEC inputs (`CurrentSessionFrontEndInputMatcher`), on both hosts, open to any role: `get-session-output` with `Session.Current->SessionView`; `handle-session-input` with `Session.Current->Logout->LoginView`. Both matchers extend `FrontEndInputKeyMatcher`. The handle, tokens and CSRF token are never logged.

Request authorization rules live here and only here. Session additionally gates login (default: the user must hold at least one catalog role; `granite.session.login.required-authorities` overrides) and User rejects roles outside the role catalog, but neither authorizes requests by path. Each application that instantiates a Gateway declares its rules by overriding `configureApplicationAuthorization`. Rules match on path and method, so they cannot inspect request bodies; only the FEC key matchers above, which run before them, look at the recorded key. The caller's `Authorization` header is relayed to the routed service unchanged.

Granite core's rules live in `GraniteGatewayServerConfiguration.configureAuthorization` (`final`) and are always registered, in every profile and regardless of any property. Public paths are permitted, and the `Session.Current` FEC inputs need only authentication, before these rules. Every allowed list also includes `SCOPE_internal` (`GraniteAuthorities.SCOPE_INTERNAL` from Granite.Foundation, the single definition used by the chain and `SessionQueryRevocationChecker`). The denyAll, public and current-session matchers stay ahead of everything, so application rules cannot reopen a denied path. Rules are evaluated in this order:

| Matcher | Authorities |
|---|---|
| `production` only: `/user-query-service/**`, `/user-command-service/**`, `/session-query-service/**`, `/session-command-service/**`, `/feedback-query-service/**`, `/feedback-command-service/**`, `/survey-query-service/**`, `/survey-command-service/**`, `/outbox-service/**` (after the earlier `resolve-session` denyAll, Okta-step rule and public `issue-service-token`; this covers `login` and `refresh` too) | `SCOPE_internal` only (`ROLE_ADMIN`, `ROLE_AUDITOR` and every other browser role get 403) |
| `POST /frontendcontroller-{user,admin}-service/get-preferences-output` and `handle-preferences-input`, `get-feedback-output` and `handle-feedback-input`, and `get-survey-output` and `handle-survey-input` (both hosts) | any authenticated caller (the operations act on the caller's own user; feedback and survey responses are stamped with the caller) |
| `POST /frontendcontroller-user-service/get-users-output`, `POST /frontendcontroller-admin-service/get-users-output`, and the same two hosts' `get-sessions-output` (the admin Sessions view) `get-feedback-review-output` (the Feedback review grid) and `get-survey-review-output` (the Surveys grid) | `getUsersReaderAuthorities()` |
| `/user-query-service/**` (outside `production`) | `getUsersReaderAuthorities()` |
| `POST /session-command-service/logout` (outside `production`) | `getUsersReaderAuthorities()` |
| `/session-query-service/**`, `/feedback-query-service/**`, `/survey-query-service/**` (outside `production`) | `getUsersReaderAuthorities()` |
| `POST ./handle-users-input`, `POST ./handle-sessions-input`, `POST ./handle-feedback-review-input` and `POST ./handle-survey-review-input` (both hosts), retired `handle-input` and `get-output` (both hosts), `/user-command-service/**`, `/session-command-service/**` (after the logout rule), `/feedback-command-service/**`, `/survey-command-service/**`, `/outbox-service/**` (the last five outside `production`) | `ROLE_ADMIN` |
| application rules from `configureApplicationAuthorization` | as declared |
| `/**` (catch-all, so everything else) | `ROLE_ADMIN` |

`GraniteRoleRulesGatewayServerConfiguration` (`HostConfiguration` extends it) is the Granite application: when `granite.gateway.authorization.role-rules.enabled=true` its `getUsersReaderAuthorities(Environment)` returns `ROLE_ADMIN`, `ROLE_AUDITOR`, `SCOPE_internal`; otherwise the base default (`ROLE_ADMIN`, `SCOPE_internal`). That is the flag's only effect. It is `true` in the ServiceHost `local` and `development` profiles and unset in `test` and `production`, where `ROLE_AUDITOR` therefore gets 403 on the reader paths. Granite core knows only `ROLE_ADMIN`; `ROLE_AUDITOR` belongs to the Granite application, so a base configuration with `ROLE_AUDITOR` gets 403 on those paths.

The Granite-owned admin-only paths are registered before the application hook, so a broad application rule such as `/frontendcontroller-user-service/**` or a `permitAll` on `/user-command-service/**` cannot widen access to them. `configureAuthorization` is `final`, so an application cannot drop or reorder the core rules; it adds rules only through the hooks.

The catch-all uses `pathMatchers("/**")`, not `anyExchange()`, because the base chain registers `anyExchange().authenticated()` afterwards.

Example only, not part of Granite: how a downstream application (extending `GraniteGatewayServerConfiguration`, optionally overriding `getUsersReaderAuthorities(Environment)` to add its own reader roles) declares its own rules and routes through the hooks. Granite does not ship these rules.

```java
@Override
protected ServerHttpSecurity.AuthorizeExchangeSpec
configureApplicationAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges,Environment environment)
{
    return
        exchanges
            .pathMatchers(HttpMethod.POST,"/frontendcontroller-user-service/get-promotions-output")
                .hasAnyAuthority("ROLE_PROMOTIONS_REVIEWER","ROLE_ADMIN")
            .pathMatchers("/promotion-query-service/**")
                .hasAnyAuthority("ROLE_ADMIN","SCOPE_internal");
}
```

Rules for Granite-owned paths (for example `/user-command-service/**` or `/session-query-service/**`) in this hook never take effect, because the core rules matched first. `ApplicationHooksGatewayTestConfiguration` with `GatewayApplicationHooksTest`, and `AuthorizingGatewayTestConfiguration` with `GatewayAuthorizationTest`, in ServerTest show this end to end. `GatewayTestBackend` serves a JWKS and stands in for the routed services.

To add routes, override `configureApplicationRoutes` and call `addRoute`; it runs after Granite's routes in every profile. To expose only some Granite routes, override `configureRoutes` without calling `super` and add the ones you want.

```java
@Configuration
public
class HostConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected RouteLocatorBuilder.Builder
    configureApplicationRoutes(RouteLocatorBuilder.Builder routes,Environment environment)
    {
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