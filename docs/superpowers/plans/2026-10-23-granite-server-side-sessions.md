# Granite Server-Side Sessions Implementation Plan

> **For agentic workers:** this is a roadmap with seven steps. Before starting a step, write that step's detailed task-by-task plan with superpowers:writing-plans, saving it next to this file as `2026-xx-xx-granite-server-side-sessions-step-N.md`. Then execute it with superpowers:subagent-driven-development or superpowers:executing-plans. Check each step against its acceptance criteria before starting the next.

**Goal:** Keep all tokens and session data on the server, in the Session and User entities. The browser holds only opaque HttpOnly cookies, and the FrontEndController (FEC) is the only front-end entry point. The user sees one continuous session even when the session underneath is revoked and replaced.

**Architecture:** Design A, where the Gateway swaps the cookie for a token. The browser sends `GRANITE_SID` (opaque handle) and `GRANITE_DID` (device ID). The Gateway resolves the handle through Session `resolveSession`, injects `Authorization: Bearer <jwt>`, and routes to the FEC. Behind the Gateway, every service still sees ordinary JWTs, so the Gateway role rules and the services' JWT validation don't change. Session keeps the JWT, refresh token and Okta tokens, and re-mints the JWT itself until `max-lifetime`. Login and logout become FrontEndController inputs.

**Tech Stack:** Java 23, Spring Boot 3.4 and Spring Security (servlet in Session, User and FEC; reactive in the Gateway, which uses Spring Cloud Gateway), JUnit 5 and Mockito, React 19, TypeScript, Vitest, Okta (enterprise, OIDC).

**Spec:** this document. The design was agreed in conversation on 2026-10-22 and 2026-10-23, and the decisions are recorded below.

```
Browser ──GRANITE_SID / GRANITE_DID (HttpOnly)──► Gateway ──Bearer JWT──► FEC ──Bearer JWT──► User / Session
                                                    │ resolveSession(handle) (cached, fails closed)
                                                    └──────────────► Session CommandHost
Okta ◄── private_key_jwt code exchange, JWKS, end-session ── Session (Okta tokens stay here)
```

---

## Global Constraints

- **Java style** follows each component's `CLAUDE.md`:
  - `////` file header and footer.
  - `public` on its own line, then `class X`.
  - Return type on its own line.
  - Allman braces.
  - Fluent setters.
  - `(a,b)` with no space after commas.
- No static inner classes, and no explanatory comments.
- TypeScript uses `Array<T>`, not `T[]`. Every new Java request or reply type gets a TS mirror in the same service component.
- Build and test only with the existing Gradle tasks and Yarn scripts. Don't modify build tooling, wrappers or `package.json` scripts, and don't start or stop daemons.
- **Never run `jib` or `publish*` tasks** (including `publishToMavenLocal`) without the user's explicit approval. `--dry-run` doesn't stop included builds in the composite build from running.
- Java tests run from the microservice's directory, for example `cd Microservices/Granite.Session && ./gradlew :Tests:Granite.Session.ServerTest:test --tests "<class>"`.
- Authorization rules belong only in the Gateway's security chain. Don't add `@PreAuthorize` to User or Session.
- New security seams follow the existing entry-configuration pattern (Outbox-style). Shared security code goes in Granite.Foundation.Core.
- Allowed environments are `local`, `development`, `test` and `production` only. Production secrets use `${...}` placeholders, and keystores and truststores are supplied externally, not stored in the repo.
- **Never log or `toString()` a session handle, an access token, a refresh token, an Okta code, an Okta token or a PKCE verifier.**

## Decisions

