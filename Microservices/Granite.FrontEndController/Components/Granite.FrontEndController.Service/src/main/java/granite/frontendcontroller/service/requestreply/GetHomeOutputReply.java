//////////////////////////////////////////////////////////////////////////////
// GetHomeOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetHomeOutputReply
    extends GetOutputReply<HomeOutput>
{
    public
    GetHomeOutputReply()
    {
        super();
    }

    public
    GetHomeOutputReply(GetHomeOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetHomeOutputReply
    setReplyId(UUID replyId)
    {
        return (GetHomeOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetHomeOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetHomeOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetHomeOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetHomeOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetHomeOutputReply
    setSuccess(boolean success)
    {
        return (GetHomeOutputReply)super.setSuccess(success);
    }

    @Override
    public GetHomeOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetHomeOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetHomeOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetHomeOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetHomeOutputReply
    setException(ExceptionData exception)
    {
        return (GetHomeOutputReply)super.setException(exception);
    }

    @Override
    public GetHomeOutputReply
    setOutput(HomeOutput output)
    {
        return (GetHomeOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
