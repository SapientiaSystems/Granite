import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ConversationData } from "./ConversationData";

export
interface SendAgentMessageReply
    extends AbstractServiceReply
{
    conversation?: ConversationData;
}
