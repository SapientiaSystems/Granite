import type { AbstractServiceRequest } from "strata.foundation.core/Transfer";
import type { SubmitSurveyResponseData } from "./SubmitSurveyResponseData";

export
interface SubmitSurveyResponseRequest
    extends AbstractServiceRequest
{
    response?: SubmitSurveyResponseData;
}
