import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { SurveyResponseData } from "./SurveyResponseData";

export
interface FindAllSurveyResponsesReply
    extends AbstractServiceReply
{
    responses?: Array<SurveyResponseData>;
}
