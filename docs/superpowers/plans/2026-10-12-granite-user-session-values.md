# Granite.User + Granite.Session — add `values: Map<String,Object>`

**Scope:** Add generic typed-key/value state to both `User` and `Session` entities per the UserAndSessionDomainModel UML. API shape: `setValue<T>(String,T)`, `getValue<T>(Class<T>,String):Optional<T>`, `hasValue(String):boolean`. Both entities' DTOs expose the map as a read-only snapshot. Create/Update DTOs do NOT accept values.

**Branch / commit policy:** stay on `development`, do NOT commit. Phase-gated review pacing. Hybrid plan: this file is the plan artifact; no separate spec.

**Progress ledger:** `.superpowers/sdd/2026-10-12-granite-user-session-values/progress.md`

## Design decisions (from interview, 2026-10-12)

1. **Persistence:** single JSON blob column on each entity, serialized via a JPA `AttributeConverter<Map<String,Object>,String>` named `ValueMapConverter`. One `ValueMapConverter` per microservice (duplicated into `granite.user.server.domain` and `granite.session.server.domain`), mirroring the `StringSetConverter` pattern. Column type: `TEXT`. Null-in → null-out on write; null-in → empty map on read; empty string → empty map on read.
2. **Jackson:** `ValueMapConverter` holds a `private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();`. Hibernate instantiates `AttributeConverter`s itself (no Spring injection), so a static mapper is the clean path. Read uses `TypeReference<Map<String,Object>>` so the result is a generic map of Jackson-native types (String, Integer/Long, Double, Boolean, List, Map).
3. **`getValue<T>(Class<T>, String)` semantics:** strict `type.isInstance(stored)` check. Missing key → `Optional.empty()`. Stored but wrong type → `Optional.empty()`. Correct type → `Optional.of((T)stored)`. No conversion attempted. This is tight and predictable but means custom POJOs round-trip as `LinkedHashMap` (Jackson's default); callers reading back POJOs must deserialize themselves. Documented limitation.
4. **`hasValue(String)` semantics:** returns `values.containsKey(key)`. True even if the value is null.
5. **API scope on Session:** DELETE existing `setValues(Map)`, `getValues()`, `putValue(String,String)`, `removeValue(String)`. Replace with `setValue<T>`, `getValue<T>`, `hasValue`. SessionTest rewrites to the new API. UML shows only these three; no legacy map-level API.
6. **notifyUpdated behavior:**
   - **Session.setValue → DOES `notifyUpdated()`.** Matches existing Session convention (setValues/putValue/removeValue all fired updated). Session values are user-activity state that mutate mid-lifetime outside the command pipeline.
   - **User.setValue → does NOT `notifyUpdated()`.** Matches User convention (setUserName/setAuthorities/setExternalIdentity do not notifyUpdated). User mutations go through `UserCommandService.applyUpdate` which fires `UserUpdated` at the pipeline level. Firing from the setter would double-fire.
7. **DTO exposure:** `UserData.values: Map<String,Object>` + getter/setter. `SessionData.values` widens from `Map<String,String>` to `Map<String,Object>`. Both DTOs carry the map for client reads. `CreateUserData` / `UpdateUserData` are NOT modified — values are set only via server-side logic in future command flows.
8. **TypeScript mirror:** `UserData.ts` and `SessionData.ts` expose `values?: Record<string, unknown>`.
9. **Schema migration:** `session_values` element-collection table GOES AWAY. Replaced by a `values TEXT` column on the `session` table. New `values TEXT` column on the `user` table. Test `schema.sql` scripts update accordingly. No production migration script in scope (per standing rules on dev tools).
10. **Granite.User gets Jackson dependency confirmation:** Spring Boot 3.4 pulls `jackson-databind` transitively; no new build-file edits required.

## Phases

### Phase A — Session value refactor
Files:
- NEW `Granite.Session.Server/.../domain/ValueMapConverter.java`
- MOD `.../domain/Session.java` — widen `values: Map<String,Object>`, delete `setValues/getValues/putValue/removeValue`, add `setValue<T>`, `getValue<T>`, `hasValue`. `notifyUpdated()` in `setValue`.
- MOD `.../resources/Session.orm.xml` — remove `<element-collection>`, add `<basic name="values">` with `<convert converter="granite.session.server.domain.ValueMapConverter"/>`
- MOD `.../resources/schema.sql` — drop `session_values` table create, add `values TEXT` column on `session` table
- MOD `Granite.Session.Service/.../SessionData.java` — widen `values: Map<String,Object>`
- MOD `Granite.Session.Server/.../application/MappingModule.java` — existing values mapping (if any) adjusts to new type; verify ModelMapper handles Map<String,Object> round-trip
- MOD `Granite.Session.ServerTest/.../domain/SessionTest.java` — rewrite `testValuesAreStoredAndRetrievable`, `testPutValueAndRemoveValue`, `testGetValuesReturnsUnmodifiableMap` tests to new API

Verify:
- `./gradlew :Granite.Session:Tests:Granite.Session.ServerTest:build` → BUILD SUCCESSFUL

### Phase B — User value add
Files:
- NEW `Granite.User.Server/.../domain/ValueMapConverter.java`
- MOD `.../domain/User.java` — add `values: Map<String,Object>` field (init to empty LinkedHashMap), add `setValue<T>` (no notifyUpdated), `getValue<T>`, `hasValue`
- MOD `.../resources/User.orm.xml` — add `<basic name="values">` with `<convert converter="granite.user.server.domain.ValueMapConverter"/>`
- MOD `.../resources/schema.sql` — add `values TEXT` column on `user` table (test schema)
- MOD `Granite.User.Service/.../UserData.java` — add `values: Map<String,Object>` field + setter/getter
- MOD `Granite.User.Server/.../application/MappingModule.java` — add `.addMapping(User::getValues, UserData::setValues)`
- MOD `Granite.User.ServerTest/.../domain/UserTest.java` — add tests covering `setValue/getValue/hasValue`
- MOD `Granite.User.ServerTest/.../application/ModelMapperTest.java` — add round-trip assertion for values

Verify:
- `./gradlew :Granite.User:Tests:Granite.User.ServerTest:build` → BUILD SUCCESSFUL

### Phase C — TypeScript mirror
Files:
- MOD `Granite.User.Service/.../typescript/RequestReply/UserData.ts` — `values?: Record<string, unknown>`
- MOD `Granite.Session.Service/.../typescript/RequestReply/SessionData.ts` — `values?: Record<string, unknown>` (widen from current string-map if present)

### Phase D — Root build verification
Verify: `yarn run build` from `C:\Development\Granite` succeeds end-to-end.

## Deferred / out of scope

- No new User/Session commands for mutating values from the client. Values are server-side state; future refactors can add command flows if needed.
- No new query endpoints to find users/sessions by a value key. Only read-back through the DTO snapshot.
- No production PostgreSQL migration script. Only test `schema.sql` (H2) is in scope.
- No wire-format control over how custom POJOs round-trip through `Map<String,Object>` — the strict `instanceof` semantics mean POJOs read back as `LinkedHashMap` unless the caller stores primitives/strings or deserializes themselves.
- No change to `CreateUserData` / `UpdateUserData` / `CreateUsersRequest` DTOs.

## Known risks

- **ModelMapper + Map<String,Object>:** verify ModelMapper copies the map reference correctly between User/Session and their DTOs. If ModelMapper mangles typed-wildcard maps, add an explicit `Converter` in `MappingModule`.
- **Schema drop of `session_values`:** any existing DDL migrations outside the test `schema.sql` may need coordination. Test schema is the only one in scope here.
- **Jackson default-typing:** `ObjectMapper()` with no `activateDefaultTyping` means a stored `Long` round-trips as `Integer` or `Long` depending on magnitude. `getValue(Long.class, "k")` could return empty for small integers that Jackson parsed as `Integer`. Known Jackson quirk; may need `enableDefaultTyping` or a documented range caveat. **Call this out to user if the first test exposes it.**
- **ValueMapConverter duplication:** two near-identical files in two packages. Deliberate — keeps microservice boundaries clean.
