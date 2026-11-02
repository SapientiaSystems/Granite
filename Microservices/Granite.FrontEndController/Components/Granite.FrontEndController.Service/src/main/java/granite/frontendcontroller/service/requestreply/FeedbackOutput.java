//////////////////////////////////////////////////////////////////////////////
// FeedbackOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.feedback.service.requestreply.FeedbackData;
import granite.feedback.service.requestreply.FeedbackType;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.List;

public
class FeedbackOutput
    extends FrontEndOutput
{
    private List<FeedbackType> types;
    private FeedbackData       submitted;
    private List<String>       failureMessages;

    public
    FeedbackOutput()
    {
        super();
        types           = new ArrayList<>(List.of(FeedbackType.values()));
        submitted       = null;
        failureMessages = new ArrayList<>();
    }

    @Override
    public FeedbackOutput
    setUserId(Long id)
    {
        return (FeedbackOutput)super.setUserId(id);
    }

    @Override
    public FeedbackOutput
    setSource(String src)
    {
        return (FeedbackOutput)super.setSource(src);
    }

    @Override
    public FeedbackOutput
    setDestination(String dest)
    {
        return (FeedbackOutput)super.setDestination(dest);
    }

    @Override
    public FeedbackOutput
    setCommand(String cmd)
    {
        return (FeedbackOutput)super.setCommand(cmd);
    }

    public FeedbackOutput
    setTypes(List<FeedbackType> value)
    {
        types = value;
        return this;
    }

    public FeedbackOutput
    setSubmitted(FeedbackData value)
    {
        submitted = value;
        return this;
    }

    public FeedbackOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public List<FeedbackType>
    getTypes() { return types; }

    public FeedbackData
    getSubmitted() { return submitted; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
