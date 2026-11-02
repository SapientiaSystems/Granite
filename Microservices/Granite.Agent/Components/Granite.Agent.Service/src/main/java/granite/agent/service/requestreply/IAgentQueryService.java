//////////////////////////////////////////////////////////////////////////////
// IAgentQueryService.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import java.util.concurrent.CompletionStage;

public
interface IAgentQueryService
{
    CompletionStage<FindAgentConversationsReply>
    findAgentConversations(FindAgentConversationsRequest request);

    CompletionStage<FindAgentConversationReply>
    findAgentConversation(FindAgentConversationRequest request);
}

//////////////////////////////////////////////////////////////////////////////
