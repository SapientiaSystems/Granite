import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface MarkFeedbackReceivedRequest
    extends AbstractServiceRequest
{
    feedbackIds?: Array<number>;
}
