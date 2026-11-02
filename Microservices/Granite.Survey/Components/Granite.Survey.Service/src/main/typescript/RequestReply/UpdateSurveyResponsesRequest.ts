import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { UpdateSurveyResponseData } from "./UpdateSurveyResponseData";

export
interface UpdateSurveyResponsesRequest
    extends AbstractServiceRequest
{
    responses?: Array<UpdateSurveyResponseData>;
}
