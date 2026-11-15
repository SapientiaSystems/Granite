# Granite API Clients Design

## Intent

Machines (scripts, partner systems, other applications) need to call Granite and Granite-derived application APIs without a browser or an Okta login. Today the only non-user identities are the configured service clients (`granite-fec`, `granite-agent`), which all get `SCOPE_internal` and are defined in deployment configuration.

A **Client** is a stored, admin-managed identity with a server-generated key and secret and a set of role-catalog authorities, like a User. A Client exchanges its key and secret for a short-lived Session-signed JWT through a standard OAuth2 client-credentials token endpoint, then calls services through the Gateway with `Authorization: Bearer <token>`. The Gateway authorizes the call by the token's authorities with the rules it already has. Service hosts stay JWT-check only.

## Goals

1. A new microservice **Granite.Client** stores Clients and verifies their credentials.
2. Session issues Client access tokens at `POST /session-command-service/oauth2/token` (RFC 6749 §4.4, client credentials).
3. The Gateway accepts Client tokens and authorizes them by authorities; Client tokens never reach FEC operations or identity plumbing.
4. A **Clients** admin view in the web app, patterned after the Users view, with create, edit, enable/disable, rotate secret and delete; the secret is revealed once with Copy and Download `.env`.
5. Applications (Poplar) can reuse all of it through the same entry-configuration seams as other services.
6. A Granite core role `ROLE_CLIENT_ADMIN` (ClientAdmin) lets the owners of a Client rotate its secret: a User holding the role rotates the Clients they own in a separate web app, ClientHost, at `client.granite.<domain>`, and a Client holding the role rotates its own secret through the API. Only `ROLE_ADMIN` creates, edits and deletes Clients and assigns owners (as for Users).

## Non-goals

- OAuth2 scopes, consent, refresh tokens, `private_key_jwt`, mTLS client authentication.
- Immediate revocation of already-issued Client tokens (they expire; see Revocation).
- Clients calling FrontEndController operations.
- Per-client rate limits, per-call audit log, usage metrics beyond `lastTokenIssuedAt`.
- A Home dashboard tile for Clients.
- Okta-managed clients.

## Anti-goals (failure even if it works)

- A secret or secret hash appears in a log, an event, a query reply, an FEC output (other than the one-time reveal reply), or a stored plaintext column.
- A Client token carries `SCOPE_internal`, any `SCOPE_*`, or `ROLE_ADMIN`.
- Response time or error text reveals whether a client id exists.
- Service hosts gain role or path rules.

## Decisions made by the user

| Topic | Decision |
|---|---|
| Name | The identity type is `Client`. |
| Storage | New microservice `Granite.Client`, not inside Granite.User. |
| Credentials | Server-generated key and 32-byte random secret, stored as SHA-256 (high entropy, so unsalted SHA-256 is acceptable, as for service clients). |
| Token API | OAuth2 standard: `POST /session-command-service/oauth2/token`, `grant_type=client_credentials`, credentials by HTTP Basic or form fields; reply `{access_token, token_type: "Bearer", expires_in}`. |
| Revocation | Short lifetime only: disabling, rotating or deleting stops new tokens; issued tokens live until expiry. Default lifetime 15 minutes. |
| Authorities | Any role from the application's role catalog except `ROLE_ADMIN`; never `SCOPE_*`. |
| Fields | key, name, description, authorities, enabled, secret expiry date (optional), last token issued at. |
| First use | Granite and application service APIs through the Gateway; FEC operations stay user-only. |
| Production exposure | Granite's production overlay publishes only the token endpoint; each application adds Ingress paths for the service APIs it exposes; Granite documents how. |
| ClientAdmin | `ROLE_CLIENT_ADMIN` is a Granite core role (in every catalog, like `ROLE_ADMIN`); implies nothing, implied by nothing. Held by Users and/or Clients. |
| Ownership | Admin assigns owner Users to each Client. A User with `ROLE_CLIENT_ADMIN` sees only the Clients they own and may only rotate their secret. A Client with `ROLE_CLIENT_ADMIN` may rotate its own secret through the API. Everything else (create, name, description, authorities, owners, enabled, expiry, delete) is `ROLE_ADMIN` only. |
| ClientHost | ClientAdmin Users work in a separate web app, ClientHost, at `client.granite.<domain>`, that only shows the Clients they own and lets them rotate secrets. The main web app (AppHost) shows the full Clients view to readers/Admin only. |
| Keep-out strictness | Strict: every session is bound to the web app it was created for. AppHost login refuses a User whose only catalog role is `ROLE_CLIENT_ADMIN`; ClientHost login requires `ROLE_CLIENT_ADMIN`; the Gateway lets ClientHost sessions reach only the owned-clients operations and current-session basics. |
| Okta on ClientHost | Per-app Okta redirect URIs in Session, chosen by the login's application; the ClientHost callback must be registered in the Okta app. |
| Granite reach | No new Client-callable path in Granite: Granite provides the mechanism, applications grant paths. Manual test in development: a Client holding `ROLE_AUDITOR` (role-rules enabled) calls a Granite query path such as `/feedback-query-service/...`. Automated Gateway tests use a test-backend path authorized in a test `configureApplicationAuthorization`. |
| UI access | Same as Users: readers are the Gateway's `getUsersReaderAuthorities`; create/edit/rotate/delete need `ROLE_ADMIN`. Menu entry `Clients` next to `Users`. |
| Secret reveal | Shown once in a dialog with Copy buttons and a Download `.env` button. |

