//////////////////////////////////////////////////////////////////////////////
// FeedbackReviewInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.feedback.service.requestreply.UpdateFeedbackData;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.ArrayList;
import java.util.List;

public
class FeedbackReviewInput
    extends FrontEndInput
{
    private List<UpdateFeedbackData> changes;

    public
    FeedbackReviewInput()
    {
        super();
        changes = new ArrayList<>();
    }

    @Override
    public FeedbackReviewInput
    setUserId(Long id)
    {
        return (FeedbackReviewInput)super.setUserId(id);
    }

    @Override
    public FeedbackReviewInput
    setSource(String src)
    {
        return (FeedbackReviewInput)super.setSource(src);
    }

    @Override
    public FeedbackReviewInput
    setDestination(String dest)
    {
        return (FeedbackReviewInput)super.setDestination(dest);
    }

    @Override
    public FeedbackReviewInput
    setCommand(String cmd)
    {
        return (FeedbackReviewInput)super.setCommand(cmd);
    }

    public FeedbackReviewInput
    setChanges(List<UpdateFeedbackData> value)
    {
        changes = value;
        return this;
    }

    public List<UpdateFeedbackData>
    getChanges() { return changes; }
}

//////////////////////////////////////////////////////////////////////////////
