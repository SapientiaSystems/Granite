//////////////////////////////////////////////////////////////////////////////
// GetUsersOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetUsersOutputReply
    extends GetOutputReply<UsersOutput>
{
    public
    GetUsersOutputReply()
    {
        super();
    }

    public
    GetUsersOutputReply(GetUsersOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetUsersOutputReply
    setReplyId(UUID replyId)
    {
        return (GetUsersOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetUsersOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetUsersOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetUsersOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetUsersOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetUsersOutputReply
    setSuccess(boolean success)
    {
        return (GetUsersOutputReply)super.setSuccess(success);
    }

    @Override
    public GetUsersOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetUsersOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetUsersOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetUsersOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetUsersOutputReply
    setException(ExceptionData exception)
    {
        return (GetUsersOutputReply)super.setException(exception);
    }

    @Override
    public GetUsersOutputReply
    setOutput(UsersOutput output)
    {
        return (GetUsersOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
