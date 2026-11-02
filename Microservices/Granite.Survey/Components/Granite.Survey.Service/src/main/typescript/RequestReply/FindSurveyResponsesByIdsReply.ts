import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { SurveyResponseData } from "./SurveyResponseData";

export
interface FindSurveyResponsesByIdsReply
    extends AbstractServiceReply
{
    responses?: Array<SurveyResponseData>;
}
