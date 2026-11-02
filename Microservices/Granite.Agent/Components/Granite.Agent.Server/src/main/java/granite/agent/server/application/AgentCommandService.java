//////////////////////////////////////////////////////////////////////////////
// AgentCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import granite.agent.server.agent.AgentToolContext;
import granite.agent.server.agent.AgentTurnRunner;
import granite.agent.server.agent.IDelegatedTokenSource;
import granite.agent.server.domain.Conversation;
import granite.agent.server.domain.IConversationEvent;
import granite.agent.server.domain.IConversationEventObserver;
import granite.agent.server.domain.IConversationRepository;
import granite.agent.service.event.AgentEvent;
import granite.agent.service.event.IAgentEventSender;
import granite.agent.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.event.EventIdentifiersData;
import strata.foundation.core.event.StandardEventType;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class AgentCommandService
    implements ITransactionalAgentCommandService,IConversationEventObserver
{
    public static final int    MAX_MESSAGE_LENGTH = 4000;
    public static final String PENDING_ACTION     = "approve or decline the pending action first";
    public static final String NOT_FOUND          = "conversation not found";

    private final IConversationRepository repository;
    private final IAgentEventSender       sender;
    private final ModelMapper             mapper;
    private final Clock                   clock;
    private final AgentTurnRunner         runner;
    private final IDelegatedTokenSource   tokens;
    private final Logger                  logger;

    public
    AgentCommandService(
        IConversationRepository r,
        IAgentEventSender       s,
        ModelMapper             m,
        Clock                   c,
        AgentTurnRunner         t,
        IDelegatedTokenSource   d)
    {
        repository = r;
        sender     = s;
        mapper     = m;
        clock      = c;
        runner     = t;
        tokens     = d;
        logger     = LogManager.getLogger(AgentCommandService.class);
    }

    @Override
    public CompletionStage<SendAgentMessageReply>
    sendAgentMessage(SendAgentMessageRequest request)
    {
        logger.debug("sendAgentMessage({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(valid -> send(valid))
                .thenApply(
                    conversation ->
                        new SendAgentMessageReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Conversation: " + conversation.getPrimaryId() + " updated")
                            .setConversation(mapper.map(conversation,ConversationData.class)))
                .exceptionally(
                    e ->
                        new SendAgentMessageReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to send message: " + rootMessage(e))
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<ResolveAgentActionReply>
    resolveAgentAction(ResolveAgentActionRequest request)
    {
        logger.debug("resolveAgentAction({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(valid -> resolve(valid))
                .thenApply(
                    conversation ->
                        new ResolveAgentActionReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Conversation: " + conversation.getPrimaryId() + " updated")
                            .setConversation(mapper.map(conversation,ConversationData.class)))
                .exceptionally(
                    e ->
                        new ResolveAgentActionReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to resolve action: " + rootMessage(e))
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public void
    onEvent(IConversationEvent event)
    {
        switch (event.getName())
        {
            case "ConversationCreated":
                sender.send(toServiceEvent(event,StandardEventType.CREATED));
                break;

            case "ConversationUpdated":
                sender.send(toServiceEvent(event,StandardEventType.UPDATED));
                break;

            case "ConversationDestroyed":
                sender.send(toServiceEvent(event,StandardEventType.DESTROYED));
                break;

            default:
                logger.error("Encountered unknown event type: {}",event.getName());
                break;
        }
    }

    @Override
    public void
    onException(Exception e)
    {
        logger.error("Observing AgentEvents and caught exception: {}",e.getMessage());
    }

    protected SendAgentMessageRequest
    validate(SendAgentMessageRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getUserId(),"validation failed: userId was not provided");
        Objects.requireNonNull(request.getSessionId(),"validation failed: sessionId was not provided");

        if (request.getText() == null || request.getText().isBlank())
            throw new IllegalArgumentException("validation failed: text was blank");

        if (request.getText().length() > MAX_MESSAGE_LENGTH)
            throw new IllegalArgumentException("validation failed: text is longer than " + MAX_MESSAGE_LENGTH + " characters");

        return request;
    }

    protected ResolveAgentActionRequest
    validate(ResolveAgentActionRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getUserId(),"validation failed: userId was not provided");
        Objects.requireNonNull(request.getSessionId(),"validation failed: sessionId was not provided");
        Objects.requireNonNull(request.getConversationId(),"validation failed: conversationId was not provided");
        Objects.requireNonNull(request.getToolUseId(),"validation failed: toolUseId was not provided");
        Objects.requireNonNull(request.getApproved(),"validation failed: approved was not provided");
        return request;
    }

    protected Conversation
    send(SendAgentMessageRequest request)
    {
        Instant      now          = Instant.now(clock);
        boolean      created      = request.getConversationId() == null;
        Conversation conversation =
            created
                ? new Conversation().setUserId(request.getUserId()).setStarted(now)
                : owned(request.getConversationId(),request.getUserId());

        if (conversation.hasPendingAction())
            throw new IllegalStateException(PENDING_ACTION);

        runner.send(conversation,request.getText().trim(),context(request.getUserId(),request.getSessionId(),request.getAuthorities()));
        conversation.setChanged(Instant.now(clock));

        Conversation saved = repository.save(conversation).attach(this);

        return created ? saved.notifyCreated() : saved.notifyUpdated();
    }

    protected Conversation
    resolve(ResolveAgentActionRequest request)
    {
        Conversation conversation = owned(request.getConversationId(),request.getUserId());

        if (!conversation.hasPendingAction() || !conversation.getPendingCalls().contains("\"" + request.getToolUseId() + "\""))
            throw new IllegalStateException("no such pending action");

        runner.resolve(conversation,request.getApproved(),context(request.getUserId(),request.getSessionId(),request.getAuthorities()));
        conversation.setChanged(Instant.now(clock));
        return repository.save(conversation).attach(this).notifyUpdated();
    }

    private Conversation
    owned(Long conversationId,Long userId)
    {
        Optional<Conversation> found = repository.findById(conversationId);

        if (found.isEmpty() || !userId.equals(found.get().getUserId()))
            throw new IllegalArgumentException(NOT_FOUND);

        return found.get();
    }

    private AgentToolContext
    context(Long userId,Long sessionId,Set<String> authorities)
    {
        return new AgentToolContext(userId,sessionId,authorities,tokens);
    }

    private String
    rootMessage(Throwable e)
    {
        Throwable cause = e;

        while (cause.getCause() != null && cause != cause.getCause())
            cause = cause.getCause();

        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }

    private AgentEvent
    toServiceEvent(IConversationEvent domainEvent,StandardEventType eventType)
    {
        return
            AgentEvent
                .newBuilder()
                .setEventType(eventType)
                .setIdentifiers(
                    EventIdentifiersData
                        .newBuilder()
                        .setEventId(UUID.randomUUID().toString())
                        .setCorrelationId(domainEvent.getCorrelationId())
                        .setTimestamp(domainEvent.getTimestamp())
                        .build())
                .setSourceType(domainEvent.getSource().getClass().getCanonicalName())
                .setSourceId(domainEvent.getSource().getPrimaryId())
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
