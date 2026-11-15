# Granite

Multi-service Spring Boot system (User, Session, Feedback, Survey, Content, Agent, Client, FrontEndController, Gateway, Outbox) with a React web front end. Each service has its own `CLAUDE.md` (`Claude.md` for User, Session, Feedback, Survey, Content, Agent and Client); read it before changing that service.

## Extending Granite

An application (for example Poplar) reuses Granite by extending the entry configuration of each service and overriding its seams. Seams are Java overrides of `@Bean` or protected methods on the entry configuration classes, because Spring Boot forbids redefining imported bean names. Extension points are documented per service:

- User: `AbstractGraniteUserServerConfiguration` and `GraniteUserCommandServerConfiguration` (`Microservices/Granite.User/Claude.md`)
- Session: `applicationRoles` seam, login gate, Okta role assignment (`Microservices/Granite.Session/Claude.md`)
- FrontEndController: `AbstractGraniteFrontEndControllerConfiguration`, `GraniteFrontEndControllerContext`, the protected `cookieNames()` seam (cookie prefix) and the singleton `sessionCookies()` bean built from it at startup, controller recipe (`Microservices/Granite.FrontEndController/CLAUDE.md`); set `granite.frontendcontroller.ssl-bundle` (default `granite-services`) to use the application's own SSL bundle for all downstream FEC clients
- Feedback: `AbstractGraniteFeedbackServerConfiguration`, `GraniteFeedbackCommandServerConfiguration`, `GraniteFeedbackQueryServerConfiguration` (`Microservices/Granite.Feedback/Claude.md`); the FEC reaches it through `GraniteHelpServiceClients` (`createHelpServiceClients()`, `createFeedbackCommandClient()`, `createFeedbackQueryClient()`)
- Survey: `AbstractGraniteSurveyServerConfiguration`, `GraniteSurveyCommandServerConfiguration`, `GraniteSurveyQueryServerConfiguration` (`Microservices/Granite.Survey/Claude.md`); questions are fixed in `GraniteSurveyQuestions` (Java and TS); the FEC reaches it through `GraniteHelpServiceClients` (`createSurveyCommandClient()`, `createSurveyQueryClient()`)
- Content: `AbstractGraniteContentServerConfiguration` (`contentSearchEngine()`), `GraniteContentCommandServerConfiguration` (`contentSources()`), `GraniteContentQueryServerConfiguration` (`Microservices/Granite.Content/Claude.md`). An application ships its help and guides as Markdown under `content/<application>/` on the Content CommandHost classpath (front matter `key`, `title`, `kind`, `keywords`, `requiredAuthorities`, `version`; links `[text](content:<key>)`); they are synchronized at start. The FEC reaches it through `GraniteHelpServiceClients` (`createContentQueryClient()`) and passes the caller's verified authorities (`SessionRequestContext.getCallerAuthorities()`, never `SCOPE_*`)
- Agent: `GraniteAgentCommandServerConfiguration` (`agentProvider(Environment)`, `agentTools(Environment,Supplier<String>)`, `agentSystemPrompt()`), `AbstractGraniteAgentServerConfiguration` (`Microservices/Granite.Agent/Claude.md`). An application adds its own `IAgentTool`s (mutating tools need the user's approval in the chat; tools change data only through services, with the user's delegated token from Session `issue-delegated-token`) and its own system prompt text. The FEC reaches it through `GraniteHelpServiceClients` (`createAgentCommandClient()`, `createAgentQueryClient()`)
- Client: `AbstractGraniteClientServerConfiguration` (`applicationRoles()`, `clientIdGenerator(Environment)` for the Client ID prefix `granite.client.id-prefix`), `GraniteClientCommandServerConfiguration` (`clientEventSender(IOutboxEventRepository)`), `GraniteClientQueryServerConfiguration` (`Microservices/Granite.Client/Claude.md`); the FEC reaches it through `GraniteClientServiceClients` (`createClientServiceClients()`, `createClientCommandClient()`, `createClientQueryClient()`) and Session through the Gateway. Granite provides the mechanism and no Client-callable Granite path: an application grants paths in its Gateway (see "API clients")
- Gateway: `configureApplicationRoutes`, `configureApplicationAuthorization`, `getUsersReaderAuthorities(Environment)`, the overridable `cookieNames(Environment)` `@Bean` (`granite.cookies.prefix`) (`Microservices/Granite.Gateway/CLAUDE.md`)

### Role catalog

