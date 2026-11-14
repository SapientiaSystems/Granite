# Granite.Outbox Microservice

## Overview

Granite.Outbox is a Spring Boot microservice responsible for consuming domain events from the Transactional Outbox tables of other Granite microservices and publishing them to Kafka using Avro serialization. It acts as the event relay — polling outbox records written by command-side services and forwarding them as Kafka messages for downstream consumers. The Outbox microservice does not expose REST APIs or manage its own domain entities.

## Technology Stack

- **Language**: Java 23 (Amazon Corretto)
- **Build**: Gradle (multi-project)
- **Framework**: Spring Boot 3.4, Spring 6.2
- **Eventing**: Kafka, Avro serialization (strata.foundation.kafka, strata.outbox)
- **Testing**: JUnit Jupiter 5
- **Foundation Libraries**: Strata Framework Set (strata.outbox.server, strata.foundation.kafka)

## Project Structure

```
Granite.Outbox/
├── Applications/
│   └── Granite.Outbox.ServiceHost/     # Spring Boot application that hosts the outbox processor
├── Components/
│   └── Granite.Outbox.Server/          # Kafka event sender implementations
├── Tests/
│   └── Granite.Outbox.ServerTest/      # Server tests
├── build.gradle                         # Root build with dependency versions
└── settings.gradle                      # Multi-project module declarations
```

## Architecture

The Outbox microservice follows a simplified architecture focused on event relay. There are no REST controllers, no CQRS split, and no domain model. The microservice's role is to bridge the Transactional Outbox pattern with Kafka event delivery.

### Component Responsibilities

#### Granite.Outbox.Server

Contains Kafka event sender implementations for each entity type in the Granite system:

- **`server.user`**:
  - `KafkaAvroUserEventSender` — Extends `AbstractKafkaAvroEventSender<UserEvent>` and implements `IUserEventSender`. Serializes `UserEvent` instances to Avro format and publishes them to a configured Kafka topic. Uses the event's `sourceId` as the Kafka partition key to ensure ordering per entity.
- **`server.feedback`**: `KafkaAvroFeedbackEventSender` and `FeedbackEventOutboxEventReceiver`, the same pattern for `FeedbackEvent` (Granite.Feedback), topic `granite.feedbackevent.avro`.
- **`server.survey`**: `KafkaAvroSurveyEventSender` and `SurveyEventOutboxEventReceiver` for `SurveyEvent` (Granite.Survey), topic `granite.surveyevent.avro`.
- **`server.content`**: `KafkaAvroContentEventSender` and `ContentEventOutboxEventReceiver` for `ContentEvent` (Granite.Content), topic `granite.contentevent.avro`.
- **`server.agent`**: `KafkaAvroAgentEventSender` and `AgentEventOutboxEventReceiver` for `AgentEvent` (Granite.Agent), topic `granite.agentevent.avro`.
- **`server.client`**: `KafkaAvroClientEventSender` and `ClientEventOutboxEventReceiver` for `ClientEvent` (Granite.Client), topic `granite.clientevent.avro`.
- `HostConfiguration.receiverMapProvider` maps each event's simple name (`UserEvent`, `FeedbackEvent`, `SurveyEvent`, `ContentEvent`, `AgentEvent`, `ClientEvent`) to its receiver; topics `granite.userevent.avro`, `granite.feedbackevent.avro`, `granite.surveyevent.avro`, `granite.contentevent.avro`, `granite.agentevent.avro` and `granite.clientevent.avro`. Because every service shares one database per environment, this single host relays all of their outbox rows.

#### Granite.Outbox.Server security (`server.configuration`)

- `GraniteOutboxSecurityConfiguration` — `@EnableWebSecurity`; declares exactly one `SecurityFilterChain`, built by the Granite.Foundation `GraniteJwtCheckSecurity.jwtCheckChain(...)` in `REQUIRED` mode: a request without a valid Session-issued bearer (signature against the Session JWKS, expiry, issuer, audience) gets 401, except on the tokenless paths. There are no role or authority rules (the Gateway authorizes `/outbox-service/**`; in `production` it requires `SCOPE_internal`). The `jwtDecoder` bean uses `GraniteJwtDecoderFactory` with `GraniteResourceServerSettings.from(environment)` (`granite.security.jwt.jwk-set-uri` direct to Session Command, `granite.security.jwt.ssl-bundle`). Protected seams: `getTokenlessPathPatterns(Environment)` (default `GraniteJwtCheckSecurity.defaultTokenlessPaths`: `OPTIONS`, `${strata.health.base-path}/**` (`/outbox-service/health/**`), `/error`, springdoc paths outside `production`) and `createJwkSetRestOperations(ObjectProvider<SslBundles>,Environment)`. `OutboxSecurityFilterChainTest` (ServerTest) covers no token, untrusted, expired and wrong-audience tokens (401), a valid token and the tokenless health path.

