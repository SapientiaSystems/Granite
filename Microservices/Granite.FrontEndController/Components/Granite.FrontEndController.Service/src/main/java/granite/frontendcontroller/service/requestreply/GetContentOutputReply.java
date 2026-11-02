//////////////////////////////////////////////////////////////////////////////
// GetContentOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetContentOutputReply
    extends GetOutputReply<ContentOutput>
{
    public
    GetContentOutputReply()
    {
        super();
    }

    public
    GetContentOutputReply(GetContentOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetContentOutputReply
    setReplyId(UUID replyId)
    {
        return (GetContentOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetContentOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetContentOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetContentOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetContentOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetContentOutputReply
    setSuccess(boolean success)
    {
        return (GetContentOutputReply)super.setSuccess(success);
    }

    @Override
    public GetContentOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetContentOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetContentOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetContentOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetContentOutputReply
    setException(ExceptionData exception)
    {
        return (GetContentOutputReply)super.setException(exception);
    }

    @Override
    public GetContentOutputReply
    setOutput(ContentOutput output)
    {
        return (GetContentOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
