//////////////////////////////////////////////////////////////////////////////
// InitializeAgentView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.agent;

import granite.agent.service.requestreply.ConversationData;
import granite.agent.service.requestreply.FindAgentConversationReply;
import granite.agent.service.requestreply.FindAgentConversationRequest;
import granite.agent.service.requestreply.FindAgentConversationsReply;
import granite.agent.service.requestreply.FindAgentConversationsRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IOutputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.AgentOutput;
import granite.frontendcontroller.service.requestreply.GetAgentOutputRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class InitializeAgentView
    implements IOutputAction<GetAgentOutputRequest,AgentOutput>
{
    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    InitializeAgentView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<AgentOutput>
    apply(GetAgentOutputRequest request)
    {
        Long caller = context.getCallerUserId().orElse(null);

        CompletionStage<AgentOutput> output =
            caller == null
                ? CompletableFuture.completedFuture(AgentOutputs.failure(AgentOutputs.NOT_AUTHENTICATED))
                : clients
                    .getAgentQueryClient()
                    .findAgentConversations(new FindAgentConversationsRequest().setUserId(caller))
                    .thenCompose(reply -> latest(caller,reply));

        return
            output.thenApply(
                result ->
                    result
                        .setUserId(caller)
                        .setSource(request.getSource())
                        .setCommand(request.getCommand())
                        .setDestination(request.getDestination()));
    }

    private CompletionStage<AgentOutput>
    latest(Long caller,FindAgentConversationsReply reply)
    {
        if (reply == null || !reply.isSuccess())
            return CompletableFuture.completedFuture(AgentOutputs.failure("Unable to load conversations: " + AgentOutputs.reason(reply == null ? null : reply.getFailureMessage())));

        List<ConversationData> conversations = reply.getConversations();

        if (conversations.isEmpty())
            return CompletableFuture.completedFuture(new AgentOutput());

        return
            clients
                .getAgentQueryClient()
                .findAgentConversation(new FindAgentConversationRequest().setUserId(caller).setConversationId(conversations.get(0).getConversationId()))
                .thenApply(
                    (FindAgentConversationReply found) ->
                        new AgentOutput()
                            .setConversations(conversations)
                            .setConversation(found != null && found.isSuccess() ? found.getConversation() : null));
    }
}

//////////////////////////////////////////////////////////////////////////////
