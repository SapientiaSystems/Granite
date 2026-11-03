//////////////////////////////////////////////////////////////////////////////
// HomeSessionMetrics.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class HomeSessionMetrics
    implements Serializable
{
    private long                    open;
    private long                    activeLastHour;
    private long                    expiringWithinHour;
    private List<HomeCountData>     openByLoginMethod;
    private List<HomeBreakdownData> dailyLogins;
    private List<HomeCountData>     recentOutcomes;

    public
    HomeSessionMetrics()
    {
        open               = 0;
        activeLastHour     = 0;
        expiringWithinHour = 0;
        openByLoginMethod  = new ArrayList<>();
        dailyLogins        = new ArrayList<>();
        recentOutcomes     = new ArrayList<>();
    }

    public HomeSessionMetrics
    setOpen(long o)
    {
        open = o;
        return this;
    }

    public HomeSessionMetrics
    setActiveLastHour(long a)
    {
        activeLastHour = a;
        return this;
    }

    public HomeSessionMetrics
    setExpiringWithinHour(long e)
    {
        expiringWithinHour = e;
        return this;
    }

    public HomeSessionMetrics
    setOpenByLoginMethod(List<HomeCountData> o)
    {
        openByLoginMethod = o;
        return this;
    }

    public HomeSessionMetrics
    setDailyLogins(List<HomeBreakdownData> d)
    {
        dailyLogins = d;
        return this;
    }

    public HomeSessionMetrics
    setRecentOutcomes(List<HomeCountData> r)
    {
        recentOutcomes = r;
        return this;
    }

    public long
    getOpen() { return open; }

    public long
    getActiveLastHour() { return activeLastHour; }

    public long
    getExpiringWithinHour() { return expiringWithinHour; }

    public List<HomeCountData>
    getOpenByLoginMethod() { return openByLoginMethod; }

    public List<HomeBreakdownData>
    getDailyLogins() { return dailyLogins; }

    public List<HomeCountData>
    getRecentOutcomes() { return recentOutcomes; }
}

//////////////////////////////////////////////////////////////////////////////
