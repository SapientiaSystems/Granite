//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HomeAverageData } from "./HomeAverageData";
import type { HomeCountData }   from "./HomeCountData";

export
interface HomeSurveyMetrics
{
    responses:         number;
    averages:          Array<HomeAverageData>;
    satisfactionBands: Array<HomeCountData>;
    daily:             Array<HomeCountData>;
}

//////////////////////////////////////////////////////////////////////////////
