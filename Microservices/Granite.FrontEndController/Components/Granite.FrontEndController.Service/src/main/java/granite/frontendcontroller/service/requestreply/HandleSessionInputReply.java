//////////////////////////////////////////////////////////////////////////////
// HandleSessionInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleSessionInputReply
    extends HandleInputReply<SessionInput,SessionOutput>
{
    public
    HandleSessionInputReply()
    {
        super();
    }

    public
    HandleSessionInputReply(HandleSessionInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleSessionInputReply
    setReplyId(UUID replyId)
    {
        return (HandleSessionInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleSessionInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleSessionInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleSessionInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleSessionInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSessionInputReply
    setSuccess(boolean success)
    {
        return (HandleSessionInputReply)super.setSuccess(success);
    }

    @Override
    public HandleSessionInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleSessionInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleSessionInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleSessionInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleSessionInputReply
    setException(ExceptionData exception)
    {
        return (HandleSessionInputReply)super.setException(exception);
    }

    @Override
    public HandleSessionInputReply
    setInput(SessionInput input)
    {
        return (HandleSessionInputReply)super.setInput(input);
    }

    @Override
    public HandleSessionInputReply
    setOutput(SessionOutput output)
    {
        return (HandleSessionInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
