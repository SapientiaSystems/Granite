//////////////////////////////////////////////////////////////////////////////
// HomeFeedbackMetrics.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class HomeFeedbackMetrics
    implements Serializable
{
    private long                    total;
    private List<HomeCountData>     byStatus;
    private List<HomeBreakdownData> byType;
    private List<HomeCountData>     daily;

    public
    HomeFeedbackMetrics()
    {
        total    = 0;
        byStatus = new ArrayList<>();
        byType   = new ArrayList<>();
        daily    = new ArrayList<>();
    }

    public HomeFeedbackMetrics
    setTotal(long t)
    {
        total = t;
        return this;
    }

    public HomeFeedbackMetrics
    setByStatus(List<HomeCountData> b)
    {
        byStatus = b;
        return this;
    }

    public HomeFeedbackMetrics
    setByType(List<HomeBreakdownData> b)
    {
        byType = b;
        return this;
    }

    public HomeFeedbackMetrics
    setDaily(List<HomeCountData> d)
    {
        daily = d;
        return this;
    }

    public long
    getTotal() { return total; }

    public List<HomeCountData>
    getByStatus() { return byStatus; }

    public List<HomeBreakdownData>
    getByType() { return byType; }

    public List<HomeCountData>
    getDaily() { return daily; }
}

//////////////////////////////////////////////////////////////////////////////