| Topic | Decision |
|---|---|
| Token custody | The browser holds no tokens, only `GRANITE_SID` and `GRANITE_DID`, both HttpOnly. The JWT, refresh token and Okta tokens stay in Session. |
| Front-end entry point | The FEC only. Login, logout and session status are FrontEndController inputs. |
| Device | One browser profile, identified by `GRANITE_DID` |
| Users per device | One at a time (mechanism A). Shared workstations use separate OS accounts, and a shared tablet is used by one signed-in person at a time. |
| Sessions | One active session per (user, device), shared by every tab. |
| Lifetime | `max-lifetime` only, no idle timeout. Session re-mints the JWT until the cap. |
| Persistent state | State that must outlast sessions is stored by services in User (`User.values` or user-keyed storage in the owning service). `Session.values` is session-scoped. Server-side UI workflow state is owned by each microservice, not by Granite. |
| Replacement continuity | A replacement copies the session being replaced (`values` and `continuityId`), so to the user and to services it looks like one session. |
| "Start from latest" | Applies only to a user's first session on a device. It copies `values` from the user's most recently active open session on another device. Closed sessions are never a source. |
| Resolving a session | `resolveSession(ResolveSessionRequest {handle})` returns `ResolveSessionReply {session, accessToken}`, on the command side because it may re-mint the token. `SessionKey` is unchanged. |
| External sign-in | Enterprise Okta through a server-side authorization-code flow with PKCE `S256`. Session authenticates to Okta as a confidential client with `private_key_jwt`, and the key is in the external keystore. When another user's session is active on the device, the authorize request uses `prompt=login`. **Replaces Google sign-in.** |
| New Okta users | Created automatically with `ROLE_USER`. An admin adds other roles later. |
| Authorities | Configurable: `USER`, `OKTA_GROUPS` or `BOTH` (union), with a group-to-role mapping. Recalculated at login and whenever the token is re-minted. Never copied between sessions. |
| Password login | Every environment, with an on/off switch. Okta has its own switch. |
| Routes | Non-production: all direct routes stay. Production: only `/frontendcontroller-*`. Requests with a bearer header keep the current JWT validation and `jti` revocation check. |
| CSRF | `SameSite=Strict` plus a double-submit CSRF token at the Gateway |

## Cookies

| Cookie | Attributes | Contents and lifetime |
|---|---|---|
| `GRANITE_SID` | `HttpOnly; Secure; SameSite=Strict; Path=/` | Random 256-bit handle, base64url. Session stores only its hash. New on every session creation. Expires with the session. |
| `GRANITE_DID` | `HttpOnly; Secure; SameSite=Lax; Path=/` | Random device ID, issued on the first visit. One year. Replaces the `localStorage` device key. |
| `GRANITE_LOGIN` | `HttpOnly; Secure; SameSite=Lax; Path=/` | Okta login transaction ID. Short-lived (5 minutes). Cleared on completion. |
| CSRF token cookie | Spring `CookieCsrfTokenRepository` (readable by JavaScript, by design) | Double-submit token for state-changing requests |

## Session lifecycle

### New Session fields

| Field | Purpose |
|---|---|
| `hashedHandle` | Hash of the opaque handle, using the same hashing as `refreshTokenHash` |
| `continuityId` | UUID that stays the same across replacements. Exposed in `SessionData` and as the JWT claim `cid`. |
| `lastActivity` | Updated by `resolveSession`, at most once a minute. Used to pick the "latest" session. |
| `closedReason` | `LOGOUT`, `EXPIRED`, `REPLACED`, `DISPLACED`, `REVOKED_SECURITY` or `ADMIN` |
| `replacedBy`, `copiedFrom` | Links between sessions for audit and support |

### What happens at login for user U on device D

| Situation | Action | `values` | `continuityId` |
|---|---|---|---|
| U already has an active session on D (signing in again) | Replace: create a copy, close the old session as `REPLACED`, and set `replacedBy` and `copiedFrom` | Copied | Same |
| Another user V has an active session on D | Close V's session as `DISPLACED`, then continue with the rows below for U. Nothing passes between users. | n/a | n/a |
| U has no active session on D but has one elsewhere | Start from latest: copy U's most recently active open session (by `lastActivity`) | Copied | New |
| U has no active session anywhere | New, empty session | Empty | New |

- **Copied:** `values`, plus `continuityId` for a replacement.
- **Recalculated or new:** session ID, handle, refresh token, JWT, `expires` (now plus `max-lifetime`), authorities, and the sign-in method and Okta `id_token`.
- **Concurrency:** a uniqueness constraint on the active (user, device) pair plus optimistic locking (`version`). When two logins race, one succeeds and the other retries once, copying from the winner.
- **Events:** session close and revocation events include `closedReason`.

### What services and the SPA must do for continuity

- **Services:** key session-scoped workflow state by `continuityId` (from the `cid` claim), and key state that must persist by `userId` (stored in User).
- **SPA, password sign-in:** re-authenticate in a dialog without leaving the page, then retry the request that failed.
- **SPA, Okta sign-in:** the login transaction stores a return-to route.
- **SPA, after any 401:** retry once if `GRANITE_SID` has changed.

## `resolveSession` contract