- `ApplicationRolesManager` (Foundation) is the role catalog; each application supplies its own, Granite's comes from `GraniteApplicationRolesManagerSupplier` (`ROLE_ADMIN`, `ROLE_CLIENT_ADMIN`, `ROLE_AUDITOR`, `ROLE_USER`).
- An application provides ONE catalog, an `ApplicationRolesManager` (for example via your own `Supplier<ApplicationRolesManager>` in its Foundation library, as `new GraniteApplicationRolesManagerSupplier().get()` is for Granite), and returns it from the `applicationRoles()` override on all four hosts that use the catalog: User, Session, FrontEndController and Client. Each of those hosts logs the catalog's role names (names only) once at startup (`applicationRolesStartupLog`), so a host left on a different catalog is visible in the logs.
- Granite core owns two roles, `ROLE_ADMIN` and `ROLE_CLIENT_ADMIN` (label `ClientAdmin`, present in every catalog); neither implies an application role nor is implied by one. Breaking change for downstream applications: an application that copies Granite's catalog roles into its own catalog must skip both core roles, because `ApplicationRolesManager` rejects them as reserved names (`<name> is reserved`). `ROLE_CLIENT_ADMIN` lets a User sign in to ClientHost only (see "Web applications"). `ROLE_AUDITOR` and `ROLE_USER` belong to the Granite application catalog.
- Role names are `ROLE_` plus UPPER_SNAKE (`ROLE_[A-Z0-9_]+`, enforced by `ApplicationRolesManager`). Stored authority sets are always closed under the role hierarchy (the expanded set is stored).
- Renames, applied once at User startup by `AuthorityRenamer`: `ROLE_Admin` to `ROLE_ADMIN`, `ROLE_Auditor` to `ROLE_AUDITOR`, `ROLE_User` to `ROLE_USER` (the interim mixed-case names become uppercase).
- Request authorization rules (which caller may reach which path) live only in the Gateway chain. Outside it, Session gates login (default: the user must hold at least one catalog role; `granite.session.login.required-authorities` overrides with an explicit list) and User rejects stored roles outside the catalog.
- Gateway core rules (Preferences operations `authenticated()` for any signed-in caller, users readers, Granite admin-only paths, application hook, `/**` → `ROLE_ADMIN`) are always registered; `granite.gateway.authorization.role-rules.enabled` only makes the Granite application grant `ROLE_AUDITOR` reader access.

### Preferences

- Preferences are stored per user under `values["preferences.<key>"]` and always act on the caller's own user. Granite declares `granite.theme` (`light|dark`, default `light`). Value keys prefixed `preferences.` or `granite.` are reserved: admin create/update never changes them and the Users screen shows them read-only.
- Server side: an application overrides `preferenceDefinitions()` on its FrontEndController configuration, calls `super.preferenceDefinitions()` and adds `PreferenceDefinition` entries (STRING, BOOLEAN, NUMBER or ENUM) with its own namespaced keys, for example `poplar.defaultRegion`. Undeclared keys or wrong types are rejected on save.
- Web side: write a section component that reads and saves with `usePreference("poplar.defaultRegion")` and pass it as a child of `<PreferencesView>`. Saving is per change. `PreferencesProvider` (Granite.Web.Account) needs `MantineProvider` (it drives the colour scheme) and `AuthenticationProvider` above it.

### UI building blocks

Import from the published subpaths:

