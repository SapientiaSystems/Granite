//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { FeedbackData }   from "granite.feedback.service/RequestReply";

export
interface FeedbackReviewOutput
    extends FrontEndOutput
{
    feedback:        Array<FeedbackData>;
    userNames:       Record<string,string>;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