| Item | Definition |
|---|---|
| Interface | `ISessionCommandService.resolveSession(ResolveSessionRequest request)` returns `CompletionStage<ResolveSessionReply>` in Java and `ICompletionStage<ResolveSessionReply>` in TS |
| Endpoint | `POST /session-command-service/resolve-session` |
| Request | `ResolveSessionRequest extends AbstractServiceRequest { handle: String }`, with fluent setter and getter. Its `toString()` never prints the handle. |
| Behavior | 1. Reject a blank handle. 2. Hash it and look up `hashedHandle`. 3. Fail if the session is missing or closed. If `max-lifetime` has passed, close it as `EXPIRED` and fail. 4. Inside the renewal margin, re-mint the JWT (never beyond `max-lifetime`) and recalculate authorities. 5. Update `lastActivity` (at most once a minute). 6. Return the reply. |
| Reply | `ResolveSessionReply extends AbstractServiceReply { session: SessionData, accessToken: String }`. `SessionData` gains `continuityId` and never carries a token. |
| Failure | `success=false` with a generic `failureMessage` ("invalid session"). Don't reveal whether the handle was unknown, closed or expired. |
| Caller | The Gateway, authenticated with the Phase 2 service token |

## FrontEndController inputs and outputs

| Key | Type | Input | Output |
|---|---|---|---|
| `Session.New->LoginView` | get-output (public) | n/a | `LoginMethodsOutput {passwordEnabled, oktaEnabled}` |
| `Session.New->Login->LoginView` | handle-input (public) | `LoginInput {userName, password}` | `SessionOutput {userName, authorities, status}`; sets `GRANITE_SID` |
| `Session.New->StartOktaLogin->LoginView` | handle-input (public) | `StartOktaLoginInput {returnTo}` | `RedirectOutput {url}`; sets `GRANITE_LOGIN` |
| `Session.New->CompleteOktaLogin->LoginView` | handle-input (public) | `CompleteOktaLoginInput {code, state}` | `SessionOutput` plus `returnTo`; sets `GRANITE_SID` and clears `GRANITE_LOGIN` |
| `Session.Current->SessionView` | get-output | n/a | `SessionOutput` |
| `Session.Current->Logout->LoginView` | handle-input | `LogoutInput {}` | `LogoutOutput {endSessionUrl?}`; clears `GRANITE_SID` |

Each subtype must have at least one property name no sibling has, and must be registered in `FrontEndJacksonModule` (see the FEC `CLAUDE.md`). Register these on both FEC hosts: they go through the request processors, and the Gateway decides which paths are public.

## Configuration

| Property | Where | Purpose |
|---|---|---|
| `granite.session.login.password.enabled` | Session, FEC | Turns password login on or off (default `true`) |
| `granite.session.login.okta.enabled` | Session, FEC | Turns Okta login on or off (default `false` until configured) |
| `granite.session.okta.issuer` | Session | For example `https://{org}.okta.com/oauth2/{authServerId}` |
| `granite.session.okta.client-id` | Session | Okta app client ID |
| `granite.session.okta.redirect-uri` | Session | The SPA's `/login/callback` URL |
| `granite.session.okta.keystore.*` | Session | `private_key_jwt` signing key (external keystore, `${...}` in production) |
| `granite.session.okta.authorities-source` | Session | `USER`, `OKTA_GROUPS` or `BOTH` |
| `granite.session.okta.group-mapping.<group>` | Session | Maps an Okta group to a Granite role, for example `=ROLE_ADMIN` |
| `granite.session.token.renewal-margin` | Session | How close to expiry a JWT must be before it's re-minted (default `PT2M`) |
| `granite.gateway.session-cache-ttl` | Gateway | Upper limit on how long a resolved handle is cached (default `PT60S`; the actual time is the smaller of this and the time until the token expires) |

---

## Step 1: Session handle and `resolveSession`

**Component:** Granite.Session (Service, Server, Client: Java and TS). **Model:** Opus.

- [ ] Add `hashedHandle` to `Session` and its ORM mapping. Login generates a handle (256-bit, base64url), stores its hash and returns the raw handle once, internally to the caller of login. The FEC uses it in Step 4.
- [ ] Add `ResolveSessionRequest {handle}` with a `toString` that hides the handle, and `ResolveSessionReply {session, accessToken}`, in Java and TS.
- [ ] Add `resolveSession` to `ISessionCommandService`, `SessionCommandService` (pipeline: validate, look up, check lifetime, renew, record activity, build reply), `SessionCommandServiceController` (`/resolve-session`), the Java client and the TS client.
- [ ] Renewal inside `granite.session.token.renewal-margin`, capped at `max-lifetime`, with authorities recalculated through User.
- [ ] Add `lastActivity` and update it at most once a minute.

