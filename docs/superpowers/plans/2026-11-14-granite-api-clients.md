# Granite API Clients Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Machine callers authenticate with a Granite-generated key and secret, receive a short-lived Session JWT through an OAuth2 client-credentials endpoint, and call services through the Gateway, authorized by their stored role-catalog authorities; Admin manages Clients in a Clients view in AppHost, and ClientAdmin Users rotate the secrets of the Clients they own in a separate web app, ClientHost, whose sessions are bound to it.

**Architecture:** A new microservice `Granite.Client` (cloned from `Granite.Feedback`) stores Clients, verifies credentials and rotates secrets. Session's new `oauth2/token` endpoint verifies through the Gateway with its service token and mints `sub=client:<id>` tokens. The Gateway routes the service, rate-limits the token endpoint, skips revocation for `client:` tokens and keeps Client tokens off FEC and identity paths. The FEC adds typed Clients operations and the web adds `granite.web.client/Clients`.

**Tech Stack:** Java 23, Spring Boot 3.4 / Spring Security 6 (servlet hosts, WebFlux Gateway), JPA/Hibernate + PostgreSQL (H2 in tests), Strata frameworks, Avro + Outbox + Kafka, React 19 / TypeScript / Mantine 7 / ag-grid 33 / Vitest, Kustomize.

**Spec:** `docs/superpowers/specs/2026-11-14-granite-api-clients-design.md`

## Global Constraints

- Identity type name `Client`; service `Microservices/Granite.Client`, group `granite.client`, packages `granite.client.{service,server,client}`, paths `/client-command-service`, `/client-query-service`, ports CommandHost 8097, QueryHost 8098, images `granite-client-commandhost`, `granite-client-queryhost`.
- Core role `ApplicationRolesManager.CLIENT_ADMIN = "ROLE_CLIENT_ADMIN"` (label `ClientAdmin`), in every catalog, implies nothing, implied by nothing, reserved like `ROLE_ADMIN`.
- Client authorities: catalog roles only, never `ROLE_ADMIN`, never `SCOPE_*`; stored closed under the hierarchy; an added role whose expansion contains `ROLE_ADMIN` is rejected.
- `clientId` = `gc_` + 32 lowercase base32 chars (`a-z2-7`), 20 random bytes. Secret = 32 `SecureRandom` bytes, base64url no padding. Stored as SHA-256 only; constant-time compare; unknown id compared against a fixed dummy hash.
- Token endpoint `POST /session-command-service/oauth2/token`, form-encoded, `grant_type=client_credentials`, Basic or `client_id`/`client_secret` form fields (not both); reply `{access_token, token_type:"Bearer", expires_in}` with `Cache-Control: no-store` and `Pragma: no-cache`; errors `invalid_request` 400, `unsupported_grant_type` 400, `invalid_client` 401 + `WWW-Authenticate: Basic realm="granite"`, `temporarily_unavailable` 503.
- Client token: `sub=client:<clientId>`, authorities minus `SCOPE_*` and `ROLE_ADMIN`, random `jti`, session `iss`/`aud`, lifetime `granite.session.client-token.lifetime` default `PT15M`, startup fails above `PT1H`. No session record.
- Secret expiry: UI date → stored `Instant` at start of the following day UTC; valid while `now < secretExpiresAt`; null = no expiry.
- Never log or emit secrets, hashes, Authorization headers or tokens. Every DTO carrying a secret overrides `toString` to omit it (as `IssueServiceTokenRequest` does). Domain events carry no secret or hash.
- Applications: `granite` (AppHost, default) and `client` (ClientHost); `granite.session.applications` default `granite,client`; session and Okta transaction column `application` `VARCHAR(32)`; JWT claim `app`; `granite.session.login.applications.client.required-authorities` default `ROLE_CLIENT_ADMIN`; `ROLE_CLIENT_ADMIN` does not qualify a `granite` login; `granite.session.okta.redirect-uris.<app>` (granite falls back to `granite.session.okta.redirect-uri`); `app=client` sessions reach only `getClientApplicationPathPatterns`; tokens without `app` count as `granite`.
- ClientHost: `FrontEnds/Granite.Web/Applications/Granite.Web.ClientHost`, image `granite-web-clienthost`, `config.js` `application: "client"`, dev port 5174, hosts `client.granite.localhost`, `client.granite-dev.__domain__`, `client.granite-test.__domain__`, `client.granite.__domain__`.
- Ownership checks live in the Client service (data rules); who reaches which path is decided only in the Gateway. The FEC fills owner ids from the verified caller (`SessionRequestContext.getCallerUserId()`), never from the payload.
- Build and test only with existing Gradle tasks (from the repo root composite build, `--rerun`) and yarn scripts; no daemon start/stop, no tooling changes. `jib`/`containerize`/`push` only in Task 13. Never `--dry-run`.
- Style: `//////` headers/footers, Allman braces, return type on its own line, fluent setters, no explanatory comments, no static inner classes; TypeScript `Array<T>`; build output only in `.build`.
- Commit after each task with explicit paths; never `.idea/**`, `dist/**`, `.gradle/**`, `executionHistory`, or snapshot-version bumps in `package.json`. Messages end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.

## Review Focus

