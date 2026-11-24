//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HomeBreakdownData } from "./HomeBreakdownData";
import type { HomeCountData }     from "./HomeCountData";

export
interface HomeSessionMetrics
{
    open:               number;
    activeLastHour:     number;
    expiringWithinHour: number;
    openByLoginMethod:  Array<HomeCountData>;
    dailyLogins:        Array<HomeBreakdownData>;
    recentOutcomes:     Array<HomeCountData>;
}

//////////////////////////////////////////////////////////////////////////////
