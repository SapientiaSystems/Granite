# ClientHost Clients Tab and Dark Default Implementation Plan

**Goal:** ClientHost gets a menu bar with a "Clients" tab (the owned-Clients grid), so a user can go to Preferences from the User menu and back to Clients. Dark becomes the default theme across Granite.

**Branch:** `feature/clienthost-clients-tab` off `development`.

## Decisions

- Dark default is Granite-wide: the FEC `granite.theme` definition default, the Account cached-theme fallback and both apps' `MantineProvider` become `dark`. Users with a stored theme keep it.
- The Clients grid moves to `/clients`; `/` redirects to `/clients` (replace).
- The tab bar follows the Navigator `MenuBar` markup (`NavLink` + `app-bar-tab`, burger + `nav-link` on mobile), in `ClientHostFrame`; ClientHost does not depend on Granite.Web.Navigator. One tab: Clients.
- TypeScript builds install from and publish to the remote npmjs registry (check `yarn config get registry`; `remote-registry-config` if needed).

## Tasks

### Task 1: FEC theme default dark (Java)
- `AbstractGraniteFrontEndControllerConfiguration.preferenceDefinitions()`: `enumeration("granite.theme",List.of("light","dark"),"dark")`.
- Test in `ExtendedFrontEndControllerConfigurationTest` (or the closest configuration test): Granite's theme definition default is `dark`.
- Build FEC top-down, run FEC server tests. Commit.

### Task 2: Account fallback dark (TS)
- `ThemeCache.readCachedTheme()` returns `dark` when nothing valid is cached or storage throws.
- AccountTest: no cached theme → scheme `dark` before preferences load; existing tests keep explicit themes.
- Publish `granite.web.account`, run `test-granite-web` for Account. Commit.

### Task 3: ClientHost tab bar, route, dark provider
- `ClientHostFrame`: tab bar with Clients → `/clients` (active underline), mobile burger navbar, `UserMenu preferencesPath="/preferences"`.
- `App.tsx`: `clients` route with `OwnedClientsView`; index → `<Navigate to="/clients" replace />`.
- `main.tsx` in ClientHost and AppHost: `defaultColorScheme="dark"`.
- Build ClientHost and AppHost (`build-granite-web-clienthost`, AppHost build). Commit.

### Task 4: Docs
- Root `CLAUDE.md` Preferences (`granite.theme` default `dark`), Web `CLAUDE.md` ClientHost views. Commit.

### Then
- Build and push images: frontendcontroller-userhost, frontendcontroller-adminhost, web-apphost, web-clienthost.
- You deploy and test; merge to `development` on approval.

## Acceptance

- Fresh browser, user with no stored theme: both apps render dark, the Preferences switch shows dark.
- ClientHost: Clients tab visible and underlined on `/clients`; `/` lands on `/clients`; User menu → Preferences → Clients tab returns to the grid.
