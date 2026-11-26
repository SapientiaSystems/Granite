# Granite

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)]()
[![Snapshot](https://img.shields.io/badge/snapshot-1.0.0--SNAPSHOT-orange.svg)]()

Granite is a reusable, multi-service Spring Boot backend with a React web front end, built on the Strata Framework Set. It provides the parts every business application needs: users, sessions with password and Okta login, a role catalog, preferences, API clients, help content, feedback, surveys and a virtual assistant, all behind one Gateway that performs authentication and authorization. Applications (for example Poplar) reuse Granite by extending each service's entry configuration and overriding its seams, and by composing their own web app from Granite's published React components.

## Purpose

- Give applications a secure, production-ready foundation (identity, sessions, authorization, auditability) so they can focus on their own domain.
- Keep the extension surface explicit: Java seams on entry configurations, Gateway hooks, and published TypeScript components.
- Apply the Strata conventions (request/reply services, command/query hosts, transactional outbox, front-end controller) consistently across services.

## Features

- **Gateway Security Split**: the Gateway performs all authentication and every role/path rule; service hosts only verify the JWT (signature, expiry, issuer, audience) through one shared `GraniteJwtCheckSecurity` chain
- **Sessions and Login**: password and Okta login, switchable per environment; server-side sessions bound to an application, `__Host-` cookies, change password, admin session management
- **Role Catalog**: one `ApplicationRolesManager` per application with a role hierarchy; Granite owns `ROLE_ADMIN` and `ROLE_CLIENT_ADMIN`
- **Service Tokens**: the FrontEndController and the Agent call services with client-credential tokens (`SCOPE_internal`); the Agent can act for a user with short-lived delegated tokens
- **Preferences**: per-user, typed preferences declared on the server and edited through `usePreference` on the web side
- **API Clients**: OAuth2 client-credentials token endpoint, admin-managed Clients, self-service secret rotation, and ClientHost, a separate web app for client administrators
- **Help Services**: Feedback, Survey, Content (Markdown help with search) and Agent (Claude-backed assistant with approval for mutating tools)
- **Several Applications in One Browser**: per-application hostnames, cookie prefixes and JWT issuer/audience with Okta single sign-on
- **Transactional Outbox**: every service's events go through one shared outbox relayed to Kafka
- **Container Images and Kubernetes**: Jib images for every host and test suite, Docker images for the web apps, kustomize overlays per environment with Maple-style image tags

## Architecture

Every browser and API-client call enters through the Gateway. The FrontEndController (FEC) serves the web apps' typed operations and reaches the User, Session and help services through the Gateway with its own service token.

```
Browser ──► Ingress ──► Granite.Web.AppHost / ClientHost (static app, config.js)
                   └──► Gateway ──► FrontEndController (User/Admin hosts)
                            │            └──► Gateway ──► User, Session, Client, Feedback,
                            │                              Survey, Content, Agent (Command/Query)
                            ├──► Session (resolve-session, revocation check, JWKS)
                            └──► application paths (configureApplicationRoutes)

Services ──► PostgreSQL (one database per environment)
Outbox   ──► Kafka
Session  ──► Okta
```

| Call | Route |
|---|---|
| Browser to services | Gateway |
| FEC to User and Session | Gateway, FEC service token |
| Session to User | Gateway, Session service token |
| Session to Client Command `verify-client-credentials` | Gateway, Session service token |
| API client (token request, calls with the token) | Gateway only |
| Gateway to Session `resolve-session` and revocation check | direct |
| Gateway and hosts to Session JWKS | direct |
| Session to Okta; services to PostgreSQL and Kafka | direct |

The detailed security model (tokenless paths, production Ingress, rate limits) is in [CLAUDE.md](CLAUDE.md#security-split-and-call-routing).

## Components

Each microservice has `Service` (request/reply types, Java and TypeScript), `Server` (configuration, services, repositories), `Client` (Java and TypeScript REST clients) and `TestSuite` components, plus Spring Boot hosts. Service-specific documentation lives next to the code (`Microservices/<Service>/CLAUDE.md` or `Claude.md`).

### Granite.User

Users, passwords and per-user values (including preferences). Hosts: CommandHost (8083), QueryHost (8084). Seams on `AbstractGraniteUserServerConfiguration` (`applicationRoles()`) and `GraniteUserCommandServerConfiguration`. See [Microservices/Granite.User/Claude.md](Microservices/Granite.User/Claude.md).

### Granite.Session

Sessions, password and Okta login, JWT issuing and JWKS, service and delegated tokens, and the OAuth2 client-credentials token endpoint. Hosts: CommandHost (8088), QueryHost (8089). See [Microservices/Granite.Session/Claude.md](Microservices/Granite.Session/Claude.md).

### Granite.Client

API Clients: id, hashed secret, catalog roles, owners, expiry and rotation. Hosts: CommandHost (8097), QueryHost (8098). Seams on `AbstractGraniteClientServerConfiguration` (`applicationRoles()`, `clientIdGenerator(Environment)`). See [Microservices/Granite.Client/Claude.md](Microservices/Granite.Client/Claude.md).

### Granite.Feedback and Granite.Survey

User feedback (defects, suggestions, kudos) and satisfaction surveys with admin review. Hosts: Feedback CommandHost (8086) / QueryHost (8087), Survey CommandHost (8091) / QueryHost (8092).

### Granite.Content

Markdown help and guides, synchronized from the classpath at start and searchable by the caller's roles. Hosts: CommandHost (8093), QueryHost (8094). Seams: `contentSources()`, `contentSearchEngine()`.

### Granite.Agent

The virtual assistant: conversations, a Claude-backed agent loop, and tools that read help content or (with the user's approval) change data through services. Hosts: CommandHost (8095), QueryHost (8096). Seams on `GraniteAgentCommandServerConfiguration`: `agentProvider(Environment)`, `agentTools(Environment,Supplier<String>)`, `agentSystemPrompt()`.

### Granite.FrontEndController

Typed, per-subject operations for the web apps (`get<Subject>Output` / `handle<Subject>Input`) built on Strata.FrontEndController. Sets and clears the session cookie. Hosts: UserHost (8081, `/frontendcontroller-user-service`), AdminHost (8082, `/frontendcontroller-admin-service`). See [Microservices/Granite.FrontEndController/CLAUDE.md](Microservices/Granite.FrontEndController/CLAUDE.md).

### Granite.Gateway

Spring Cloud Gateway: authentication, CSRF, session resolution, every authorization rule, rate limits and routing. Host: ServiceHost (8181). Seams: `configureApplicationRoutes`, `configureApplicationAuthorization`, `getUsersReaderAuthorities`, `getClientApplicationPathPatterns`. See [Microservices/Granite.Gateway/CLAUDE.md](Microservices/Granite.Gateway/CLAUDE.md).

### Granite.Outbox

The single relay of every service's outbox events to Kafka. Host: ServiceHost (8085).

### Granite.Foundation

Shared Java code: the role catalog (`ApplicationRolesManager`, `GraniteApplicationRolesManagerSupplier`), the JWT check chain (`GraniteJwtCheckSecurity`), `GraniteAuthorities`, cookie names and client settings.

### Granite.Web

The React 19 / Mantine front end: published components (`granite.web.authentication`, `.user`, `.client`, `.session`, `.feedback`, `.survey`, `.content`, `.agent`, `.account`, `.navigator`) and two Vite applications, `Granite.Web.AppHost` (the Granite app) and `Granite.Web.ClientHost` (the client-admin app). See [FrontEnds/Granite.Web/CLAUDE.md](FrontEnds/Granite.Web/CLAUDE.md).

### Granite.Kubernetes

Kustomize base and overlays (`local`, `development`, `test`, `production`), one namespace per environment (`granite-local`, `granite-dev`, `granite-test`, `granite-prod`), and the build, deploy, tag and monitoring scripts under `Shared/Granite.Kubernetes/scripts`. See [Shared/Granite.Kubernetes/IMAGE-TAGGING.md](Shared/Granite.Kubernetes/IMAGE-TAGGING.md).

## REST Endpoints

All operations are `POST` with JSON bodies, reached through the Gateway.

| Service path | Operations |
|---|---|
| `/frontendcontroller-user-service`, `/frontendcontroller-admin-service` | `get-/handle-` pairs for Users, Session, Sessions, Preferences, Home, Clients, Owned clients, Feedback, Feedback review, Survey, Survey review, Content, Agent |
| `/user-command-service`, `/user-query-service` | user create/update/delete, password, values; user queries |
| `/session-command-service` | `login`, `refresh`, `logout`, Okta start/complete, `issue-service-token`, `issue-delegated-token`, `oauth2/token`, `/.well-known/jwks.json` |
| `/session-query-service` | session queries, `find-sessions-by-key` |
| `/client-command-service`, `/client-query-service` | Client administration, `rotate-own-client-secret`, `verify-client-credentials` |
| `/feedback-*-service`, `/survey-*-service`, `/content-*-service`, `/agent-*-service` | the help services |
| `/outbox-service` | outbox administration |

The production overlay publishes only the FEC paths, `/gateway-service`, the token endpoint and `rotate-own-client-secret`; see [CLAUDE.md](CLAUDE.md#security-split-and-call-routing). The full FEC operation table is in [Microservices/Granite.FrontEndController/CLAUDE.md](Microservices/Granite.FrontEndController/CLAUDE.md#typed-operations).

## Installation

Granite is in development; the current version is **1.0.0-SNAPSHOT**, built against Strata.Foundation, Strata.Client, Strata.Server, Strata.FrontEndController and Strata.Outbox **1.0.0** (see [Versioning](#versioning)).

### Gradle

```gradle
dependencies {
    implementation 'granite.foundation:granite-foundation-core:1.0.0-SNAPSHOT'
    implementation 'granite.user:granite-user-server:1.0.0-SNAPSHOT'
    implementation 'granite.session:granite-session-server:1.0.0-SNAPSHOT'
    implementation 'granite.client:granite-client-server:1.0.0-SNAPSHOT'
    implementation 'granite.frontendcontroller:granite-frontendcontroller-server:1.0.0-SNAPSHOT'
    implementation 'granite.gateway:granite-gateway-server:1.0.0-SNAPSHOT'
}
```

Every service publishes `granite.<service>:granite-<service>-service`, `-client`, `-server` and `-testsuite`; Foundation publishes `granite-foundation-core` and `granite-foundation-testsuite`, the Gateway and the Outbox publish `-server` only.

### Maven

```xml
<dependency>
    <groupId>granite.user</groupId>
    <artifactId>granite-user-server</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### TypeScript (npm / yarn)

The TypeScript packages are published to npmjs.org. Install them by dist-tag:

```bash
# Latest development snapshot (1.0.0)
npm install granite.web.authentication@snapshot-1.0.0 granite.web.account@snapshot-1.0.0

# or, with yarn
yarn add granite.web.authentication@snapshot-1.0.0 granite.web.user@snapshot-1.0.0 granite.web.client@snapshot-1.0.0
```

The service-level packages (`granite.<service>.service`, `granite.<service>.client`, `granite.frontendcontroller.service` / `.client`) use the same dist-tags.

### Docker Images

All 29 images (every host, every test suite and the two web apps) are published to GitHub Container Registry under one tag per build, `<version>-<yyyyMMddHHmmss>-<local|dev|test|prod>`; there is no `latest`.

```bash
docker pull ghcr.io/johnliebenau/granite/granite-gateway-servicehost:1.0.0-SNAPSHOT-20261126173343-dev
docker pull ghcr.io/johnliebenau/granite/granite-web-apphost:1.0.0-SNAPSHOT-20261126173343-dev
```

### Kubernetes

```bash
# Build every image locally (tag <version>-<timestamp>-local, written to .build/image-tag)
Shared/Granite.Kubernetes/scripts/granite-build-images.sh --environment local

# Deploy to docker-desktop, namespace granite-local
Shared/Granite.Kubernetes/scripts/granite-system-deploy.sh local --wait
```

`development` deploys a `-dev` tag (`--tag`), `test` and `production` deploy the `IMAGE_TAG` committed in their overlay. PowerShell equivalents (`.ps1`) exist for every script.

### Repository Configuration

The jars (Granite and Strata) are published to GitHub Packages:

```gradle
repositories {
    maven {
        name "GitHubPackages"
        url "https://maven.pkg.github.com/StrataFrameworkSet/repository"
        credentials {
            username = System.getenv("REPOSITORY_USER")
            password = System.getenv("REPOSITORY_PASSWORD")
        }
    }
}
```

## Versioning

| | Development snapshot | Release |
|---|---|---|
| Maven jars | `X.Y.0-SNAPSHOT` (e.g. `1.0.0-SNAPSHOT`) | `X.Y.0` (e.g. `1.0.0`) |
| npm packages | `X.Y.0-snapshot.<timestamp>`, dist-tag `snapshot-X.Y.0` | `X.Y.0-release.<timestamp>`, dist-tags `release-X.Y.0` and `latest` |
| Docker images | `<version>-<yyyyMMddHHmmss>-local` (developer) or `-dev` (pipeline) | promoted by retag to `-test` and `-prod` (same digest) |

The version is set once in `gradle/granite-versions.gradle` (`major`, `minor`, `patch`), which also pins the Strata versions.

- `.github/workflows/build-pipeline.yml` runs on `development` and `release-*` (pull requests build and test, and publish nothing). Its stages are:
  - **prepare-build-tag**: computes the build's image tag
  - **java-commit-stage** and **typescript-commit-stage**, in parallel: the Gradle build and tests push every host and test-suite image; the TypeScript build and tests run against a throwaway Verdaccio in the runner, then the web images are pushed and `publish-npm.js` publishes the built tarballs to npmjs
  - **java-integration-stage** and **java-acceptance-stage**: placeholders
  - **deploy-stage**: publishes the snapshot jars to GitHub Packages
  - **cleanup-failed-build**: on failure, deletes that build's `-dev` images
- `.github/workflows/image-promotion.yml` (manual) retags a `-dev` build to `-test`, or `-test` to `-prod`; then set `IMAGE_TAG` in the `test` or `production` overlay and commit it.

To release `X.Y.0`:

1. Merge `development` into `release-X.Y`; the build pipeline publishes the snapshot.
2. Run `./granite-promote.sh --version X.Y.0 --dry-run` (or `.\granite-promote.ps1 -Version X.Y.0 -DryRun`), check the output, then run it without the dry-run flag. Jars are republished as `X.Y.0`; npm packages get their `strata.*` and `granite.*` dependencies pinned to exact release versions.
3. Promote the images with `granite-image-tags promote` or the image-promotion workflow.
4. Tag the commit `vX.Y.0`.
5. Move `development` to the next minor version.

## Usage

The walkthrough below builds a hypothetical application, **Poplar**, on Granite. Each step names the seam to override; the per-service documentation lists every seam.

### 1. Depend on Granite

Each Poplar host depends on the matching Granite `-server` artifact (Gradle above), and the Poplar web app depends on the Granite web components:

```json
"dependencies": {
    "granite.web.authentication": "snapshot-1.0.0",
    "granite.web.account": "snapshot-1.0.0",
    "granite.web.user": "snapshot-1.0.0",
    "granite.web.client": "snapshot-1.0.0"
}
```

### 2. Role Catalog

An application provides one role catalog and returns it from `applicationRoles()` on the four hosts that use it: User, Session, FrontEndController and Client. Role names are `ROLE_` plus UPPER_SNAKE; `ROLE_ADMIN` and `ROLE_CLIENT_ADMIN` are Granite's reserved core roles and must not be redefined.

```java
public
class PoplarApplicationRolesManagerSupplier
    implements Supplier<ApplicationRolesManager>
{
    public static final String PLANNER = "ROLE_POPLAR_PLANNER";
    public static final String READER  = "ROLE_POPLAR_READER";

    @Override
    public ApplicationRolesManager
    get()
    {
        return
            new ApplicationRolesManager(
                List.of(
                    new RoleDefinition(PLANNER,"Planner",Set.of(READER)),
                    new RoleDefinition(READER,"Reader",Set.of())),
                READER);
    }
}
```

Each host logs the catalog's role names once at startup, so a host left on a different catalog is visible in the logs.

### 3. Extend the Service Hosts

A Poplar host extends Granite's entry configuration and overrides the seams it needs. For the User CommandHost:

```java
@Configuration
public
class HostConfiguration
    extends GraniteUserCommandServerConfiguration
{
    @Bean
    @SingletonScoped
    @Override
    public ApplicationRolesManager
    applicationRoles()
    {
        return new PoplarApplicationRolesManagerSupplier().get();
    }
}
```

Seams are Java overrides of `@Bean` or protected methods, because Spring Boot forbids redefining imported bean names. Session, FrontEndController and Client follow the same pattern.

### 4. FrontEndController

Poplar extends the FEC service interface, the service, the controller and the configuration. The configuration declares exactly two request-scoped beans, and adds its own preferences:

```java
@Configuration
public
class PoplarFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IPoplarFrontEndControllerService>
{
    @Bean
    @RequestScoped
    @Named("Granite")
    public IPoplarFrontEndControllerService
    frontEndControllerService(
        @Named("FrontEndController")
        Executor                executor,
        ModelMapper             mapper,
        ApplicationRolesManager roles)
    {
        return createService(createContext(mapper,executor,roles));
    }

    @Bean
    @RequestScoped
    @Named("Granite")
    public PoplarFrontEndControllerServiceController
    frontEndControllerServiceController(@Named("Granite") IPoplarFrontEndControllerService service)
    {
        return new PoplarFrontEndControllerServiceController(service);
    }

    @Override
    protected IPoplarFrontEndControllerService
    createService(GraniteFrontEndControllerContext context)
    {
        return new PoplarFrontEndControllerService(context);
    }

    @Override
    protected List<PreferenceDefinition>
    preferenceDefinitions()
    {
        List<PreferenceDefinition> definitions = new ArrayList<>(super.preferenceDefinitions());

        definitions.add(PreferenceDefinition.string("poplar.defaultRegion","EMEA"));
        return definitions;
    }
}
```

`PoplarFrontEndControllerServiceController` is a `@RestController @RequestMapping("${granite.frontendcontroller.service-path}")` subclass of `GraniteFrontEndControllerServiceController<IPoplarFrontEndControllerService>` that adds Poplar's own `@PostMapping` operations.

### 5. Gateway Routes and Authorization

Poplar's Gateway routes its own services and authorizes them by role. Granite's core rules are always registered; anything not matched falls through to `ROLE_ADMIN`.

```java
@Override
protected Builder
configureApplicationRoutes(Builder routes,Environment environment)
{
    return addRoute(routes,"poplar-orders-service","/poplar-orders-service/**","poplar-orders-service.host",environment);
}

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

In `production`, publish the path in the application's Ingress overlay:

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

### 6. Web Application Frame

Poplar builds its own frame from the published components; `MantineProvider` sits above everything:

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

A preference section reads and saves one declared key; saving is per change:

```tsx
import { usePreference } from "granite.web.account/Preference";

export function PoplarSection()
{
    const [region, setRegion] = usePreference<string>("poplar.defaultRegion");

    return <Select label="Default region" data={["EMEA","AMER","APAC"]} value={region} onChange={value => value && setRegion(value)} />;
}
```

Runtime configuration comes from `window.__GRANITE_CONFIG__` (`apiBaseUrl`, `application`, `cookiePrefix`), served as `config.js`.

### 7. Help Content and the Assistant

Poplar ships its help as Markdown under `content/poplar/` on the Content CommandHost classpath; documents are synchronized at start:

```markdown
---
key: poplar/orders-help
title: Working with Orders
kind: HELP
keywords: orders, planning
---

# Working with Orders

See also [Planning](content:poplar/planning-help).
```

The assistant takes Poplar's own tools and prompt by overriding `agentTools(Environment,Supplier<String>)` and `agentSystemPrompt()` on `GraniteAgentCommandServerConfiguration`. A tool implements `IAgentTool`; tools that change data return `isMutating() == true`, need the user's approval in the chat, and call services with the user's delegated token.

### 8. API Clients

Admins create Clients in the Clients grid (the secret is shown once). A Client gets a token with client credentials and calls the application paths its roles allow:

```sh
curl -s https://poplar.example/session-command-service/oauth2/token \
  -u "$CLIENT_ID:$CLIENT_SECRET" \
  -d grant_type=client_credentials
# {"access_token":"eyJ...","token_type":"Bearer","expires_in":900}

curl -s https://poplar.example/poplar-orders-service/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN"

curl -s -X POST https://poplar.example/client-command-service/rotate-own-client-secret \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" -d '{}'
```

Client tokens are refused on Granite's own service paths; Granite ships no Client-callable path.

### 9. Your Own Client-Admin Web App

1. Name it: `GRANITE_CLIENT_ADMIN_APPLICATIONS=poplar-clients` for the Session CommandHost and the Gateway.
2. Build its frame from `AuthenticationProvider`, `PreferencesProvider`, `LoginView`, `LoginCallbackView`, `UserMenu`, `OwnedClientsView` and `PreferencesView`, with `application: "poplar-clients"` in its `config.js`.
3. Pass the FEC path and `.env` prefix: `<OwnedClientsView service="frontendcontroller-poplar-service" envPrefix="poplar" />`, and override `getClientApplicationPathPatterns(Environment)` in the Gateway when the path is not `/frontendcontroller-user-service`.
4. Register its Okta callback: `granite.session.okta.redirect-uris.poplar-clients`.
5. Optionally prefix new Client IDs: `granite.client.id-prefix=pc_` on the Client CommandHost.

### 10. Several Applications in One Browser

- One hostname per application, also locally (`granite.localhost`, `poplar.localhost`).
- A cookie prefix per application: `granite.cookies.prefix=POPLAR` on the FEC and the Gateway, and `cookiePrefix` in `config.js`.
- A JWT identity per application: `granite.security.jwt.issuer` / `.audience` on every host and the Gateway, and `granite.session.jwt.issuer` / `.audience` on the Session CommandHost (required in `production`).

## Testing

- **CommitStage** (Java): `./gradlew build` runs the unit, MockMvc and Spring context tests of every service.
- **TypeScript**: test projects under `<Group>/Tests/<Component>Test` run Vitest against the component's published package: `yarn run test-granite-web` (web) and `yarn run test-granite` (all).
- **Test suites**: every service builds a `granite-<service>-testsuite` image for the integration and acceptance stages, which are placeholders today.

## Building

### Prerequisites

- Java 23 (Amazon Corretto)
- Node.js 24 and Yarn 1
- Docker (images) and kubectl (deployment)
- Gradle (via the included wrapper)

### Build Commands

```bash
# Build and test all Java components (images are built only when BUILD_IMAGE_TAG is set)
./gradlew build

# Build, publish (snapshot, to yarn's registry) and test every TypeScript component
yarn run test

# Show which built tarballs would be published to npmjs
node publish-npm.js --dry-run

# Build and push every image under one -local tag, then deploy it
Shared/Granite.Kubernetes/scripts/granite-build-images.sh --environment local
Shared/Granite.Kubernetes/scripts/granite-system-deploy.sh local --wait

# Publish the snapshot jars to GitHub Packages (requires credentials)
./gradlew publishToGitHubPackages
```

### Project Structure

```
Granite/
├── Microservices/
│   ├── Granite.User/                 # Users, passwords, values
│   ├── Granite.Session/              # Sessions, login, tokens, JWKS
│   ├── Granite.Client/               # API Clients
│   ├── Granite.Feedback/             # Feedback
│   ├── Granite.Survey/               # Surveys
│   ├── Granite.Content/              # Help content
│   ├── Granite.Agent/                # Virtual assistant
│   ├── Granite.FrontEndController/   # Web app operations (User and Admin hosts)
│   ├── Granite.Gateway/              # Authentication, authorization, routing
│   └── Granite.Outbox/               # Outbox relay to Kafka
├── Shared/
│   ├── Granite.Foundation/           # Shared Java code (roles, JWT check)
│   └── Granite.Kubernetes/           # Kustomize base, overlays and scripts
├── FrontEnds/
│   └── Granite.Web/                  # React components, AppHost and ClientHost
├── gradle/                           # granite-versions.gradle, granite-image.gradle
├── .github/                          # build-pipeline.yml, image-promotion.yml, Verdaccio config
├── await-published.js                # Waits until a published snapshot is resolvable
├── publish-npm.js                    # Publishes the built tarballs to npmjs
├── granite-promote.sh / .ps1         # Release promotion of jars and npm packages
├── build.gradle                      # Root composite build
├── package.json                      # Root Yarn scripts for the TypeScript builds
└── README.md                         # This file
```

## Contributing

We welcome contributions to Granite! Please follow these guidelines:

1. **Create your feature branch** from `development`
2. **Follow the coding standards** established in the existing codebase
3. **Write tests** for your changes and ensure all existing tests pass
4. **Update documentation** (`CLAUDE.md` and the service docs) as needed for your changes
5. **Submit a pull request** against `development` with a clear description of your changes

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/SapientiaSystems/Granite.git
   cd Granite
   ```

2. Set `REPOSITORY_USER`, `REPOSITORY_PASSWORD` and `PROPERTIES_ENCRYPTION_KEY`, then build:
   ```bash
   ./gradlew build
   ```

3. Build and test the TypeScript components:
   ```bash
   yarn run test
   ```

### Code Style

- **Formatting**: Allman braces; the access modifier on its own line before `class` and before the method return type; the method name on the line after its return type; a `////` header and footer with the file name in every Java file
- **Interface Naming**: prefix all interfaces with "I" (e.g. `IAgentTool`, `IGraniteFrontEndControllerService`)
- **Method Chaining**: setters return `this` to enable fluent method chaining
- **No Static Inner Classes** in Java
- **TypeScript Array Types**: use `Array<Type>` rather than `Type[]`
- Write unit tests for new functionality; prefer explicit `for` loops and side-effect-free streams

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Support

For questions, issues, or contributions, please:

1. Check the [Issues](https://github.com/SapientiaSystems/Granite/issues) page for existing questions
2. Create a new issue if your question hasn't been addressed

## Related Projects

- [Strata.Foundation](https://github.com/StrataFrameworkSet/Strata.Foundation) - Foundational components and utilities
- [Strata.Server](https://github.com/StrataFrameworkSet/Strata.Server) - Server-side components and utilities
- [Strata.Client](https://github.com/StrataFrameworkSet/Strata.Client) - Client-side components and utilities
- [Strata.FrontEndController](https://github.com/StrataFrameworkSet/Strata.FrontEndController) - Front-end controller framework
- [Strata.Outbox](https://github.com/StrataFrameworkSet/Strata.Outbox) - Transactional outbox components

---

**Granite** is built on the Strata Framework Set, providing a secure, extensible foundation for multi-service business applications.
