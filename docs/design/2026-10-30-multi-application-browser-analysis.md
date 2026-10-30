# Multiple Granite Applications in One Browser — Analysis

Date: 2026-10-30
Status: analysis; all recommendations (R1–R6) selected for the design on 2026-10-30

## Question

Several applications derived from Granite (each with its own Gateway, User and Session) will run at the same time in the same browser. Each has its own login, users and authorities, and they share Okta single sign-on. Does the current `GRANITE_SID` cookie design support this, or does the system need a redesign?

## Short answer

It already works when each application is served from its own hostname, for example `granite.localhost` and `poplar.localhost`. No redesign is needed. It breaks when two applications share a hostname, whether behind different paths or on different ports. Cheap hardening (below) makes the separation robust.

## How the browser separates the applications today

Granite's browser state is held in four cookies. All are `Secure`, `Path=/` and have **no `Domain` attribute**, so each belongs only to the exact host that set it.

| Cookie | Set by | Other attributes |
|---|---|---|
| `GRANITE_SID` (session handle) | FEC `SessionCookies.issue` | `HttpOnly`, `SameSite=Strict`, `Max-Age` = session lifetime |
| `GRANITE_LOGIN` (Okta transaction) | FEC `SessionCookies.issueLogin` | `HttpOnly`, `SameSite=Lax`, 5 minutes |
| `GRANITE_DID` (device id) | Gateway `DeviceCookieWebFilter` | `HttpOnly`, `SameSite=Lax` |
| `XSRF-TOKEN` | Gateway `createCsrfTokenRepository()` | readable by script, `SameSite=Strict` |

Because the cookies are host-only, `granite.localhost` and `poplar.localhost` each get their own copies. The browser never sends one application's cookie to the other's Gateway. The server side is separate too: each application has its own Gateway, Session, User, signing keys and user records, so a handle or token from one application means nothing to another. The cached theme in `localStorage` is stored per origin, so it doesn't collide either. The local cluster already runs this way (`granite.localhost`, `poplar.localhost`, `admin.poplar.localhost`).

## Where it breaks

| Setup | Result |
|---|---|
| Same host, different paths (`apps.example.com/granite`, `/poplar`) | Same cookie names with `Path=/`, so the last login overwrites `GRANITE_SID`. Each Gateway receives the other application's handle, `resolve-session` fails, and both applications keep showing re-login prompts. `XSRF-TOKEN` and `GRANITE_DID` collide the same way. |
| Same host, different ports (local Vite on `localhost:5173` and `localhost:5174`) | Cookies are not separated by port, so this is the same collision. |
| Sibling subdomains (`granite.example.com`, `poplar.example.com`) | Works. One risk remains: a compromised or careless app on any other subdomain of `example.com` can set `GRANITE_SID` with `Domain=example.com` (cookie tossing). The browser then sends both cookies and the Gateway may read the wrong one. |
| `poplar.localhost` and `admin.poplar.localhost` | Separate sessions and separate logins. Okta SSO hides this for Okta users; password users log in twice. |
| Two Okta logins started at once on the same host | `GRANITE_LOGIN` is overwritten and the first login fails. Only possible when applications share a host. |

## Okta single sign-on with separate application sessions

The current model already matches the target:

- **One Okta app per application.** Each has its own `granite.session.okta.client-id` and `redirect-uri`.
- **One Okta login for all applications.** Okta's own session cookie, on the Okta domain, gives single sign-on: the second application's authorize redirect returns without a password prompt.
- **Separate users and roles per application.** Each application's Session creates its own user record and maps Okta groups to its own roles (`ON_CREATE` or `EVERY_LOGIN`, with the application's default role when no group maps).
- **`prompt=login` doesn't block SSO.** Session forces the Okta password prompt only when the same device already has a live session in that application (`SessionCommandService.startOktaLogin`, `findOpenByDeviceId`). `GRANITE_DID` is per host, so another application's session never triggers it.

Open points:

1. **What logout means.** For an Okta session, logout sends the browser to Okta's end-session URL with `id_token_hint` (`OktaClient`). That ends the Okta login for every application. The other applications keep their own sessions, but their next Okta login asks for the password.
2. **Okta logout doesn't reach the applications.** Ending the Okta session doesn't end Granite sessions; they last until they expire. Propagating it would need back-channel logout, which Okta offers only in some products (Universal Logout). The alternative is to accept this and keep session lifetimes short.
3. **Issuer and audience are optional** on the JWT check (`GraniteResourceServerSettings`). If applications ever share a network, a service token from one application could be replayed against another's hosts unless each application sets its own issuer and audience.

## Recommended changes

