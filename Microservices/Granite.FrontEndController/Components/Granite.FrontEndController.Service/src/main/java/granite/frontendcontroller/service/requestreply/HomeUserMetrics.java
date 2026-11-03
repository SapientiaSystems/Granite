//////////////////////////////////////////////////////////////////////////////
// HomeUserMetrics.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class HomeUserMetrics
    implements Serializable
{
    private long                total;
    private long                oktaLinked;
    private List<HomeCountData> byRole;

    public
    HomeUserMetrics()
    {
        total      = 0;
        oktaLinked = 0;
        byRole     = new ArrayList<>();
    }

    public HomeUserMetrics
    setTotal(long t)
    {
        total = t;
        return this;
    }

    public HomeUserMetrics
    setOktaLinked(long o)
    {
        oktaLinked = o;
        return this;
    }

    public HomeUserMetrics
    setByRole(List<HomeCountData> b)
    {
        byRole = b;
        return this;
    }

    public long
    getTotal() { return total; }

    public long
    getOktaLinked() { return oktaLinked; }

    public List<HomeCountData>
    getByRole() { return byRole; }
}

//////////////////////////////////////////////////////////////////////////////
