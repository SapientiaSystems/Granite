# Granite as a Reusable Framework — Design

Date: 2026-10-26
Branch: `feature/granite-framework-extensibility` (Granite only; Poplar adoption is a separate, later effort in the Poplar repo)

## Intent

Granite becomes a framework that applications such as Poplar build on. An application gets Granite's login/logout (password and Okta), sessions, users, roles, preferences, Gateway security and UI building blocks, and adds its own front-end operations, routes, role rules, roles and preferences — by extending Granite base classes (Spring `@Configuration` subclasses overriding `@Bean`/helper methods, Java/TS class inheritance, React composition), not by forking or copying Granite code.

The first consumer is Poplar (`C:\Development\Poplar`), whose FrontEndController is still a generated CRUD stub and whose planned User host is replaced by Granite's.

## Goals

- An application's Gateway, FrontEndController, User and Session hosts are thin subclasses of Granite base configurations.
- An application's FEC service, controller and TS client **inherit** Granite's (Session, Users, Preferences operations) and add their own operations.
- Applications define their own roles, role hierarchy and default role; Granite core owns only `ROLE_Admin`.
- Okta group → role mapping with an application-selected mode (on creation / every login).
- Server-side, per-user preferences stored in the User `values` map; Granite owns theme, applications add their own keys and UI sections.
- Granite's UI pieces are consumable by an application that owns its own frame (menu bar, routes).
- Granite's own repo stays a working application built on the same extension points ("Granite application").

## Non-goals

- Changing Poplar (later effort).
- Self sign-up for password users.
- Running Granite services as unmodified images configured only by properties (rejected: applications build their own hosts).
- A Granite-owned application frame that applications plug tabs into (rejected: the application owns the frame).
- Account linking between password and Okta identities.

## Anti-goals

- An application having to copy Granite's security chain, session handling, cookie/CSRF logic or login code.
- An application being able to accidentally remove Granite's core rules (session paths, `ROLE_Admin` catch-all).
- Clients writing arbitrary keys into a user's `values` map.
- A stored authority set that violates the role hierarchy.

## Decisions (made by the user)

### Distribution
Java via published Maven artifacts (`granite-*-server`, `-service`, `-client`), TypeScript via Verdaccio npm packages, consistent with how Strata is consumed. Granite Server components already have `maven-publish`.

### Extension pattern per service
| Service | Application does |
|---|---|
| Gateway | extends Granite's Gateway base configuration; adds routes and role rules |
| FrontEndController | extends Granite's FEC base configuration; supplies its own service/controller subclasses |
| User | extends Granite's User base configuration (Command/Query) |
| Session | extends Granite's Session base configuration (Command/Query) |

### FEC — inheritance shape
```java
public interface IPoplarFrontEndControllerService extends IGraniteFrontEndControllerService { ...Promotions ops... }

public class PoplarFrontEndControllerService
    extends GraniteFrontEndControllerService implements IPoplarFrontEndControllerService
{ public PoplarFrontEndControllerService(GraniteFrontEndControllerContext granite, IPromotionQueryServiceClient p, ...) { super(granite); ... } }

@RestController
public class PoplarFrontEndControllerServiceController
    extends GraniteFrontEndControllerServiceController<IPoplarFrontEndControllerService> { ...own @PostMappings... }

@Configuration
public class PoplarFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IPoplarFrontEndControllerService>
{ createService(GraniteFrontEndControllerContext) / createController(service) overrides }
```
TS: `PoplarFrontEndControllerServiceClient extends GraniteFrontEndControllerServiceClient` — one client for Session, Users, Preferences and the application's operations.

### Roles
- Granite core: `ROLE_Admin` only (renamed from `ROLE_ADMIN`).
- Granite application: `ROLE_Auditor`, `ROLE_User` (renamed from `ROLE_AUDITOR`, `ROLE_USER`); `ROLE_User` is its default role. These are not inherited by other applications.
- Mixed case role names (`ROLE_PromotionsReviewer`).
- Hierarchy example (Poplar): Reviewer (read) ⊂ Planner (+write) ⊂ Approver (+approve).
- **The expanded set is stored** on the user: an Approver's record holds `ROLE_PromotionsReviewer`, `ROLE_PromotionsPlanner`, `ROLE_PromotionsApprover`.
- `ROLE_Admin` stays **separate** from application hierarchies: it implies no application role and no application role implies it. An Admin performs application actions only when also granted those roles.
- Existing stored `ROLE_ADMIN`/`ROLE_AUDITOR`/`ROLE_USER` values are **converted** to the new names automatically.