1. **Secret leaks through `toString` or logs.** A reply or request carrying `clientSecret` is logged by a controller's debug line or an exception. Test: Task 2 `secretBearingDtosOmitSecretFromToString`, Task 9 `clientsOutputToStringOmitsSecret`.
2. **A ClientAdmin rotates a Client they do not own** by sending another `clientId` to `handle-owned-client-secret-input`. Expect the same `not found` as an unknown id and no change. Test: Task 3 `rotateOwnedRejectsNonOwnerLikeUnknown`, Task 9 `ownedRotateUsesVerifiedCallerNotPayload`.
3. **Client token reaches user-only paths** (Preferences via the authenticated matcher, FEC Users, Session logout). Expect 403. Test: Task 8 `clientTokenRefusedOnFrontEndAndIdentityPaths`.
4. **Token request edge inputs**: both Basic and form credentials, malformed Basic, wrong grant type, JSON body. Expect the RFC error codes, never 500. Test: Task 7 `tokenEndpointErrorCases`.
5. **Secret expiry boundary and disabled clients**: verification at `secretExpiresAt - 1ms` succeeds, at `secretExpiresAt` fails; disabled fails with the same reason as unknown. Test: Task 3 `verifyHonoursExpiryBoundaryAndEnabled`.
6. **ClientHost session used against admin paths**: an Admin who also holds `ROLE_CLIENT_ADMIN` signs in on ClientHost and replays the session against `handle-users-input`. Expect 403. Test: Task 8 `clientApplicationSessionConfined`.
7. **ClientAdmin-only User signs in on AppHost** (password or Okta). Expect the generic login refusal. Test: Task 7a `graniteLoginRefusesClientAdminOnly`.

## Model per task

Sonnet for Tasks 1–11 (Java/TS code against exact interfaces); Haiku for Task 12 (docs) only after Sonnet output is reviewed; Task 13 is run by the orchestrator. No model above Opus 4.6.

---

### Task 0: Branch and design documents

- [ ] **Step 1:** `git checkout development && git checkout -b feature/granite-api-clients`.
- [ ] **Step 2: Commit** the spec and this plan: `git add docs/superpowers/specs/2026-11-14-granite-api-clients-design.md docs/superpowers/plans/2026-11-14-granite-api-clients.md && git commit -m "docs: API clients design and plan"`.

### Task 1: Core role ROLE_CLIENT_ADMIN

**Files:**
- Modify: `Shared/Granite.Foundation/Components/Granite.Foundation.Core/src/main/java/granite/foundation/core/security/ApplicationRolesManager.java`
- Test: `Shared/Granite.Foundation/Tests/Granite.Foundation.CoreTest/src/test/java/granite/foundation/core/security/ApplicationRolesManagerTest.java`

**Interfaces:**
- Produces: `public static final String CLIENT_ADMIN = "ROLE_CLIENT_ADMIN"`; core `RoleDefinition(CLIENT_ADMIN,"ClientAdmin",Set.of())` added after `ADMIN`; reserved-name, must-not-imply and default-role checks cover both core roles.

- [ ] **Step 1: Write failing tests** `clientAdminIsCoreRole` (`isRole("ROLE_CLIENT_ADMIN")`, in `getRoleNames()` of `new GraniteApplicationRolesManagerSupplier().get()`), `clientAdminIsReserved` (app role named `ROLE_CLIENT_ADMIN` → `IllegalArgumentException`), `clientAdminCannotBeImplied`, `clientAdminCannotBeDefault`, `clientAdminExpandsToItselfOnly` (`expand(Set.of(CLIENT_ADMIN))` equals `{CLIENT_ADMIN}`).
- [ ] **Step 2: Run** `./gradlew :Granite.Foundation:Tests:Granite.Foundation.CoreTest:test --rerun`; expected FAIL (symbol missing).
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** the same command plus `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test :Granite.User:Tests:Granite.User.ServerTest:test --rerun`; expected PASS (no catalog test assumed a single core role; fix any that did by adding the new role to the expected list).
- [ ] **Step 5: Commit** `feat(foundation): core role ROLE_CLIENT_ADMIN`.

### Task 2: Granite.Client scaffold and service contract

Clone `Microservices/Granite.Feedback` structure (build files, gradle wrapper, `.gitignore`, workflow, hosts, components, tests) renamed to Client; this task keeps only the contract and builds.

**Files:**
- Create: `Microservices/Granite.Client/{build.gradle,settings.gradle,package.json,gradle.properties,gradlew,gradlew.bat,.gitignore,.gradle/wrapper/*}` (from Feedback, renamed)
- Create: `Components/Granite.Client.Service/src/main/java/granite/client/service/requestreply/*`, `.../service/event/{IClientEventSender,IClientEventReceiver,IClientEventListener}.java`, `src/main/schema/avro/ClientEvent.avsc` (same fields as `FeedbackEvent.avsc`, namespace `granite.client.service.event`), `src/main/typescript/RequestReply/*.ts` + `index.ts`, package `granite.client.service`
- Modify: root `settings.gradle` (`includeBuild('Microservices/Granite.Client')` with substitution of `granite.client:granite-client-{service,client,server}`), root `package.json` (`clean-granite-client`, `deep-clean-granite-client`, `build-granite-client`, added to the `*-microservices` chains after `build-granite-user`)
- Test: `Tests/Granite.Client.ServiceTest/src/testFixtures/java/granite/client/service/requestreply/ClientServiceTest.java` (abstract fixture, filled in Task 3), `Tests/Granite.Client.ServerTest/src/test/java/granite/client/service/requestreply/ClientDtoToStringTest.java`

**Interfaces (Produces, Java and TS mirror):**
- `ClientData {clientId, name, description, authorities: Set<String>, owners: Set<Long>, enabled: boolean, secretExpiresAt: Instant, lastTokenIssuedAt: Instant, createdAt: Instant, updatedAt: Instant, version: Long}`
- `CreateClientData {name, description, authorities, owners, enabled, secretExpiresAt}`; `UpdateClientData {clientId, name, description, authorities, owners, enabled, secretExpiresAt, version}`
- Requests/replies (each reply `success`, `failureMessage` like Feedback): `CreateClientRequest{client: CreateClientData}` → `CreateClientReply{client, clientSecret}`; `UpdateClientsRequest{clients: List<UpdateClientData>}` → `UpdateClientsReply{clients}`; `RotateClientSecretRequest{clientId, secretExpiresAt}` → `RotateClientSecretReply{client, clientSecret}`; `RotateOwnedClientSecretRequest{clientId, ownerUserId: Long}` → `RotateClientSecretReply`; `RotateOwnClientSecretRequest{}` → `RotateOwnClientSecretReply{clientId, clientSecret}`; `DestroyClientsRequest{clientIds: List<String>}` → `DestroyClientsReply{clientIds}`; `VerifyClientCredentialsRequest{clientId, clientSecret}` → `VerifyClientCredentialsReply{valid, clientId, authorities}`; `FindAllClientsRequest` / `FindClientsByIdsRequest{clientIds}` / `FindClientsByOwnerRequest{userId: Long}` → `FindClientsReply{clients}`.
- `IClientCommandService`: `CompletionStage<CreateClientReply> createClient(CreateClientRequest)`, `updateClients`, `rotateClientSecret`, `rotateOwnedClientSecret`, `rotateOwnClientSecret`, `destroyClients`, `verifyClientCredentials`. `IClientQueryService`: `findAllClients`, `findClientsByIds`, `findClientsByOwner`.