#### Granite.Outbox.ServiceHost

The Spring Boot application entry point. `HostConfiguration` extends Strata `StrataPollingOutboxServerConfiguration` and imports `HealthCheckConfiguration` and `GraniteOutboxSecurityConfiguration`; `OutboxServiceHost` keeps `SecurityAutoConfiguration` excluded because the imported configuration enables web security itself. Every profile sets `granite.security.jwt.jwk-set-uri=https://granite-session-commandhost:443/.well-known/jwks.json` and `granite.security.jwt.ssl-bundle=granite-services` (as the User hosts do); the Session Command JWKS network policy admits the Outbox pods. The request-logging filter logs the query string only (no headers or payload, so bearer tokens are never logged).

### Dependencies

The Server component depends on:

- `strata.outbox:strata-outbox-server` — Outbox polling and relay infrastructure
- `strata.foundation:strata-foundation-kafka` — Kafka producer abstractions and Avro serialization (`AbstractKafkaAvroEventSender`, `IKafkaConfigurationProvider`)
- `granite.user:granite-user-service` — User event interfaces (`IUserEventSender`, `UserEvent`)
- `granite.feedback:granite-feedback-service` — Feedback event interfaces (`IFeedbackEventSender`, `FeedbackEvent`)
- `granite.survey:granite-survey-service` — Survey event interfaces (`ISurveyEventSender`, `SurveyEvent`)
- `granite.content:granite-content-service` — Content event interfaces (`IContentEventSender`, `ContentEvent`)
- `granite.agent:granite-agent-service` — Agent event interfaces (`IAgentEventSender`, `AgentEvent`)
- `granite.foundation:granite-foundation-core` and `spring-boot-starter-security` — the shared JWT check chain

As additional Granite microservices are added, corresponding event sender implementations will be added to the Server component (e.g., `KafkaAvroFrontEndControllerEventSender`).

## Commonly Used Design Patterns

### Strategy (Event Sending)

Each entity type has its own `IUserEventSender` (or equivalent) implementation. The Outbox microservice provides the Kafka/Avro implementation (`KafkaAvroUserEventSender`), while the originating microservice may use alternative implementations (`MockUserEventSender`, `OnCommitUserEventSender`) depending on environment and configuration. This strategy pattern allows event delivery to be swapped without changing service code.

### Transactional Outbox (Consumer Side)

While the command-side microservices (User, FrontEndController) write events to their outbox tables, the Outbox microservice is the consumer side — it polls those outbox tables and relays events to Kafka. The `strata-outbox-server` library provides the polling and relay infrastructure.

### Abstract Event Sender

`KafkaAvroUserEventSender` extends `AbstractKafkaAvroEventSender<UserEvent>`, which handles Kafka producer setup, Avro serialization, and message sending. Subclasses provide:

- The Kafka configuration (via `IKafkaConfigurationProvider`)
- A partition key extractor function (`e -> e.getSourceId().toString()`)
- The event class type (`UserEvent.class`)
- The target Kafka topic name

## Preferred Coding Style and Idioms

### Interface Naming

All interfaces are prefixed with `I`. Examples: `IUserEventSender`, `IKafkaConfigurationProvider`.

### Class and Method Formatting

The `class` keyword and the `implements`/`extends` clause each start on their own line. Opening braces are on their own line (Allman style). Method return types are on a separate line from the method name. Example:

```java
//////////////////////////////////////////////////////////////////////////////
// KafkaAvroUserEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.user;

public
class KafkaAvroUserEventSender
    extends AbstractKafkaAvroEventSender<UserEvent>
    implements IUserEventSender
{
    public
    KafkaAvroUserEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            UserEvent.class,
            topic);
    }
}

//////////////////////////////////////////////////////////////////////////////
```

Key formatting rules:

- **File header/footer**: Every Java file begins and ends with a `//////////////////////////////////////////////////////////////////////////////` comment. The second line contains the filename as a comment.
- **`class` on its own line**: The access modifier (`public`) is on one line, and `class ClassName` is on the next line. `extends` and `implements` clauses are indented on subsequent lines.
- **Method return type on its own line**: The access modifier and return type are on one line, and the method name with parameters is on the next line.
- **Allman brace style**: Opening braces `{` are on their own line for classes, methods, constructors, and control structures.
- **Column alignment**: Related field declarations are column-aligned by type and name.
- **Minimal space after commas**: Method calls and declarations use `(param1,param2)` with no space after commas in most cases.