## Design

### 1. Granite.Client microservice

Mirrors `Microservices/Granite.Feedback` in layout, build, packaging and conventions (Service / Server / Client components with Java and TypeScript, CommandHost and QueryHost, ServiceTest / ServerTest / ClientTest / TestSuite, jib images `granite-client-commandhost` and `granite-client-queryhost`). Paths `/client-command-service/**` and `/client-query-service/**`. Ports: CommandHost 8097, QueryHost 8098 (my call: next free ports). Same shared PostgreSQL database; domain events `ClientCreated`, `ClientUpdated`, `ClientDestroyed` through the Outbox, like Feedback.

**Entity `Client`:**

| Field | Notes |
|---|---|
| `clientId` | Key. Server-generated, `gc_` + 32 lowercase base32 characters (my call; a recognisable prefix lets secret scanners and humans spot it). Immutable. |
| `name` | Required, unique, at most 100 characters. |
| `description` | Optional, at most 1000 characters. |
| `authorities` | Catalog roles, stored closed under the role hierarchy (as for Users). Rejected: non-catalog role, `ROLE_ADMIN`, any `SCOPE_*`; a role whose expansion includes `ROLE_ADMIN` is also rejected. |
| `owners` | Set of User ids (`userId`), assigned by Admin, may be empty. A deleted User's id stays until an Admin removes it and is shown as unknown (my call). |
| `enabled` | Default `true`. |
| `secretSha256` | SHA-256 of the secret. Never mapped to any DTO. |
| `secretExpiresAt` | Optional instant; after it the secret is rejected. |
| `lastTokenIssuedAt` | Set on each successful verification. |
| `createdAt`, `updatedAt`, `version` | Optimistic locking as for other entities. |

**Secret:** 32 bytes from `SecureRandom`, base64url without padding. Returned only in the reply of `create-client` and `rotate-client-secret`.

**Command operations** (`/client-command-service/`):
- `create-client` `{name, description, authorities, enabled, secretExpiresAt}` → `ClientData` + `clientSecret`.
- `update-client` `{clientId, name, description, authorities, enabled, secretExpiresAt}` → `ClientData`. Authorities rule as above; on update only newly added roles are checked against the catalog (matches Users).
- `rotate-client-secret` `{clientId, secretExpiresAt}` → `ClientData` + `clientSecret`. The old secret stops working immediately. Admin path.
- `rotate-owned-client-secret` `{clientId, ownerUserId}` → `ClientData` + `clientSecret`. Rotates only when `ownerUserId` is in the Client's `owners`; otherwise fails with the same generic `not found` as an unknown id. `secretExpiresAt` is unchanged. The FEC fills `ownerUserId` from the verified caller, never from the browser payload.
- `rotate-own-client-secret` (no payload fields) → `clientId` + `clientSecret`. The Client is taken from the verified JWT `sub` (`client:<clientId>`); any other subject fails. `secretExpiresAt` is unchanged. Tokens issued with the old secret stay valid until they expire.

Ownership checks in Client are data rules (who owns which record), not role rules; which caller may reach each operation is decided only by the Gateway.
- `destroy-client`, `destroy-clients`.
- `verify-client-credentials` `{clientId, clientSecret}` → `{valid, clientId, authorities}`. Constant-time: an unknown id is compared against a fixed dummy hash; disabled, expired, unknown and wrong secret all return `valid=false` with the same generic reason. The returned authorities are the stored ones intersected with the current catalog (a role later removed from the catalog is not minted), minus `ROLE_ADMIN` and `SCOPE_*` (my call). On success it sets `lastTokenIssuedAt` best-effort with a direct update that does not bump `version` and fires no event; a failure of that update is logged at debug and never fails the verification (my call).