### Default roles at first login
- Password user saved with no roles → on first successful login receives the application's configured default role (expanded, stored).
- Okta user → roles from the Okta group mapping (expanded). If no group maps, **fall back to the application's default role** (for now; may become refusal later).
- Okta mapping mode is chosen by application configuration: **on creation** (groups set stored roles once; Admin edits afterwards stick) or **every login** (groups are the source of truth: the expanded group-derived roles are **written back** to the user record at each Okta login, overwriting Admin edits for that user, so the Users screen always matches).

### Preferences
- Stored server-side in the User `values` map.
- Granite's `PreferencesView` is the base: user card + theme, with a slot for application sections. Applications compose their own sections (React components) into it.

### UI
- The application owns its frame (option 2). Granite provides building blocks: `AuthenticationProvider`, `RequireAuthentication`, `LoginView`, `LoginCallbackView`, re-authentication dialog, logout, Users admin view, `PreferencesView`, `createFrontEndClient`.

## Builder's latitude and recommended design (Claude's calls — review these)

### Shared role catalog
- New type in `Granite.Foundation.Core`, e.g. `granite.foundation.core.security.ApplicationRoles`: list of role definitions (name, label, implied roles), `adminRole()` (always `ROLE_Admin`, always present), `defaultRole()`, and `expand(Set<String>)` (transitive closure).
- `GraniteApplicationRoles` (Admin, Auditor, User; default User) lives beside it. An application defines its own (Poplar: in `Poplar.Foundation.Core`) and returns it from an overridable `applicationRoles()` `@Bean` on each of its host configurations that use the catalog (User, Session, FrontEndController; each logs the catalog role names once at startup).
- Granite's concrete `Granite*ServerConfiguration` classes default `applicationRoles()` to `GraniteApplicationRoles`; applications override.

### User service
- Command side validates stored authorities: every role must be in the catalog (non-role authorities such as `SCOPE_*` are not storable), and the set is normalized by adding implied roles. Removal cascade (unchecking Reviewer also unchecks Planner and Approver) is a UI behaviour; the server only guarantees closure.
- New merge command for `values` (merge only the given keys; null value removes the key) so preference saves never replace the whole map. Callable only with `SCOPE_internal` (FEC).
- Startup authority renamer (like `UserBootstrapper`): idempotent, rewrites `ROLE_ADMIN→ROLE_Admin`, `ROLE_AUDITOR→ROLE_Auditor`, `ROLE_USER→ROLE_User`; rename map is an overridable method. Session applies the same renamer to stored session authorities so existing sessions keep working.

