//////////////////////////////////////////////////////////////////////////////
// IAgentCommandService.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IAgentCommandService
{
    CompletionStage<SendAgentMessageReply>
    sendAgentMessage(SendAgentMessageRequest request);

    CompletionStage<ResolveAgentActionReply>
    resolveAgentAction(ResolveAgentActionRequest request);
}

//////////////////////////////////////////////////////////////////////////////
