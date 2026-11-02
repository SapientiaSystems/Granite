import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { FeedbackData } from "./FeedbackData";

export
interface FindAllFeedbackReply
    extends AbstractServiceReply
{
    feedback?: Array<FeedbackData>;
}
