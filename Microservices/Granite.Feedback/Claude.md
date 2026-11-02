# Granite.Feedback Microservice

## Overview

Granite.Feedback stores the feedback users send from the Help view (**Provide Feedback**): defects, problems, suggestions and kudos, each with its type-specific fields. Administrators review it in the Feedback grid and move each record through the status `NEW → SENT → RECEIVED`, with free-form `notes`. `markFeedbackSent` and `markFeedbackReceived` exist for a future `FeedbackExporter` that forwards feedback to a team or tracker (not part of Granite yet). The service mirrors Granite.User: CQRS hosts, Service/Server/Client components (Java and TypeScript), entry configurations with seams, and `FeedbackEvent`s published through the Transactional Outbox (topic `granite.feedbackevent.avro`).

## Project Structure

```
Granite.Feedback/
├── Applications/
│   ├── Granite.Feedback.CommandHost/   # port 8086, /feedback-command-service
│   └── Granite.Feedback.QueryHost/     # port 8087, /feedback-query-service
├── Components/
│   ├── Granite.Feedback.Service/       # contracts, DTOs, FeedbackEvent.avsc, TS mirror (granite.feedback.service)
│   ├── Granite.Feedback.Server/        # domain, application, platform, configuration
│   └── Granite.Feedback.Client/        # REST clients (Java + TS granite.feedback.client), Kafka receivers
└── Tests/
    ├── Granite.Feedback.ServiceTest/   # FeedbackServiceTest fixture
    ├── Granite.Feedback.ServerTest/    # H2 server test, domain test
    ├── Granite.Feedback.ClientTest/    # fixture against live hosts (IntegrationStage)
    └── Granite.Feedback.TestSuite/
```

## Contract (Granite.Feedback.Service)

- `FeedbackType` `DEFECT | PROBLEM | SUGGESTION | KUDOS`; `FeedbackStatus` `NEW | SENT | RECEIVED`.
- `FeedbackData {feedbackId, userId, type, summary, details: Map<String,String>, status, notes, created, updated, sent, received}`. The `details` keys are the web form's fields (`stepsToReproduce`, `expectedBehavior`, `actualBehavior`; `description`, `impact`, `workaround`; `description`, `expectedBenefit`; `whatYouLike`, `additionalComments`).
- Commands (`IFeedbackCommandService`, `/feedback-command-service/`):

| Endpoint | Request → Reply | Behavior |
|---|---|---|
| `POST /create-feedback` | `CreateFeedbackRequest {feedback: CreateFeedbackData {userId, type, summary, details}}` → `CreateFeedbackReply {createdFeedback}` | `userId`, `type` and a non-blank summary (at most 512 characters) are required; status `NEW` |
| `POST /update-feedbacks` | `UpdateFeedbacksRequest {feedback: List<UpdateFeedbackData {feedbackId, status, notes}>}` → plural reply | admin change: any status (stamping `sent`/`received` the first time), notes replaced |
| `POST /mark-feedback-sent` | `MarkFeedbackSentRequest {feedbackIds}` → plural reply | only `NEW` records; others fail |
| `POST /mark-feedback-received` | `MarkFeedbackReceivedRequest {feedbackIds}` → plural reply | only `SENT` records; others fail |

  Plural replies carry `requestedFeedbackIds`, `failedFeedbackIds` and `updatedFeedback`; `success` only when nothing failed. Every change fires `FeedbackCreated`/`FeedbackUpdated` → `FeedbackEvent` in the outbox.
- Queries (`IFeedbackQueryService`, `/feedback-query-service/`): `POST /find-feedback-by-ids` (`FindFeedbackByIdsRequest {feedbackIds}`), `POST /find-all-feedback` (`FindAllFeedbackRequest {statuses}`; empty = all), newest first.

## Server

- `domain`: `Feedback` (`AbstractEntityAndDomainEventSource`; `changeStatus(status,at)`, `canMarkSent()`, `canMarkReceived()`), `Feedback.orm.xml` (table `Feedback`, `details` as JSON TEXT via `StringMapConverter`), `IFeedbackRepository`/`FeedbackRepository`, domain events.
- `application`: `FeedbackCommandService`, `FeedbackQueryService`, `MappingModule` (explicit `Feedback → FeedbackData` converter), `ApplicationConfiguration`.
- `platform`: controllers, `OutboxEventFeedbackEventSender`, `PlatformConfiguration`/`CommandPlatformConfiguration`/`QueryPlatformConfiguration`.
- `configuration` (seams, overridden with `@Bean` like User): `AbstractGraniteFeedbackServerConfiguration` (`configuration(Environment)`, `mapper()`/`configureMapper(ModelMapper)`, `clock()`, `securityFilterChain` (JWT check, `REQUIRED`), `jwtDecoder`, `openApi`, `getTokenlessPathPatterns(Environment)`, `createJwkSetRestOperations(...)`), `GraniteFeedbackCommandServerConfiguration` (`feedbackEventSender(IOutboxEventRepository)`), `GraniteFeedbackQueryServerConfiguration`.

## Hosts and environments

Both hosts serve HTTPS with the `granite-services` bundle and validate Session JWTs (`granite.security.jwt.jwk-set-uri`). They use the shared database of each environment (`granite-db-local`, `granite-db-dev`, `granite-db-test`, `granite-db-prod`; see the root CLAUDE.md); `test` and `production` use `ddl-auto=validate`, so the `Feedback` table must exist before rollout. The Gateway authorizes `/feedback-query-service/**` for users readers and `/feedback-command-service/**` for `ROLE_ADMIN` (`SCOPE_internal` only in `production`). The FEC calls both through the Gateway with its service token: the Help view's Provide Feedback (any signed-in user, stamped with the caller) and the Feedback review grid (`granite.web.feedback`).

## Style

Same as Granite.User (`//////` header/footer, `class` on its own line, return types on their own line, Allman braces, fluent setters, `(a,b)` call style, `Array<T>` in TypeScript).
