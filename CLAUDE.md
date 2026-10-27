# Granite

Multi-service Spring Boot system (User, Session, FrontEndController, Gateway, Outbox) with a React web front end. Each service has its own `CLAUDE.md` (`Claude.md` for User and Session); read it before changing that service.

## Extending Granite

An application (for example Poplar) reuses Granite by extending the entry configuration of each service and overriding its seams. Seams are Java overrides of `@Bean` or protected methods on the entry configuration classes, because Spring Boot forbids redefining imported bean names. Extension points are documented per service:

- User: `AbstractGraniteUserServerConfiguration` and `GraniteUserCommandServerConfiguration` (`Microservices/Granite.User/Claude.md`)
- Session: `applicationRoles` seam, login gate, Okta role assignment (`Microservices/Granite.Session/Claude.md`)
- FrontEndController: `AbstractGraniteFrontEndControllerConfiguration`, `GraniteFrontEndControllerContext`, controller recipe (`Microservices/Granite.FrontEndController/CLAUDE.md`)
- Gateway: `configureApplicationRoutes`, `configureApplicationAuthorization`, `getUsersReaderAuthorities(Environment)` (`Microservices/Granite.Gateway/CLAUDE.md`)

### Role catalog

- `ApplicationRoles` (Foundation) is the role catalog; each application supplies its own, Granite's is `GraniteApplicationRoles` (`ROLE_Admin`, `ROLE_Auditor`, `ROLE_User`).
- An application provides ONE catalog factory (for example a static `create()` in its Foundation library, as `GraniteApplicationRoles.create()` is for Granite) and returns it from the `applicationRoles()` override on all three hosts that use the catalog: User, Session and FrontEndController. Each of those hosts logs the catalog's role names (names only) once at startup (`applicationRolesStartupLog`), so a host left on a different catalog is visible in the logs.
- Granite core owns only `ROLE_Admin`; it implies no application role and is implied by none. `ROLE_Auditor` and `ROLE_User` belong to the Granite application catalog.
- Role names are mixed case. Stored authority sets are always closed under the role hierarchy (the expanded set is stored).
- Renames, applied once at User startup by `AuthorityRenamer`: `ROLE_ADMIN` to `ROLE_Admin`, `ROLE_AUDITOR` to `ROLE_Auditor`, `ROLE_USER` to `ROLE_User`.
- Request authorization rules (which caller may reach which path) live only in the Gateway chain. Outside it, Session gates login (default: the user must hold at least one catalog role; `granite.session.login.required-authorities` overrides with an explicit list) and User rejects stored roles outside the catalog.
- Gateway core rules (users readers, Granite admin-only paths, application hook, `/**` → `ROLE_Admin`) are always registered; `granite.gateway.authorization.role-rules.enabled` only makes the Granite application grant `ROLE_Auditor` reader access.

### Okta properties (Session)

- `granite.session.okta.role-assignment`: `ON_CREATE` (default) or `EVERY_LOGIN`.
- `granite.session.okta.unmapped-groups`: `DEFAULT_ROLE` (default) or `REFUSE`.
- `granite.session.okta.authorities-source` is removed.
