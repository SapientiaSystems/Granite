//////////////////////////////////////////////////////////////////////////////
// GetOwnedClientsOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetOwnedClientsOutputReply
    extends GetOutputReply<ClientsOutput>
{
    public
    GetOwnedClientsOutputReply()
    {
        super();
    }

    public
    GetOwnedClientsOutputReply(GetOwnedClientsOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetOwnedClientsOutputReply
    setReplyId(UUID replyId)
    {
        return (GetOwnedClientsOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetOwnedClientsOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetOwnedClientsOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetOwnedClientsOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetOwnedClientsOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetOwnedClientsOutputReply
    setSuccess(boolean success)
    {
        return (GetOwnedClientsOutputReply)super.setSuccess(success);
    }

    @Override
    public GetOwnedClientsOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetOwnedClientsOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetOwnedClientsOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetOwnedClientsOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetOwnedClientsOutputReply
    setException(ExceptionData exception)
    {
        return (GetOwnedClientsOutputReply)super.setException(exception);
    }

    @Override
    public GetOwnedClientsOutputReply
    setOutput(ClientsOutput output)
    {
        return (GetOwnedClientsOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
