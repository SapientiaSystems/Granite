import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { FindAgentConversationsRequest } from "./FindAgentConversationsRequest";
import type { FindAgentConversationsReply } from "./FindAgentConversationsReply";
import type { FindAgentConversationRequest } from "./FindAgentConversationRequest";
import type { FindAgentConversationReply } from "./FindAgentConversationReply";

export
interface IAgentQueryService
{
    findAgentConversations(request: FindAgentConversationsRequest): ICompletionStage<FindAgentConversationsReply>;

    findAgentConversation(request: FindAgentConversationRequest): ICompletionStage<FindAgentConversationReply>;
}
