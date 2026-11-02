//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HandleInputReply } from "strata.frontendcontroller.service/RequestReply";
import type { SurveyReviewInput } from "./SurveyReviewInput";
import type { SurveyReviewOutput } from "./SurveyReviewOutput";

export
interface HandleSurveyReviewInputReply
    extends HandleInputReply<SurveyReviewInput,SurveyReviewOutput>
{
}

//////////////////////////////////////////////////////////////////////////////
