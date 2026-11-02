import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { FeedbackStatus } from "./FeedbackStatus";

export
interface FindAllFeedbackRequest
    extends AbstractServiceRequest
{
    statuses?: Array<FeedbackStatus>;
}