**Acceptance criteria:**
- A valid handle returns `SessionData` plus `accessToken`.
- A blank, unknown, closed or expired handle fails with the generic message, and an expired session is closed as `EXPIRED`.
- Renewal happens only inside the margin and never past `max-lifetime`.
- Authorities changed in User appear after renewal.
- The handle never appears in logs or in any `toString`.
- Existing Session tests still pass.

## Step 2: Session lifecycle and continuity

**Component:** Granite.Session. **Model:** Opus.

- [ ] Add `continuityId` (also in `SessionData`, and as the `cid` claim minted by `JwtTokenService`), `closedReason`, `replacedBy` and `copiedFrom`.
- [ ] Login applies the lifecycle table: replace, displace, start from latest (open sessions only, by `lastActivity`), or new.
- [ ] Uniqueness constraint on the active (user, device) pair, plus optimistic locking with one retry.
- [ ] Session close and revocation events include `closedReason`. Logout closes as `LOGOUT`, and refresh-token reuse closes as `REVOKED_SECURITY`.

**Acceptance criteria:**
- Signing in again on the same device gives a new session with the same `continuityId` and copied `values`, and the old session is `REPLACED` with the links set.
- Another user on the device makes the old session `DISPLACED`, and nothing passes between users.
- A first session on a new device copies the latest **open** session and gets a new `continuityId`.
- Closed sessions are never a source.
- Two racing logins leave exactly one active session.
- Authorities are never copied.

## Step 3: Gateway cookie-to-token filter

**Component:** Granite.Gateway. **Model:** Opus.

