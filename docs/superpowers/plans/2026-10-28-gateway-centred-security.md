# Gateway-Centred Security Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** All role authorization in the single Gateway; service hosts keep only a JWT check; all service-to-service HTTP calls go through the Gateway.

**Architecture:** A shared servlet JWT-check configuration in Granite.Foundation replaces the User/Session chains and is added to the FEC (optional mode). Session issues FEC service tokens via client credentials. Gateway rules open the Okta steps and production internal routes to `SCOPE_internal` only. Base URLs point at the Gateway; JWKS stays direct.

**Tech Stack:** Java 23, Spring Boot 3 / Spring Security 6 (servlet + WebFlux Gateway), Strata, JUnit 5, Kubernetes manifests (Kustomize).

**Spec:** `docs/superpowers/specs/2026-10-28-gateway-centred-security-design.md`

## Global Constraints

- One Gateway. Service hosts: exactly one `SecurityFilterChain`, JWT check only (signature, expiry, issuer), no `hasAuthority`/`hasRole`/role path rules.
- JWKS fetch direct to Session Command.
- Gateway → Session `resolve-session` and revocation check stay direct.
- Never log tokens, client secrets, handles, passwords.
- Build/test only with existing Gradle tasks (`--rerun` after each task name) and yarn scripts; no daemon start/stop, no tool-config changes, no `publish*`/jib/deploy scripts. Never `--dry-run` across included builds.
- Style: `//////` headers/footers, Granite formatting, no explanatory comments, no static inner classes.
- Commits end with `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`; explicit paths only; never `.idea/**`, `dist/**`, `.gradle`, package.json version bumps.

## Review Focus

1. A request straight to a User/Session host with no/forged/expired token → 401 (Task 2 tests).
2. FEC anonymous login-page request (no bearer) still works; a forged bearer to the FEC → 401 (Task 3).
3. A browser session (ROLE_USER or ROLE_ADMIN) calling an internal route through the production Gateway → 403; the FEC service token → allowed (Task 5).
4. `resolve-session` through the Gateway → still denied for everyone (Task 5).
5. Wrong client secret on the service-token endpoint → 401, nothing logged but the client id (Task 4).

---

### Task 1: Foundation JWT-check configuration

**Files:** `Shared/Granite.Foundation/Components/Granite.Foundation.Core/src/main/java/granite/foundation/core/security/` (e.g. `GraniteJwtCheckConfigurer` or `GraniteJwtCheckSecurity` + `JwtCheckMode` enum `REQUIRED|OPTIONAL`), reuse `GraniteJwtDecoderFactory`/`GraniteResourceServerSettings`; tests in Granite.Foundation.CoreTest.

**Interfaces:** a single method building the chain: `SecurityFilterChain jwtCheckChain(HttpSecurity http,JwtDecoder decoder,JwtCheckMode mode,List<String> tokenlessPaths)` — CSRF disabled, stateless, OPTIONS permitted, tokenless paths permitted, `REQUIRED` → `anyRequest().authenticated()`, `OPTIONAL` → `anyRequest().permitAll()` with bearer still validated when present (invalid → 401). No authority mapping rules; the authentication converter may still map authorities for downstream reading but nothing authorizes on them. Default tokenless paths helper: health base path, `/error`, API docs outside production.

- [ ] Tests: valid token passes; expired → 401; bad signature → 401; missing token on REQUIRED → 401; tokenless path passes without token; OPTIONAL + no token passes; OPTIONAL + invalid token → 401.
- [ ] Run `./gradlew :Granite.Foundation:Tests:Granite.Foundation.CoreTest:test --rerun`; commit `feat(foundation): shared JWT-check security chain for service hosts`.

### Task 2: User and Session hosts use the JWT check

**Files:** `AbstractGraniteUserServerConfiguration`, `AbstractGraniteSessionServerConfiguration` (and Session Command subclass for its tokenless paths), host/test configs, CLAUDE.md files.

