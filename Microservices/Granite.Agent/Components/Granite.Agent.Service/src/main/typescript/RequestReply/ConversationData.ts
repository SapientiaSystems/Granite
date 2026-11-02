import type { AgentMessageData } from "./AgentMessageData";
import type { AgentPendingActionData } from "./AgentPendingActionData";

export
interface ConversationData
{
    conversationId?: number;
    userId?: number;
    title?: string;
    messages?: Array<AgentMessageData>;
    pendingAction?: AgentPendingActionData;
    inputTokens?: number;
    outputTokens?: number;
    created?: string;
    updated?: string;
}