| # | Change | Effect | Cost and rollout |
|---|---|---|---|
| R1 | Document "one hostname per application" as a deployment rule, including local dev (`granite.localhost`, `poplar.localhost`, never plain `localhost` ports) | Makes multiple applications work today | Documentation only. Vite dev may need a host name per application. |
| R2 | Add the `__Host-` prefix to the cookie names (`__Host-GRANITE_SID`, and so on) | The browser refuses a cookie with that name if it comes from another subdomain or carries a `Domain` attribute, which closes the cookie-tossing risk | The current attributes already meet the prefix rules (`Secure`, `Path=/`, no `Domain`). Users must log in once after rollout, because the old names stop being read. |
| R3 | Make the cookie names configurable per application (for example `granite.session.cookie-prefix=POPLAR` giving `__Host-POPLAR_SID`) | Makes a shared host safe, and makes each application's cookies easy to identify in the browser's dev tools | The names are hard-coded in the FEC `SessionCookies`, the Gateway `SessionCookieWebFilter`, `DeviceCookieWebFilter` and CSRF repository, and the web `CsrfFetch` (which would read the name from `__GRANITE_CONFIG__`). Users must log in once. Path-based co-hosting is still not recommended: the applications would share an origin, so a script injection in one could read the others' data. |
| R4 | Make Okta logout a Session setting, `granite.session.okta.logout=APPLICATION\|GLOBAL`, defaulting to `APPLICATION` | Logging out of one application no longer logs the user out of Okta for all of them | Small change in Session and the FEC logout output. The web app already handles a missing `endSessionUrl`. |
| R5 | Require each application to set its own JWT issuer and audience | Tokens can't be replayed across applications | Configuration in every host profile, plus making issuer and audience required (or warned about) in production. Existing tokens stop validating at rollout; sessions must be refreshed. |
| R6 | Accept that Okta logout doesn't propagate, and document it (as opposed to building back-channel logout) | Keeps scope small; the risk is bounded by session lifetime | Documentation only. Back-channel logout can be revisited if Okta Universal Logout becomes available. |

## Recommendation

R1 is the minimum and makes the target work now. R2 and R4 are cheap and close the main risks. R3 is worth doing only if a shared host is a real possibility. R5 is worth doing while the applications are still young. R6 is the pragmatic choice for logout propagation.

## Why R2 matters: cookie tossing between sibling subdomains

Applications on `granite.example.com`, `poplar.example.com` and any other `*.example.com` host are the same site, so `SameSite` does not separate them. Script on any of those hosts (a hostile page, or a cross-site scripting bug in another Granite-derived application) can run:

```js
document.cookie = "GRANITE_SID=<attacker's own handle>; Domain=example.com; Path=/frontendcontroller-user-service; Secure";
```

The browser then sends both cookies to Granite, with the attacker's first because its path is longer: `Cookie: GRANITE_SID=<attacker's>; GRANITE_SID=<victim's>`.

- **Session swap (login CSRF).** The Gateway resolves the attacker's session. Anything the victim saves goes into the attacker's account.
- **CSRF bypass.** The attacker tosses `XSRF-TOKEN=known` with `Domain=example.com`, then sends same-site requests with `X-XSRF-TOKEN: known`. The victim's `SameSite=Strict` session cookie goes along, and the double-submit check passes.
- **Denial of service.** A tossed garbage `GRANITE_SID` or `GRANITE_DID` fails every request with 401 until the user clears their cookies.

A `__Host-` cookie is accepted only when it is `Secure`, has `Path=/` and has no `Domain`. So the tossed `Domain=example.com` or non-root-path cookies are rejected, and all three attacks stop. Browsers treat `localhost` and `*.localhost` as secure, so local dev keeps working.

## Selected for the design

| # | Decision |
|---|---|
| R1 | One hostname per application, including local dev; documented as a deployment rule. |
| R2 | Cookies use the `__Host-` prefix. |
| R3 | Cookie names are configurable per application. Combined with R2, the default names are `__Host-<PREFIX>_SID`, `__Host-<PREFIX>_DID`, `__Host-<PREFIX>_LOGIN` and `__Host-<PREFIX>-XSRF-TOKEN`, with prefix `GRANITE` by default. The same setting drives the FEC, the Gateway and the web app (through `__GRANITE_CONFIG__`). |
| R4 | `granite.session.okta.logout=APPLICATION\|GLOBAL`, default `APPLICATION`. |
| R5 | Each application sets its own JWT issuer and audience; required in production. |
| R6 | Okta logout does not propagate to application sessions; documented and accepted, bounded by session lifetime. |

Rollout: R2 and R3 together force one re-login (one change of names). R5 invalidates tokens issued before rollout.
