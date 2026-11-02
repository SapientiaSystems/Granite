//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HandleInputReply } from "strata.frontendcontroller.service/RequestReply";
import type { FeedbackReviewInput } from "./FeedbackReviewInput";
import type { FeedbackReviewOutput } from "./FeedbackReviewOutput";

export
interface HandleFeedbackReviewInputReply
    extends HandleInputReply<FeedbackReviewInput,FeedbackReviewOutput>
{
}

//////////////////////////////////////////////////////////////////////////////
