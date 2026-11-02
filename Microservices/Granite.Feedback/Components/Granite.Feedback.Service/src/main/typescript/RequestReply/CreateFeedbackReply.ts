import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { FeedbackData } from "./FeedbackData";

export
interface CreateFeedbackReply
    extends AbstractServiceReply
{
    createdFeedback?: FeedbackData;
}
