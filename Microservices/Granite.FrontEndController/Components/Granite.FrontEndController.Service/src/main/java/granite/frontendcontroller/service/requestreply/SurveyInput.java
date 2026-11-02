//////////////////////////////////////////////////////////////////////////////
// SurveyInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;
import java.util.LinkedHashMap;
import java.util.Map;

public
class SurveyInput
    extends FrontEndInput
{
    private Map<String,Integer> answers;
    private String              comment;

    public
    SurveyInput()
    {
        super();
        answers = new LinkedHashMap<>();
        comment = null;
    }

    @Override
    public SurveyInput
    setUserId(Long id)
    {
        return (SurveyInput)super.setUserId(id);
    }

    @Override
    public SurveyInput
    setSource(String src)
    {
        return (SurveyInput)super.setSource(src);
    }

    @Override
    public SurveyInput
    setDestination(String dest)
    {
        return (SurveyInput)super.setDestination(dest);
    }

    @Override
    public SurveyInput
    setCommand(String cmd)
    {
        return (SurveyInput)super.setCommand(cmd);
    }

    public SurveyInput
    setAnswers(Map<String,Integer> value)
    {
        answers = value;
        return this;
    }

    public SurveyInput
    setComment(String value)
    {
        comment = value;
        return this;
    }

    public Map<String,Integer>
    getAnswers() { return answers; }

    public String
    getComment() { return comment; }
}

//////////////////////////////////////////////////////////////////////////////