- `granite.web.authentication/Authentication`: `AuthenticationProvider`, `RequireAuthentication`, `useAuthentication` (including `createFrontEndClient`), `AuthenticationContext`, `hasAuthority`, `canEdit`
- `granite.web.authentication/Login`: `LoginView`, `LoginCallbackView`, `LoginForm`, `ReauthenticationDialog` (rendered by `AuthenticationProvider`), `UserMenu({ preferencesPath?, loginPath = "/login", items?, className?, style? })`
- `granite.web.user/Users`: `UsersView({ service?, height = "calc(100vh - var(--app-header-height, 0px))", className?, style? })`, default service `frontendcontroller-user-service`; authorities are edited as a pick list of the role catalog with hierarchy cascade
- `granite.web.feedback/Feedback`: `FeedbackView({ service?, className?, style? })`, the Help view's Provide Feedback form (any signed-in user; per-type fields; thank-you screen). `granite.web.feedback/Review`: `FeedbackReviewView({ service?, height?, className?, style? })`, the admin grid (readers view; `ROLE_ADMIN` edits status `NEW`/`SENT`/`RECEIVED` and notes, then Save)
- `granite.web.content/Search`: `ContentSearchView({ service?, initialKey?, className?, style? })`, the Help view's Search Help (own back/forward and search box; topics from `get-content-output`, search and open through `handle-content-input`). `granite.web.content/Markdown`: `MarkdownView({ source, onNavigate?, ... })` and `parseMarkdown` (headings, paragraphs, lists, code, emphasis, `content:` links; no raw HTML)
- `granite.web.agent/Chat`: `AgentChatView({ service?, className?, style? })`, the Help view's Chat With Virtual Assistant (latest conversation from `get-agent-output`, Send/Approve/Decline through `handle-agent-input`, a New conversation button, assistant replies rendered with `MarkdownView`)
- `granite.web.survey/Survey`: `SurveyView({ service?, onDismiss?, className?, style? })`, the Help view's Take Survey form (any signed-in user; questions from the FEC output, falling back to `GRANITE_SURVEY_QUESTIONS`; thank-you screen whose Dismiss calls `onDismiss`). `granite.web.survey/Review`: `SurveyReviewView({ service?, height?, className?, style? })`, the admin grid (one column per question, per-question averages in the toolbar; readers view; `ROLE_ADMIN` edits status and notes, then Save)
- `granite.web.session/Sessions`: `SessionsView({ service?, height = "calc(100vh - var(--app-header-height, 0px))", className?, style? })`, default service `frontendcontroller-user-service`; read-only grid of open sessions (switch to include closed ones) with user names and a values detail row; `ROLE_ADMIN` can end selected sessions (closed as `ADMIN`), never the caller's own. Readers are the Gateway's `getUsersReaderAuthorities` on `get-sessions-output`; `handle-sessions-input` is admin-only
- `granite.web.client/Clients`: `ClientsView({ service?, height = "calc(100vh - var(--app-header-height, 0px))", className?, style?, envPrefix = "granite" })`, default service `frontendcontroller-user-service`; the admin grid of API Clients (readers view; `ROLE_ADMIN` adds, edits, deletes and rotates; rows are read-only until Add or Edit, and Save or Undo ends editing, as in the Users grid; one new Client per Save; a new or rotated secret is shown once in a dialog with Copy and Download `.env` buttons; the file holds `<envPrefix>-client-name`, `<envPrefix>-client-id` and `<envPrefix>-client-secret`, and `envPrefix` may contain only letters, digits, `.`, `_` and `-`). `granite.web.client/Owned`: `OwnedClientsView({ service?, height = "calc(100vh - var(--app-header-height, 0px))", className?, style?, envPrefix = "granite" })`, the caller's own Clients, read-only with Rotate Secret as the only action (used by ClientHost; any `ROLE_CLIENT_ADMIN` or admin caller). A `service` other than `frontendcontroller-user-service` also needs the Gateway `getClientApplicationPathPatterns(Environment)` override (see "Your own client-admin web app")
- `granite.web.account/Preference`: `PreferencesProvider`, `PreferencesView({ userName, userRole, children })`, `usePreference`

An application frame (providers, its own header, routes); `MantineProvider` sits above everything:

```tsx
<AuthenticationProvider>
    <PreferencesProvider>
        <BrowserRouter>
            <PoplarHeader />   {/* contains <UserMenu preferencesPath="/preferences" /> */}
            <Routes>
                <Route path="login"          element={<LoginView />} />
                <Route path="login/callback" element={<LoginCallbackView />} />
                <Route path="users"          element={<RequireAuthentication><UsersView /></RequireAuthentication>} />
                <Route path="sessions"       element={<RequireAuthentication><SessionsView /></RequireAuthentication>} />
                <Route path="preferences"    element={<RequireAuthentication><PreferencesView userName={state.userName ?? ""} userRole={state.authorities[0] ?? ""}><PoplarSection /></PreferencesView></RequireAuthentication>} />
            </Routes>
        </BrowserRouter>
    </PreferencesProvider>
</AuthenticationProvider>
```

`UsersView` and `UserMenu` are self-contained (inline layout and colours from the Mantine theme, no AppHost CSS needed); they keep the `view-container`, `view-toolbar` and `app-bar-user-menu` class names as hooks and accept `className` and `style` to adapt them. Runtime configuration is `window.__GRANITE_CONFIG__.apiBaseUrl` (served as `config.js`). In a route component derive the Preferences props with `const { state } = useAuthentication();`, `userName={state.userName ?? ""}` and a role label from `state.authorities` (for example the first entry or a join).

