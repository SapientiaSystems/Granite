import type { ICompletionStage } from "strata.foundation.core/Concurrent";
import type { SendAgentMessageRequest } from "./SendAgentMessageRequest";
import type { SendAgentMessageReply } from "./SendAgentMessageReply";
import type { ResolveAgentActionRequest } from "./ResolveAgentActionRequest";
import type { ResolveAgentActionReply } from "./ResolveAgentActionReply";

export
interface IAgentCommandService
{
    sendAgentMessage(request: SendAgentMessageRequest): ICompletionStage<SendAgentMessageReply>;

    resolveAgentAction(request: ResolveAgentActionRequest): ICompletionStage<ResolveAgentActionReply>;
}
