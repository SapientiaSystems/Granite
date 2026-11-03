//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }      from "strata.frontendcontroller.service/RequestReply";
import type { HomeFeedbackMetrics } from "./HomeFeedbackMetrics";
import type { HomeSessionMetrics }  from "./HomeSessionMetrics";
import type { HomeSurveyMetrics }   from "./HomeSurveyMetrics";
import type { HomeUserMetrics }     from "./HomeUserMetrics";

export
interface HomeOutput
    extends FrontEndOutput
{
    userName?:       string | null;
    roles:           Array<string>;
    generated?:      string | null;
    users?:          HomeUserMetrics | null;
    sessions?:       HomeSessionMetrics | null;
    feedback?:       HomeFeedbackMetrics | null;
    survey?:         HomeSurveyMetrics | null;
    failureMessages: Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
