# Plan: Environment-aware `SpringConfigurationProvider` (Strata.Foundation)

**Context:** Granite is converging User, Session, and Gateway on `strata.foundation.spring.inject.SpringConfigurationProvider` as the `IConfiguration` source. Granite.Session currently depends on Spring `${...}` placeholder resolution (e.g. env-var secrets) before properties are parsed, which `SpringConfigurationProvider` does not support.

**Goal:** Improve Spring configuration security and robustness. Add an opt-in, backward-compatible, Spring-`PropertyResolver`-aware mode to `SpringConfigurationProvider` that resolves `${...}` placeholders safely, validate the profile name, and fix resource loading so it works in fat jars. Then publish it so Granite can consume it.

**Workspace:** `C:\Development\StrataFrameworkSet\Strata.Foundation`

## Current state

- Class: `Components/Strata.Foundation.Spring/main/java/strata/foundation/spring/inject/SpringConfigurationProvider.java`
  - Constructors: `()` (equivalent to `("development",true)`) and `(String defaultProfile,boolean requireProfileProperties)`.
  - Profile: `System.getenv("DEPLOY_ENV")`, otherwise `defaultProfile`. Not validated.
  - Loads `application.properties` plus `application-<profile>.properties` via `ClassLoader.getSystemResourceAsStream`. This fails inside a Spring Boot fat jar.
  - Builds `new SecureConfiguration(base,profileSpecific)`. `SecureConfiguration(InputStream...)` accepts varargs, skips null streams, loads with `Properties.load(InputStream)` (ISO-8859-1), and decrypts `ENC(...)` values. It requires `PROPERTIES_ENCRYPTION_KEY` in the environment.
  - Streams are never closed.
  - The file header says `ApplicationPropertiesProvider.java`, which is wrong.
- Test: `Tests/Strata.Foundation.SpringTest/test/java/strata/foundation/spring/inject/SpringConfigurationProviderTest.java` has 10 tests that use the no-arg and `(String,boolean)` constructors. Resources: `application.properties`, `application-development.properties`.
- **Consumers (all must keep working unchanged):** 18 call sites using `new SpringConfigurationProvider()`:
  - Granite FrontEndController hosts
  - Mahogany User, PurchaseOrder, PurchaseOrderAdapter and FrontEndController hosts and test configurations
  - `Templates/__System__` FrontEndController hosts

## Design

```java
public
class SpringConfigurationProvider
    implements Provider<IConfiguration>
{
    private final String                     defaultProfile;
    private final boolean                    requireProfileProperties;
    private final Optional<PropertyResolver> resolver;    // empty = legacy behavior
    private final Logger                     logger;

    public  SpringConfigurationProvider()                                  // unchanged → legacy
    public  SpringConfigurationProvider(String,boolean)                    // unchanged → legacy
    public  SpringConfigurationProvider(PropertyResolver)                  // NEW → ("development",true,resolver)
    public  SpringConfigurationProvider(String,boolean,PropertyResolver)   // NEW, rejects null
    private SpringConfigurationProvider(String,boolean,Optional<PropertyResolver>)  // canonical
}
```

- Use `org.springframework.core.env.PropertyResolver`, the narrowest interface that has `resolveRequiredPlaceholders` and `getProperty`. Spring's `Environment` is a `PropertyResolver`, so Granite passes its `Environment` bean directly.
- **Null safety:** the public resolver constructors delegate with `Optional.of(resolver)`, so an explicit `null` throws `NullPointerException` instead of silently falling back to legacy mode. The legacy constructors delegate with `Optional.empty()`.
- **Profile resolution** in priority order:
  1. `System.getenv("DEPLOY_ENV")`. This is unchanged and keeps jib/K8s behavior.
  2. `resolver.getProperty("DEPLOY_ENV")`, only when a resolver is present. This lets a host's `main()` supply `DEPLOY_ENV` as a Spring default property.
  3. `defaultProfile`.
