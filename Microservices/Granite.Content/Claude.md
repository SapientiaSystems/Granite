# Granite.Content Microservice

## Overview

Granite.Content serves an application's help content, user guides and other reference material to the Help view's **Search Help** and to the virtual assistant (Granite.Agent). Documents come from content sources (Granite ships Markdown files packaged on the classpath) and are synchronized into the shared database when the CommandHost starts; search ranks the documents the caller may see. The service mirrors Granite.User: CQRS hosts, Service/Server/Client components (Java and TypeScript), entry configurations with seams, and `ContentEvent`s through the Transactional Outbox (topic `granite.contentevent.avro`).

## Project Structure

```
Granite.Content/
├── Applications/
│   ├── Granite.Content.CommandHost/   # port 8093, /content-command-service (runs the synchronizer)
│   └── Granite.Content.QueryHost/     # port 8094, /content-query-service
├── Components/
│   ├── Granite.Content.Service/       # contracts, DTOs, ContentEvent.avsc, TS mirror (granite.content.service)
│   ├── Granite.Content.Server/        # domain, application (search engine), source (sources, synchronizer), platform, configuration; content/granite/*.md
│   └── Granite.Content.Client/        # REST clients (Java + TS granite.content.client)
└── Tests/
    ├── Granite.Content.ServiceTest/   # ContentServiceTest fixture
    ├── Granite.Content.ServerTest/    # H2 server test, search engine and source tests
    ├── Granite.Content.ClientTest/    # fixture against live hosts (IntegrationStage)
    └── Granite.Content.TestSuite/
```

## Contract (Granite.Content.Service)

- `ContentKind` `HELP | GUIDE | REFERENCE`; `ContentFormat` `MARKDOWN | TEXT`.
- `ContentDocumentData {documentId, key, title, kind, format, body, keywords, requiredAuthorities, source, version, updated}`. `key` is the stable slug (`[a-z0-9][a-z0-9._/-]{0,254}`, for example `granite/users-help`); an application namespaces its own (`poplar/orders`). A document with `requiredAuthorities` is visible only to callers holding at least one of them; an empty set is visible to everyone.
- `ContentHitData {key, title, kind, snippet, score}`.
- Commands (`IContentCommandService`, `/content-command-service/`): `POST /upsert-content-documents` (`UpsertContentDocumentsRequest {documents: List<UpsertContentDocumentData>}` → `requestedKeys`, `failedKeys`, `upsertedDocuments`; creates or updates by key, title required (at most 512 characters), body at most 200000 characters; a document whose `version` and `source` are unchanged is left as is) and `POST /destroy-content-documents` (`keys` → `requestedKeys`, `failedKeys`, `destroyedKeys`).
- Queries (`IContentQueryService`, `/content-query-service/`): `POST /search-content` (`SearchContentRequest {query, kinds, authorities, limit}`; query required, at most 500 characters; `limit` default 10, at most 50; `kinds` empty = all), `POST /find-content-documents-by-keys` (`keys`, `authorities`) and `POST /find-all-content-documents` (`kinds`, `authorities`). The caller passes the end user's authorities; only trusted callers reach these paths (the FEC and the Agent with service tokens; in `production` the Gateway requires `SCOPE_internal`).

## Server

- `domain`: `ContentDocument` (property `documentKey`, because `key` is a JPQL keyword; `contentVersion`, because `version` is the optimistic lock; `isVisibleTo(authorities)`), `ContentDocument.orm.xml` (table `ContentDocument`, unique `documentKey`, `keywords` and `requiredAuthorities` as JSON TEXT via `StringListConverter`/`StringSetConverter`), `IContentDocumentRepository`/`ContentDocumentRepository`.
- `application`: `ContentCommandService` (also `synchronizeSource(source,documents)` on `ITransactionalContentCommandService`: upserts the source's documents, stamped with the source name, and removes that source's documents that are gone, in one transaction), `ContentQueryService`, `IContentSearchEngine` and the default `KeywordContentSearchEngine` (title match 5, keyword match 3, body occurrences 1 each up to 5, +4 when a multi-word query appears as a phrase; terms of at least 2 characters; snippet of ±80 characters around the first match with Markdown stripped). Search ranks the visible documents in memory, which suits help-sized collections; an application with a large corpus overrides `contentSearchEngine()` (for example with PostgreSQL full-text search).
- `source`: `IContentSource {getName(), load()}`, `ClasspathContentSource` (default pattern `classpath*:content/**/*.md`; front matter between `---` lines with `key`, `title`, `kind`, `keywords` and `requiredAuthorities` (comma lists) and `version`; the key defaults to the file name and the version to a SHA-256 prefix of the file, so an edited file is re-synchronized), `ContentSynchronizer` (on context refresh, once, for every source; failures are logged, never fatal).
- `configuration` seams: `AbstractGraniteContentServerConfiguration` (`contentSearchEngine()` plus the usual `configuration`, `mapper`/`configureMapper`, `clock`, security and OpenAPI seams), `GraniteContentCommandServerConfiguration` (`contentEventSender(IOutboxEventRepository)`, protected `contentSources()` (default one `ClasspathContentSource`), `contentSynchronizer(...)`), `GraniteContentQueryServerConfiguration`.

### Adding application content

Put Markdown files under `src/main/resources/content/<application>/` of the application's Content CommandHost (or any jar on its classpath); Granite's own articles are under `content/granite/`. Link to another article with `[text](content:<key>)`. To read from somewhere else, override `contentSources()` and return `super.contentSources()` plus your own `IContentSource`; to drop Granite's articles, return a `ClasspathContentSource("app","classpath*:content/app/**/*.md")` instead.

## Hosts and environments

Both hosts serve HTTPS with the `granite-services` bundle and validate Session JWTs. They use the shared database of each environment; `test` and `production` use `ddl-auto=validate`, so the `ContentDocument` table must exist before rollout. With two CommandHost replicas both synchronize at start; a concurrent insert of the same key fails on the unique key for one of them, which logs it and leaves the other's result.

## Style

Same as Granite.User (`//////` header/footer, `class` on its own line, return types on their own line, Allman braces, fluent setters, `(a,b)` call style, `Array<T>` in TypeScript).
