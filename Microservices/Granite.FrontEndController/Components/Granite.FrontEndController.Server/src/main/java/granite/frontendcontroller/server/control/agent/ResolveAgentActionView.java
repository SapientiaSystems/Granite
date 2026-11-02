//////////////////////////////////////////////////////////////////////////////
// ResolveAgentActionView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.agent;

import granite.agent.service.requestreply.ResolveAgentActionReply;
import granite.agent.service.requestreply.ResolveAgentActionRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.AgentOutput;

import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class ResolveAgentActionView
    implements IInputAction<AgentInput,AgentOutput>
{
    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;
    private final boolean                   approve;

    public
    ResolveAgentActionView(GraniteHelpServiceClients clients,SessionRequestContext context,boolean approve)
    {
        this.clients = clients;
        this.context = context;
        this.approve = approve;
    }

    @Override
    public CompletionStage<AgentOutput>
    apply(AgentInput input)
    {
        Long caller  = context.getCallerUserId().orElse(null);
        Long session = context.getSessionId().orElse(null);

        if (caller == null || session == null)
            return CompletableFuture.completedFuture(AgentOutputs.describe(input,AgentOutputs.failure(AgentOutputs.NOT_AUTHENTICATED),null));

        if (input.getConversationId() == null || input.getToolUseId() == null || input.getToolUseId().isBlank())
            return CompletableFuture.completedFuture(AgentOutputs.describe(input,AgentOutputs.failure("conversationId and toolUseId are required"),caller));

        return
            clients
                .getAgentCommandClient()
                .resolveAgentAction(
                    new ResolveAgentActionRequest()
                        .setUserId(caller)
                        .setSessionId(session)
                        .setConversationId(input.getConversationId())
                        .setToolUseId(input.getToolUseId())
                        .setApproved(approve)
                        .setAuthorities(new TreeSet<>(context.getCallerAuthorities())))
                .thenApply(reply -> AgentOutputs.describe(input,toOutput(reply),caller));
    }

    private AgentOutput
    toOutput(ResolveAgentActionReply reply)
    {
        if (reply != null && reply.isSuccess())
            return new AgentOutput().setConversation(reply.getConversation());

        return AgentOutputs.failure(AgentOutputs.reason(reply == null ? null : reply.getFailureMessage()));
    }
}

//////////////////////////////////////////////////////////////////////////////