- **Profile validation (all modes):** the chosen profile must match `[A-Za-z0-9_-]+`, otherwise `get()` throws `IllegalArgumentException`. The message states the rule and does not echo the value. This blocks resource-name traversal (`../`) now that the profile can come from any Spring property source (command-line args, etc.).
- **Loading (all modes):** `readResource(name)` returns `Optional<byte[]>`, reading the whole resource with try-with-resources.
- **Legacy mode (no resolver):** pass the raw bytes through unchanged in a `ByteArrayInputStream`. `${...}` stays literal and encoding behavior is exactly as today.
- **Placeholder resolution (resolver mode only), per value, never over raw file text:**
  1. Parse the bytes into a plain `java.util.Properties` with `load(InputStream)` (ISO-8859-1, same as `SecureConfiguration`).
  2. For each key, set `resolver.resolveRequiredPlaceholders(value)` into a new `Properties`. Keys are not resolved.
  3. `store()` the result to a `ByteArrayOutputStream` and pass those bytes to `SecureConfiguration`. `store()` escapes `\`, newlines, `=`, `:`, `#`, `!` and non-Latin-1 characters, so resolved values can't corrupt or inject properties syntax.
  4. An unresolvable `${x}` with no default throws `IllegalArgumentException` (fail fast). It is rethrown with a message naming only the key and the file, keeping Spring's exception as the cause. The cause describes only that one raw value, never the rest of the file.
  5. `${x:default}` works.
  6. `ENC(...)` values pass through as literals and are decrypted afterwards inside `SecureConfiguration`. A resolved value that is itself `ENC(...)` is decrypted too.
- **Class loading (all modes):** resolve resources with `Thread.currentThread().getContextClassLoader()`, falling back to `SpringConfigurationProvider.class.getClassLoader()`.
  - This fixes fat-jar loading.
  - It sees everything the system class loader sees in Gradle tests and Jib images. Like today, the first `application.properties` on the classpath wins, so a dependency jar that ships one could shadow the host's file.
- **Logging:** debug logs name the profile and the files only, never property values or resolved text.

## Behavior changes for existing (legacy) callers

1. Context class loader instead of system class loader (fixes fat jars).
2. Profile validation: a `DEPLOY_ENV` or default profile with characters outside `[A-Za-z0-9_-]` now fails. **Confirm that all deployed `DEPLOY_ENV` values conform before publishing.**

Everything else, including the missing-file and `requireProfileProperties` messages, stays identical.

## Steps and acceptance criteria

| Step | Work | Model |
|------|------|-------|
| 1 | Refactor loading | Sonnet 4.6 |
| 2 | Constructors, profile resolution and validation | Sonnet 4.6 |
| 3 | Per-value placeholder resolution | Opus 4.6 (security-sensitive) |
| 4 | Tests | Sonnet 4.6 |
| 5 | Build and publish | Haiku 4.5 |

### Step 1: Refactor loading (no functional change)
- Extract the private helpers `getClassLoader()` and `readResource(String name): Optional<byte[]>` (try-with-resources).
- Pass raw bytes to `SecureConfiguration` via `ByteArrayInputStream`. No decoding or re-encoding.
- Switch to the context class loader.
- Fix the file header to `SpringConfigurationProvider.java`.
- **AC:** all 10 existing `SpringConfigurationProviderTest` tests pass unchanged.

### Step 2: Resolver constructors, profile resolution and validation
- Add the `Optional<PropertyResolver>` field, the private canonical constructor and the two public resolver constructors. The existing constructors delegate with `Optional.empty()`.
- Implement the 3-step profile priority in `getProfile()`.
- Validate the chosen profile against `[A-Za-z0-9_-]+` before any resource is loaded.
- **AC:**
  - The existing tests still pass.
  - The new constructors compile and can be called with a `StandardEnvironment`.
  - `new SpringConfigurationProvider((PropertyResolver) null)` throws `NullPointerException`.

### Step 3: Per-value placeholder resolution
- In `get()`, when a resolver is present, run base and profile bytes through parse → resolve each value → `store()` before building `SecureConfiguration`.
- Unresolved placeholders throw `IllegalArgumentException` naming the key and the file, with Spring's exception as cause. This is not wrapped by the existing `RuntimeException` catch around `SecureConfiguration`.
- Keep the existing missing-file and `requireProfileProperties` semantics and messages identical.
- **AC:** the behavior matches the design for both resolver and legacy modes (verified in Step 4).

