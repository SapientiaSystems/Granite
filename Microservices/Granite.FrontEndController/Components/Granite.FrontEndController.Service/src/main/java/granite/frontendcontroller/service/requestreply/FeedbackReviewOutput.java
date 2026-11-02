//////////////////////////////////////////////////////////////////////////////
// FeedbackReviewOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.feedback.service.requestreply.FeedbackData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public
class FeedbackReviewOutput
    extends FrontEndOutput
{
    private List<FeedbackData> feedback;
    private Map<Long,String>   userNames;
    private List<String>       failureMessages;

    public
    FeedbackReviewOutput()
    {
        super();
        feedback        = new ArrayList<>();
        userNames       = new LinkedHashMap<>();
        failureMessages = new ArrayList<>();
    }

    @Override
    public FeedbackReviewOutput
    setUserId(Long id)
    {
        return (FeedbackReviewOutput)super.setUserId(id);
    }

    @Override
    public FeedbackReviewOutput
    setSource(String src)
    {
        return (FeedbackReviewOutput)super.setSource(src);
    }

    @Override
    public FeedbackReviewOutput
    setDestination(String dest)
    {
        return (FeedbackReviewOutput)super.setDestination(dest);
    }

    @Override
    public FeedbackReviewOutput
    setCommand(String cmd)
    {
        return (FeedbackReviewOutput)super.setCommand(cmd);
    }

    public FeedbackReviewOutput
    setFeedback(List<FeedbackData> value)
    {
        feedback = value;
        return this;
    }

    public FeedbackReviewOutput
    setUserNames(Map<Long,String> value)
    {
        userNames = value;
        return this;
    }

    public FeedbackReviewOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public List<FeedbackData>
    getFeedback() { return feedback; }

    public Map<Long,String>
    getUserNames() { return userNames; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
