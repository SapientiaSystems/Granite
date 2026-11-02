//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { ConversationData } from "granite.agent.service/RequestReply";

export
interface AgentOutput
    extends FrontEndOutput
{
    conversation?: ConversationData | null;
    conversations: Array<ConversationData>;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
