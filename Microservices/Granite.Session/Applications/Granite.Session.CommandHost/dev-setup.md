# Dev setup — JWT test keys

Session.CommandHost's `dev` profile reads the JWT keypair from `~/.granite/dev-keys/`.
Before running `./gradlew :Granite.Session:Applications:Granite.Session.CommandHost:bootRun --args='--spring.profiles.active=development'`:

```bash
mkdir -p ~/.granite/dev-keys
cp Microservices/Granite.Session/Components/Granite.Session.Server/src/test/resources/jwt-test-private-key.pem ~/.granite/dev-keys/
cp Microservices/Granite.Session/Components/Granite.Session.Server/src/test/resources/jwt-test-public-key.pem ~/.granite/dev-keys/
```

Smoke test and ServerTest use classpath test resources directly — no setup needed for `./gradlew test`.