### Session service
- `applicationRoles()` bean used for: default role on first password login (when stored authorities are empty, Session asks User to store the expanded default before the login gate runs), Okta creation roles, and expansion.
- Okta mode property `granite.session.okta.role-assignment=ON_CREATE|EVERY_LOGIN` (default `ON_CREATE`), plus `granite.session.okta.unmapped-groups=DEFAULT_ROLE|REFUSE` (default `DEFAULT_ROLE`). Group mapping stays property-driven (`granite.session.okta.group-mapping`) because Okta group names are environment-specific. The existing `authorities-source` property is superseded by `role-assignment`. In `EVERY_LOGIN` mode Session stores the expanded roles through the User command service before the login gate runs; a failed write refuses the login. Session authorities always equal the stored roles.
- Hard-coded `OKTA_ROLE = "ROLE_USER"` removed.
- Login gate default becomes "has at least one catalog role"; `granite.session.login.required-authorities` still overrides (values renamed in Granite's properties).

### FrontEndController
- `AbstractGraniteFrontEndControllerConfiguration<S extends IGraniteFrontEndControllerService>` holds the executor, `ResponseCookieAdvice`, health import and the request-scoped service/controller beans built from `createService(GraniteFrontEndControllerContext)` and `createController(S)`. Client factories for User/Session are protected helpers; applications add their own.
- `GraniteFrontEndControllerContext` bundles mapper, executor, the four Granite clients, `SessionRequestContext`, `SessionLoginSettings`, `ApplicationRoles`, preference definitions — so adding a Granite dependency later doesn't break application constructors.
- `GraniteFrontEndControllerServiceController<S>` becomes generic; Granite's concrete controller is the Granite-application binding. Spring MVC picks up inherited `@PostMapping`s.
- `GraniteFrontEndControllerConfiguration` (Granite application) extends the abstract base.
- New Preferences pair: `getPreferencesOutput`/`handlePreferencesInput` (`/get-preferences-output`, `/handle-preferences-input`), always acting on the session's user (never a request `userId`). Output: values for declared keys (defaults filled in). Input: changed keys; undeclared keys or wrong types rejected.
- Preference definitions: overridable `preferenceDefinitions()` returning `(key, type, default)`; Granite declares `granite.theme` (`light|dark`). Stored under `preferences.<key>` in `values`. Application keys namespaced (`poplar.*`).
- `getUsersOutput` returns the role catalog (names, labels, implications) so the Users screen offers a pick list instead of free text.

### Gateway
- Base configuration keeps owning: cookie→JWT swap, CSRF, session body-key filter, public/current-session matchers, Granite routes, `ROLE_Admin` catch-all.
- Core rules in the base: preferences paths → any authenticated user; `get-users-output`/`handle-users-input` → `ROLE_Admin`, `SCOPE_internal`; everything else → `ROLE_Admin`, `SCOPE_internal`.
- Hooks: `configureApplicationRoutes(Builder,Environment)` (additive) and `configureApplicationAuthorization(AuthorizeExchangeSpec,Environment)` invoked **after** Granite's specific rules and **before** the catch-all, so an application can't drop core rules.
- Granite application rules (`ROLE_Auditor` read access to Users/Sessions) move to the Granite application's subclass.
- **Core rules are always registered** (user decision 2026-10-26, final review): the users-reader rules (`getUsersReaderAuthorities(Environment)`, default `ROLE_Admin`, `SCOPE_internal`), the Granite admin-only paths, then the application hook, then `/**` → `ROLE_Admin`, `SCOPE_internal`, regardless of any property. `configureAuthorization` is `final`. `granite.gateway.authorization.role-rules.enabled` no longer switches the core rules; it only controls whether the Granite application's `getUsersReaderAuthorities` adds `ROLE_Auditor` (default `false`: Admin/internal only).

### Web (phase 3)
- `UsersView` moves from `Granite.Web.Navigator` into `Granite.Web.User` (reusable). Authorities editor becomes a pick list from the role catalog with hierarchy cascade (grant adds implied; removing removes dependents).
- `Granite.Web.Account`: `PreferencesProvider` + `usePreference(key)` hook; `PreferencesView` takes `children` sections; theme loads from and saves to the server.
- `Authorities.ts`: `ROLE_ADMIN` → `ROLE_Admin`; a generic `hasRole` helper exported.
- A `UserMenu`/logout building block exported from Authentication.
- `Granite.Web.Navigator` becomes Granite's own frame only.

### Further builder calls (from the design review)
- Strata `SpringConfigurationProvider(PropertyResolver)` (the 2026-10-14 blocker) is already published; no Strata step needed.
- The startup renamer also normalizes hierarchy closure. Stored values outside the catalog are left untouched (logged as a count, never the values); the command side validates only the roles being added, so editing a user with legacy values doesn't fail.
- Preference types: `STRING`, `BOOLEAN`, `NUMBER`, `ENUM` (with allowed values). A stored value that no longer validates is reported as the default and overwritten on the next save.
- Granite's own builds consume in-repo projects exactly as today (Gradle composite `includeBuild` substitution; yarn component builds publishing to local Verdaccio via the existing scripts). No GitHub Packages publish is part of this work.
- If storing the default role on first password login fails, the login is refused ("user unavailable"), matching Okta auto-create failure today.

## Phasing (agreed order)
1. Backend extension points: Gateway, FEC, User, Session; role catalog, hierarchy, renames + data conversion, default roles, Okta modes.
2. Preferences end to end (User merge command, FEC ops, Gateway rule, web hook/provider/view).
3. UI packaging (reusable Users view, role pick list, user menu, docs).
4. (Out of this branch) Poplar adoption.

Each phase ends with Granite's own application building and its existing tests passing; the user does E2E.

## Constraints
- Build/test only with existing Gradle tasks and yarn scripts; don't touch tool config, daemons or the yarn registry; no `publish*`/jib without approval.
- Dev schema via JPA ORM + `ddl-auto=update`; no hand-written DDL (data conversion is code, not SQL scripts).
- Never log tokens, handles, codes, passwords.
- Code style: Granite/Strata conventions; no explanatory comments; no static inner classes.