**Secret expiry** (my call): the UI edits a date; it is stored as an `Instant` at the start of the following day UTC and the secret is valid while `now < secretExpiresAt` (so "expires 2026-12-31" works through the end of that day UTC). Blank means no expiry.

**Query operations** (`/client-query-service/`): `find-clients-by-key`, `find-clients-by-key-set`, `find-all-clients` (ordered by name), `find-clients-by-owner` `{userId}` (ordered by name). `ClientData` never contains the secret or its hash.

**Entry configurations** (extension seams, like Feedback): `AbstractGraniteClientServerConfiguration` (including the `applicationRoles()` seam and `applicationRolesStartupLog`), `GraniteClientCommandServerConfiguration` (`clientEventSender`), `GraniteClientQueryServerConfiguration`. Security: one `GraniteJwtCheckSecurity.jwtCheckChain` in `REQUIRED` mode, no tokenless paths beyond the defaults. The role catalog is now used by four hosts: User, Session, FrontEndController and Client.

### 2. Session token endpoint

`POST /session-command-service/oauth2/token`, `Content-Type: application/x-www-form-urlencoded`, tokenless at Session.

- Request: `grant_type=client_credentials`; credentials in `Authorization: Basic base64(client_id:client_secret)` or as `client_id` / `client_secret` form fields (not both). A `scope` parameter is ignored.
- Session calls Client Command `verify-client-credentials` through the Gateway with its service token (`ServiceTokenProvider`), as it calls User.
- Success `200`, headers `Cache-Control: no-store`, `Pragma: no-cache`, body `{"access_token": "...", "token_type": "Bearer", "expires_in": <seconds>}`.
- Errors per RFC 6749 §5.2, JSON `{"error": "..."}`: `invalid_request` (400, missing or duplicate credentials, malformed body), `unsupported_grant_type` (400), `invalid_client` (401 with `WWW-Authenticate: Basic realm="granite"`) for every credential failure. Client service unreachable → 503 `{"error":"temporarily_unavailable"}`.
- Token claims: `sub` = `client:<clientId>`, `authorities` = the verified authorities with any `SCOPE_*` and `ROLE_ADMIN` removed defensively, `jti` random, `iss` / `aud` the same values as session tokens, `exp` = now + `granite.session.client-token.lifetime` (default `PT15M`, startup fails above `PT1H`). No session record is created.
- Never logs the secret, the Authorization header or the token.

### 3. Gateway

- Route `/client-command-service/**` and `/client-query-service/**` (host properties `granite.client-command-service.host` / `granite.client-query-service.host`, like Feedback).
- Token endpoint: permitted without authentication in every environment and rate limited per remote address with its own limiter (`granite.gateway.client-token.rate-limit.requests`, `.window`, `.max-clients`; defaults as for `issue-service-token`).
- Revocation: tokens whose `sub` starts with `client:` skip the session revocation lookup, like `service:` tokens.
- Client tokens are refused (403) on `/frontendcontroller-*/**`, `/user-*-service/**`, `/session-*-service/**`, `/client-*-service/**` and `/outbox-service/**`, in every environment (my call: keeps Clients out of identity plumbing and user-only FEC operations, including Preferences and the current-session paths). Detection is by the verified JWT `sub` starting with `client:`. It is a WebFilter placed after authentication, so it applies whatever matcher later admits the path; the token endpoint is unaffected because its requests carry no bearer. The pattern list comes from an overridable `getClientDeniedPathPatterns(Environment)` hook (my call). The one exception is `POST /client-command-service/rotate-own-client-secret`, which Client tokens may reach.
- `POST /client-command-service/rotate-own-client-secret`: `ROLE_CLIENT_ADMIN` in every environment, placed before the production `SCOPE_internal` rule. Published in the production Ingress as an `Exact` path, next to the token endpoint.
- Core role: `ApplicationRolesManager` (Foundation) gains `CLIENT_ADMIN = "ROLE_CLIENT_ADMIN"` as a second core role present in every catalog. The root `CLAUDE.md` statement "Granite core owns only `ROLE_ADMIN`" becomes "`ROLE_ADMIN` and `ROLE_CLIENT_ADMIN`". A User holding only `ROLE_CLIENT_ADMIN` can sign in to ClientHost but not AppHost (see section 9).
- Rules for the new paths: `/client-query-service/**` readers, `/client-command-service/**` admins; in `production` both `SCOPE_internal` only, like the other services. `verify-client-credentials` therefore reaches Client only with Session's service token in production.
- No other new rules: Clients reach whatever their authorities allow under the existing rules and `configureApplicationAuthorization`. Consequence to document: in `production` Granite's own service paths require `SCOPE_internal`, so a Client reaches only the application paths that the application's Gateway authorizes for its roles.

