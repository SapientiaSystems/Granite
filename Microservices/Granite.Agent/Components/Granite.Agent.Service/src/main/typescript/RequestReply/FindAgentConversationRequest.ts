import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindAgentConversationRequest
    extends AbstractServiceRequest
{
    userId?: number;
    conversationId?: number;
}
