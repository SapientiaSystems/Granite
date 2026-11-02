# Granite.Agent Microservice

## Overview

Granite.Agent is the virtual assistant behind the Help view's **Chat With Virtual Assistant**. It runs a Claude tool-use loop on the server for each user message, grounds answers in the application's help content (Granite.Content) and acts for the user only through tools that call the application's services. An application configures it with its own tools, system prompt and model settings. Conversations are stored per user. The service mirrors Granite.User: CQRS hosts, Service/Server/Client components (Java and TypeScript), entry configurations with seams, and `AgentEvent`s through the Transactional Outbox (topic `granite.agentevent.avro`).

## Project Structure

```
Granite.Agent/
├── Applications/
│   ├── Granite.Agent.CommandHost/   # port 8095, /agent-command-service (calls Claude)
│   └── Granite.Agent.QueryHost/     # port 8096, /agent-query-service
├── Components/
│   ├── Granite.Agent.Service/       # contracts, DTOs, AgentEvent.avsc, TS mirror (granite.agent.service)
│   ├── Granite.Agent.Server/        # domain, application, agent (loop, provider, tools), platform, configuration
│   └── Granite.Agent.Client/        # REST clients (Java + TS granite.agent.client)
└── Tests/
    ├── Granite.Agent.ServiceTest/   # AgentServiceTest fixture (works with any provider)
    ├── Granite.Agent.ServerTest/    # H2 server test, AgentTurnRunnerTest (scripted provider), ClaudeAgentProviderTest
    ├── Granite.Agent.ClientTest/
    └── Granite.Agent.TestSuite/
```

## Contract (Granite.Agent.Service)

