//////////////////////////////////////////////////////////////////////////////
// HomeCountData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;

public
class HomeCountData
    implements Serializable
{
    private String key;
    private String label;
    private long   count;

    public
    HomeCountData()
    {
        key   = null;
        label = null;
        count = 0;
    }

    public
    HomeCountData(String key,String label,long count)
    {
        this.key   = key;
        this.label = label;
        this.count = count;
    }

    public HomeCountData
    setKey(String k)
    {
        key = k;
        return this;
    }

    public HomeCountData
    setLabel(String l)
    {
        label = l;
        return this;
    }

    public HomeCountData
    setCount(long c)
    {
        count = c;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getLabel() { return label; }

    public long
    getCount() { return count; }
}

//////////////////////////////////////////////////////////////////////////////
