//////////////////////////////////////////////////////////////////////////////
// HomeSurveyMetrics.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public
class HomeSurveyMetrics
    implements Serializable
{
    private long                  responses;
    private List<HomeAverageData> averages;
    private List<HomeCountData>   satisfactionBands;
    private List<HomeCountData>   daily;

    public
    HomeSurveyMetrics()
    {
        responses         = 0;
        averages          = new ArrayList<>();
        satisfactionBands = new ArrayList<>();
        daily             = new ArrayList<>();
    }

    public HomeSurveyMetrics
    setResponses(long r)
    {
        responses = r;
        return this;
    }

    public HomeSurveyMetrics
    setAverages(List<HomeAverageData> a)
    {
        averages = a;
        return this;
    }

    public HomeSurveyMetrics
    setSatisfactionBands(List<HomeCountData> s)
    {
        satisfactionBands = s;
        return this;
    }

    public HomeSurveyMetrics
    setDaily(List<HomeCountData> d)
    {
        daily = d;
        return this;
    }

    public long
    getResponses() { return responses; }

    public List<HomeAverageData>
    getAverages() { return averages; }

    public List<HomeCountData>
    getSatisfactionBands() { return satisfactionBands; }

    public List<HomeCountData>
    getDaily() { return daily; }
}

//////////////////////////////////////////////////////////////////////////////
