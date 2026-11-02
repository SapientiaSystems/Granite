//////////////////////////////////////////////////////////////////////////////
// HandleSessionsInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleSessionsInputReply
    extends HandleInputReply<SessionsInput,SessionsOutput>
{
    public
    HandleSessionsInputReply()
    {
        super();
    }

    public
    HandleSessionsInputReply(HandleSessionsInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleSessionsInputReply
    setReplyId(UUID replyId)
    {
        return (HandleSessionsInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleSessionsInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleSessionsInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleSessionsInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleSessionsInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSessionsInputReply
    setSuccess(boolean success)
    {
        return (HandleSessionsInputReply)super.setSuccess(success);
    }

    @Override
    public HandleSessionsInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleSessionsInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleSessionsInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleSessionsInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleSessionsInputReply
    setException(ExceptionData exception)
    {
        return (HandleSessionsInputReply)super.setException(exception);
    }

    @Override
    public HandleSessionsInputReply
    setInput(SessionsInput input)
    {
        return (HandleSessionsInputReply)super.setInput(input);
    }

    @Override
    public HandleSessionsInputReply
    setOutput(SessionsOutput output)
    {
        return (HandleSessionsInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
