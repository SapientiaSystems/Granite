# Granite

Multi-service Spring Boot system (User, Session, FrontEndController, Gateway, Outbox) with a React web front end. Each service has its own `CLAUDE.md` (`Claude.md` for User and Session); read it before changing that service.

## Extending Granite

An application (for example Poplar) reuses Granite by extending the entry configuration of each service and overriding its seams. Seams are Java overrides of `@Bean` or protected methods on the entry configuration classes, because Spring Boot forbids redefining imported bean names. Extension points are documented per service:

- User: `AbstractGraniteUserServerConfiguration` and `GraniteUserCommandServerConfiguration` (`Microservices/Granite.User/Claude.md`)
- Session: `applicationRoles` seam, login gate, Okta role assignment (`Microservices/Granite.Session/Claude.md`)
- FrontEndController: `AbstractGraniteFrontEndControllerConfiguration`, `GraniteFrontEndControllerContext`, controller recipe (`Microservices/Granite.FrontEndController/CLAUDE.md`)
- Gateway: `configureApplicationRoutes`, `configureApplicationAuthorization`, `getUsersReaderAuthorities(Environment)` (`Microservices/Granite.Gateway/CLAUDE.md`)

### Role catalog

- `ApplicationRolesManager` (Foundation) is the role catalog; each application supplies its own, Granite's comes from `GraniteApplicationRolesManagerSupplier` (`ROLE_ADMIN`, `ROLE_AUDITOR`, `ROLE_USER`).
- An application provides ONE catalog, an `ApplicationRolesManager` (for example via your own `Supplier<ApplicationRolesManager>` in its Foundation library, as `new GraniteApplicationRolesManagerSupplier().get()` is for Granite), and returns it from the `applicationRoles()` override on all three hosts that use the catalog: User, Session and FrontEndController. Each of those hosts logs the catalog's role names (names only) once at startup (`applicationRolesStartupLog`), so a host left on a different catalog is visible in the logs.
- Granite core owns only `ROLE_ADMIN`; it implies no application role and is implied by none. `ROLE_AUDITOR` and `ROLE_USER` belong to the Granite application catalog.
- Role names are `ROLE_` plus UPPER_SNAKE (`ROLE_[A-Z0-9_]+`, enforced by `ApplicationRolesManager`). Stored authority sets are always closed under the role hierarchy (the expanded set is stored).
- Renames, applied once at User startup by `AuthorityRenamer`: `ROLE_Admin` to `ROLE_ADMIN`, `ROLE_Auditor` to `ROLE_AUDITOR`, `ROLE_User` to `ROLE_USER` (the interim mixed-case names become uppercase).
- Request authorization rules (which caller may reach which path) live only in the Gateway chain. Outside it, Session gates login (default: the user must hold at least one catalog role; `granite.session.login.required-authorities` overrides with an explicit list) and User rejects stored roles outside the catalog.
- Gateway core rules (Preferences operations `authenticated()` for any signed-in caller, users readers, Granite admin-only paths, application hook, `/**` → `ROLE_ADMIN`) are always registered; `granite.gateway.authorization.role-rules.enabled` only makes the Granite application grant `ROLE_AUDITOR` reader access.

### Preferences

- Preferences are stored per user under `values["preferences.<key>"]` and always act on the caller's own user. Granite declares `granite.theme` (`light|dark`, default `light`). Value keys prefixed `preferences.` or `granite.` are reserved: admin create/update never changes them and the Users screen shows them read-only.
- Server side: an application overrides `preferenceDefinitions()` on its FrontEndController configuration, calls `super.preferenceDefinitions()` and adds `PreferenceDefinition` entries (STRING, BOOLEAN, NUMBER or ENUM) with its own namespaced keys, for example `poplar.defaultRegion`. Undeclared keys or wrong types are rejected on save.
- Web side: write a section component that reads and saves with `usePreference("poplar.defaultRegion")` and pass it as a child of `<PreferencesView>`. Saving is per change. `PreferencesProvider` (Granite.Web.Account) needs `MantineProvider` (it drives the colour scheme) and `AuthenticationProvider` above it.

### Okta properties (Session)

- `granite.session.okta.role-assignment`: `ON_CREATE` (default) or `EVERY_LOGIN`.
- `granite.session.okta.unmapped-groups`: `DEFAULT_ROLE` (default) or `REFUSE`.
- `granite.session.okta.authorities-source` is removed.

## TypeScript tests

- Layout mirrors Java: `<Group>/Tests/<Component>Test/src/test/typescript/<Subject>Test.ts(x)` (for example `FrontEnds/Granite.Web/Tests/Granite.Web.AuthenticationTest`). The same convention applies to microservice TypeScript components when they get tests; the only microservice TypeScript test project today is `Granite.FrontEndController.ServiceTest`, which has no tests yet and is not wired into `test-granite`.
- A test project is private and never published. It depends on the component's published package (`1.0.0-snapshot` from the local Verdaccio) and imports its public subpaths (for example `granite.web.authentication/Authentication`), never the component's source. Test projects run vitest without type-checking (no `tsc`).
- Root scripts: `yarn run test-granite-web` (web test projects) and `yarn run test-granite` (every TypeScript test project). Each publishes the component to the local Verdaccio first (the component's existing `publish-package`), then runs its test project.
