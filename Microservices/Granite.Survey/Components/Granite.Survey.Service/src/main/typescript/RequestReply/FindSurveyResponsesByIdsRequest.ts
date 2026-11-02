import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";

export
interface FindSurveyResponsesByIdsRequest
    extends AbstractServiceRequest
{
    responseIds?: Array<number>;
}
