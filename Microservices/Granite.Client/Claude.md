# Granite.Client Microservice

## Overview

Granite.Client stores API Clients: non-interactive identities (scripts, integrations, other systems) that obtain a short-lived access token with the OAuth2 `client_credentials` grant and call the application's own Gateway paths. A Client has a name, a description, catalog roles (`authorities`), owners (User ids), an `enabled` flag, an optional secret expiry and the SHA-256 of its secret. Administrators (`ROLE_ADMIN`) manage Clients in the Clients grid; users holding `ROLE_CLIENT_ADMIN` see and rotate the Clients they own in ClientHost. The service mirrors Granite.Feedback: CQRS hosts, Service/Server/Client components (Java and TypeScript), entry configurations with seams, and `ClientEvent`s (`ClientCreated`, `ClientUpdated`, `ClientDestroyed`) published through the Transactional Outbox (topic `granite.clientevent.avro`). Events carry no secret or hash.

Session verifies credentials against this service and mints the token (`POST /session-command-service/oauth2/token`, see the root `CLAUDE.md`, "API clients", and `Microservices/Granite.Session/Claude.md`).

## Project Structure

```
Granite.Client/
├── Applications/
│   ├── Granite.Client.CommandHost/   # port 8097, /client-command-service
│   └── Granite.Client.QueryHost/     # port 8098, /client-query-service
├── Components/
│   ├── Granite.Client.Service/       # contracts, DTOs, ClientEvent, TS mirror (granite.client.service)
│   ├── Granite.Client.Server/        # domain, application, platform, configuration
│   └── Granite.Client.Client/        # REST clients (Java + TS granite.client.client), Kafka receivers
└── Tests/
```

Images: `granite-client-commandhost` and `granite-client-queryhost`.

## Contract (Granite.Client.Service)

- `ClientData {clientId, name, description, authorities, owners, enabled, secretExpiresAt, lastTokenIssuedAt, createdAt, ...}`. It never carries the secret or its hash. The secret is returned only in `CreateClientReply.clientSecret`, `RotateClientSecretReply.clientSecret` (also used by `rotate-owned-client-secret`) and `RotateOwnClientSecretReply.clientSecret`; every DTO carrying a secret overrides `toString` to omit it.
- `clientId` is `gc_` plus 32 lowercase base32 characters (`a-z2-7`, 20 random bytes) and never changes. The secret is 32 `SecureRandom` bytes, base64url without padding, stored only as SHA-256 and compared in constant time; an unknown id is compared against a fixed dummy hash.
- `name` is required, unique, at most 100 characters; `description` at most 1000.
- `authorities` are catalog roles only (`ApplicationRolesManager`), stored closed under the role hierarchy. `ROLE_ADMIN` and `SCOPE_*` are rejected, and so is a role whose expansion contains `ROLE_ADMIN` (`ClientAuthorityPolicy`); on update only newly added roles are checked against the catalog.
- Secret expiry (`ClientSecretExpiry`): the UI edits a date, stored as the `Instant` at the start of the following day UTC. The secret is valid while `now < secretExpiresAt`; null means no expiry.
- Commands (`IClientCommandService`, `/client-command-service/`):

| Endpoint | Request → Reply | Behavior |
|---|---|---|
| `POST /create-client` | `CreateClientRequest {client: CreateClientData {name, description, authorities, owners, enabled, secretExpiresAt}}` → `CreateClientReply {client, clientSecret}` | one Client per call |
| `POST /update-clients` | `UpdateClientsRequest {clients: List<UpdateClientData {clientId, name, description, authorities, owners, enabled, secretExpiresAt, version}>}` → `UpdateClientsReply {clients}` | admin change; the secret is untouched |
| `POST /rotate-client-secret` | `RotateClientSecretRequest {clientId, secretExpiresAt}` → `RotateClientSecretReply {client, clientSecret}` | admin path; the old secret stops working at once |
| `POST /rotate-owned-client-secret` | `RotateOwnedClientSecretRequest {clientId, ownerUserId}` → `RotateClientSecretReply` | rotates only when `ownerUserId` is in `owners`, otherwise the same generic `not found` as an unknown id; `secretExpiresAt` unchanged. The FEC fills `ownerUserId` from the verified caller |
| `POST /rotate-own-client-secret` | `RotateOwnClientSecretRequest` (no fields) → `RotateOwnClientSecretReply {clientId, clientSecret}` | the Client is taken from the verified JWT `sub` (`client:<clientId>`, `JwtCallerSubject`); any other subject fails. `secretExpiresAt` unchanged. Tokens issued with the old secret live until they expire |
| `POST /destroy-clients` | `DestroyClientsRequest {clientIds}` → `DestroyClientsReply` | |
| `POST /verify-client-credentials` | `VerifyClientCredentialsRequest {clientId, clientSecret}` → `VerifyClientCredentialsReply {valid, clientId, authorities}` | used by Session. Disabled, expired, unknown and wrong secret all give `valid=false` with the same generic reason. The authorities are the stored ones intersected with the current catalog, minus `ROLE_ADMIN` and `SCOPE_*`. On success `lastTokenIssuedAt` is set best-effort with a direct update that does not bump `version` and fires no event; its failure is logged at debug and never fails the verification |

