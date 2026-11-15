//////////////////////////////////////////////////////////////////////////////
// HandleOwnedClientSecretInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleOwnedClientSecretInputReply
    extends HandleInputReply<OwnedClientSecretInput,ClientsOutput>
{
    public
    HandleOwnedClientSecretInputReply()
    {
        super();
    }

    public
    HandleOwnedClientSecretInputReply(HandleOwnedClientSecretInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setReplyId(UUID replyId)
    {
        return (HandleOwnedClientSecretInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleOwnedClientSecretInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleOwnedClientSecretInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setSuccess(boolean success)
    {
        return (HandleOwnedClientSecretInputReply)super.setSuccess(success);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleOwnedClientSecretInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleOwnedClientSecretInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setException(ExceptionData exception)
    {
        return (HandleOwnedClientSecretInputReply)super.setException(exception);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setInput(OwnedClientSecretInput input)
    {
        return (HandleOwnedClientSecretInputReply)super.setInput(input);
    }

    @Override
    public HandleOwnedClientSecretInputReply
    setOutput(ClientsOutput output)
    {
        return (HandleOwnedClientSecretInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
