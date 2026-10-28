# Gateway-Centred Security — Design

Date: 2026-10-28
Branch: `feature/gateway-centred-security`

## Intent

The single Gateway performs all authentication and all Spring Security role/path authorization. Service hosts keep only a minimal JWT check (defence in depth against calls that bypass the Gateway) and contain no authorization rules. All service-to-service HTTP calls are routed through the Gateway.

## Decisions (user)

- One Gateway (no separate internal gateway).
- Service hosts keep a `SecurityFilterChain` limited to the JWT check: valid signature (Session JWKS), unexpired, expected issuer. No `hasAuthority`/`hasRole`, no per-path role config.
- JWKS fetches stay direct (Gateway and services → Session Command `/.well-known/jwks.json`).
- FEC service identity: Session client-credentials endpoint issuing short-lived `SCOPE_internal` tokens; FEC credentials from a Kubernetes Secret (recommended option, approved with the plan).

## Call routing

| Call | Route |
|---|---|
| Browser → services | Gateway (unchanged) |
| FEC → User Query/Command, Session Command/Query | Gateway, FEC service token |
| Session → User | Gateway, Session service token (existing `ServiceTokenProvider`) |
| Gateway → Session `resolve-session`, revocation check | direct (the Gateway's own calls) |
| Gateway and services → Session JWKS | direct |
| Session → Okta; services → Postgres/Kafka | direct (not Gateway traffic) |

## Service-host JWT check

- Shared configuration in Granite.Foundation (servlet), imported by User, Session and FEC hosts; Outbox host if it exposes HTTP routes.
- Modes: `REQUIRED` (User, Session, Outbox) — a request without a valid bearer is rejected except on tokenless paths; `OPTIONAL` (FEC) — a bearer, if present, must be valid; anonymous requests pass because the Gateway already authorized FEC public operations.
- Tokenless paths: health, `/error`, API docs outside production; Session Command adds `login`, `refresh`, `resolve-session`, `/.well-known/jwks.json`.
- Session Command validates with its own key material; other hosts fetch JWKS from Session directly.
- FEC derives the caller from the verified token (replaces the current unverified payload decode in `SessionRequestContext`).

## Gateway

- `start-okta-login`, `complete-okta-login`: `denyAll` → `hasAuthority("SCOPE_internal")`.
- `resolve-session`: stays `denyAll`.
- Production registers the User, Session and Outbox routes; in production their paths require `SCOPE_internal` only (browser sessions cannot use them; admins work through the FEC).
- Session's `X-Forwarded-For`-dependent logic trusts the header only from the Gateway.

## FEC service identity

- Session Command `POST /session-command-service/issue-service-token` (name may be adjusted): client id + secret (secret stored hashed or compared in constant time; from configuration/Secret), returns a short-lived token with `sub` = client id and authorities `["SCOPE_internal"]`. Tokenless at the Session host; public path at the Gateway; credentials and tokens never logged.
- FEC caches the token (renew before expiry, like `ServiceTokenProvider`) and sends it on User/Session calls; FEC continues to read the caller from the user's verified JWT for its caller-only checks.

## Network

- Kubernetes NetworkPolicy per service: ingress only from the Gateway; Session additionally from services for JWKS; egress to Postgres/Kafka as needed.
- FEC client credentials in a Kubernetes Secret.

## Non-goals

- Mutual TLS between Gateway and services (optional later).
- Changing browser-facing behaviour.

## Status
- Implemented 2026-10-28.
