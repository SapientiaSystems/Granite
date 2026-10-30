# Granite.FrontEndController Microservice

## Overview

Granite.FrontEndController is a Spring Boot microservice built on the Strata FrontEndController framework. The web application calls typed, per-subject operations: `getUsersOutput` / `handleUsersInput` for the Users screen and `getSessionOutput` / `handleSessionInput` for login and session. Each operation has its own request, reply, input and output types and its own endpoint. The service holds no persistent state of its own: the services translate inputs into calls on the Granite.User and Granite.Session services, authenticated with the FEC's own service token, and wrap the results in outputs. It is also the only place that sets and clears the `__Host-<P>_SID` session cookie.

## Technology Stack

- **Language**: Java 23 (Amazon Corretto), TypeScript
- **Build**: Gradle (multi-project), Yarn (TypeScript components)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Serialization**: Jackson (concrete `FrontEndInput`/`FrontEndOutput` types bound by the request/reply generics; no type property, no deduction)
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
│   ├── Granite.FrontEndController.Service/        # Typed request/reply, input/output and service interfaces (Java + TypeScript)
│   ├── Granite.FrontEndController.Server/         # Services, controllers, view actions, Spring configuration
│   └── Granite.FrontEndController.Client/         # REST client (Java + TypeScript)
├── Tests/
│   ├── Granite.FrontEndController.ServiceTest/
│   ├── Granite.FrontEndController.ServerTest/     # Plain JUnit unit tests and MockMvc tests
│   ├── Granite.FrontEndController.ClientTest/
│   └── Granite.FrontEndController.TestSuite/
├── build.gradle
├── settings.gradle
└── package.json
```

## Architecture

### Typed operations

Strata's `FrontEndControllerService` is an abstract pipeline (validate, map to context, run the operation, build the reply). Granite has one service, `GraniteFrontEndControllerService`, which extends it and implements `IGraniteFrontEndControllerService`; each subject (Users, Session, Preferences) has one get-output and one handle-input operation on it. Each screen interaction is a `FrontEndInput` identified by its key `source->command->destination` (for example `User.All->Save->UsersEditableView`); a view request is identified by `source->destination` (for example `User.All->UsersEditableView`). The service dispatches on the key to a view action. An unknown key fails the reply: its exception chain ends in `IllegalArgumentException("No action found for key: ...")`.

| Subject | Operation | Request / Reply | Input / Output | Keys |
|---|---|---|---|---|
| Users | `getUsersOutput` | `GetUsersOutputRequest` / `GetUsersOutputReply` | `UsersOutput` | `User.All->UsersEditableView` |
| Users | `handleUsersInput` | `HandleUsersInputRequest` / `HandleUsersInputReply` | `UsersInput` / `UsersOutput` | `User.All->Save->UsersEditableView`, `User.All->Delete->UsersEditableView` |
| Session | `getSessionOutput` | `GetSessionOutputRequest` / `GetSessionOutputReply` | `SessionOutput` | `Session.New->LoginView`, `Session.Current->SessionView` |
| Session | `handleSessionInput` | `HandleSessionInputRequest` / `HandleSessionInputReply` | `SessionInput` / `SessionOutput` | `Session.New->Login->LoginView`, `Session.New->StartOktaLogin->LoginView`, `Session.New->CompleteOktaLogin->LoginView`, `Session.Current->Logout->LoginView` |
| Preferences | `getPreferencesOutput` | `GetPreferencesOutputRequest` / `GetPreferencesOutputReply` | `PreferencesOutput` | `Preferences.Current->PreferencesView` |
| Preferences | `handlePreferencesInput` | `HandlePreferencesInputRequest` / `HandlePreferencesInputReply` | `PreferencesInput` / `PreferencesOutput` | `Preferences.Current->Save->PreferencesView` |

`userId` is no longer required by Strata, so callers do not send a placeholder.

#### Granite.FrontEndController.Service

- Package `granite.frontendcontroller.service.requestreply`. `IGraniteFrontEndControllerService` declares `handleUsersInput`, `getUsersOutput`, `handleSessionInput`, `getSessionOutput`, `handlePreferencesInput` and `getPreferencesOutput`, each returning `CompletionStage<...Reply>`. Requests extend Strata `HandleInputRequest<I>` / `GetOutputRequest`; replies extend `HandleInputReply<I,O>` / `GetOutputReply<O>` and have a constructor taking the originating request.
- `UsersInput`: `usersToCreate: List<CreateUserData>`, `usersToUpdate: List<UpdateUserData>`, `userIdsToDelete: List<Long>`; all default to empty lists. `UsersOutput`: `users: List<UserData>`, `failureMessages: List<String>`, `roles: List<RoleData>` (the application role catalog, Admin first; filled in every Users output). `RoleData {name, label, impliedRoles}`.
- `SessionInput {userName, password, returnTo, code, state}`: one flat input for every session command. `password` and `code` are write-only: never serialized, never in `toString`. Each command reads only the fields it needs (`Login`: `userName`, `password`; `StartOktaLogin`: `returnTo`; `CompleteOktaLogin`: `code`, `state`; `Logout`: none).
- `SessionOutput {status, userName, authorities, returnTo, passwordEnabled, oktaEnabled, redirectUrl, loggedOut, endSessionUrl}`: one flat output for every session key. `status` is `AUTHENTICATED` or `UNAUTHENTICATED`. Each key fills only its own fields (boolean getters are `isX`).
- `PreferencesInput {values}`: `values` maps declared preference keys (without the `preferences.` prefix) to JSON values. `PreferencesOutput {values, definitions, failureMessages}`: `values` holds every declared key (the stored value when it validates, otherwise the default), `definitions` lists every declared `PreferenceDefinitionData {key, type, defaultValue, allowedValues}` (`type` is the string `STRING`, `BOOLEAN`, `NUMBER` or `ENUM`; `allowedValues` is filled for `ENUM`).
- TypeScript mirrors of each type live under `src/main/typescript/RequestReply/` and are exported from `index.ts`.

#### Granite.FrontEndController.Server

- `server.application`: `GraniteFrontEndControllerService` extends Strata `FrontEndControllerService` and calls its protected `handleInput(request,operation,replyFactory)` / `getOutput(...)` helpers. It holds a `Map` of key to action for the inputs and another for the outputs of each subject; the generic `dispatchInput` / `dispatchOutput` look the key up in the subject's map, fail with `IllegalArgumentException("No action found for key: <key>")` when absent, and otherwise set the action's output on the context.
- `server.control`: `IInputAction<I,O>` (`CompletionStage<O> apply(I input)`) and `IOutputAction<R,O>` (`CompletionStage<O> apply(R request)`) are the Granite-local typed action interfaces. The view actions implement them and do no casting.
- `server.control.user`: the Users actions, constructed with `IUserQueryServiceClient`, `IUserCommandServiceClient` (Save and Delete), the `SessionRequestContext` and the `ApplicationRolesManager`. Every Users action requires a verified caller (`SessionRequestContext.getCallerUserId()`): with none it returns `UnauthenticatedUsersOutput` (failure message `not authenticated`, empty `users`, the role catalog, `userId` null) without calling User. This is a caller check only; the FEC has no role rules (those stay in the Gateway).

| Key | Action | Behavior |
|---|---|---|
| `User.All->UsersEditableView` (get-output) | `InitializeUsersView` | `findAllUsers`; returns `UsersOutput` with all users and the `ApplicationRolesManager` catalog as `roles` (`impliedRoles` sorted; every Users output carries it via `RoleCatalogData`) |
| `User.All->Save->UsersEditableView` (handle-input) | `UpdateUsersView` | `createUsers` then `updateUsers` (each skipped when its list is empty), then re-queries all users |
| `User.All->Delete->UsersEditableView` (handle-input) | `DeleteUsersView` | refuses the verified caller's own id (never the payload `userId`), `destroyUsers` for the rest, then re-queries all users |

  Every output copies `source`/`command`/`destination` from the request; its `userId` is the verified caller id (the payload `userId` is ignored). Save and delete report failed users and failed batches as `failureMessages` (`<userName or id>: <reason>`) alongside the refreshed `users` list.
- `server.control.session`: the Session actions (clients `ISessionCommandServiceClient`, `ISessionQueryServiceClient`, `IUserQueryServiceClient`, a `SessionRequestContext` and `SessionLoginSettings`). All of them produce `SessionOutput`. The login and logout outputs never carry a token or the handle.

| Key | Action | Behavior |
|---|---|---|
| `Session.New->LoginView` (get-output, public) | `LoginMethodsView` | `SessionOutput` with `passwordEnabled` from `granite.session.login.password.enabled` (default `true`) and `oktaEnabled` from `granite.session.login.okta.enabled` (default `false`) |
| `Session.New->Login->LoginView` (handle-input, public) | `LoginView` | Session `login` (`PASSWORD`, `deviceId` from the `__Host-<P>_DID` cookie). Success adds `Set-Cookie: __Host-<P>_SID=<handle>; HttpOnly; Secure; SameSite=Strict; Path=/ (no Domain)` (`Max-Age` = remaining session lifetime) and returns `AUTHENTICATED`. A failed login, disabled password login, blank field, missing `__Host-<P>_DID` or Session error all return `UNAUTHENTICATED` with no cookie and no hint why |
| `Session.New->StartOktaLogin->LoginView` (handle-input, public) | `StartOktaLoginView` | Session `startOktaLogin` (`deviceId` from `__Host-<P>_DID`, `returnTo` from the input). Success adds `Set-Cookie: __Host-<P>_LOGIN=<transaction id>; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=300` and returns `SessionOutput` with `redirectUrl` (the Okta authorize URL; the SPA navigates the window to it). Okta disabled, missing `__Host-<P>_DID` or a Session failure return `UNAUTHENTICATED` with no `redirectUrl` and no cookie |
| `Session.New->CompleteOktaLogin->LoginView` (handle-input, public) | `CompleteOktaLoginView` | Always adds `Set-Cookie: __Host-<P>_LOGIN=; Max-Age=0`. Session `completeOktaLogin` with `code`, `state`, the `__Host-<P>_LOGIN` transaction id and `__Host-<P>_DID`. Success also adds `__Host-<P>_SID` (as for password login) and returns `AUTHENTICATED` with `userName`, `authorities` and `returnTo`. Okta disabled, a missing cookie or any Session failure return `UNAUTHENTICATED` with no `__Host-<P>_SID` |
| `Session.Current->SessionView` (get-output) | `SessionStatusView` | Session id is the `jti` of the caller's verified bearer; Session query `findSessionsByKey` (open, unexpired, same user) and User `findUsersByKey` for `userName`; `authorities` from the session |
| `Session.Current->Logout->LoginView` (handle-input) | `LogoutView` | Session `logout` for the bearer's `jti` (closes as `LOGOUT`), then `Set-Cookie: __Host-<P>_SID=; Max-Age=0` and `loggedOut=true`. On failure `loggedOut=false` and the cookie is kept. `endSessionUrl` is Session's end-session URL for an Okta session (the SPA navigates to it to end the Okta session), otherwise null |

  `granite.session.login.okta.enabled` must match Session's flag of the same name (both default `false`; `local` and `production` read `${OKTA_ENABLED:false}`, `development` and `test` read `${OKTA_ENABLED_DEVELOPMENT:false}`/`${OKTA_ENABLED_TEST:false}`). Both `HostConfiguration` classes build `IConfiguration` with `new SpringConfigurationProvider(environment)`, which is what resolves these placeholders from environment variables; `IConfiguration` does not apply Spring relaxed binding, so a `GRANITE_SESSION_LOGIN_OKTA_ENABLED` variable has no effect.
- `server.application`: `PreferenceDefinition {key, type, defaultValue, allowedValues, maxLength}` (factories `string` (optionally with a max length, default 1024 characters; longer values are rejected on save and read as the default), `bool`, `number`, `enumeration`; `isValid(value)`) and `PreferenceType` (`STRING`, `BOOLEAN`, `NUMBER`, `ENUM`) describe the preferences an application declares. `GraniteFrontEndControllerContext` carries them as `List<PreferenceDefinition>` and throws `IllegalArgumentException` for duplicate keys.
- `server.control.preference`: the Preferences actions, constructed with `IUserQueryServiceClient` (and `IUserCommandServiceClient` for save), a `SessionRequestContext` and the definitions. Preferences always act on the caller's own user (`SessionRequestContext.getCallerUserId()`); a request or input `userId` is ignored, and no caller yields the failure message `not authenticated` with `definitions` filled and empty `values`. The output `userId` is the caller id (null when unauthenticated). A failed read replies `success=true` with the defaults as `values` and the failure message `unable to read preferences`; the web provider treats any non-empty `failureMessages` on a load as an error and keeps the cached theme. A preference `<key>` is stored in the user's `values` as `preferences.<key>`.

| Key | Action | Behavior |
|---|---|---|
| `Preferences.Current->PreferencesView` (get-output) | `InitializePreferencesView` | User `findUsersByKey` for the caller; `values` has every declared key, using the stored value when it validates against its definition and the default otherwise |
| `Preferences.Current->Save->PreferencesView` (handle-input) | `SavePreferencesView` | Validates every input key first (undeclared key or wrong type or value not allowed for an `ENUM` fails the whole save, nothing is stored; a null value resets the key), then one User `mergeUserValues` with the `preferences.<key>` entries, then builds the output from the merged user returned by that call (no follow-up read, so a stored save is never reported as failed). Concurrent saves for the same user conflict through optimistic locking in User: the second fails and the web UI reverts |

  Granite declares `granite.theme` (`ENUM` `light|dark`, default `light`), stored as `preferences.granite.theme`. An application adds its own by overriding the protected `preferenceDefinitions()` in its configuration and including `super.preferenceDefinitions()`.
- `server.control.user.UpdateUsersView` strips entries whose key starts with the reserved prefixes `preferences.` or `granite.` from the incoming `values` of every created and updated user, so admins cannot seed or change them; Granite.User additionally preserves stored reserved entries on update. No stored-user read is needed.
- `server.platform`: `GraniteFrontEndControllerServiceController<S extends IGraniteFrontEndControllerService>` extends Strata `AbstractFrontEndControllerServiceController<S>` (it does not implement the service interface) and exposes one `@PostMapping` method per operation that delegates to the injected service. It carries no `@RestController` or `@RequestMapping`; Granite's binding is `GraniteApplicationFrontEndControllerServiceController`, which is `@RestController @CrossOrigin(origins = "*") @RequestMapping("${granite.frontendcontroller.service-path}")`. An application extends the controller (adding its own `@PostMapping` methods) and binds its own subclass. `SessionRequestContext` is captured on the request thread when the service is built (inbound cookies, the `jti` and `sub` of the verified `Jwt` principal in the `SecurityContext` (never decoded from the raw header), and a list of `Set-Cookie` values) and stored as a request attribute. Strata's reply cookies never reach HTTP, so `ResponseCookieAdvice` (a `@ControllerAdvice` `ResponseBodyAdvice` with no selectors, so it applies to every controller, including the Granite controller; bean in `AbstractGraniteFrontEndControllerConfiguration`) copies the list to `Set-Cookie` response headers (plus `Cache-Control: no-store`) on the container thread. `SessionCookies` (an instance built from `GraniteCookieNames`, carried by `GraniteFrontEndControllerContext`) builds the `__Host-<P>_SID` and `__Host-<P>_LOGIN` header values; the cookie names come from the protected overridable `cookieNames()` of `AbstractGraniteFrontEndControllerConfiguration`, which reads `granite.cookies.prefix` (`P`, default `GRANITE`, must match `[A-Z0-9_]+`) and is the same prefix the Gateway uses. Every cookie is `Secure`, `Path=/` and has no `Domain` (the `__Host-` rules); a device cookie with the old unprefixed name is not read.
- `server.configuration`: `AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>` imports Strata `ApplicationConfiguration` (the `ModelMapper`) and `FrontEndControllerHealthCheckConfiguration`, and provides a singleton `@Named("FrontEndController")` executor (`BoundedExecutor(200,10s)`), an overridable singleton `applicationRoles()` bean (default `new GraniteApplicationRolesManagerSupplier().get()`), a protected overridable `preferenceDefinitions()` (default `granite.theme`), an `applicationRolesStartupLog(ApplicationRolesManager)` bean that logs the catalog role names once at startup, and the `ResponseCookieAdvice` bean. It declares no service or controller bean: Spring builds a request-scoped proxy from a bean method's declared return type, so the type variable `S` would be erased and an `Object` controller would never be an MVC handler. `createContext` builds the immutable `GraniteFrontEndControllerContext` (mapper, executor, the four clients, `SessionRequestContext`, `SessionLoginSettings`, `ApplicationRolesManager`, the preference definitions) with **concrete** User and Session clients from the protected `createUserQueryClient()`, `createUserCommandClient()`, `createSessionCommandClient()` and `createSessionQueryClient()` (each built from `createClientConfiguration(baseUrlProperty)`, which returns `GraniteClientSettings`, which registers `ServiceTokenClientRequestFilter` over the singleton `serviceTokenSource()` bean), and captures `SessionRequestContext.fromCurrentRequest()`. The service is therefore request-scoped, so it is created on the request thread. Never pass request-scoped client beans to the service: a continuation that runs off the request thread would fail (the Users Save bug). Each concrete configuration extends the base and declares exactly two typed request-scoped beans (the base provides `createContext(mapper,executor,roles)`, the abstract `createService(GraniteFrontEndControllerContext)`, and the protected client factories): `@Bean @RequestScoped @Named("Granite") public IXService frontEndControllerService(@Named("FrontEndController") Executor executor,ModelMapper mapper,ApplicationRolesManager roles) { return createService(createContext(mapper,executor,roles)); }` and `@Bean @RequestScoped @Named("Granite") public XController frontEndControllerServiceController(@Named("Granite") IXService service) { return new XController(service); }`, where `XController` is the application's `@RestController @CrossOrigin(origins = "*") @RequestMapping("${granite.frontendcontroller.service-path}")` subclass of `GraniteFrontEndControllerServiceController<IXService>`. `GraniteFrontEndControllerConfiguration` does this for `IGraniteFrontEndControllerService` with `GraniteApplicationFrontEndControllerServiceController`. An application such as Poplar therefore extends `IGraniteFrontEndControllerService`, `GraniteFrontEndControllerService` (constructed from the context; `getContext()` is protected), the controller and the configuration, implements `createService`, declares the two beans above and may override `applicationRoles()` and the client factories. `FrontEndControllerMvcContextTest` proves this recipe in a real Spring MVC context for the Granite configuration and the test-only `Extended*` configuration.

### Jackson binding

Strata's request and reply classes carry the concrete input and output as generics (`HandleInputRequest<I>`, `HandleInputReply<I,O>`, `GetOutputReply<O>`), so Jackson binds the concrete `UsersInput`, `SessionInput`, `PreferencesInput`, `UsersOutput`, `SessionOutput` or `PreferencesOutput` without a type property. There is no deduction and no custom Jackson module; do not add one. A new subject adds its own typed request, reply, input and output classes and a get-output and handle-input pair to `IGraniteFrontEndControllerService`, `GraniteFrontEndControllerService` and `GraniteFrontEndControllerServiceController`. `FrontEndJsonSerializationTest` covers the JSON round trips; keep it passing when a type changes.

### JWT check and caller identity

`AbstractGraniteFrontEndControllerConfiguration` is `@EnableWebSecurity` and declares exactly one `SecurityFilterChain`, built by `GraniteJwtCheckSecurity.jwtCheckChain(...)` in `OPTIONAL` mode: a request without a bearer token passes (login and Okta start/complete are tokenless), while a bearer that fails signature, expiry, issuer or audience checks is rejected with 401. There are no role or authority rules. The `jwtDecoder` bean uses `GraniteResourceServerSettings.from(environment)` (`granite.security.jwt.jwk-set-uri` direct to Session Command, `granite.security.jwt.ssl-bundle`, optional issuer and audience), the same properties as the User hosts; both host `application-*.properties` files set them per profile. `getTokenlessPathPatterns(environment)` (default `defaultTokenlessPaths`: health, error, and outside production the OpenAPI and Swagger UI paths) and `createJwkSetRestOperations(environment)` are protected seams. The caller id and session id come only from the verified `Jwt` of the current request's `getUserPrincipal()` (the `JwtAuthenticationToken` the security chain set; its `Jwt` principal, or the `Jwt` token of any `AbstractOAuth2TokenAuthenticationToken`), read on the request thread when `SessionRequestContext.fromCurrentRequest()` runs; an unverified, unauthenticated, anonymous or absent principal yields no caller. It never reads the static `SecurityContextHolder`, and the hosts do not change the holder strategy (an `InitializingBean` that switched to `MODE_INHERITABLETHREADLOCAL` after the filters were built left the static holder empty, so every caller was refused). Nothing needs the security context on executor threads: downstream calls use the service token. `FrontEndControllerSecurityTest` covers anonymous, forged, expired, unsigned and valid tokens (the MVC context tests need `granite.security.jwt.jwk-set-uri` in the environment).

### Calling User and Session with the FEC service identity

User and Session require a valid Session-issued bearer token. The FEC calls them with its own service token, never the caller's bearer: the caller's identity is already captured from the verified `Jwt` in `SessionRequestContext`, and the caller-only checks use that. The concrete User and Session clients built by `AbstractGraniteFrontEndControllerConfiguration` register `ServiceTokenClientRequestFilter`, which sets `Authorization: Bearer <service token>` as a real HTTP header on every outgoing call (replacing any other `Authorization`). Copying `Authorization` into a request DTO's `headers` does not reach the services, because DTO headers are part of the JSON body.

The token comes from the singleton, lazily created `serviceTokenSource()` bean (`ServiceTokenSource`, an `IServiceTokenSource`): it calls Session `issue-service-token` with `granite.frontendcontroller.service-client.id` (default `granite-frontendcontroller`) and `granite.frontendcontroller.service-client.secret` (from `${FEC_SERVICE_CLIENT_SECRET:}` in every host profile, a Kubernetes Secret in the cluster), caches the token and renews it a minute before `expiresAt`; it is thread-safe (`synchronized`). A blank id or secret throws `IllegalStateException` without calling Session. The issue call is bounded by `ServiceTokenSource.DEFAULT_ISSUE_TIMEOUT` (5 s, `orTimeout`), so a stalled Session cannot park every FEC call on the lock. When a renewal fails or times out while the cached token is still valid (`now < expiresAt`), the cached token is returned and the failure logged (client id only); otherwise a refused or failed issue throws `IllegalStateException` without the secret. After any failed issue or renewal the source does not call Session again for `ServiceTokenSource.FAILURE_BACKOFF` (5 s, counted from the end of the failed call); in that window `getToken` returns the still-valid cached token or throws at once, so concurrent callers do not each wait up to the issue timeout on the lock. The issuing call goes through a separate Session command client from the protected `createServiceTokenClient()` seam, which has no service-token filter, so it carries no `Authorization` header. Session must register the same client id with the SHA-256 of the secret (`granite.session.service-clients.granite-frontendcontroller.secret-sha256`). The token has `sub` = client id and `authorities` = `[SCOPE_internal]`. The FEC sends it on every Session call too, including `login` and `refresh`, which in `production` the Gateway opens only to `SCOPE_internal`. `ServiceTokenSourceTest`, `ServiceTokenClientRequestFilterTest`, `GraniteFrontEndControllerConfigurationTest` and `GraniteFrontEndControllerConfigurationClientsTest` cover caching, renewal and the headers sent (`FakeSessionBackend` answers `issue-service-token` and records those calls separately).

Routing and secrets: in every profile `user-query-service.base-url`, `user-command-service.base-url`, `session-command-service.base-url` and `session-query-service.base-url` point at `https://granite-gateway-servicehost:443`, so every FEC call to User and Session passes the Gateway, which authorizes `SCOPE_internal` and rate limits `issue-service-token`; only the JWKS fetch (`granite.security.jwt.jwk-set-uri`) goes directly to Session Command. Session verifies `FEC_SERVICE_CLIENT_SECRET` against `FEC_SERVICE_CLIENT_SECRET_SHA256` (SHA-256 of the same secret, set on the Session CommandHost); the hash is unsalted, so generate the secret as at least 32 random bytes, base64-encoded, never a password. Kubernetes keeps both in the Secret `granite-fec-service-client`, created by the deploy scripts.

