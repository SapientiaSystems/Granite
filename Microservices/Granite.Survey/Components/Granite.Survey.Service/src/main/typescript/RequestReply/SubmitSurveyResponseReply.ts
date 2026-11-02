import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { SurveyResponseData } from "./SurveyResponseData";

export
interface SubmitSurveyResponseReply
    extends AbstractServiceReply
{
    submittedResponse?: SurveyResponseData;
}
