//////////////////////////////////////////////////////////////////////////////
// FindFeedbackByIdsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindFeedbackByIdsReply
    extends AbstractServiceReply
{
    private List<FeedbackData> feedback;

    public
    FindFeedbackByIdsReply()
    {
        super();
        feedback = new ArrayList<>();
    }

    public
    FindFeedbackByIdsReply(FindFeedbackByIdsRequest originatingRequest)
    {
        super(originatingRequest);
        feedback = new ArrayList<>();
    }

    @Override
    public FindFeedbackByIdsReply
    setReplyId(UUID replyId)
    {
        return (FindFeedbackByIdsReply)super.setReplyId(replyId);
    }

    @Override
    public FindFeedbackByIdsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindFeedbackByIdsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindFeedbackByIdsReply
    setTimestamp(Instant timestamp)
    {
        return (FindFeedbackByIdsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindFeedbackByIdsReply
    setSuccess(boolean success)
    {
        return (FindFeedbackByIdsReply)super.setSuccess(success);
    }

    @Override
    public FindFeedbackByIdsReply
    setSuccessMessage(String successMessage)
    {
        return (FindFeedbackByIdsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindFeedbackByIdsReply
    setFailureMessage(String failureMessage)
    {
        return (FindFeedbackByIdsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindFeedbackByIdsReply
    setException(ExceptionData exception)
    {
        return (FindFeedbackByIdsReply)super.setException(exception);
    }

    public FindFeedbackByIdsReply
    setFeedback(List<FeedbackData> value)
    {
        feedback = value;
        return this;
    }

    public List<FeedbackData>
    getFeedback() { return feedback; }
}

//////////////////////////////////////////////////////////////////////////////
