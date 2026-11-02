import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { ConversationData } from "./ConversationData";

export
interface ResolveAgentActionReply
    extends AbstractServiceReply
{
    conversation?: ConversationData;
}
