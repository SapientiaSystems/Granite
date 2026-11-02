//////////////////////////////////////////////////////////////////////////////
// FindAllFeedbackReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindAllFeedbackReply
    extends AbstractServiceReply
{
    private List<FeedbackData> feedback;

    public
    FindAllFeedbackReply()
    {
        super();
        feedback = new ArrayList<>();
    }

    public
    FindAllFeedbackReply(FindAllFeedbackRequest originatingRequest)
    {
        super(originatingRequest);
        feedback = new ArrayList<>();
    }

    @Override
    public FindAllFeedbackReply
    setReplyId(UUID replyId)
    {
        return (FindAllFeedbackReply)super.setReplyId(replyId);
    }

    @Override
    public FindAllFeedbackReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAllFeedbackReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAllFeedbackReply
    setTimestamp(Instant timestamp)
    {
        return (FindAllFeedbackReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllFeedbackReply
    setSuccess(boolean success)
    {
        return (FindAllFeedbackReply)super.setSuccess(success);
    }

    @Override
    public FindAllFeedbackReply
    setSuccessMessage(String successMessage)
    {
        return (FindAllFeedbackReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAllFeedbackReply
    setFailureMessage(String failureMessage)
    {
        return (FindAllFeedbackReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAllFeedbackReply
    setException(ExceptionData exception)
    {
        return (FindAllFeedbackReply)super.setException(exception);
    }

    public FindAllFeedbackReply
    setFeedback(List<FeedbackData> value)
    {
        feedback = value;
        return this;
    }

    public List<FeedbackData>
    getFeedback() { return feedback; }
}

//////////////////////////////////////////////////////////////////////////////
