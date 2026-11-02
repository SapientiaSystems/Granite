//////////////////////////////////////////////////////////////////////////////
// GetSessionsOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetSessionsOutputReply
    extends GetOutputReply<SessionsOutput>
{
    public
    GetSessionsOutputReply()
    {
        super();
    }

    public
    GetSessionsOutputReply(GetSessionsOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetSessionsOutputReply
    setReplyId(UUID replyId)
    {
        return (GetSessionsOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetSessionsOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetSessionsOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetSessionsOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetSessionsOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetSessionsOutputReply
    setSuccess(boolean success)
    {
        return (GetSessionsOutputReply)super.setSuccess(success);
    }

    @Override
    public GetSessionsOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetSessionsOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetSessionsOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetSessionsOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetSessionsOutputReply
    setException(ExceptionData exception)
    {
        return (GetSessionsOutputReply)super.setException(exception);
    }

    @Override
    public GetSessionsOutputReply
    setOutput(SessionsOutput output)
    {
        return (GetSessionsOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
