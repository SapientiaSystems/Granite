//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }             from "strata.frontendcontroller.service/RequestReply";
import type { FeedbackData, FeedbackType } from "granite.feedback.service/RequestReply";

export
interface FeedbackOutput
    extends FrontEndOutput
{
    types:           Array<FeedbackType>;
    submitted?:      FeedbackData | null;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
