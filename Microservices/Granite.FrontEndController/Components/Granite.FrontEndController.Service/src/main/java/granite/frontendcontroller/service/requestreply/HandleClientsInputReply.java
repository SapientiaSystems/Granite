//////////////////////////////////////////////////////////////////////////////
// HandleClientsInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleClientsInputReply
    extends HandleInputReply<ClientsInput,ClientsOutput>
{
    public
    HandleClientsInputReply()
    {
        super();
    }

    public
    HandleClientsInputReply(HandleClientsInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleClientsInputReply
    setReplyId(UUID replyId)
    {
        return (HandleClientsInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleClientsInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleClientsInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleClientsInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleClientsInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleClientsInputReply
    setSuccess(boolean success)
    {
        return (HandleClientsInputReply)super.setSuccess(success);
    }

    @Override
    public HandleClientsInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleClientsInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleClientsInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleClientsInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleClientsInputReply
    setException(ExceptionData exception)
    {
        return (HandleClientsInputReply)super.setException(exception);
    }

    @Override
    public HandleClientsInputReply
    setInput(ClientsInput input)
    {
        return (HandleClientsInputReply)super.setInput(input);
    }

    @Override
    public HandleClientsInputReply
    setOutput(ClientsOutput output)
    {
        return (HandleClientsInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
