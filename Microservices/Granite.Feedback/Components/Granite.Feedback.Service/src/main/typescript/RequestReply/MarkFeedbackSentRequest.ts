import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface MarkFeedbackSentRequest
    extends AbstractServiceRequest
{
    feedbackIds?: Array<number>;
}