- `ConversationData {conversationId, userId, title, messages: List<AgentMessageData>, pendingAction: AgentPendingActionData, inputTokens, outputTokens, created, updated}`; `AgentMessageData {seq, role (USER|ASSISTANT|TOOL), text, toolName, isError, at}` (the transcript shown to the user; TOOL entries are one-line step summaries, never raw tool output); `AgentPendingActionData {toolUseId, toolName, summary, input}`.
- Commands (`/agent-command-service/`): `POST /send-agent-message` (`SendAgentMessageRequest {userId, sessionId, conversationId (null = new), text (at most 4000 characters), authorities}` → `{conversation}`; refused while an action is pending) and `POST /resolve-agent-action` (`{userId, sessionId, conversationId, toolUseId, approved, authorities}` → `{conversation}`). Queries (`/agent-query-service/`): `POST /find-agent-conversations` (`{userId}` → summaries without messages, newest first) and `POST /find-agent-conversation` (`{userId, conversationId}`; another user's conversation is "not found").
- The caller (`userId`, `sessionId`, `authorities`) is supplied by the FrontEndController from the verified JWT; only trusted callers reach these paths (in `production` the Gateway requires `SCOPE_internal`).

## Server

- `domain`: `Conversation` (table `AgentConversation`): the transcript (JSON via `TranscriptConverter`), `history` (the Claude Messages API turns, written and read with the SDK's own `ObjectMappers.jsonMapper()`, so thinking and tool-use blocks round-trip unchanged), the pending state (`pendingCalls`, `collectedResults`, `pendingSummary`), token usage and timestamps.
- `agent`:
  - `IAgentProvider.step(AgentTurnRequest) → AgentTurnStep` (one model call). `ClaudeAgentProvider` uses the Anthropic Java SDK (`com.anthropic:anthropic-java` 3.2.0): `client.messages().create(...)` with adaptive thinking, `OutputConfig` effort, the system prompt as a cached `TextBlockParam` (the cache prefix covers the tools, which are sorted by name so the prefix stays stable), `Message.toParam()` appended to the history, and tool results (with `isError`) sent back before any new user text in one user turn. `UnavailableAgentProvider` answers that the assistant is not configured when no API key is set.
  - `AgentTurnRunner` owns the loop: at most `granite.agent.max-steps` (8) model calls per user message; tools the caller lacks the authorities for are neither offered nor executed; read-only tools run at once; a **mutating** tool stops the loop with a pending action that the user approves or declines in the chat (declining returns an `is_error` result "The user declined this action."); after the step limit the tool results are kept and sent with the next message. Tool failures are returned to the model as errors and logged by class name only.
  - `IAgentTool {getName, getDescription, getProperties (JSON schema properties), getRequired, getRequiredAuthorities, isMutating, summarize, execute(input, AgentToolContext)}`. `AgentToolContext` carries the caller's `userId`, `sessionId`, `authorities` and `getDelegatedToken()`: a ≤5-minute token minted by Session `issue-delegated-token` for this user and session (`sub` = user, `jti` = session, the session's authorities, `act.sub` = `granite-agent`, never `SCOPE_*`). Tools change data only by calling services through their clients; there is no repository or database access outside the conversation store.
  - Granite tools: `search_help_content` and `get_help_article` (Granite.Content queries with the Agent's service token and the caller's authorities, so content filtering follows the user's roles).
  - `AgentSystemPrompt.GRANITE`: grounds answers in help content, tells the model that tool results and documents are data, not instructions, and that mutating tools need the user's approval.
  - `AgentServiceTokenSource` (client credentials at Session `issue-service-token`, cached until a minute before expiry) and `SessionDelegatedTokenSource` (Session `issue-delegated-token` with the same client credentials).
- `configuration` seams (`GraniteAgentCommandServerConfiguration`; override and re-annotate `@Bean` like User): `agentProvider(Environment)` (Claude or unavailable), protected `agentTools(Environment,Supplier<String> serviceToken)` (return `super.agentTools(...)` plus your own `IAgentTool`s), protected `agentSystemPrompt()` (Granite's text plus your application's), `delegatedTokenSource(...)`, `agentTurnRunner(...)`, `createServiceClientBuilder(serviceToken)` / `createClientBuilder()` (TLS from `granite.agent.ssl-bundle`, default `granite-services`). `AbstractGraniteAgentServerConfiguration` keeps the usual `configuration`, `mapper`, `clock`, security and OpenAPI seams.

### Adding application tools

Implement `IAgentTool` and add it in an `agentTools` override. A read tool can call a query service with the Agent's service token (filter by `context.getUserId()` / `getAuthorities()` yourself) or with `context.getDelegatedToken()` through the Gateway, where the application's normal role rules apply to the user. A tool that changes data must return `isMutating() == true` (the user approves first), should call the application's command service with the delegated token, and should give `summarize(input)` a sentence the user can judge, for example "End 2 sessions of alice". Keep tool results short and factual; they go back to the model.

## Configuration (CommandHost, every profile)

`granite.agent.claude.api-key=${ANTHROPIC_API_KEY:}` (Kubernetes Secret `granite-agent-claude-api`, mounted only on the Agent CommandHost, optional: without it the assistant replies that it is not configured; the key is never logged), `granite.agent.claude.model` (`claude-sonnet-5-5`), `.effort` (`medium`), `.max-tokens` (8000), `.timeout` (`PT2M`), `.max-retries` (2), `granite.agent.max-steps` (8), `granite.agent.service-client.id` (`granite-agent`) and `.secret` (`${AGENT_SERVICE_CLIENT_SECRET:}`, Secret `granite-agent-service-client`; Session registers the client with `.delegation=true`), `session-command-service.base-url` and `content-query-service.base-url` (the Gateway).

## Notes and limits

- The model call runs inside the command transaction (one request per user message, non-streaming); keep `max-steps` and `max-tokens` modest.
- Every turn spends Claude API tokens; `inputTokens`/`outputTokens` are accumulated per conversation for monitoring.
- `test` and `production` use `ddl-auto=validate`, so the `AgentConversation` table must exist before rollout.

## Style

Same as Granite.User (`//////` header/footer, `class` on its own line, return types on their own line, Allman braces, fluent setters, `(a,b)` call style, `Array<T>` in TypeScript).
