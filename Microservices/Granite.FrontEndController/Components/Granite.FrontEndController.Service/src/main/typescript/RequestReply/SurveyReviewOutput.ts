//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { SurveyQuestionData, SurveyResponseData } from "granite.survey.service/RequestReply";

export
interface SurveyReviewOutput
    extends FrontEndOutput
{
    responses: Array<SurveyResponseData>;
    questions: Array<SurveyQuestionData>;
    averages: Record<string,number>;
    userNames: Record<string,string>;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
