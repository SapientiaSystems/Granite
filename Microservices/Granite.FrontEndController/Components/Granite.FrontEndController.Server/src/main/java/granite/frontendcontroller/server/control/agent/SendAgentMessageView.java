//////////////////////////////////////////////////////////////////////////////
// SendAgentMessageView.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.agent;

import granite.agent.service.requestreply.SendAgentMessageReply;
import granite.agent.service.requestreply.SendAgentMessageRequest;
import granite.frontendcontroller.server.application.GraniteHelpServiceClients;
import granite.frontendcontroller.server.control.IInputAction;
import granite.frontendcontroller.server.platform.SessionRequestContext;
import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.AgentOutput;

import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public
class SendAgentMessageView
    implements IInputAction<AgentInput,AgentOutput>
{
    public static final int MAX_LENGTH = 4000;

    private final GraniteHelpServiceClients clients;
    private final SessionRequestContext     context;

    public
    SendAgentMessageView(GraniteHelpServiceClients clients,SessionRequestContext context)
    {
        this.clients = clients;
        this.context = context;
    }

    @Override
    public CompletionStage<AgentOutput>
    apply(AgentInput input)
    {
        Long caller  = context.getCallerUserId().orElse(null);
        Long session = context.getSessionId().orElse(null);

        if (caller == null || session == null)
            return CompletableFuture.completedFuture(AgentOutputs.describe(input,AgentOutputs.failure(AgentOutputs.NOT_AUTHENTICATED),null));

        String text = input.getText() == null ? "" : input.getText().trim();

        if (text.isEmpty() || text.length() > MAX_LENGTH)
            return
                CompletableFuture.completedFuture(
                    AgentOutputs.describe(input,AgentOutputs.failure(text.isEmpty() ? "text is required" : "text is longer than " + MAX_LENGTH + " characters"),caller));

        return
            clients
                .getAgentCommandClient()
                .sendAgentMessage(
                    new SendAgentMessageRequest()
                        .setUserId(caller)
                        .setSessionId(session)
                        .setConversationId(input.getConversationId())
                        .setText(text)
                        .setAuthorities(new TreeSet<>(context.getCallerAuthorities())))
                .thenApply(reply -> AgentOutputs.describe(input,toOutput(reply),caller));
    }

    private AgentOutput
    toOutput(SendAgentMessageReply reply)
    {
        if (reply != null && reply.isSuccess())
            return new AgentOutput().setConversation(reply.getConversation());

        return AgentOutputs.failure(AgentOutputs.reason(reply == null ? null : reply.getFailureMessage()));
    }
}

//////////////////////////////////////////////////////////////////////////////
