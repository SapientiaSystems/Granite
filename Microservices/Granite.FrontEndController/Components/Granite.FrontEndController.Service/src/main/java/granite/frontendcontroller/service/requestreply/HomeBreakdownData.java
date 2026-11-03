//////////////////////////////////////////////////////////////////////////////
// HomeBreakdownData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public
class HomeBreakdownData
    implements Serializable
{
    private String           key;
    private String           label;
    private Map<String,Long> counts;

    public
    HomeBreakdownData()
    {
        key    = null;
        label  = null;
        counts = new LinkedHashMap<>();
    }

    public
    HomeBreakdownData(String key,String label,Map<String,Long> counts)
    {
        this.key    = key;
        this.label  = label;
        this.counts = counts;
    }

    public HomeBreakdownData
    setKey(String k)
    {
        key = k;
        return this;
    }

    public HomeBreakdownData
    setLabel(String l)
    {
        label = l;
        return this;
    }

    public HomeBreakdownData
    setCounts(Map<String,Long> c)
    {
        counts = c;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getLabel() { return label; }

    public Map<String,Long>
    getCounts() { return counts; }
}

//////////////////////////////////////////////////////////////////////////////
