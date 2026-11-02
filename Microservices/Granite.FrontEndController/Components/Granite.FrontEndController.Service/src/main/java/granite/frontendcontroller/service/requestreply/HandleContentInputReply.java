//////////////////////////////////////////////////////////////////////////////
// HandleContentInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleContentInputReply
    extends HandleInputReply<ContentInput,ContentOutput>
{
    public
    HandleContentInputReply()
    {
        super();
    }

    public
    HandleContentInputReply(HandleContentInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleContentInputReply
    setReplyId(UUID replyId)
    {
        return (HandleContentInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleContentInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleContentInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleContentInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleContentInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleContentInputReply
    setSuccess(boolean success)
    {
        return (HandleContentInputReply)super.setSuccess(success);
    }

    @Override
    public HandleContentInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleContentInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleContentInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleContentInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleContentInputReply
    setException(ExceptionData exception)
    {
        return (HandleContentInputReply)super.setException(exception);
    }

    @Override
    public HandleContentInputReply
    setInput(ContentInput input)
    {
        return (HandleContentInputReply)super.setInput(input);
    }

    @Override
    public HandleContentInputReply
    setOutput(ContentOutput output)
    {
        return (HandleContentInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
