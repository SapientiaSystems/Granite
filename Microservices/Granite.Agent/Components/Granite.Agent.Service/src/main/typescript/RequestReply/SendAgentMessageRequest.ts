import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface SendAgentMessageRequest
    extends AbstractServiceRequest
{
    userId?: number;
    sessionId?: number;
    conversationId?: number;
    text?: string;
    authorities?: Array<string>;
}
