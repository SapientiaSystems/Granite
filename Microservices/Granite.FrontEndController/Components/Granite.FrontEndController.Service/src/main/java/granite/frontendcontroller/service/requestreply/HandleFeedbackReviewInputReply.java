//////////////////////////////////////////////////////////////////////////////
// HandleFeedbackReviewInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleFeedbackReviewInputReply
    extends HandleInputReply<FeedbackReviewInput,FeedbackReviewOutput>
{
    public
    HandleFeedbackReviewInputReply()
    {
        super();
    }

    public
    HandleFeedbackReviewInputReply(HandleFeedbackReviewInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setReplyId(UUID replyId)
    {
        return (HandleFeedbackReviewInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleFeedbackReviewInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleFeedbackReviewInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setSuccess(boolean success)
    {
        return (HandleFeedbackReviewInputReply)super.setSuccess(success);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleFeedbackReviewInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleFeedbackReviewInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setException(ExceptionData exception)
    {
        return (HandleFeedbackReviewInputReply)super.setException(exception);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setInput(FeedbackReviewInput input)
    {
        return (HandleFeedbackReviewInputReply)super.setInput(input);
    }

    @Override
    public HandleFeedbackReviewInputReply
    setOutput(FeedbackReviewOutput output)
    {
        return (HandleFeedbackReviewInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
