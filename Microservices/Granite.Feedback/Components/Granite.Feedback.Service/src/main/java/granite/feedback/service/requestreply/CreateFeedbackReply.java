//////////////////////////////////////////////////////////////////////////////
// CreateFeedbackReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class CreateFeedbackReply
    extends AbstractServiceReply
{
    private FeedbackData createdFeedback;

    public
    CreateFeedbackReply()
    {
        super();
        createdFeedback = null;
    }

    public
    CreateFeedbackReply(CreateFeedbackRequest originatingRequest)
    {
        super(originatingRequest);
        createdFeedback = null;
    }

    @Override
    public CreateFeedbackReply
    setReplyId(UUID replyId)
    {
        return (CreateFeedbackReply)super.setReplyId(replyId);
    }

    @Override
    public CreateFeedbackReply
    setOriginatingRequestId(UUID requestId)
    {
        return (CreateFeedbackReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public CreateFeedbackReply
    setTimestamp(Instant timestamp)
    {
        return (CreateFeedbackReply)super.setTimestamp(timestamp);
    }

    @Override
    public CreateFeedbackReply
    setSuccess(boolean success)
    {
        return (CreateFeedbackReply)super.setSuccess(success);
    }

    @Override
    public CreateFeedbackReply
    setSuccessMessage(String successMessage)
    {
        return (CreateFeedbackReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public CreateFeedbackReply
    setFailureMessage(String failureMessage)
    {
        return (CreateFeedbackReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public CreateFeedbackReply
    setException(ExceptionData exception)
    {
        return (CreateFeedbackReply)super.setException(exception);
    }

    public CreateFeedbackReply
    setCreatedFeedback(FeedbackData value)
    {
        createdFeedback = value;
        return this;
    }

    public FeedbackData
    getCreatedFeedback() { return createdFeedback; }
}

//////////////////////////////////////////////////////////////////////////////
