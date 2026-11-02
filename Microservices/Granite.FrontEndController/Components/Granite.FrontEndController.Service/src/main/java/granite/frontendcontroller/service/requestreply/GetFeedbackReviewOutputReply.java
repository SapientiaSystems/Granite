//////////////////////////////////////////////////////////////////////////////
// GetFeedbackReviewOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetFeedbackReviewOutputReply
    extends GetOutputReply<FeedbackReviewOutput>
{
    public
    GetFeedbackReviewOutputReply()
    {
        super();
    }

    public
    GetFeedbackReviewOutputReply(GetFeedbackReviewOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setReplyId(UUID replyId)
    {
        return (GetFeedbackReviewOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetFeedbackReviewOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetFeedbackReviewOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setSuccess(boolean success)
    {
        return (GetFeedbackReviewOutputReply)super.setSuccess(success);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetFeedbackReviewOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetFeedbackReviewOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setException(ExceptionData exception)
    {
        return (GetFeedbackReviewOutputReply)super.setException(exception);
    }

    @Override
    public GetFeedbackReviewOutputReply
    setOutput(FeedbackReviewOutput output)
    {
        return (GetFeedbackReviewOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
