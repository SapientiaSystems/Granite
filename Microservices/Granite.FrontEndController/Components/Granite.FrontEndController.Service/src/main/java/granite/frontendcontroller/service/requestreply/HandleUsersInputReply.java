//////////////////////////////////////////////////////////////////////////////
// HandleUsersInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleUsersInputReply
    extends HandleInputReply<UsersInput,UsersOutput>
{
    public
    HandleUsersInputReply()
    {
        super();
    }

    public
    HandleUsersInputReply(HandleUsersInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleUsersInputReply
    setReplyId(UUID replyId)
    {
        return (HandleUsersInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleUsersInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleUsersInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleUsersInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleUsersInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleUsersInputReply
    setSuccess(boolean success)
    {
        return (HandleUsersInputReply)super.setSuccess(success);
    }

    @Override
    public HandleUsersInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleUsersInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleUsersInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleUsersInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleUsersInputReply
    setException(ExceptionData exception)
    {
        return (HandleUsersInputReply)super.setException(exception);
    }

    @Override
    public HandleUsersInputReply
    setInput(UsersInput input)
    {
        return (HandleUsersInputReply)super.setInput(input);
    }

    @Override
    public HandleUsersInputReply
    setOutput(UsersOutput output)
    {
        return (HandleUsersInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