- Queries (`IClientQueryService`, `/client-query-service/`): `POST /find-all-clients` (`FindAllClientsRequest`, ordered by name), `POST /find-clients-by-ids` (`FindClientsByIdsRequest {clientIds}`), `POST /find-clients-by-owner` (`FindClientsByOwnerRequest {userId}`, ordered by name); replies are `FindClientsReply {clients}`.
- Ownership checks here are data rules (who owns which record). Which caller may reach which path is decided only in the Gateway.

## Server

- `domain`: `Client` (`AbstractEntityAndDomainEventSource`), `Client.orm.xml`, `IClientRepository`/`ClientRepository`, `LongSetConverter` (owners), domain events `ClientCreated`, `ClientUpdated`, `ClientDestroyed`.
- `application`: `ClientCommandService`, `ClientQueryService`, `ClientCredentials`, `ClientSecretExpiry`, `ClientAuthorityPolicy`, `MappingModule`, `ApplicationConfiguration`.
- `platform`: controllers, `JwtCallerSubject`, `OutboxEventClientEventSender`, `ClientEventToOutboxEventMapper`, `PlatformConfiguration`/`CommandPlatformConfiguration`/`QueryPlatformConfiguration`.
- `configuration` (seams, overridden with `@Bean` like User and Feedback): `AbstractGraniteClientServerConfiguration` (`configuration(Environment)`, `mapper()`/`configureMapper(ModelMapper)`, `applicationRoles()` and `applicationRolesStartupLog(ApplicationRolesManager)`, `clientCallerSubject()`, `clock()`, `securityFilterChain` (JWT check, `REQUIRED`), `jwtDecoder`, `openApi`, `getTokenlessPathPatterns(Environment)`, `createJwkSetRestOperations(...)`), `GraniteClientCommandServerConfiguration` (`clientEventSender(IOutboxEventRepository)`), `GraniteClientQueryServerConfiguration`. An application returns its own catalog from `applicationRoles()` (see "Role catalog" in the root `CLAUDE.md`).

## Hosts, tables and environments

Both hosts serve HTTPS with the `granite-services` bundle and validate Session JWTs (`granite.security.jwt.jwk-set-uri`). They use the shared database of each environment (see the root `CLAUDE.md`). `development` and `local` create schema from the ORM mapping (`ddl-auto=update`). `test` and `production` use `ddl-auto=validate`, so before rollout:

- Table `Client` (`Client.orm.xml`, `SINGLE_TABLE` inheritance like the other entities): `primaryId` (identity key), `created`, `lastModified`, `clientId` `VARCHAR(64)` unique not null, `name` `VARCHAR(100)` unique not null, `description` `VARCHAR(1000)`, `authorities` `VARCHAR(1024)`, `owners` `VARCHAR(1024)`, `enabled` not null, `secretSha256` (32 bytes), `secretExpiresAt`, `lastTokenIssuedAt`, `version`; plus whatever discriminator column the JPA provider generates for the other `SINGLE_TABLE` entities.
- Column `application VARCHAR(32)` (nullable; null reads as `granite`) on the Session tables `Session` and `OktaLoginTransaction`.

The Gateway routes `/client-command-service/**` and `/client-query-service/**` (`granite.client-command-service.host`, `granite.client-query-service.host`) and authorizes `/client-query-service/**` for users readers and `/client-command-service/**` for `ROLE_ADMIN` (`SCOPE_internal` only in `production`). Exception: `POST /client-command-service/rotate-own-client-secret` needs `ROLE_CLIENT_ADMIN` in every environment, and Client tokens (`sub` starting with `client:`) are refused on every other path of this service. The FEC calls both hosts through the Gateway with its service token (`GraniteClientServiceClients`: Clients grid for admins, owned Clients for ClientAdmins); Session calls Client Command `verify-client-credentials` through the Gateway with its service token. Outbox relays `ClientEvent`s like the other services' events.

## Style

Same as Granite.Feedback (`//////` header/footer, `class` on its own line, return types on their own line, Allman braces, fluent setters, `(a,b)` call style, `Array<T>` in TypeScript).