`Granite.Web.Navigator` (`MenuBar`, Home, Help, Account) is Granite's own frame; an application builds its own instead of reusing it. Yarn build order: authentication, user, client, session, feedback, survey, content, agent, account, navigator.

### Web applications

- Two web applications share one Granite backend: `granite` (`Granite.Web.AppHost`, the default) and `client` (`Granite.Web.ClientHost`, image `granite-web-clienthost`, dev port 5174, only the owned-Clients view). Each names itself in `window.__GRANITE_CONFIG__.application` (`config.js`; ClientHost's `config.js.template` sets `application: "client"`, default `granite`) and `granite.web.authentication` sends it with the password login and the Okta start.
- Sessions are bound to that application (`granite.session.applications`, default `granite` plus the client-admin applications, so `granite,client`; unknown names fail the login; `Session` and `OktaLoginTransaction` carry `application`, the session JWT claim `app`). The client-admin applications are named once in `granite.client.admin-applications` (default `client`), read by both Session (`clientAdminApplications(Environment)`) and the Gateway (`getClientApplicationNames(Environment)`); set it in both hosts' configuration, or once as the environment variable `GRANITE_CLIENT_ADMIN_APPLICATIONS`. The login gate is per application: `granite` keeps the catalog rule but `ROLE_CLIENT_ADMIN` never qualifies, so a User holding only that role cannot sign in to AppHost; a client-admin application always requires `ROLE_CLIENT_ADMIN` (and `granite.session.login.applications.<app>.required-authorities` when set). The Gateway lets a session of a client-admin application reach only `getClientApplicationPathPatterns` (the FEC `get-owned-clients-output`, `handle-owned-client-secret-input`, `get-preferences-output` and `handle-preferences-input` on the User host, plus the current-session inputs); tokens without `app` count as `granite`.
- Okta redirect URIs are per application: `granite.session.okta.redirect-uris.<app>` (`granite` falls back to `granite.session.okta.redirect-uri`), reused at completion from the transaction. Register the ClientHost callback `https://client.<app host>/login/callback` in the Okta application. Environment variables for `granite.session.okta.redirect-uris.client`: `OKTA_REDIRECT_URI_CLIENT` (production), `OKTA_REDIRECT_URI_CLIENT_DEVELOPMENT`, `OKTA_REDIRECT_URI_CLIENT_TEST` (optional entries of `granite-secrets` in the deploy scripts); `local` uses `http://localhost:5174/login/callback`, and the in-cluster oauth2-mock overlay sets `GRANITE_SESSION_OKTA_REDIRECTURIS_CLIENT` to `https://client.granite.localhost/login/callback`.
- ClientHost lives on its own host name next to the application: `client.granite.localhost`, `client.granite-dev.__domain__`, `client.granite-test.__domain__`, `client.granite.__domain__` (second Ingress rule: the FEC User paths go to the Gateway, `/` to `granite-web-clienthost`). Build and image scripts from `FrontEnds/Granite.Web`: `build-granite-web-clienthost` (also part of `build-applications` and `build`), `containerize-granite-web-clienthost`, `push-granite-web-clienthost`; the root `containerize-granite-web` and `push-granite-web` run AppHost then ClientHost. Never run containerize or push without approval.

### Your own client-admin web app

An application (for example Poplar) gives its ClientAdmins their own web app the same way Granite does with ClientHost:

1. Name it: `granite.client.admin-applications=poplar-clients` on the Session CommandHost and the Gateway (or `GRANITE_CLIENT_ADMIN_APPLICATIONS=poplar-clients` for both). Session then knows the name, requires `ROLE_CLIENT_ADMIN` for it, and the Gateway confines its sessions. Granite's own `client` is no longer a client-admin application unless it is in the list.
2. Build the frame from the published pieces (`AuthenticationProvider`, `PreferencesProvider`, `LoginView`, `LoginCallbackView`, `UserMenu`, `OwnedClientsView`, `PreferencesView`), as ClientHost does, and set `application: "poplar-clients"` in its `config.js`.
3. Pass the application's own FEC path and `.env` prefix to the view: `<OwnedClientsView service="frontendcontroller-poplar-service" envPrefix="poplar" />`. When the FEC path is not `/frontendcontroller-user-service`, override `getClientApplicationPathPatterns(Environment)` in the application's Gateway with the same four operations on its own path.
4. Register its Okta callback: `granite.session.okta.redirect-uris.poplar-clients`.
5. Optionally give new Client IDs the application's prefix: `granite.client.id-prefix=pc_` on the Client CommandHost.

### Okta properties (Session)

- `granite.session.okta.role-assignment`: `ON_CREATE` (default) or `EVERY_LOGIN`.
- `granite.session.okta.unmapped-groups`: `DEFAULT_ROLE` (default) or `REFUSE`.
- `granite.session.okta.authorities-source` is removed.

## TypeScript tests

- Layout mirrors Java: `<Group>/Tests/<Component>Test/src/test/typescript/<Subject>Test.ts(x)` (for example `FrontEnds/Granite.Web/Tests/Granite.Web.AuthenticationTest`). The same convention applies to microservice TypeScript components when they get tests; the only microservice TypeScript test project today is `Granite.FrontEndController.ServiceTest`, which has no tests yet and is not wired into `test-granite`.
- A test project is private and never published. It depends on the component's published package (`1.0.0-snapshot` from the local Verdaccio) and imports its public subpaths (for example `granite.web.authentication/Authentication`), never the component's source. Test projects run vitest without type-checking (no `tsc`).
- Root scripts: `yarn run test-granite-web` (web test projects) and `yarn run test-granite` (every TypeScript test project). Each publishes the component to the local Verdaccio first (the component's existing `publish-package`), then runs its test project.

## Security split and call routing

- The Gateway performs all authentication and every role/path authorization (`ROLE_*`, `SCOPE_internal`). Service hosts (User, Session, FrontEndController, Outbox) have exactly one `SecurityFilterChain`, `GraniteJwtCheckSecurity.jwtCheckChain` from Granite.Foundation: signature (Session JWKS), expiry, issuer and audience only, stateless, no `hasAuthority`/`hasRole`/role path rules.
- Modes: `REQUIRED` (User, Session, Outbox; Outbox through `GraniteOutboxSecurityConfiguration`): a request without a valid bearer is rejected except on the tokenless paths. `OPTIONAL` (FEC): a bearer, if present, must be valid; anonymous requests pass because the Gateway already authorized them, and the caller comes from the verified `Jwt`. The FEC Users and Preferences views refuse (a `not authenticated` failure output, no User call) when there is no verified caller, and the Users self-delete guard uses the verified caller id, never the payload `userId`; the FEC has no role rules.
- Tokenless paths per host (`getTokenlessPathPatterns(Environment)`, default `GraniteJwtCheckSecurity.defaultTokenlessPaths`: `OPTIONS`, health, `/error`, and outside `production` the springdoc paths): Session Command adds `login`, `refresh`, `resolve-session`, `issue-service-token`, `oauth2/token` and `/.well-known/jwks.json`; User, Session Query and Outbox add none; the FEC needs no list of its own beyond the default because it runs in `OPTIONAL` mode.
- JWT identity: in `production`, `granite.security.jwt.issuer` and `granite.security.jwt.audience` (every host and the Gateway) and `granite.session.jwt.issuer` and `granite.session.jwt.audience` (Session Command) must be set explicitly or startup fails; give each application its own values so tokens cannot be replayed across applications. Outside `production` the defaults `granite-session` / `granite-services` apply.
- Breaking change for downstream applications: the `jwtAuthenticationConverter` bean seams on the User and Session hosts are removed. An application that overrode them no longer compiles (the `@Override` fails) and must delete the override; hosts do not authorize by role, so a role-mapping converter has no place there. Move any such rule to `configureApplicationAuthorization` in its Gateway.
- Breaking change for downstream applications (several applications in one browser): the Gateway seams `securityWebFilterChain(...)` and `createCsrfTokenRepository(GraniteCookieNames)` gained a `GraniteCookieNames` parameter, so overrides must adopt the new signatures. The `SessionCookies` static methods and constants are removed (use an instance built from `GraniteCookieNames`), as are `DeviceCookieWebFilter.DEVICE_COOKIE` and `SessionCookieWebFilter.SESSION_COOKIE` (use the configured names), and the TypeScript `CSRF_COOKIE` export (cookie names now carry the `granite.cookies.prefix`). `LogoutReply` and `SessionOutput` no longer have `endSessionUrl` (Java and TypeScript), `IOktaClient.buildEndSessionUrl` is removed, and the `granite.session.okta.post-logout-redirect-uri` property is no longer read: logout is application-level only and never ends the Okta session.

| Call | Route |
|---|---|
| Browser to services | Gateway |
| FEC to User Query/Command and Session Command/Query | Gateway (`https://granite-gateway-servicehost:443`), FEC service token |
| Session to User | Gateway, Session service token (`ServiceTokenProvider`) |
| Session to Client Command `verify-client-credentials` | Gateway, Session service token (`granite.client-command-service.host`) |
| API client (token request, calls with the token) | Gateway only (`/session-command-service/oauth2/token`, then application paths) |
| Gateway to Session `resolve-session` and revocation check (Session Query `find-sessions-by-key`) | direct (the Gateway's own calls) |
| Gateway and hosts to Session JWKS | direct |
| Session to Okta; services to Postgres and Kafka | direct (not Gateway traffic) |

Public Gateway paths in `production`: health, `issue-service-token` and `/session-command-service/oauth2/token` (both rate limited), the static app and the public FEC `LoginView` inputs. Session `login` and `refresh` are public only outside `production`; in `production` they require `SCOPE_internal` like every other User, Session and Outbox path, so only the FEC (which sends its service token on every Session call) reaches them, and browser login keeps going through the FEC.

Production Ingress: the `production` overlay removes `/user-command-service`, `/user-query-service`, `/session-command-service`, `/session-query-service`, `/feedback-command-service`, `/feedback-query-service`, `/survey-command-service`, `/survey-query-service`, `/content-command-service`, `/content-query-service`, `/agent-command-service`, `/agent-query-service`, `/client-command-service`, `/client-query-service` and `/outbox-service` from `granite-ingress` (JSON-patch `test` + `remove`), then adds two `Exact` paths to the Gateway on the application host, `/session-command-service/oauth2/token` and `/client-command-service/rotate-own-client-secret`, so only the FEC paths, those two, `/gateway-service` and the web app are published (the `client.<app host>` rule publishes only the FEC User paths and ClientHost); `development`, `local` and `test` keep them. The FEC reaches those services through the in-cluster Gateway service.

### Service tokens

- Session's own calls to User use `ServiceTokenProvider` (`sub` starting with `service:`).
- The Agent uses client credentials too (client `granite-agent`, Secret `granite-agent-service-client` with `secret` and `secret-sha256`, generated by the deploy scripts like the FEC's; `AGENT_SERVICE_CLIENT_SECRET` on the Agent CommandHost, `AGENT_SERVICE_CLIENT_SECRET_SHA256` on the Session CommandHost). Session registers it with `granite.session.service-clients.granite-agent.delegation=true`, which also lets it call Session Command `issue-delegated-token` (`{clientId, clientSecret, sessionId, userId}` → a token for that user's open session, at most 5 minutes and never past the session's expiry, `sub` = user, `jti` = session, the session's authorities without `SCOPE_*`, claim `act` = `{sub: granite-agent}`). The Gateway allows `issue-delegated-token` only to `SCOPE_internal` in every environment. The Claude API key comes from the optional Secret `granite-agent-claude-api` (`ANTHROPIC_API_KEY`, created by the deploy scripts only when `ANTHROPIC_API_KEY` is set in their environment; never generated or logged).
- The FEC uses client credentials: Session Command `POST /session-command-service/issue-service-token` (tokenless at Session, public and rate limited at the Gateway) returns a token with `sub` = client id and `authorities` = `[SCOPE_internal]`, valid 1 hour at most. `SCOPE_internal` is defined once as `GraniteAuthorities.SCOPE_INTERNAL` (Granite.Foundation). Session never mints a `SCOPE_*` authority into a session token, even when the user record holds one. The FEC reads `FEC_SERVICE_CLIENT_SECRET`; Session registers the client with `FEC_SERVICE_CLIENT_SECRET_SHA256` (SHA-256 of the same secret, hex or base64). Both come from the Kubernetes Secret `granite-fec-service-client`, which the deploy scripts generate. The hash is unsalted, so the secret must be high-entropy random data (at least 32 random bytes, base64-encoded), never a password. An empty secret is refused. Secrets and tokens are never logged.
- Rate limit: the Gateway limits `issue-service-token` per remote socket address (`granite.gateway.issue-service-token.rate-limit.requests`, `.window`, `.max-clients`). Deployment invariant: the Gateway must not be exposed directly to untrusted clients and must not trust forwarded headers. Behind a proxy every caller shares the proxy address, and when the tracked-address map is full a new address is refused, so a flood of distinct addresses can lock out the FEC's token issuance (the FEC then falls back to its cached token until it expires). After a failed issue the FEC waits 5 s (`ServiceTokenSource.FAILURE_BACKOFF`) before asking again; meanwhile calls use the still-valid cached token or fail at once.

### API clients

An API client is a `Client` record (Granite.Client) with a `clientId` (a prefix, `gc_` unless `granite.client.id-prefix` sets another, plus 32 lowercase base32 characters), a secret and catalog roles. Admins create Clients in the Clients grid (the secret is shown once); a `ROLE_CLIENT_ADMIN` owner rotates the secret of an owned Client in ClientHost. Authorities are catalog roles only, never `ROLE_ADMIN`, never `SCOPE_*`. Secrets are stored as SHA-256 only and never logged.

- Token endpoint: `POST /session-command-service/oauth2/token` (tokenless at Session, public at the Gateway in every environment, `application/x-www-form-urlencoded`). `grant_type=client_credentials`, credentials either as `Authorization: Basic base64(client_id:client_secret)` or as the form fields `client_id` and `client_secret`, not both; `scope` is ignored. Any query string is rejected with 400 `invalid_request`. Success is 200 `{"access_token","token_type":"Bearer","expires_in"}` with `Cache-Control: no-store` and `Pragma: no-cache`. Errors are JSON `{"error"}`: `invalid_request` 400, `unsupported_grant_type` 400, `invalid_client` 401 (with `WWW-Authenticate: Basic realm="granite"`, for every credential failure: unknown, disabled, expired or wrong secret), `temporarily_unavailable` 503 (Client service unreachable or slower than `granite.session.client-token.verify-timeout`, default `PT5S`).
- Token: `sub` = `client:<clientId>`, the Client's authorities, random `jti`, the session `iss`/`aud`, no session record, lifetime `granite.session.client-token.lifetime` (default `PT15M`; startup fails above `PT1H`). The Gateway skips the revocation lookup for `client:` subjects.
- Rate limit: two Gateway limiters guard the token endpoint, both 429 over the limit and both in memory per Gateway instance. (1) Per remote socket address (`granite.gateway.client-token.rate-limit.requests` default 600, `.window` `PT1M`, `.max-clients` 10000), forwarded headers untrusted, with the same deployment invariant as `issue-service-token`. Behind the production Ingress (ingress-nginx) all callers share one remote address, so this is a global ceiling, not a per-caller limit. (2) Per client_id (`granite.gateway.client-token.per-client-rate-limit.requests` default 10, `.window` `PT1M`, `.max-clients` 10000), keyed on the client_id of the Basic header or the `client_id` form field; requests with no parsable client_id share one `unknown` key. It isolates clients from each other. Client ids and secrets are never logged.
- What a Client may reach: the Gateway refuses Client tokens (403, in every environment) on `/frontendcontroller-*/**`, `/user-*-service/**`, `/session-*-service/**`, `/client-*-service/**` and `/outbox-service/**` (overridable `getClientDeniedPathPatterns(Environment)`), except `POST /client-command-service/rotate-own-client-secret`, which needs `ROLE_CLIENT_ADMIN` among the Client's authorities. Everywhere else the existing rules apply. In `production` Granite's own service paths are `SCOPE_internal` only, so a Client reaches only the application paths that the application's Gateway authorizes for its roles in `configureApplicationAuthorization` and publishes in its production Ingress overlay. Granite ships no Client-callable path.
- Rotation: a Client rotates its own secret with `POST /client-command-service/rotate-own-client-secret` (empty JSON body; the Client comes from the token `sub`). The reply carries `clientId` and the new `clientSecret`; `secretExpiresAt` is unchanged, the old secret stops working at once and tokens already issued live until they expire. An expired secret cannot get a token, so rotate before `secretExpiresAt` (an admin or owner can rotate afterwards in the UI).

```sh
curl -s https://granite.example/session-command-service/oauth2/token \
  -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d grant_type=client_credentials
# {"access_token":"eyJ...","token_type":"Bearer","expires_in":900}

curl -s https://granite.example/poplar-orders-service/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN"

curl -s -X POST https://granite.example/client-command-service/rotate-own-client-secret \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" -d '{}'
# {"success":true,"clientId":"gc_...","clientSecret":"..."}
```

Example only, not part of Granite: an application (here with a hypothetical `/poplar-orders-service`) authorizes a path for a role in its Gateway and publishes it in its production overlay. A Client holding `ROLE_POPLAR_READER` can then call it with its token.

```java
@Override
protected ServerHttpSecurity.AuthorizeExchangeSpec
configureApplicationAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges,Environment environment)
{
    return
        exchanges
            .pathMatchers(HttpMethod.GET,"/poplar-orders-service/orders/**")
                .hasAnyAuthority("ROLE_POPLAR_READER","ROLE_ADMIN","SCOPE_internal");
}
```

```yaml
patches:
  - target:
      kind: Ingress
      name: poplar-ingress
    patch: |-
      - op: add
        path: /spec/rules/0/http/paths/2
        value:
          path: /poplar-orders-service/orders
          pathType: Prefix
          backend:
            service:
              name: poplar-gateway-servicehost
              port:
                number: 443
```

A path that matches a denied pattern (for example one under `/user-*-service/`) stays closed to Clients whatever the rule says, and the application's own route must exist in its Gateway (`configureApplicationRoutes`).

## Several applications in one browser

Several Granite-derived applications (Granite, Poplar, ...) can run in one browser with Okta single sign-on and separate application sessions.

- One hostname per application, local development included (`granite.localhost`, `poplar.localhost`; never plain `localhost` with different ports, and never several applications under paths of one host). Cookies are host-scoped, so a shared host would make the applications overwrite each other's cookies.
- Cookie names come from `granite.cookies.prefix` (default `GRANITE`, `[A-Z0-9_]+`, startup fails otherwise), read by the FrontEndController, the Gateway and the web app (`window.__GRANITE_CONFIG__.cookiePrefix`, `COOKIE_PREFIX`): `__Host-<PREFIX>_SID` (session handle), `__Host-<PREFIX>_DID` (device), `__Host-<PREFIX>_LOGIN` (Okta login transaction) and `__Host-<PREFIX>-XSRF-TOKEN`. All are `Secure`, `Path=/` and have no `Domain`. The `__Host-` prefix makes the browser refuse such a cookie when another subdomain tosses it or when it carries a `Domain`. Give each application its own prefix (for example `POPLAR`) so they are easy to tell apart in the dev tools. Every component of one application must use the same prefix, or login fails silently. Users log in once after the rollout, because the old names are ignored.
- Logout is application-level only: it closes the application session and clears its cookies, and never ends the Okta session, so a following Okta login is silent. There is no setting for a global Okta logout.
- Each application sets its own JWT issuer and audience, for example Poplar `poplar-session` / `poplar-services`, so a token cannot be replayed against another application; the properties and the production requirement are under "Security split and call routing".
- Accepted limit: an Okta logout or session end elsewhere does not propagate to application sessions. They end at their own expiry or logout, so the exposure is bounded by the session lifetime. Back-channel logout can be revisited if Okta Universal Logout becomes available.

Rationale and alternatives: `docs/design/2026-10-30-multi-application-browser-analysis.md`.

## Database

Every service (User, Session, Outbox and the services added after them) shares one PostgreSQL database per environment: `local` `jdbc:postgresql://host.docker.internal:5432/granite-db-local`, `development` `jdbc:postgresql://granite-db-dev:5432/granite-db-dev`, `test` `…//granite-db-test:5432/granite-db-test`, `production` `…//granite-db-prod:5432/granite-db-prod`, with `${DB_USERNAME}`/`${DB_PASSWORD}` outside `local`. Because the outbox tables live in that one database, the single Outbox host relays every service's events. `test` and `production` use `ddl-auto=validate` (no migration tool), so new tables must exist before rollout. Outbox reads Kafka settings from `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SECURITY_PROTOCOL` (default `SASL_SSL`), `KAFKA_API_KEY`, `KAFKA_API_SECRET`, `KAFKA_SCHEMA_REGISTRY_URL`, `KAFKA_SCHEMA_REGISTRY_API_KEY` and `KAFKA_SCHEMA_REGISTRY_API_SECRET` outside `local` (optional entries of `granite-secrets` in the deploy scripts).

## Kubernetes network policies

- No overlay deploys a NetworkPolicy; they were removed because kindnet enforced them unreliably. Every pod in the namespace can reach every service port, so routing through the Gateway is a convention backed only by the hosts' JWT check, not by the network. The tokenless Session Command endpoints (resolve-session, login, refresh, issue-service-token) are reachable from any pod in the cluster.
