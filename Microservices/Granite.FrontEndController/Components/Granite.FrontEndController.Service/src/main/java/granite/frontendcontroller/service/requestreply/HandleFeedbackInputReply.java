//////////////////////////////////////////////////////////////////////////////
// HandleFeedbackInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleFeedbackInputReply
    extends HandleInputReply<FeedbackInput,FeedbackOutput>
{
    public
    HandleFeedbackInputReply()
    {
        super();
    }

    public
    HandleFeedbackInputReply(HandleFeedbackInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleFeedbackInputReply
    setReplyId(UUID replyId)
    {
        return (HandleFeedbackInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleFeedbackInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleFeedbackInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleFeedbackInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleFeedbackInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleFeedbackInputReply
    setSuccess(boolean success)
    {
        return (HandleFeedbackInputReply)super.setSuccess(success);
    }

    @Override
    public HandleFeedbackInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleFeedbackInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleFeedbackInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleFeedbackInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleFeedbackInputReply
    setException(ExceptionData exception)
    {
        return (HandleFeedbackInputReply)super.setException(exception);
    }

    @Override
    public HandleFeedbackInputReply
    setInput(FeedbackInput input)
    {
        return (HandleFeedbackInputReply)super.setInput(input);
    }

    @Override
    public HandleFeedbackInputReply
    setOutput(FeedbackOutput output)
    {
        return (HandleFeedbackInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
