import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ConversationData } from "./ConversationData";

export
interface FindAgentConversationsReply
    extends AbstractServiceReply
{
    conversations?: Array<ConversationData>;
}
