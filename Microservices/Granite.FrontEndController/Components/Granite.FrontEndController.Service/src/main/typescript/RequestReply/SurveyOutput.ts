//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { SurveyQuestionData, SurveyResponseData } from "granite.survey.service/RequestReply";

export
interface SurveyOutput
    extends FrontEndOutput
{
    questions: Array<SurveyQuestionData>;
    submitted?: SurveyResponseData | null;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
