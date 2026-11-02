import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { SurveyResponseStatus } from "./SurveyResponseStatus";

export
interface FindAllSurveyResponsesRequest
    extends AbstractServiceRequest
{
    statuses?: Array<SurveyResponseStatus>;
}
