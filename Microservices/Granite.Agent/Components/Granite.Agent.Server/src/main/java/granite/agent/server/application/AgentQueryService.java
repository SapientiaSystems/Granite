//////////////////////////////////////////////////////////////////////////////
// AgentQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import granite.agent.server.domain.Conversation;
import granite.agent.server.domain.IConversationRepository;
import granite.agent.service.requestreply.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import strata.foundation.core.concurrent.CurrentThreadExecutor;
import strata.foundation.core.transfer.ExceptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class AgentQueryService
    implements ITransactionalAgentQueryService
{
    private final IConversationRepository repository;
    private final ModelMapper             mapper;
    private final Logger                  logger;

    public
    AgentQueryService(IConversationRepository r,ModelMapper m)
    {
        repository = r;
        mapper     = m;
        logger     = LogManager.getLogger(AgentQueryService.class);
    }

    @Override
    public CompletionStage<FindAgentConversationsReply>
    findAgentConversations(FindAgentConversationsRequest request)
    {
        logger.debug("findAgentConversations({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> Objects.requireNonNull(Objects.requireNonNull(request,"request").getUserId(),"validation failed: userId was not provided"),new CurrentThreadExecutor())
                .thenApply(repository::findAllByUserId)
                .thenApply(
                    found ->
                        new FindAgentConversationsReply(request)
                            .setSuccess(true)
                            .setSuccessMessage("Found " + found.size() + " conversation(s)")
                            .setConversations(summaries(found)))
                .exceptionally(
                    e ->
                        new FindAgentConversationsReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find conversations: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    @Override
    public CompletionStage<FindAgentConversationReply>
    findAgentConversation(FindAgentConversationRequest request)
    {
        logger.debug("findAgentConversation({})",request != null ? request.getRequestId() : null);
        return
            CompletableFuture
                .supplyAsync(() -> validate(request),new CurrentThreadExecutor())
                .thenApply(valid -> repository.findById(valid.getConversationId()).filter(c -> valid.getUserId().equals(c.getUserId())))
                .thenApply(
                    found ->
                        found.isEmpty()
                            ? new FindAgentConversationReply(request).setSuccess(false).setFailureMessage(AgentCommandService.NOT_FOUND)
                            : new FindAgentConversationReply(request)
                                .setSuccess(true)
                                .setSuccessMessage("Found conversation")
                                .setConversation(mapper.map(found.get(),ConversationData.class)))
                .exceptionally(
                    e ->
                        new FindAgentConversationReply(request)
                            .setSuccess(false)
                            .setFailureMessage("Unable to find conversation: " + e.getMessage())
                            .setException(ExceptionData.of(e)));
    }

    protected FindAgentConversationRequest
    validate(FindAgentConversationRequest request)
    {
        Objects.requireNonNull(request,"request");
        Objects.requireNonNull(request.getUserId(),"validation failed: userId was not provided");
        Objects.requireNonNull(request.getConversationId(),"validation failed: conversationId was not provided");
        return request;
    }

    private List<ConversationData>
    summaries(List<Conversation> found)
    {
        List<ConversationData> result = new ArrayList<>();

        for (Conversation conversation : found)
            result.add(mapper.map(conversation,ConversationData.class).setMessages(new ArrayList<>()));

        return result;
    }
}

//////////////////////////////////////////////////////////////////////////////
