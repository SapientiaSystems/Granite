# Granite.User — remove `foo`, add `authorities: Set<String>`

**Scope:** Replace the `foo` business field on `User` with an `authorities: Set<String>` property. Rename repository/service lookups from foo-based to userName-based. Flow authorities into the Session access-token JWT via `AuthenticateUserReply`.

**Branch / commit policy:** stay on `development`, do NOT commit. Phase-gated review pacing (pause after each phase for user approval). Hybrid plan: this file is the plan artifact; no separate spec document.

**Progress ledger:** `.superpowers/sdd/2026-10-12-granite-user-authorities/progress.md`

## Design decisions (from interview, 2026-10-12)

1. **Persistence:** `authorities` persists as a single VARCHAR column via a `StringSetConverter` adapted from Sayin's `StringListConverter` pattern. New class `granite.user.server.domain.StringSetConverter implements AttributeConverter<Set<String>,String>`. Comma-joined on write; `StringTokenizer` split on read into a `LinkedHashSet<String>`. Null-in → null-out.
2. **Query rename:** `IUserRepository.findAllByFoo(String)` → `findByUserName(String)` returning `Optional<User>`. `findAllByFooIn(Set<String>)` → `findAllByUserNameIn(Set<String>)`. No new authority-based queries.
3. **UserKey rename:** `UserKey.USER_FOO` → `UserKey.USER_NAME`. `UserKeySet.USER_FOOS` → `UserKeySet.USER_NAMES`. Factory methods and validate() cases rename.
4. **IUserQueryService:** USER_FOO case in `findUsersByKey` becomes USER_NAME case calling `findByUserName`. Semantic shift: single lookup returns 0–1 Users (Optional unwrap). USER_FOOS → USER_NAMES in `findUsersByKeySet` likewise.
5. **UserCommandService batch dedup:** `createdFoos` set becomes `createdUserNames` (userName is the unique business identifier after Session SDD Phase A Task 2). Validation message "foo was not provided" becomes "userName was not provided" (userName is required at create).
6. **Session JWT:** `AuthenticateUserReply` gains `authorities: Set<String>`. `UserCommandService.authenticateUser` populates from `user.getAuthorities()`. `SessionCommandService.login` reads `authenticateReply.getAuthorities()` and passes through to `JwtTokenService.mint(...)`. `JwtTokenService.mint` adds an `authorities` claim (JSON array of strings) alongside existing iss/sub/jti/iat/exp/aud/did claims.
7. **TypeScript mirror:** `UserData.ts`, `CreateUserData.ts`, `UpdateUserData.ts` get `authorities: Set<string>` field replacing `foo`. (TypeScript `Set<string>` is natively serializable; verify ModelMapper + Jackson round-trip handles it, else fall back to `Array<string>` on the TS side.)

## Phases

### Phase A — Granite.User domain + repository
Files:
- NEW `Granite.User.Server/src/main/java/granite/user/server/domain/StringSetConverter.java`
- MOD `.../domain/User.java` — drop `foo`, add `authorities: Set<String>` with `@Convert(converter=StringSetConverter.class)` or XML-mapped equivalent
- MOD `.../src/main/resources/User.orm.xml` — swap `foo` mapping for `authorities` basic with converter
- MOD `.../domain/IUserRepository.java` — `findByUserName(String)→Optional<User>`, `findAllByUserNameIn(Set<String>)`
- MOD `.../domain/UserRepository.java` — rewire named queries (likely `User.findByUserName` and `User.findAllByUserNameIn` entries in orm.xml named-query section)
- MOD `.../src/main/resources/User.orm.xml` — named-query rename
- MOD `Granite.User.ServerTest/.../domain/UserTest.java`
- MOD `Granite.User.ServerTest/.../domain/UserRepositoryTest.java`

Verify: `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test` — domain + repository tests green.