- [x] A `WebFilter` that reads `GRANITE_SID`, calls `resolveSession` (Session client with the service token), injects `Authorization: Bearer`, removes `GRANITE_SID` from the forwarded request, caches the result (the smaller of the token's remaining life and `granite.gateway.session-cache-ttl`), and fails closed with 401.
- [x] Issue `GRANITE_DID` on the first visit when it's missing.
- [x] CSRF double-submit for state-changing requests that carry cookies.
- [x] Public paths: the Session login and `LoginView` inputs, the health endpoints and the static app.
- [x] Routes by environment: non-production unchanged; production only `/frontendcontroller-*`.
- [x] Bearer-header requests keep the existing JWT plus `jti` revocation path.

**Acceptance criteria:**
- A cookie request reaches the FEC with a valid JWT and without `GRANITE_SID`.
- An unknown, closed or expired handle gets 401.
- A missing or wrong CSRF token gets 403.
- The production profile exposes only `/frontendcontroller-*`.
- The existing Gateway role-rule tests and bearer-header tests pass unchanged.

## Step 4: FEC Session processor (password)

**Component:** Granite.FrontEndController (Service and Server: Java and TS). **Model:** Sonnet.

- [ ] Input and output types for `LoginView`, `Login`, `SessionView` and `Logout` (Java and TS), registered in `FrontEndJacksonModule`.
- [ ] A Session request processor and its actions, with Session clients built from concrete instances. Don't use request-scoped proxies inside async continuations; see the Save bug from 2026-10-22.
- [ ] Set and clear `GRANITE_SID`, and pass `GRANITE_DID` to Session login.
- [ ] Honor `granite.session.login.password.enabled`.

**Acceptance criteria:**
- Password login sets `GRANITE_SID`, and the reply has no token.
- `SessionView` reflects the session.
- Logout closes the session as `LOGOUT` and clears the cookie.
- When password login is disabled, it's refused and missing from `LoginMethodsOutput`.

## Step 5: Okta

**Components:** Granite.Session and Granite.FrontEndController. **Model:** Opus.

- [ ] Before starting, confirm that User `CreateUserData` and `createUsers` support `externalIdentity`. If they don't, add that as a first task.
- [ ] Session: an Okta login transaction (`state`, `nonce`, PKCE verifier, `returnTo`, 5-minute expiry, stored on the server and bound to `GRANITE_LOGIN`).
- [ ] Authorize URL (`response_type=code`, `S256`, `prompt=login` when another user is active on the device).
- [ ] Code exchange with a `private_key_jwt` client assertion.
- [ ] ID-token validation: `iss`, `aud`, `nonce`, `exp` and signature through Okta's JWKS.
- [ ] Map `okta:{sub}` through `externalIdentity`, creating the User with `ROLE_USER` if it doesn't exist.
- [ ] Authorities mode and group mapping.
- [ ] Keep the Okta `id_token` for logout and build the end-session URL.
- [ ] FEC: `StartOktaLogin` and `CompleteOktaLogin` with `GRANITE_LOGIN`, honoring `granite.session.login.okta.enabled`.
- [ ] Remove the Google sign-in path from Session (the `loginWithOauth2` Google validation).

**Acceptance criteria:**
- Against a mocked Okta (authorize, token and JWKS endpoints), a wrong or missing `state`, `nonce`, `aud`, `iss` or signature is rejected.
- First login creates the User with only `ROLE_USER`.
- `USER`, `OKTA_GROUPS` and `BOTH` each give the expected authorities.
- The Step 2 lifecycle rules apply.
- Logout returns the end-session URL with `id_token_hint`.

## Step 6: Web

**Component:** Granite.Web (Authentication and Navigator). **Model:** Sonnet.

- [ ] Replace the internals of `Granite.Web.Authentication` with a session-status hook built on `SessionView` (the FEC client).
- [ ] Login view showing the enabled methods (from `LoginMethodsOutput`), the password form, the Okta button and the `/login/callback` route that sends `CompleteOktaLogin`.
- [ ] Password re-authentication dialog that retries the failed request; retry once after a 401 when `GRANITE_SID` has changed; Okta `returnTo`.
- [ ] Remove `SessionApi`, `ISessionApi`, `TokenStorage`, the refresh scheduler, the Google SDK and `getRuntimeConfig().googleClientId`.
- [ ] Remove the `Authorization` header handling from the Navigator's FEC clients (the Gateway injects it).

**Acceptance criteria:**
- Vitest: no tokens in browser storage.
- The password, Okta callback, re-authentication and logout flows work through a faked FEC client.
- Disabled methods aren't shown.
- The existing Users-screen tests pass.

## Step 7: End to end (local cluster)

**Model:** Sonnet (checklist).

- [ ] Password login, and Okta login against a development Okta org.
- [ ] Sign in again on the same browser: `continuityId` stays the same and `values` are kept.
- [ ] A second user on the same browser: the first user's session is `DISPLACED`.
- [ ] A second device: starts from latest, with a new `continuityId`.
- [ ] A Users save works through the cookie path.
- [ ] Logout, including Okta's end-session.
- [ ] The production profile exposes only `/frontendcontroller-*`.

**Acceptance criteria:** browser devtools show only the HttpOnly cookies `GRANITE_SID` and `GRANITE_DID` (plus the CSRF cookie), and no tokens anywhere JavaScript can read them. Every item above passes.

---

## Dependencies

1 → 2 → 3 → 4 → 5 → 6 → 7. Step 5 also needs Step 4's Session processor.

## Interactions with earlier work

- **Login gate** (`docs/superpowers/plans/2026-10-21-granite-login-roles.md`): `granite.session.login.required-authorities=ROLE_ADMIN,ROLE_AUDITOR` is set in local and development. Automatically created Okta users with only `ROLE_USER` will be refused there unless an admin assigns a role first, or the gate configuration changes. **Decide this before Step 5.**
- **Google sign-in removal:** the 2026-10-21 change stopped automatic user creation on first Google sign-in. Okta reintroduces automatic creation with `ROLE_USER`, as decided above.
- **Phase 1 to 3 security work:**
  - Refresh-token rotation and reuse detection remain inside Session; reuse closes the session as `REVOKED_SECURITY`.
  - The Gateway's `jti` revocation check remains for bearer-header requests.
  - The expired-session cleanup job can keep deleting closed sessions, because closed sessions are never copied.
- **FEC request scope:** the Users Save fix (concrete clients rather than scoped proxies in `userRequestProcessor()`) should be in place before Step 4, so the new Session processor follows the same pattern.

## Open items (decide when the relevant step starts)

1. Login gate versus Okta automatic creation — **decided 2026-10-24:** keep the gate. Okta still creates the User with `ROLE_USER`, but login is refused until an admin grants a gated role. The gate applies equally to password and Okta login.
2. Gateway handle cache — **decided 2026-10-24:** TTL only (at most 60 seconds). Session close events do not evict the cache.
3. Okta back-channel logout: out of scope for now, a candidate follow-up.
4. Revoking all of a user's sessions when the account is locked or deleted: a candidate follow-up.
5. Gateway service token — **open (found in Step 3):** the Gateway has no Phase 2 service token (only Session can mint one; the `jti` check forwards the caller's own bearer). Step 3 added the `serviceTokenSupplier` seam, whose default fails closed, so cookie requests get 401 until this is decided (e.g. Session trusts a Gateway signing key, a client-credentials exchange, or making `/resolve-session` reachable only in-cluster).

## Out of scope

- Server-side UI workflow state (owned by each microservice).
- Login rate limiting and the Gateway actuator lockdown (deferred earlier).
- Okta back-channel logout.
- Sign-in providers other than Okta.