**Interfaces:** each abstract config's `securityFilterChain` delegates to Task 1 (`REQUIRED`); protected `getTokenlessPathPatterns(Environment)` seam (renamed from `getPublicPathPatterns`); Session Command adds `/session-command-service/login`, `/refresh`, `/resolve-session`, `/issue-service-token` (Task 4), `/.well-known/jwks.json`.

- [ ] Tests: per host, no-token → 401 on a service path; tokenless paths open; a test asserting the chain has no authority-based rules (e.g. a `ROLE_`-less valid token reaches a service path).
- [ ] Run User ServerTest + CommandHost, Session ServerTest + CommandHost + QueryHost; commit `refactor(security): User and Session hosts keep only the JWT check`.

### Task 3: FEC JWT check (optional mode), verified caller

**Files:** `AbstractGraniteFrontEndControllerConfiguration` (+ decoder/JWKS settings), FEC host properties (`granite.security.jwt.*` like User hosts), `SessionRequestContext` (caller from the verified `Jwt` in the security context instead of manual base64 decode), FEC tests and CLAUDE.md (+ identical copilot copy).

- [ ] Tests: anonymous Session.New request passes; forged bearer → 401; caller id comes from the verified token.
- [ ] Run FEC ServerTest + host compiles; commit `feat(frontendcontroller): JWT check and verified caller identity`.

### Task 4: FEC service identity

**Files:** Session Service/Server/Client (request/reply `IssueServiceTokenRequest {clientId, clientSecret}` / `IssueServiceTokenReply {token, expiresAt}`, command service method, controller `POST /session-command-service/issue-service-token`, client method), client-credentials settings (`granite.session.service-clients.<id>.secret-hash` or equivalent bound from Secret-backed env), FEC `ServiceTokenSource` (cache + renew) and client filter attaching it on User/Session calls (replacing the forwarded user bearer for those calls), FEC properties for its client id/secret, tests.

- [ ] Tests: correct credentials → token with `sub`=client id, `SCOPE_internal`, short lifetime; wrong secret / unknown client → 401-style failure; no secret/token in logs; FEC attaches the service token on User/Session calls and renews before expiry.
- [ ] Run Session ServerTest, FEC ServerTest; commit `feat(session,frontendcontroller): client-credentials service tokens for the FEC`.

### Task 5: Gateway rules for internal traffic

**Files:** `GraniteGatewayServerConfiguration` (Okta steps `SCOPE_internal`; production registers User/Session/Outbox routes; production internal paths `SCOPE_internal` only; `issue-service-token` public path), Gateway tests, Gateway CLAUDE.md.

- [ ] Tests: browser-session JWT (ROLE_ADMIN and ROLE_USER) → 403 on internal routes in production profile; service token → allowed; Okta steps: browser 403, service token allowed; `resolve-session` denied for all; non-production behaviour unchanged.
- [ ] Run Gateway ServerTest + ServiceHost compile; commit `feat(gateway): route and authorize internal service traffic`.

### Task 6: Configuration and Kubernetes

**Files:** FEC and Session host `application-*.properties` (`*-service.base-url` / `.host` → `https://granite-gateway:443`; JWKS stays at Session), FEC service-client properties, `Shared/Granite.Kubernetes/{base,development,local,test,production}` (NetworkPolicies; FEC client-credential Secret and Session client registration; `X-Forwarded-For` trust settings if needed), deploy script env-var lists if they enumerate secrets.

- [ ] Verify host smoke tests (Session CommandHost/QueryHost, User CommandHost) and FEC host compiles; `kubectl kustomize` render is NOT run unless an existing script does it — review manifests by reading; commit `chore(deploy): route service calls through the Gateway; network policies`.

### Task 7: Docs and verification

- [ ] Root and per-service CLAUDE.md: security split, call routing, tokenless paths, service tokens, NetworkPolicy; spec status.
- [ ] Run all Gradle suites (Foundation, User, Session + hosts, FEC + hosts, Gateway + ServiceHost) and root `yarn run test-granite`; commit `docs: gateway-centred security`; hand over for E2E (password login, Okta login, logout, Users, preferences, re-authentication).
