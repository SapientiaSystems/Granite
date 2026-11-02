//////////////////////////////////////////////////////////////////////////////
// FeedbackInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.feedback.service.requestreply.FeedbackType;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.LinkedHashMap;
import java.util.Map;

public
class FeedbackInput
    extends FrontEndInput
{
    private FeedbackType       type;
    private String             summary;
    private Map<String,String> details;

    public
    FeedbackInput()
    {
        super();
        type    = null;
        summary = null;
        details = new LinkedHashMap<>();
    }

    @Override
    public FeedbackInput
    setUserId(Long id)
    {
        return (FeedbackInput)super.setUserId(id);
    }

    @Override
    public FeedbackInput
    setSource(String src)
    {
        return (FeedbackInput)super.setSource(src);
    }

    @Override
    public FeedbackInput
    setDestination(String dest)
    {
        return (FeedbackInput)super.setDestination(dest);
    }

    @Override
    public FeedbackInput
    setCommand(String cmd)
    {
        return (FeedbackInput)super.setCommand(cmd);
    }

    public FeedbackInput
    setType(FeedbackType value)
    {
        type = value;
        return this;
    }

    public FeedbackInput
    setSummary(String value)
    {
        summary = value;
        return this;
    }

    public FeedbackInput
    setDetails(Map<String,String> value)
    {
        details = value;
        return this;
    }

    public FeedbackType
    getType() { return type; }

    public String
    getSummary() { return summary; }

    public Map<String,String>
    getDetails() { return details; }
}

//////////////////////////////////////////////////////////////////////////////
