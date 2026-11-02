import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindFeedbackByIdsRequest
    extends AbstractServiceRequest
{
    feedbackIds?: Array<number>;
}
