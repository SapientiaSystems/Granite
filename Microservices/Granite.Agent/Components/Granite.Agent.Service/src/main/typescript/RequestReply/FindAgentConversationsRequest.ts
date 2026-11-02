import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindAgentConversationsRequest
    extends AbstractServiceRequest
{
    userId?: number;
}
