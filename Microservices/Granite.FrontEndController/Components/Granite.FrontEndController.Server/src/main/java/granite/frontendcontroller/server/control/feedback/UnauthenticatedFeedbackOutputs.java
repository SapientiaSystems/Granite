//////////////////////////////////////////////////////////////////////////////
// UnauthenticatedFeedbackOutputs.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.feedback;

import granite.frontendcontroller.service.requestreply.FeedbackOutput;
import granite.frontendcontroller.service.requestreply.FeedbackReviewOutput;

import java.util.ArrayList;
import java.util.List;

public
class UnauthenticatedFeedbackOutputs
{
    public static final String MESSAGE = "not authenticated";

    private
    UnauthenticatedFeedbackOutputs() {}

    public static FeedbackOutput
    feedback()
    {
        return new FeedbackOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }

    public static FeedbackReviewOutput
    review()
    {
        return new FeedbackReviewOutput().setFailureMessages(new ArrayList<>(List.of(MESSAGE)));
    }
}

//////////////////////////////////////////////////////////////////////////////