### 4. FrontEndController

- Typed operations `get-clients-output` and `handle-clients-input` on both FEC services, modeled on `get-users-output` / `handle-users-input`. Gateway: `get-clients-output` to readers, `handle-clients-input` to admins.
- The output lists Clients and the role catalog (for the pick list, without `ROLE_ADMIN`). The input carries a change set (create, update, rotate, delete); a create or rotate reply carries the new `clientId` and `clientSecret` once. The FEC never logs inputs or outputs of these operations.
- For Admin, the output also lists Users (id and name, from User Query) for the owners pick list; owners are shown by user name.
- ClientAdmin operations: `get-owned-clients-output` (Client Query `find-clients-by-owner` with the verified caller's id) and `handle-owned-client-secret-input` `{clientId}` (Client Command `rotate-owned-client-secret` with `ownerUserId` = verified caller id). Gateway: both to `ROLE_CLIENT_ADMIN` and admins.
- No verified caller → a `not authenticated` failure output, no Client call (as for Users).
- The FEC reaches Client Command and Query through the Gateway with its service token, through overridable client-factory seams (`createClientCommandClient()`, `createClientQueryClient()`) on the FEC configuration. Exact placement (a new clients holder or the existing service-clients pattern) is left to the builder, following how User clients are wired.

### 5. Web: Clients view

New component `FrontEnds/Granite.Web/Components/Granite.Web.Client`, package `granite.web.client`, subpath `granite.web.client/Clients`: `ClientsView({ service?, height = "100%", className?, style? })`, default service `frontendcontroller-user-service`.

- Patterned on `UsersView` / `UsersEditableView`: same grid, toolbar, change-set save, authorities pick list with hierarchy cascade (`ROLE_ADMIN` not offered), self-contained styling from the Mantine theme.
- Columns: Name, Client ID (read-only, copy icon), Description, Authorities, Enabled, Secret expires, Last token issued (read-only), Created (read-only).
- Toolbar for `ROLE_ADMIN`: Add, Delete, Rotate secret (selected row, confirm dialog), Save. Readers see the grid read-only.
- Secret dialog after Save of a new Client or after Rotate: shows Client ID and secret with Copy buttons, a warning that the secret will not be shown again, a Download `.env` button (`GRANITE_CLIENT_ID=...` and `GRANITE_CLIENT_SECRET=...` lines, file name `<name>.env`), and Close. The secret lives only in that dialog's state.
- Owners column (Admin): pick list of Users, shown by user name.
- `granite.web.client/Owned`: `OwnedClientsView({ service?, height = "100%", className?, style? })`, backed by `get-owned-clients-output`: only the caller's Clients, read-only, owners column hidden, Rotate secret as the only action (same confirm and secret dialog, through `handle-owned-client-secret-input`). Used only by ClientHost.
- Navigator (AppHost): menu entry `Clients` after `Users`, shown to readers only, route `/clients` inside `RequireAuthentication`.
- Yarn build order: authentication, user, client, session, feedback, survey, content, agent, account, navigator. Test project `FrontEnds/Granite.Web/Tests/Granite.Web.ClientTest`, wired into `test-granite-web` and `test-granite`.

### 6. Deployment

- Kubernetes base: Deployments and Services for both Client hosts, Ingress paths `/client-command-service` and `/client-query-service` (local, development, test).
- Production overlay: removes those two paths (JSON-patch `test` + `remove`, like the others) and adds `/session-command-service/oauth2/token` (pathType `Exact`) to the Gateway.
- New tables must exist before rollout in `test` and `production` (`ddl-auto=validate`); development uses `ddl-auto=update` from the ORM mapping (no hand-written DDL).
- Outbox relays Client events like Feedback's (whatever Feedback needed in Outbox, Client needs too).
- Root `build.gradle` / `settings.gradle` / `package.json` gain Granite.Client like Feedback did.

### 7. Documentation

- `Microservices/Granite.Client/Claude.md` (overview, operations, seams, security).
- Root `CLAUDE.md`: Extending Granite (Client seams), role catalog (four hosts), UI building blocks (`granite.web.client/Clients`), call routing table (Session to Client via Gateway), Service tokens / API clients section, production Ingress, yarn build order.
- Session, Gateway and FEC `CLAUDE.md` updates.
- A short "Calling Granite as an API client" section with a `curl` example of getting a token, calling a path and rotating the Client's own secret, and how an application publishes a service path in its production overlay and authorizes it in `configureApplicationAuthorization`.

### 8. Applications and secrets in transit

- Each application runs its own Client hosts and its own Session, with its own catalog and its own JWT issuer and audience, so `client:` subjects are per application (consistent with "Several applications in one browser").
- The one-time secret travels Client → Gateway → FEC → browser as an ordinary reply. The plan includes an audit of request/reply logging in Strata clients (`CountedClient`, request-logging filters), the Gateway and the FEC for these operations, with suppression where anything logs bodies (my call). Replies are never written to the Outbox; only domain events are, and events carry no secret or hash.
- Schema for `test` and `production`: no migration tool and no hand-written DDL in the repo; the Client `Claude.md` lists the tables and columns, and the release note says they must exist before rollout (my call, matching how Feedback shipped).

### 9. ClientHost web app and application-bound sessions

**Application identity.** Two web applications share one Granite backend: `granite` (AppHost, default) and `client` (ClientHost). Each web app names itself in `window.__GRANITE_CONFIG__.application` (`config.js`, default `granite`); `granite.web.authentication` sends it with the login inputs (password and Okta start), the FEC passes it to Session. Session accepts only names in `granite.session.applications` (default `granite,client`), anything else → login failure. Trusting the name from the browser is safe because of the per-application login gate: naming `granite` still requires a non-ClientAdmin role, and naming `client` only restricts the session (my reasoning; no Host-header trust needed).

**Session binding.** `Session` and `OktaLoginTransaction` gain `application` (`VARCHAR(32)`, existing rows `granite`). The session JWT gains claim `app`; refresh keeps it. The admin Sessions view shows it as a column (my call).

**Per-application login gate.** `granite.session.login.applications.<app>.required-authorities` (comma list). `client` defaults to `ROLE_CLIENT_ADMIN`. `granite` keeps today's rule (explicit `granite.session.login.required-authorities`, else at least one catalog role) except that `ROLE_CLIENT_ADMIN` does not count as a qualifying role for `granite`.

**Per-application Okta redirect.** `granite.session.okta.redirect-uris.<app>`; `granite` falls back to the existing `granite.session.okta.redirect-uri`. The transaction stores the application so completion uses the same redirect URI. Local: `client` → `http://localhost:5174/login/callback` (my call: ClientHost dev server on 5174); other environments from `OKTA_REDIRECT_URI_CLIENT[_<ENV>]`.

**Gateway confinement.** A request whose verified session JWT has `app=client` may reach only `getClientApplicationPathPatterns(Environment)` (default: FEC `get-owned-clients-output`, `handle-owned-client-secret-input`, and the current-session inputs the authenticated matcher already admits such as logout and current-session output); anything else → 403. A WebFilter after authentication, like the Client-token filter. Tokens without `app` are treated as `granite`.

**ClientHost app.** `FrontEnds/Granite.Web/Applications/Granite.Web.ClientHost`: a copy of AppHost's build (Vite, nginx, `config.js.template`, Dockerfile), image `granite-web-clienthost`, with its own small frame: header with application title "Granite Clients" and `UserMenu` (no preferences link), routes `login`, `login/callback`, and `/` → `<RequireAuthentication><OwnedClientsView /></RequireAuthentication>`. `config.js` sets `application: "client"` and the same `cookiePrefix` as AppHost (cookies are host-scoped, so the two hosts never share cookies).

**Hostnames and Ingress.** `client.<app host>` per environment: `client.granite.localhost`, `client.granite-dev.<domain>`, `client.granite-test.<domain>`, `client.granite.<domain>` (my call for the non-production pattern). A second Ingress rule for that host routes the FEC paths to the Gateway and `/` to `granite-web-clienthost`; TLS hosts include it. Production publishes only the FEC user-service paths there.

## Constraints

- Granite conventions from the root and per-service `CLAUDE.md` files: `//////` headers/footers, no explanatory comments, no static inner classes, TypeScript `Array<T>`, build output only in `.build`.
- Build and test only with existing Gradle tasks and yarn scripts; new modules follow the existing build scripts without changing tooling.
- Never log secrets, hashes, Authorization headers or tokens.

## Left to the builder

- Internal class names, packages and DTO names beyond those listed, following Feedback and User.
- How the Session client for Client Command is wired, following the Session → User client.
- Grid layout details and dialog wording.
- Test breakdown, following the Review Focus in the plan.