TLS for downstream calls: `granite.frontendcontroller.ssl-bundle` (constant `AbstractGraniteFrontEndControllerConfiguration.SSL_BUNDLE`, default `granite-services`) names the Spring SSL bundle that the private `createClientBuilder()` uses for every downstream client: the four User and Session clients, any client an application builds through `createClientConfiguration(String)`, and the service-token client from `createServiceTokenClient()`. An application with its own bundle (for example Poplar's `poplar-services`) sets this property instead of overriding the client factories. The JWKS fetch is separate and keeps using `granite.security.jwt.ssl-bundle`.

### Applications

- **FrontEndControllerUserHost**: imports `GraniteFrontEndControllerConfiguration` and `ServiceConfiguration`; sets `granite.frontendcontroller.service-path=/frontendcontroller-user-service`. Listens on port 8081.
- **FrontEndControllerAdminHost**: imports `GraniteFrontEndControllerConfiguration` and `ServiceConfiguration`; sets `granite.frontendcontroller.service-path=/frontendcontroller-admin-service`. Listens on port 8082.

Both hosts serve all three subjects. Each `HostConfiguration` keeps its OpenAPI group, scanning `granite.frontendcontroller.server.platform` (where the controllers live) and the health package.

## Commonly Used Design Patterns

### Request-Reply

Service operations accept a typed request and return a typed reply. Requests extend `AbstractServiceRequest` (with `requestId`, `timestamp`, headers, cookies). Replies extend `AbstractServiceReply`. `handle<Subject>Input` wraps a `FrontEndInput` in a `Handle<Subject>InputRequest`; `get<Subject>Output` takes a `Get<Subject>OutputRequest` and returns a reply carrying the subject's `FrontEndOutput`.

### Asynchronous Pipeline

Actions return `CompletionStage<O>` and compose User and Session client calls with `thenApply`/`thenCompose`.

### Composable Configuration

Spring `@Configuration` classes are composed via `@Import`. `HostConfiguration` at the application level imports and assembles the required configurations.

## REST Endpoints

Both hosts expose the same six operations under their own service path (`granite.frontendcontroller.service-path`). All are `POST` and consume and produce JSON.

| Host | Endpoint | Request | Reply |
|---|---|---|---|
| User (`/frontendcontroller-user-service/`) | `POST /get-users-output` | `GetUsersOutputRequest` | `GetUsersOutputReply` |
| User | `POST /handle-users-input` | `HandleUsersInputRequest` | `HandleUsersInputReply` |
| User | `POST /get-session-output` | `GetSessionOutputRequest` | `GetSessionOutputReply` |
| User | `POST /handle-session-input` | `HandleSessionInputRequest` | `HandleSessionInputReply` |
| User | `POST /get-preferences-output` | `GetPreferencesOutputRequest` | `GetPreferencesOutputReply` |
| User | `POST /handle-preferences-input` | `HandlePreferencesInputRequest` | `HandlePreferencesInputReply` |
| Admin (`/frontendcontroller-admin-service/`) | `POST /get-users-output` | `GetUsersOutputRequest` | `GetUsersOutputReply` |
| Admin | `POST /handle-users-input` | `HandleUsersInputRequest` | `HandleUsersInputReply` |
| Admin | `POST /get-session-output` | `GetSessionOutputRequest` | `GetSessionOutputReply` |
| Admin | `POST /handle-session-input` | `HandleSessionInputRequest` | `HandleSessionInputReply` |
| Admin | `POST /get-preferences-output` | `GetPreferencesOutputRequest` | `GetPreferencesOutputReply` |
| Admin | `POST /handle-preferences-input` | `HandlePreferencesInputRequest` | `HandlePreferencesInputReply` |

Base URLs: `user-query-service.base-url`, `user-command-service.base-url`, `session-command-service.base-url`, `session-query-service.base-url`. Both `HostConfiguration` request-logging filters have payload and header logging turned off, because bodies carry passwords and headers carry `Cookie` and `Authorization`.

### Testing

`Granite.FrontEndController.ServerTest` holds the view-action tests, dispatch tests per subject (`GraniteFrontEndControllerServiceUsersTest`, `GraniteFrontEndControllerServiceSessionTest`), MockMvc tests on the new paths (cookie behaviour through `ResponseCookieAdvice`), the configuration tests (`GraniteFrontEndControllerConfigurationTest`, `GraniteFrontEndControllerConfigurationClientsTest`, `ExtendedFrontEndControllerConfigurationTest`, `FrontEndControllerMvcContextTest` (real `AnnotationConfigWebApplicationContext` + `@EnableWebMvc` + MockMvc) with its test-only `Extended*` service, interface, controller and configuration), and `AbstractControllerMappingTest` (in the `platform` package, with `GraniteControllerMappingTest`). The mapping test checks by reflection that every `IGraniteFrontEndControllerService` method has a controller method with a `@...Mapping` annotation. Strata's own fixture is not published, so Granite keeps this copy.

`Granite.FrontEndController.UserHost` has its own test source set (set up like the Session hosts'): `FrontEndControllerUserHostSmokeTest` boots the real host (`local` profile, `src/test/resources/application-local.properties` without TLS), serves a JWKS from a local `HttpServer`, sends a signed bearer to the test-only `CallerProbeController` and asserts that `SessionRequestContext` yields the caller id (and none for an anonymous request).

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
