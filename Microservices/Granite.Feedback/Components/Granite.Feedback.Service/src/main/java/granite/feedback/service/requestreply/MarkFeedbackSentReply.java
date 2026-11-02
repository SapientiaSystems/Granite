//////////////////////////////////////////////////////////////////////////////
// MarkFeedbackSentReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkFeedbackSentReply
    extends AbstractServiceReply
{
    private Set<Long>          requestedFeedbackIds;
    private Set<Long>          failedFeedbackIds;
    private List<FeedbackData> updatedFeedback;

    public
    MarkFeedbackSentReply()
    {
        super();
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    public
    MarkFeedbackSentReply(MarkFeedbackSentRequest originatingRequest)
    {
        super(originatingRequest);
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    @Override
    public MarkFeedbackSentReply
    setReplyId(UUID replyId)
    {
        return (MarkFeedbackSentReply)super.setReplyId(replyId);
    }

    @Override
    public MarkFeedbackSentReply
    setOriginatingRequestId(UUID requestId)
    {
        return (MarkFeedbackSentReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public MarkFeedbackSentReply
    setTimestamp(Instant timestamp)
    {
        return (MarkFeedbackSentReply)super.setTimestamp(timestamp);
    }

    @Override
    public MarkFeedbackSentReply
    setSuccess(boolean success)
    {
        return (MarkFeedbackSentReply)super.setSuccess(success);
    }

    @Override
    public MarkFeedbackSentReply
    setSuccessMessage(String successMessage)
    {
        return (MarkFeedbackSentReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public MarkFeedbackSentReply
    setFailureMessage(String failureMessage)
    {
        return (MarkFeedbackSentReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public MarkFeedbackSentReply
    setException(ExceptionData exception)
    {
        return (MarkFeedbackSentReply)super.setException(exception);
    }

    public MarkFeedbackSentReply
    setRequestedFeedbackIds(Set<Long> value)
    {
        requestedFeedbackIds = value;
        return this;
    }

    public MarkFeedbackSentReply
    setFailedFeedbackIds(Set<Long> value)
    {
        failedFeedbackIds = value;
        return this;
    }

    public MarkFeedbackSentReply
    setUpdatedFeedback(List<FeedbackData> value)
    {
        updatedFeedback = value;
        return this;
    }

    public Set<Long>
    getRequestedFeedbackIds() { return requestedFeedbackIds; }

    public Set<Long>
    getFailedFeedbackIds() { return failedFeedbackIds; }

    public List<FeedbackData>
    getUpdatedFeedback() { return updatedFeedback; }
}

//////////////////////////////////////////////////////////////////////////////
