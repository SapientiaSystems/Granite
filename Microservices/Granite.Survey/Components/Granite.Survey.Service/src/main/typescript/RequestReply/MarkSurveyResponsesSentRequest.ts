import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface MarkSurveyResponsesSentRequest
    extends AbstractServiceRequest
{
    responseIds?: Array<number>;
}
