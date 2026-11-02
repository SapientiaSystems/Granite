//////////////////////////////////////////////////////////////////////////////
// SurveyQuestionData.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public
class SurveyQuestionData
    implements Serializable
{
    private String              key;
    private String              label;
    private Map<Integer,String> labels;

    public
    SurveyQuestionData()
    {
        key    = null;
        label  = null;
        labels = new LinkedHashMap<>();
    }

    public SurveyQuestionData
    setKey(String value)
    {
        key = value;
        return this;
    }

    public SurveyQuestionData
    setLabel(String value)
    {
        label = value;
        return this;
    }

    public SurveyQuestionData
    setLabels(Map<Integer,String> value)
    {
        labels = value;
        return this;
    }

    public String
    getKey() { return key; }

    public String
    getLabel() { return label; }

    public Map<Integer,String>
    getLabels() { return labels; }
}

//////////////////////////////////////////////////////////////////////////////
