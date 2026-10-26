//////////////////////////////////////////////////////////////////////////////
// GetSessionOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetSessionOutputReply
    extends GetOutputReply<SessionOutput>
{
    public
    GetSessionOutputReply()
    {
        super();
    }

    public
    GetSessionOutputReply(GetSessionOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetSessionOutputReply
    setReplyId(UUID replyId)
    {
        return (GetSessionOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetSessionOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetSessionOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetSessionOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetSessionOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetSessionOutputReply
    setSuccess(boolean success)
    {
        return (GetSessionOutputReply)super.setSuccess(success);
    }

    @Override
    public GetSessionOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetSessionOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetSessionOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetSessionOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetSessionOutputReply
    setException(ExceptionData exception)
    {
        return (GetSessionOutputReply)super.setException(exception);
    }

    @Override
    public GetSessionOutputReply
    setOutput(SessionOutput output)
    {
        return (GetSessionOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
