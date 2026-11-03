//////////////////////////////////////////////////////////////////////////////
// HomeAverageData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;

public
class HomeAverageData
    implements Serializable
{
    private String key;
    private String label;
    private Double average;

    public
    HomeAverageData()
    {
        key     = null;
        label   = null;
        average = null;
    }

    public
    HomeAverageData(String key,String label,Double average)
    {
        this.key     = key;
        this.label   = label;
        this.average = average;
    }

    public HomeAverageData
    setKey(String k)
    {
        key = k;
        return this;
    }

    public HomeAverageData
    setLabel(String l)
    {
        label = l;
        return this;
    }

    public HomeAverageData
    setAverage(Double a)
    {
        average = a;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getLabel() { return label; }

    public Double
    getAverage() { return average; }
}

//////////////////////////////////////////////////////////////////////////////