- [ ] **Step 1: Write failing test** `secretBearingDtosOmitSecretFromToString`: for `CreateClientReply`, `RotateClientSecretReply`, `RotateOwnClientSecretReply`, `VerifyClientCredentialsRequest` with secret `"s3cr3t-value"`, `toString()` does not contain it.
- [ ] **Step 2: Run** `./gradlew :Granite.Client:Tests:Granite.Client.ServerTest:test --rerun`; expected FAIL (compile).
- [ ] **Step 3: Implement** the contract and scaffold.
- [ ] **Step 4: Run** the Step 2 command (PASS) and `yarn run build-granite-client` from the repo root (TS service package builds).
- [ ] **Step 5: Commit** `feat(client): Granite.Client service contract`.

### Task 3: Client domain and command service

**Files:**
- Create in `Components/Granite.Client.Server/src/main/java/granite/client/server/`: `domain/{Client,IClientRepository,ClientRepository,AbstractClientEvent,ClientCreated,ClientUpdated,ClientDestroyed,IClientEvent,IClientEventObserver,IClientEventSource,DomainConfiguration}.java`, `application/{ClientCommandService,ITransactionalClientCommandService,ClientCredentials,ClientAuthorityPolicy,ClientSecretExpiry,MappingModule,ApplicationConfiguration}.java`; `src/main/resources/Client.orm.xml` (table `Client`; `authorities` and `owners` as `VARCHAR(1024)` basic columns with converters like User's `authorities`)
- Test: `Tests/Granite.Client.ServerTest/src/test/java/granite/client/server/application/{ClientCommandServiceTest,TestConfiguration}.java`, `.../domain/ClientTest.java`, test resources as Feedback's (`spring.jpa.mapping-resources=Client.orm.xml,OutboxEvent.orm.xml`)

**Interfaces:**
- Consumes: Task 2 contract, `ApplicationRolesManager`.
- Produces: `ClientCredentials` with `static String newClientId()`, `static String newSecret()`, `static byte[] hash(String)`, `static boolean matches(byte[] storedHash,String secret)` (constant time); `ClientAuthorityPolicy(ApplicationRolesManager)` with `Set<String> validateAdded(Set<String> previous,Set<String> requested)` (throws `IllegalArgumentException("unknown role: X" | "role not allowed for clients: X")`, returns expanded set) and `Set<String> mintable(Set<String> stored)` (catalog ∩ stored minus `ROLE_ADMIN`, `SCOPE_*`); `ClientSecretExpiry.fromDate(LocalDate)` → start of next day UTC; `IClientRepository.recordTokenIssued(String clientId,Instant at)` as a direct JPQL update that does not touch `version`. `ClientCommandService(…, ApplicationRolesManager, Clock, Supplier<Optional<String>> callerSubject)`; `rotateOwnClientSecret` resolves the client from `callerSubject` (`client:<id>`), anything else → `failureMessage "not found"`.

- [ ] **Step 1: Write failing tests** in `ClientCommandServiceTest`: `createGeneratesIdAndSecretAndStoresOnlyHash` (id matches `gc_[a-z2-7]{32}`, secret 43 chars, verify with it valid, `ClientData` has no secret field), `createRejectsAdminScopeAndUnknownRoles`, `updateChecksOnlyNewlyAddedRoles`, `rotateInvalidatesOldSecret`, `rotateOwnedRejectsNonOwnerLikeUnknown` (non-owner and unknown id → identical `failureMessage "not found"`, secret unchanged), `rotateOwnUsesCallerSubject` (subject `client:<id>` rotates; `service:x` and `1001` → `not found`), `verifyHonoursExpiryBoundaryAndEnabled` (fixed `Clock`: `expiresAt-1ms` valid, `expiresAt` invalid; disabled invalid; all invalid replies identical), `verifyMintsOnlyCurrentCatalogRoles` (stored role no longer in catalog is dropped), `verifyRecordsLastTokenIssuedWithoutVersionBump`, `eventsCarryNoSecret` (sent `ClientEvent` payloads contain neither secret nor hex/base64 of its hash), `destroyRemovesClients`. `ClientTest`: `secretExpiryFromDateIsStartOfNextDayUtc`.
- [ ] **Step 2: Run** `./gradlew :Granite.Client:Tests:Granite.Client.ServerTest:test --rerun`; expected FAIL.
- [ ] **Step 3: Implement** (`name` unique, ≤100; `description` ≤1000; failures as `success=false` replies, never exceptions to the caller).
- [ ] **Step 4: Run** Step 2 command; expected PASS.
- [ ] **Step 5: Commit** `feat(client): Client domain and command service`.

### Task 4: Query service, platform, entry configurations, hosts and clients

**Files:**
- Create: `application/{ClientQueryService,ITransactionalClientQueryService}.java`; `platform/{ClientCommandServiceController,ClientQueryServiceController,ClientEventToOutboxEventMapper,IClientEventToOutboxEventMapper,OutboxEventClientEventSender,PlatformConfiguration,CommandPlatformConfiguration,QueryPlatformConfiguration,JwtCallerSubject}.java`; `configuration/{AbstractGraniteClientServerConfiguration,GraniteClientCommandServerConfiguration,GraniteClientQueryServerConfiguration}.java`
- Create: `Applications/Granite.Client.{CommandHost,QueryHost}` (packages `granite.client.{commandhost,queryhost}.main`, `ClientCommandHost`/`ClientQueryHost`, `HostConfiguration`, properties for local/development/test/production with ports 8097/8098 and context paths, jib images as Feedback's)
- Create: `Components/Granite.Client.Client` Java `granite.client.client.requestreply.{IClientCommandServiceClient,ClientCommandServiceClient,IClientQueryServiceClient,ClientQueryServiceClient}`, `granite.client.client.event.{ClientKafkaEventReceiver,ClientKafkaAvroEventReceiver}`, TS mirror, package `granite.client.client`
- Create: `Tests/Granite.Client.ClientTest/.../ClientServiceClientTest.java` (`@Tag("IntegrationStage")`), `Tests/Granite.Client.TestSuite/.../ClientTestSuite.java`
- Test: `Tests/Granite.Client.ServerTest/src/test/java/granite/client/server/{application/ClientQueryServiceTest,platform/ClientSecurityChainTest,platform/ClientControllerMappingTest}.java`

**Interfaces:**
- Produces: controller endpoints `/create-client`, `/update-clients`, `/rotate-client-secret`, `/rotate-owned-client-secret`, `/rotate-own-client-secret`, `/destroy-clients`, `/verify-client-credentials`, `/find-all-clients`, `/find-clients-by-ids`, `/find-clients-by-owner` (POST JSON, same shape as Feedback). `JwtCallerSubject implements Supplier<Optional<String>>` reading the verified `Jwt` subject from the security context. Seams on `AbstractGraniteClientServerConfiguration`: `configuration(Environment)`, `mapper()`/`configureMapper`, `applicationRoles()` + `applicationRolesStartupLog`, `clock()`, `requestLoggingFilter()` (query string only), `openApi()`, `securityFilterChain(...)` (`GraniteJwtCheckSecurity.jwtCheckChain` `REQUIRED`), `jwtDecoder(...)`; `GraniteClientCommandServerConfiguration.clientEventSender(IOutboxEventRepository)`. Java clients take `(CountedClient,Executor,String baseUrl)` like the FEC's User clients and a JAX-RS `ClientBuilder` constructor like Session's.

- [ ] **Step 1: Write failing tests**: `findClientsByOwnerOrdersByName`, `findAllOmitsSecretHash` (serialized JSON of `FindClientsReply` has no `secret` substring); `ClientSecurityChainTest.requiresBearerExceptHealth` (401 without token on `/client-command-service/create-client`, health open), `rotateOwnReadsSubjectFromJwt`; `ClientControllerMappingTest` (every `IClientCommandService`/`IClientQueryService` method has a mapping).
- [ ] **Step 2: Run** `./gradlew :Granite.Client:Tests:Granite.Client.ServerTest:test --rerun`; expected FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** Step 2 command, `./gradlew :Granite.Client:Applications:Granite.Client.CommandHost:compileJava :Granite.Client:Applications:Granite.Client.QueryHost:compileJava :Granite.Client:Components:Granite.Client.Client:compileJava --rerun`, and `yarn run build-granite-client`; expected PASS.
- [ ] **Step 5: Commit** `feat(client): query service, hosts and clients`.

### Task 5: Outbox relays Client events

**Files:** `Microservices/Granite.Outbox/Components/Granite.Outbox.Server/build.gradle` (`api "granite.client:granite-client-service:$version"`), `.../granite/outbox/server/client/{ClientEventOutboxEventReceiver,KafkaAvroClientEventSender}.java`, Outbox ServiceHost `HostConfiguration` (map entry `ClientEvent` → topic `granite.clientevent.avro`), Outbox ServerTest `build.gradle` + a test mirroring the Feedback one.

- [ ] **Step 1: Write failing test** `clientEventIsRelayedToClientTopic`, copied from the Feedback relay test.
- [ ] **Step 2: Run** `./gradlew :Granite.Outbox:Tests:Granite.Outbox.ServerTest:test --rerun`; expected FAIL.
- [ ] **Step 3: Implement** like `granite.outbox.server.feedback`.
- [ ] **Step 4: Run** Step 2 command plus `./gradlew :Granite.Outbox:Applications:Granite.Outbox.ServiceHost:compileJava --rerun`; PASS.
- [ ] **Step 5: Commit** `feat(outbox): relay Client events`.

### Task 6: Session calls Client through the Gateway

**Files:** `GraniteSessionCommandServerConfiguration.java` (beans `IClientCommandServiceClient clientCommandServiceClient(ObjectProvider<SslBundles>,Environment,IServiceTokenProvider)` with `granite.client-command-service.host`, built through `createClientBuilder`), Session Server `build.gradle` (`granite.client:granite-client-client`), Session CommandHost `application-{local,development,test,production}.properties` (`granite.client-command-service.host=https://granite-gateway-servicehost:443`), `PlatformContextTestConfiguration` (Mockito mock of the new client).

- [ ] **Step 1: Write failing test** `GraniteSessionCommandServerConfigurationTest.clientCommandClientTargetsGatewayWithServiceToken`.
- [ ] **Step 2–4: Run** `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun` (FAIL → implement → PASS).
- [ ] **Step 5: Commit** `feat(session): Client command client through the Gateway`.

### Task 7: Session OAuth2 token endpoint

**Files:**
- Create: `Session.Server/.../platform/ClientTokenController.java` (`@RestController @RequestMapping("/session-command-service")`, `@PostMapping(value="/oauth2/token", consumes=APPLICATION_FORM_URLENCODED_VALUE, produces=APPLICATION_JSON_VALUE) CompletionStage<ResponseEntity<Map<String,Object>>> token(@RequestHeader(value="Authorization",required=false) String authorization, @RequestParam MultiValueMap<String,String> form)`), `.../application/{IClientTokenIssuer,ClientTokenIssuer,ClientTokenResult}.java`
- Modify: `GraniteSessionCommandServerConfiguration` (bean `clientTokenIssuer(IJwtTokenService,IClientCommandServiceClient,Environment)`; `getTokenlessPathPatterns` adds `/session-command-service/oauth2/token`; startup check of `granite.session.client-token.lifetime` ≤ `PT1H`), the platform configuration that registers controllers.
- Test: `Session/Tests/Granite.Session.ServerTest/.../configuration/ClientTokenEndpointTest.java`, `.../application/ClientTokenIssuerTest.java`

**Interfaces:**
- Consumes: `IClientCommandServiceClient.verifyClientCredentials` (Task 4), `IJwtTokenService.mintServiceToken(String subject,Collection<String> authorities,Duration lifetime)`.
- Produces: `ClientTokenIssuer.issue(String clientId,String clientSecret)` → `CompletionStage<ClientTokenResult>` (`ok(token,expiresInSeconds)`, `invalidClient()`, `unavailable()`); authorities passed to `mintServiceToken` are filtered to drop `SCOPE_*` and `ROLE_ADMIN`. Basic parsing: base64-decode, split at the first `:`, URL-decode both parts (RFC 6749 §2.3.1).

- [ ] **Step 1: Write failing tests**: `issuesClientTokenWithSubjectAuthoritiesAndLifetime` (decode with `GraniteJwtDecoderFactory`: `sub=client:gc_…`, authorities exact, `exp-iat=900`, no `did`), `filtersAdminAndScopesDefensively`, `tokenEndpointErrorCases` (missing grant → `invalid_request` 400; `grant_type=password` → `unsupported_grant_type` 400; Basic and form both → `invalid_request`; malformed Basic → `invalid_request`; JSON body → 400/415 never 500; invalid credentials → 401 `invalid_client` with `WWW-Authenticate: Basic realm="granite"`; client mock fails → 503 `temporarily_unavailable`), `successHasNoStoreHeaders`, `tokenEndpointIsTokenless` (no bearer passes the security chain), `lifetimeAboveOneHourFailsStartup`.
- [ ] **Step 2: Run** `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun`; FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** Step 2 command; PASS.
- [ ] **Step 5: Commit** `feat(session): OAuth2 client-credentials token endpoint`.

### Task 7a: Application-bound sessions in Session

**Files:**
- Modify: `Session.Server/.../domain/{Session,OktaLoginTransaction}.java` + their ORM mapping (`application`, default `granite`), `.../application/{JwtTokenService,SessionCommandService,CatalogRoleLoginGate,RequiredAuthoritiesLoginGate,StartOktaLoginContext}.java`, `GraniteSessionCommandServerConfiguration` (`loginGate` becomes `IApplicationLoginGate applicationLoginGate(Environment,ApplicationRolesManager)`; Okta redirect per app), Session.Service `LoginRequest`, `StartOktaLoginRequest`, `SessionData` (+`application`, Java and TS), Session CommandHost `application-*.properties` (`granite.session.okta.redirect-uris.client`: local `http://localhost:5174/login/callback`, others `${OKTA_REDIRECT_URI_CLIENT_<ENV>:}` / production `${OKTA_REDIRECT_URI_CLIENT:}`).
- Create: `.../application/{IApplicationLoginGate,ApplicationLoginGate}.java`.
- Test: `Session/Tests/Granite.Session.ServerTest/.../application/{ApplicationLoginGateTest,SessionApplicationBindingTest}.java`.

**Interfaces:**
- Produces: `IApplicationLoginGate.permits(String application,Collection<String> authorities)`; `application` on `LoginRequest`, `StartOktaLoginRequest` (null → `granite`; unknown → login failure `invalid application`); JWT claim `app` minted by `mint(Session)` and kept on refresh; `SessionData.application`.

- [ ] **Step 1: Write failing tests**: `graniteLoginRefusesClientAdminOnly` (password login, app `granite`, authorities `{ROLE_CLIENT_ADMIN}` → refused; `{ROLE_USER,ROLE_CLIENT_ADMIN}` → allowed), `clientLoginRequiresClientAdmin` (`{ROLE_ADMIN}` → refused on `client`), `unknownApplicationRefused`, `sessionTokenCarriesAppAndRefreshKeepsIt`, `oktaStartUsesPerApplicationRedirectAndCompletionReusesIt`, `missingApplicationDefaultsToGranite`.
- [ ] **Step 2: Run** `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test --rerun`; FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** Step 2 command; PASS (existing login and Okta tests unchanged apart from the new field).
- [ ] **Step 5: Commit** `feat(session): sessions bound to the web application`.

### Task 8: Gateway support

**Files:** `GraniteGatewayServerConfiguration.java`, `.../security/{SessionQueryRevocationChecker,ClientTokenPathWebFilter}.java`, ServiceHost `application-{local,development,test,production}.properties` (`granite.client-{command,query}-service.host=https://granite-client-{commandhost,queryhost}:443`), Gateway tests.

**Interfaces:**
- Produces: `public static final String CLIENT_TOKEN_PATH = "/session-command-service/oauth2/token"`, `ROTATE_OWN_CLIENT_SECRET_PATH = "/client-command-service/rotate-own-client-secret"`; `SessionQueryRevocationChecker.CLIENT_SUBJECT_PREFIX = "client:"` skipped like `service:`; `protected List<String> getClientDeniedPathPatterns(Environment)` default `/frontendcontroller-*/**`, `/user-*-service/**`, `/session-*-service/**`, `/client-*-service/**`, `/outbox-service/**`; `ClientTokenPathWebFilter(ServerWebExchangeMatcher denied,ServerWebExchangeMatcher allowed)` placed at `SecurityWebFiltersOrder.AUTHORIZATION` − 1, 403 when the authenticated `Jwt` subject starts with `client:` and the path is denied and not `ROTATE_OWN_CLIENT_SECRET_PATH`; `protected RateLimitWebFilter createClientTokenRateLimitWebFilter(Environment)` (`granite.gateway.client-token.rate-limit.{requests,window,max-clients}`, defaults 10, `PT1M`, 10000); `CLIENT_TOKEN_PATH` added to public paths in every environment; routes `client-command-route`, `client-query-route`; rules: `POST ROTATE_OWN_CLIENT_SECRET_PATH` → `ROLE_CLIENT_ADMIN` before the production `SCOPE_internal` block; production block adds `/client-query-service/**`, `/client-command-service/**`; readers get `/client-query-service/**` and FEC `get-clients-output`; admins get `/client-command-service/**` and FEC `handle-clients-input`; FEC `get-owned-clients-output` and `handle-owned-client-secret-input` → `ROLE_CLIENT_ADMIN` + admins (both FEC services). `ApplicationSessionPathWebFilter(ServerWebExchangeMatcher allowed)` next to the Client-token filter: when the authenticated `Jwt` has claim `app=client` and the path is not allowed → 403; `protected List<String> getClientApplicationPathPatterns(Environment)` default `/frontendcontroller-user-service/get-owned-clients-output`, `/frontendcontroller-user-service/handle-owned-client-secret-input`; requests admitted by `getAuthenticatedExchangeMatcher` (current-session inputs such as logout) are also allowed.

- [ ] **Step 1: Write failing tests** (use `GatewayTestBackend`; add `signClientToken(String clientId,List<String> authorities)` with subject `client:<id>`, no `did`): `GatewayClientTokenTest.clientTokenSkipsRevocation`, `clientTokenRefusedOnFrontEndAndIdentityPaths` (403 on `/frontendcontroller-user-service/get-preferences-output`, `/frontendcontroller-user-service/get-users-output` even with `ROLE_AUDITOR`, `/session-command-service/logout`, `/user-query-service/x`, `/client-query-service/x`), `clientAdminClientMayRotateOwnSecret` (200 to backend in `local` and `production`), `clientTokenUsesApplicationRules` (test config authorizes `/poplar-service/**` for `ROLE_USER` in `configureApplicationAuthorization`; Client with `ROLE_USER` → 200), `tokenEndpointPublicAndRateLimited` (11th request in window → 429), `clientFrontEndRules` (reader may `get-clients-output`, not `handle-clients-input`; `ROLE_CLIENT_ADMIN` user may both owned operations and not `get-clients-output`), `clientApplicationSessionConfined` (session token with `app=client` and authorities `{ROLE_ADMIN,ROLE_CLIENT_ADMIN}`: owned operations and current-session logout pass, `handle-users-input`, `get-preferences-output` and `/user-query-service/x` → 403; same token without `app` → admin paths pass); add `GatewayTestBackend.sign(List<String> authorities,String application)`; update `GatewayProductionRoutesTest` for the new paths.
- [ ] **Step 2: Run** `./gradlew :Granite.Gateway:Tests:Granite.Gateway.ServerTest:test --rerun`; FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** Step 2 command and `./gradlew :Granite.Gateway:Applications:Granite.Gateway.ServiceHost:compileJava --rerun`; PASS.
- [ ] **Step 5: Commit** `feat(gateway): API client tokens, routes and rules`.

### Task 9: FrontEndController Clients operations

**Files:**
- Service (Java + TS `granite.frontendcontroller.service/RequestReply`): `ClientsInput {userId, source, command, destination, clientsToCreate: List<CreateClientData>, clientsToUpdate: List<UpdateClientData>, clientIdsToDelete: List<String>, clientIdToRotate: String, secretExpiresAt}`, `ClientsOutput {userId, source, command, destination, clients: List<ClientData>, roles: List<RoleData>, users: List<ClientOwnerData>, revealedClientId, revealedClientSecret, failureMessages}`, `ClientOwnerData {userId: Long, userName}`, `GetClientsOutputRequest/Reply`, `HandleClientsInputRequest/Reply`, `GetOwnedClientsOutputRequest/Reply`, `HandleOwnedClientSecretInputRequest/Reply` (input `OwnedClientSecretInput {userId, source, command, destination, clientId}`, output `ClientsOutput`); `IGraniteFrontEndControllerService` methods `getClientsOutput`, `handleClientsInput`, `getOwnedClientsOutput`, `handleOwnedClientSecretInput`; TS client methods of the same names.
- Server: `application/GraniteClientServiceClients.java` (memoized `Supplier` holder like `GraniteHelpServiceClients`: `setClientCommandClient`, `setClientQueryClient`, getters throwing when unavailable), `GraniteFrontEndControllerContext` gains `getClientServiceClients()`; `AbstractGraniteFrontEndControllerConfiguration` gains `@Bean` `clientCommandServiceClient()`/`clientQueryServiceClient()` (base URLs `client-command-service.base-url`, `client-query-service.base-url`), `protected createClientCommandClient()`, `createClientQueryClient()`, `createClientServiceClients()`; `control/client/{InitializeClientsView,UpdateClientsView,DeleteClientsView,RotateClientSecret,InitializeOwnedClientsView,RotateOwnedClientSecret,UnauthenticatedClientsOutput}.java`; action keys `"Client.All->ClientsEditableView"`, `"Client.All->Save->ClientsEditableView"`, `"Client.All->Delete->ClientsEditableView"`, `"Client.All->Rotate->ClientsEditableView"`, `"Client.Owned->OwnedClientsView"`, `"Client.Owned->Rotate->OwnedClientsView"`; controller mappings `/get-clients-output`, `/handle-clients-input`, `/get-owned-clients-output`, `/handle-owned-client-secret-input`; FEC host properties for both base URLs in every environment (Gateway URL); roles for the pick list = `RoleCatalogData.from(roles)` without `ROLE_ADMIN`; users from `IUserQueryServiceClient.findAllUsers`. Login: the FEC login inputs (password login and Okta start, Java + TS) gain `application` and pass it to Session `LoginRequest`/`StartOktaLoginRequest` (Task 7a); the Sessions view output carries `SessionData.application`.
- Test: `Tests/Granite.FrontEndController.ServerTest/.../application/GraniteFrontEndControllerServiceClientsTest.java`, `.../control/client/InitializeClientsViewTest.java`, `GraniteControllerMappingTest` (existing, must pass).

- [ ] **Step 1: Write failing tests**: `clientsViewListsClientsRolesWithoutAdminAndUsers`, `createRevealsSecretOnce` (output of Save with a create carries `revealedClientId`/`revealedClientSecret`; a following `get-clients-output` has neither), `rotateRevealsNewSecret`, `unauthenticatedCallerGetsFailureWithoutClientCall` (all four operations), `ownedViewUsesVerifiedCaller` (`findClientsByOwner(userId=caller)`), `ownedRotateUsesVerifiedCallerNotPayload` (payload `userId` 999 ignored; request to Client has `ownerUserId` = caller), `clientsOutputToStringOmitsSecret`, `loginForwardsApplication` (password and Okta start; missing → `granite`).
- [ ] **Step 2: Run** `./gradlew :Granite.FrontEndController:Tests:Granite.FrontEndController.ServerTest:test --rerun`; FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** Step 2 command, both FEC host `compileJava` tasks, and `yarn run build-granite-frontendcontroller`; PASS.
- [ ] **Step 5: Commit** `feat(frontendcontroller): Clients operations`.

### Task 10: Web Clients view

**Files:**
- Create: `FrontEnds/Granite.Web/Components/Granite.Web.Client` (copy of `Granite.Web.User` layout and scripts, name `granite.web.client`, deps add `granite.client.service`): `src/main/typescript/Clients/{ClientsView.tsx,SecretDialog.tsx,EnvFile.ts,index.ts}`, `src/main/typescript/Owned/{OwnedClientsView.tsx,index.ts}`, `src/main/typescript/EditableView/{ClientsEditableView.tsx,IClientsEditableViewProps.ts,ClientsChangeSet.ts,OwnersCellEditor.tsx,OwnersCellRenderer.tsx,SecretExpiryCellEditor.tsx,index.ts}`; reuse `AuthoritiesCellEditor`, `AuthoritiesCellRenderer`, `RoleCascade` by importing from `granite.web.user/EditableView`.
- Create: `FrontEnds/Granite.Web/Tests/Granite.Web.ClientTest` (copy of `Granite.Web.UserTest` setup, deps `granite.client.service`, `granite.web.client`).
- Modify: `FrontEnds/Granite.Web/package.json` (`clean-`, `deep-clean-`, `build-granite-web-client`; `build-components` order authentication, user, client, session, …; `test-granite-web-client`: `yarn run build-granite-web-authentication && yarn run build-granite-web-user && yarn run build-granite-web-client && cd Tests/Granite.Web.ClientTest && yarn run test`, added to `test-granite-web` after user), `Components/Granite.Web.Navigator/src/main/typescript/Navigation/{IMenuTab.ts,MenuBar.tsx}` (`IMenuTab.authorities?: Array<string>`; `{label:"Clients",path:"/clients",authorities:["ROLE_ADMIN","ROLE_AUDITOR","SCOPE_internal"]}` after Users, filtered with `hasAuthority`), `Applications/Granite.Web.AppHost/{package.json,src/App.tsx}` (`granite.web.client` dependency; route `clients`).
- Modify: `Components/Granite.Web.Authentication` — read `window.__GRANITE_CONFIG__.application` (default `"granite"`, exported `APPLICATION`) and send it as `application` in the password-login and Okta-start inputs; AppHost `config.js.template` gains `application: "granite"`.

**Interfaces:**
- Produces: `ClientsView({ service?, height = "100%", className?, style? })` from `granite.web.client/Clients` → full view with `getClientsOutput`/`handleClientsInput` (editing only when `canEdit(state)`). `OwnedClientsView({ service?, height = "100%", className?, style? })` from `granite.web.client/Owned` → `getOwnedClientsOutput`, Rotate through `handleOwnedClientSecretInput`, owners column hidden, read-only otherwise; used only by ClientHost (Task 10a). `EnvFile.ts`: `toEnvFile(clientId: string, secret: string): string` → `"GRANITE_CLIENT_ID=<id>\nGRANITE_CLIENT_SECRET=<secret>\n"`, `envFileName(name: string): string` → `<name>.env` with characters outside `[A-Za-z0-9._-]` replaced by `_`. `ClientsChangeSet.ts`: `toRows`, `computeChanges`, `validateRows` (name required, ≤100, unique), `toSecretExpiry(date: string | null): string | null` (ISO date → next day `T00:00:00Z`), `fromSecretExpiry(instant: string | null): string | null` (inverse).
- Columns: Name, Client ID (read-only, copy), Description, Authorities, Owners (admin only), Enabled, Secret expires, Last token issued, Created. Toolbar (admin): Add, Delete, Rotate secret (confirm), Save. `SecretDialog`: Client ID and secret with Copy buttons, warning "This secret will not be shown again.", Download .env, Close; secret held only in dialog state and cleared on close.

- [ ] **Step 1: Write failing tests** in `Tests/Granite.Web.ClientTest/src/test/typescript/`: `EnvFileTest.ts` (exact content; `envFileName("Partner A/B")` → `Partner_A_B.env`), `ClientsChangeSetTest.ts` (create/update/delete detection, duplicate and blank names, `toSecretExpiry("2026-12-31")` → `"2027-01-01T00:00:00Z"`, round trip), `ClientsAuthorityOptionsTest.ts` (`ROLE_ADMIN` never offered); in `Tests/Granite.Web.AuthenticationTest`: `ApplicationConfigTest.ts` (`APPLICATION` defaults to `"granite"`, reads `"client"` from config, login input carries it).
- [ ] **Step 2: Run** `cd FrontEnds/Granite.Web && yarn run test-granite-web-client`; FAIL.
- [ ] **Step 3: Implement.**
- [ ] **Step 4: Run** `yarn run test-granite-web-authentication`, `yarn run test-granite-web-client`, `yarn run test-granite-web-navigator`, and `yarn run build-granite-web-apphost`; PASS.
- [ ] **Step 5: Commit** `feat(web): Clients views and application identity`.

### Task 10a: ClientHost web app

**Files:**
- Create: `FrontEnds/Granite.Web/Applications/Granite.Web.ClientHost` — copy of AppHost's build setup (`package.json` name `granite.web.clienthost`, `vite.config.ts` dev port 5174, `tsconfig.json`, `postcss.config.mjs`, `index.html` title "Granite Clients", `nginx.conf`, `Dockerfile`, `docker-entrypoint.sh`, `config.js.template` with `application: "client"` and the same `apiBaseUrl`/`cookiePrefix` keys); `src/{main.tsx,App.tsx,ClientHostHeader.tsx}`. Dependencies: `granite.web.authentication`, `granite.web.client`, Mantine, ag-grid, react, react-router-dom (no Navigator, Account, User views).
- Modify: `FrontEnds/Granite.Web/package.json` (`build-granite-web-clienthost`, `containerize-granite-web-clienthost` tagging `ghcr.io/johnliebenau/granite/granite-web-clienthost:latest`, `push-granite-web-clienthost`, mirroring the AppHost scripts; the `build` chain builds it after AppHost), root `package.json` `containerize-granite-web` and `push-granite-web` include the ClientHost scripts.

**Interfaces:**
- Consumes: `OwnedClientsView` (Task 10), `AuthenticationProvider`, `RequireAuthentication`, `LoginView`, `LoginCallbackView`, `UserMenu` (Granite.Web.Authentication).
- Produces: frame `MantineProvider > AuthenticationProvider > BrowserRouter > ClientHostHeader (title + <UserMenu loginPath="/login" />) > Routes { login, login/callback, "/" → <RequireAuthentication><OwnedClientsView /></RequireAuthentication> }`.

- [ ] **Step 1:** `cd FrontEnds/Granite.Web && yarn run build-granite-web-clienthost`; expected FAIL before the app exists.
- [ ] **Step 2: Implement.**
- [ ] **Step 3: Run** `yarn run build-granite-web-clienthost` and `yarn run build-granite-web-apphost`; both PASS; `dist/config.js` contains `application`.
- [ ] **Step 4: Commit** `feat(web): ClientHost app for ClientAdmin users` (exclude `dist/**`).

### Task 11: Deployment wiring

**Files:** `Shared/Granite.Kubernetes/base/granite-system.yml` (Deployment, Service 443→8097/8098, HPA, HTTPS probes `/client-{command,query}-service/health/{startup,readiness,liveness}`, labels `microservice: client`, Ingress paths `/client-command-service`, `/client-query-service`), `production/kustomization.yaml` (JSON-patch `test`+`remove` for both paths at the right indexes; add Ingress paths `/session-command-service/oauth2/token` and `/client-command-service/rotate-own-client-secret`, `pathType: Exact`, to the Gateway service), `scripts/granite-create-private-ca.{ps1,sh}` (add `granite-client-commandhost`, `granite-client-queryhost`). ClientHost: base Deployment/Service/HPA `granite-web-clienthost` like `granite-web-apphost` (same env `apiBaseUrl`), a second Ingress rule for host `client.granite.example` with `/frontendcontroller-user-service` → Gateway and `/` → `granite-web-clienthost`, TLS hosts list includes it; each overlay replaces the host with `client.granite.localhost`, `client.granite-dev.__domain__`, `client.granite-test.__domain__`, `client.granite.__domain__` (same patch style as the existing host replacements); Session CommandHost env in the deploy scripts gains optional `OKTA_REDIRECT_URI_CLIENT*` entries like the existing `OKTA_REDIRECT_URI*`.

- [ ] **Step 1:** `kubectl kustomize Shared/Granite.Kubernetes/<env>` for local, development, test, production renders without error (read-only render, no apply); production output contains the two Exact paths and no `/client-*-service` prefix paths; other envs contain both prefixes; every env has the `client.` host rule with its env hostname.
- [ ] **Step 2: Commit** `feat(kubernetes): Client hosts, ClientHost app and API client ingress`.

### Task 12: Documentation

**Files:** `Microservices/Granite.Client/Claude.md` (overview, operations, seams, security, tables and columns for test/production), root `CLAUDE.md` (service list; Extending Granite Client bullet; role catalog: core roles `ROLE_ADMIN` and `ROLE_CLIENT_ADMIN`, catalog used by User, Session, FrontEndController, Client; UI building blocks `granite.web.client/Clients` and `granite.web.client/Owned`; a "Web applications" note: `granite` and `client` applications, `config.js` `application`, application-bound sessions and per-application login gate and Okta redirect URIs, ClientHost hostnames and the Okta callback to register; call-routing rows Session→Client via Gateway, API client → Gateway; new "API clients" section under Service tokens with curl examples for token, call, self-rotation and how an application publishes and authorizes a path; production Ingress list; yarn build order), `Microservices/Granite.Session/Claude.md`, `Microservices/Granite.Gateway/CLAUDE.md`, `Microservices/Granite.FrontEndController/CLAUDE.md`.

- [ ] **Step 1:** Write the docs; every property, path and name matches the code from Tasks 1–11 (grep each one).
- [ ] **Step 2: Commit** `docs: API clients`.

### Task 13: Top-down build and images (established method)

- [ ] **Step 1:** Repo root `./gradlew build --rerun`; expected BUILD SUCCESSFUL including every `CommitStage` test.
- [ ] **Step 2:** Repo root `yarn run build` then `yarn run test-granite`; expected success.
- [ ] **Step 3:** Repo root `./gradlew jib` (pushes all Java images, including `granite-client-commandhost` and `granite-client-queryhost`) and `yarn run containerize-granite-web && yarn run push-granite-web` (AppHost and ClientHost images).
- [ ] **Step 4:** Report to the user: commits, test totals, image list; the user deploys and tests. Merge to `development` and switch back only after the user approves.