### Phase B — DTOs, service layer, mapping, TypeScript
Files:
- MOD `Granite.User.Service/.../UserKey.java` — USER_FOO→USER_NAME, factory + validate()
- MOD `.../UserKeySet.java` — USER_FOOS→USER_NAMES
- MOD `.../UserData.java` — drop foo, add authorities
- MOD `.../CreateUserData.java` — drop foo, add authorities (nullable at create? default empty set)
- MOD `.../UpdateUserData.java` — drop foo, add authorities
- MOD `Granite.User.Server/.../application/MappingModule.java` — mapping updates
- MOD `.../application/UserCommandService.java` — `data.getFoo().ifPresent(user::setFoo)` → authorities equivalent; `createdFoos` → `createdUserNames`; validation message
- MOD `.../application/UserQueryService.java` — USER_FOO case → USER_NAME case with Optional unwrap; USER_FOOS → USER_NAMES
- MOD TypeScript: `UserData.ts`, `CreateUserData.ts`, `UpdateUserData.ts`
- MOD `UserServiceTest.java` fixture — rework test methods that assert on foo
- MOD `UserServiceServerTest.java` — any foo setup
- MOD `UserServiceClientTest.java` — any foo setup
- MOD `ModelMapperTest.java` + `UserPasswordHashingTest.java`

Verify: full `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test` + `:Granite.User.ClientTest:build` + `:Granite.User.ServiceTest:build` green. TypeScript builds via existing yarn scripts.

### Phase C — AuthenticateUserReply enrichment
Files:
- MOD `Granite.User.Service/.../AuthenticateUserReply.java` — add `authorities: Set<String>` field + setter/getter + fluent chain
- MOD TypeScript: `AuthenticateUserReply.ts`
- MOD `.../UserCommandService.authenticateUser` — populate `authorities` on the reply from `user.getAuthorities()`
- MOD `AuthenticateUserTest.java` under Granite.User.ServerTest — assert authorities populated

Verify: `./gradlew :Granite.User:Tests:Granite.User.ServerTest:test` green.

### Phase D — Session JWT embedding
Files:
- MOD `Granite.Session.Server/.../application/JwtTokenService.java` — add `authorities` claim; update mint() signature or pass via context
- MOD `.../application/SessionCommandService.java` login path — propagate `authenticateReply.getAuthorities()` into JWT mint
- MOD `.../application/LoginContext.java` or equivalent — carry authorities through the pipeline
- MOD Session.ServerTest `TestConfiguration.java` — `@MockBean IUserCommandServiceClient.authenticateUser` populates authorities on reply
- MOD Session.ServerTest `JwtTokenServiceTest.java` — assert authorities claim present
- MOD Session.ServerTest `LoginTest.java` — assert authorities flow through

Verify: `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:test` green (all CommitStage + integration-tagged tests).

### Phase E — Root build verification
Verify: `yarn run build` from `C:\Development\Granite` succeeds end-to-end (User publishes updated TypeScript, Session + FrontEndController consume, Web compiles).

## Deferred / out of scope

- No new User commands for grant/revoke authority — authorities are set wholesale via Create/Update.
- No new User queries by authority — repository has only userName-based lookups.
- Session.AccessToken consumers' authorization logic (reading `authorities` claim for RBAC decisions) — downstream work.
- The Session SDD's Phase G Task 32 end-to-end verification remains outstanding and is independent of this effort.

## Known risks

- **Hibernate 6.4 with AttributeConverter + Set:** some Hibernate versions require the converter be registered via `@Convert` on the field or via `<convert>` in orm.xml; wild type parameters (`Set<String>`) should work but may need a hint. Watch for runtime errors on schema initialization.
- **`createdFoos` → `createdUserNames`:** userName is NOT NULL + UNIQUE per Session SDD Phase A Task 2. Dedup logic reads naturally; no behavior change beyond the field swap.
- **CreateUserData.authorities default:** must decide null-vs-empty-set semantics at create time. Proposal: default to empty set if the client omits; the repository persists as empty string column; converter round-trips as empty set.
- **ModelMapper + Set<String> field:** verify ModelMapper's default behavior round-trips Set correctly between User and UserData. If not, add an explicit converter in MappingModule.
