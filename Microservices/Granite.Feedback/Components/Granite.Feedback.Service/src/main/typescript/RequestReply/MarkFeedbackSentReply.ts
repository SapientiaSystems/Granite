import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { FeedbackData } from "./FeedbackData";

export
interface MarkFeedbackSentReply
    extends AbstractServiceReply
{
    requestedFeedbackIds?: Array<number>;
    failedFeedbackIds?: Array<number>;
    updatedFeedback?: Array<FeedbackData>;
}
