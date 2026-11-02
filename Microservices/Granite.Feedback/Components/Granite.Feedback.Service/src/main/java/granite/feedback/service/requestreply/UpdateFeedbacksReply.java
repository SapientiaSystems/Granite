//////////////////////////////////////////////////////////////////////////////
// UpdateFeedbacksReply.java
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
class UpdateFeedbacksReply
    extends AbstractServiceReply
{
    private Set<Long>          requestedFeedbackIds;
    private Set<Long>          failedFeedbackIds;
    private List<FeedbackData> updatedFeedback;

    public
    UpdateFeedbacksReply()
    {
        super();
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    public
    UpdateFeedbacksReply(UpdateFeedbacksRequest originatingRequest)
    {
        super(originatingRequest);
        requestedFeedbackIds = new TreeSet<>();
        failedFeedbackIds    = new TreeSet<>();
        updatedFeedback      = new ArrayList<>();
    }

    @Override
    public UpdateFeedbacksReply
    setReplyId(UUID replyId)
    {
        return (UpdateFeedbacksReply)super.setReplyId(replyId);
    }

    @Override
    public UpdateFeedbacksReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpdateFeedbacksReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpdateFeedbacksReply
    setTimestamp(Instant timestamp)
    {
        return (UpdateFeedbacksReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateFeedbacksReply
    setSuccess(boolean success)
    {
        return (UpdateFeedbacksReply)super.setSuccess(success);
    }

    @Override
    public UpdateFeedbacksReply
    setSuccessMessage(String successMessage)
    {
        return (UpdateFeedbacksReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpdateFeedbacksReply
    setFailureMessage(String failureMessage)
    {
        return (UpdateFeedbacksReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpdateFeedbacksReply
    setException(ExceptionData exception)
    {
        return (UpdateFeedbacksReply)super.setException(exception);
    }

    public UpdateFeedbacksReply
    setRequestedFeedbackIds(Set<Long> value)
    {
        requestedFeedbackIds = value;
        return this;
    }

    public UpdateFeedbacksReply
    setFailedFeedbackIds(Set<Long> value)
    {
        failedFeedbackIds = value;
        return this;
    }

    public UpdateFeedbacksReply
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
