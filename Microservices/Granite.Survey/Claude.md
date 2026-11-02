# Granite.Survey Microservice

## Overview

Granite.Survey stores the responses users send from the Help view (**Take Survey**): three fixed questions answered 1 to 5 plus an optional comment. Administrators review responses in the Surveys grid (with per-question averages) and move each through the status `NEW → SENT → RECEIVED`, with free-form `notes`. `markSurveyResponsesSent` and `markSurveyResponsesReceived` exist for a future `SurveyExporter` (not part of Granite yet). The service mirrors Granite.Feedback: CQRS hosts, Service/Server/Client components (Java and TypeScript), entry configurations with seams, and `SurveyEvent`s published through the Transactional Outbox (topic `granite.surveyevent.avro`).

## Project Structure

```
Granite.Survey/
├── Applications/
│   ├── Granite.Survey.CommandHost/   # port 8091, /survey-command-service
│   └── Granite.Survey.QueryHost/     # port 8092, /survey-query-service
├── Components/
│   ├── Granite.Survey.Service/       # contracts, DTOs, questions, SurveyEvent.avsc, TS mirror (granite.survey.service)
│   ├── Granite.Survey.Server/        # domain, application, platform, configuration
│   └── Granite.Survey.Client/        # REST clients (Java + TS granite.survey.client)
└── Tests/
    ├── Granite.Survey.ServiceTest/   # SurveyServiceTest fixture
    ├── Granite.Survey.ServerTest/    # H2 server test, domain test
    ├── Granite.Survey.ClientTest/    # fixture against live hosts (IntegrationStage)
    └── Granite.Survey.TestSuite/
```

## Contract (Granite.Survey.Service)

- Questions are defined once in code: `GraniteSurveyQuestions` (Java: `questions()`, `keys()`, `MIN_ANSWER` 1, `MAX_ANSWER` 5, `MAX_COMMENT_LENGTH` 2000) and its TS mirror (`GRANITE_SURVEY_QUESTIONS`, `SURVEY_MIN_ANSWER`, `SURVEY_MAX_ANSWER`, `SURVEY_MAX_COMMENT_LENGTH`). Keys `satisfied`, `needs`, `recommend`; `SurveyQuestionData {key, label, labels: Map<Integer,String>}`. Keep both sides in step.
- `SurveyResponseStatus` `NEW | SENT | RECEIVED`. `SurveyResponseData {responseId, userId, answers: Map<String,Integer>, comment, status, notes, created, updated, sent, received}`.
- Commands (`ISurveyCommandService`, `/survey-command-service/`):

| Endpoint | Request → Reply | Behavior |
|---|---|---|
| `POST /submit-survey-response` | `SubmitSurveyResponseRequest {response: SubmitSurveyResponseData {userId, answers, comment}}` → `SubmitSurveyResponseReply {submittedResponse}` | `userId` required; every question answered, each 1–5, no unknown keys; comment at most 2000 characters (blank stored as null); status `NEW` |
| `POST /update-survey-responses` | `UpdateSurveyResponsesRequest {responses: List<UpdateSurveyResponseData {responseId, status, notes}>}` → plural reply | admin change: any status (stamping `sent`/`received` the first time), notes replaced |
| `POST /mark-survey-responses-sent` | `MarkSurveyResponsesSentRequest {responseIds}` → plural reply | only `NEW` responses; others fail |
| `POST /mark-survey-responses-received` | `MarkSurveyResponsesReceivedRequest {responseIds}` → plural reply | only `SENT` responses; others fail |

  Plural replies carry `requestedResponseIds`, `failedResponseIds` and `updatedResponses`; `success` only when nothing failed. Every change fires `SurveyResponseCreated`/`SurveyResponseUpdated` → `SurveyEvent` in the outbox.
- Queries (`ISurveyQueryService`, `/survey-query-service/`): `POST /find-survey-responses-by-ids` (`responseIds`), `POST /find-all-survey-responses` (`statuses`; empty = all), newest first.

## Server

- `domain`: `SurveyResponse` (`changeStatus(status,at)`, `canMarkSent()`, `canMarkReceived()`), `SurveyResponse.orm.xml` (table `SurveyResponse`, `answers` as JSON TEXT via `IntegerMapConverter`, own `submitted`/`changed` timestamps), `ISurveyResponseRepository`/`SurveyResponseRepository` (ordered by `primaryId desc`), domain events.
- `application`: `SurveyCommandService`, `SurveyQueryService`, `MappingModule` (explicit `SurveyResponse → SurveyResponseData` converter), `ApplicationConfiguration`.
- `platform`: controllers, `OutboxEventSurveyEventSender`, platform configurations.
- `configuration` (seams like Feedback): `AbstractGraniteSurveyServerConfiguration`, `GraniteSurveyCommandServerConfiguration` (`surveyEventSender(IOutboxEventRepository)`), `GraniteSurveyQueryServerConfiguration`.

## Hosts and environments

Both hosts serve HTTPS with the `granite-services` bundle and validate Session JWTs. They use the shared database of each environment; `test` and `production` use `ddl-auto=validate`, so the `SurveyResponse` table must exist before rollout. The Gateway authorizes `/survey-query-service/**` for users readers and `/survey-command-service/**` for `ROLE_ADMIN` (`SCOPE_internal` only in `production`). The FEC calls both through the Gateway with its service token: the Help view's Take Survey (any signed-in user, stamped with the caller) and the Surveys review grid (`granite.web.survey`).

## Style

Same as Granite.User (`//////` header/footer, `class` on its own line, return types on their own line, Allman braces, fluent setters, `(a,b)` call style, `Array<T>` in TypeScript).
