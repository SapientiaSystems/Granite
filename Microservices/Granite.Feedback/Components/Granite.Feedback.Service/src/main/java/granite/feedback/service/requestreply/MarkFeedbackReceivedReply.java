//////////////////////////////////////////////////////////////////////////////
// MarkFeedbackReceivedReply.java
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
class MarkFeedbackReceivedReply
    extends AbstractServiceReply
{
    private Set<Long>          requestedFeedbackIds;
    private Set<Long>          failedFeedbackIds;
    private List<FeedbackData> updatedFeedback;

    public
    MarkFeedbackReceivedReply()
    {
        super();
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    public
    MarkFeedbackReceivedReply(MarkFeedbackReceivedRequest originatingRequest)
    {
        super(originatingRequest);
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    @Override
    public MarkFeedbackReceivedReply
    setReplyId(UUID replyId)
    {
        return (MarkFeedbackReceivedReply)super.setReplyId(replyId);
    }

    @Override
    public MarkFeedbackReceivedReply
    setOriginatingRequestId(UUID requestId)
    {
        return (MarkFeedbackReceivedReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public MarkFeedbackReceivedReply
    setTimestamp(Instant timestamp)
    {
        return (MarkFeedbackReceivedReply)super.setTimestamp(timestamp);
    }

    @Override
    public MarkFeedbackReceivedReply
    setSuccess(boolean success)
    {
        return (MarkFeedbackReceivedReply)super.setSuccess(success);
    }

    @Override
    public MarkFeedbackReceivedReply
    setSuccessMessage(String successMessage)
    {
        return (MarkFeedbackReceivedReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public MarkFeedbackReceivedReply
    setFailureMessage(String failureMessage)
    {
        return (MarkFeedbackReceivedReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public MarkFeedbackReceivedReply
    setException(ExceptionData exception)
    {
        return (MarkFeedbackReceivedReply)super.setException(exception);
    }

    public MarkFeedbackReceivedReply
    setRequestedFeedbackIds(Set<Long> value)
    {
        requestedFeedbackIds = value;
        return this;
    }

    public MarkFeedbackReceivedReply
    setFailedFeedbackIds(Set<Long> value)
    {
        failedFeedbackIds = value;
        return this;
    }

    public MarkFeedbackReceivedReply
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
