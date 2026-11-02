import type { AbstractServiceReply } from "strata.foundation.core/Transfer";
import type { SurveyResponseData } from "./SurveyResponseData";

export
interface UpdateSurveyResponsesReply
    extends AbstractServiceReply
{
    requestedResponseIds?: Array<number>;
    failedResponseIds?: Array<number>;
    updatedResponses?: Array<SurveyResponseData>;
}
