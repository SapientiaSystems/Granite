import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface ResolveAgentActionRequest
    extends AbstractServiceRequest
{
    userId?: number;
    sessionId?: number;
    conversationId?: number;
    toolUseId?: string;
    approved?: boolean;
    authorities?: Array<string>;
}