### Step 4: Tests (no new build dependencies)
Use `org.springframework.core.env.StandardEnvironment` plus a `MapPropertySource` added with `getPropertySources().addFirst(...)`. These come from spring-core, which is already on the test classpath through `spring-kafka`, so `spring-test`/`MockEnvironment` isn't needed. Tests rely on `PROPERTIES_ENCRYPTION_KEY` being set, as the existing tests already do.

New test resources under `Tests/Strata.Foundation.SpringTest/test/resources/`:
- `application-placeholder.properties`
  ```
  placeholder.property=${test.placeholder}
  placeholder.defaulted=${missing.key:fallback}
  placeholder.special=${test.special}
  ```
- `application-unresolved.properties`
  ```
  unresolved.property=${missing.key}
  unresolved.sibling=sibling-secret-value
  ```

New tests in `SpringConfigurationProviderTest`:
1. `testResolverResolvesPlaceholder`: profile `placeholder`, env provides `test.placeholder=resolved`, so `placeholder.property` should be `resolved`.
2. `testResolverAppliesPlaceholderDefault`: `placeholder.defaulted` should be `fallback`.
3. `testResolverThrowsOnUnresolvedPlaceholder`: profile `unresolved`, so `get()` throws `IllegalArgumentException`. `missing.key` appears in the message or cause, and `sibling-secret-value` appears nowhere in the message or cause chain.
4. `testResolverReadsDeployEnvFromPropertyResolver`: env provides `DEPLOY_ENV=placeholder` and `test.placeholder=x`, default profile `development`, so the `placeholder` profile is loaded. Skip with `Assumptions.assumeTrue(System.getenv("DEPLOY_ENV") == null)`.
5. `testResolverResolvesBaseProperties`: base properties from `application.properties` are still present in resolver mode.
6. `testLegacyModeLeavesPlaceholdersLiteral`: `new SpringConfigurationProvider("placeholder",true)` returns the literal `${test.placeholder}`.
7. `testResolverMissingProfileFileSucceedsWhenNotRequired`: mirrors the existing legacy test, in resolver mode.
8. `testResolverPreservesSpecialCharactersWithoutInjection`: env provides `test.special` = `pa\ss\ninjected.key=evil#x`. `placeholder.special` equals that exact string, and `hasProperty("injected.key")` is false.
9. `testInvalidProfileIsRejected`: `new SpringConfigurationProvider("../development",true).get()` throws `IllegalArgumentException`. Skip with the same `DEPLOY_ENV` assumption.
10. `testResolverConstructorRejectsNull`: `new SpringConfigurationProvider((PropertyResolver) null)` throws `NullPointerException`.

- **AC:** all 20 tests pass using the module's existing Gradle test task.

### Step 5: Build and publish
- Build Strata.Foundation with its existing tasks. Don't change the Gradle setup or versions; stay on `1.0-SNAPSHOT`.
- Publish to mavenLocal with `publishToMavenLocal`.
- Publishing to GitHub Packages is a shared-repo action and happens only after explicit confirmation.
- **AC:**
  - The `strata-foundation-spring` SNAPSHOT is updated in the repo Granite resolves from.
  - A Mahogany or Granite FrontEndController host still compiles against it, as a smoke test of legacy callers.

## Out of scope
- No changes to `SecureConfiguration` or `ApplicationConfigurationProvider`.
- No resolution of placeholders in property keys.
- `spring.profiles.active` is not consulted; `DEPLOY_ENV` stays the only profile source.
- No new JavaDoc or comments, unless you request them.
- No migration of existing callers. They keep using the no-arg constructor.

## Hand-back contract for Granite
Once this is published, Granite hosts and entry configurations will use:

```java
@Bean
@SingletonScoped
public IConfiguration
configuration(Environment environment)
{
    return new SpringConfigurationProvider(environment).get();
}
```

Granite will then migrate Granite.Session from `${granite.deploy-env}.properties` to `application.properties` plus `application-<DEPLOY_ENV>.properties`, and remove its hand-rolled placeholder resolution in `HostConfiguration.configuration(...)`.
