import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface MarkSurveyResponsesReceivedRequest
    extends AbstractServiceRequest
{
    responseIds?: Array<number>;
}
