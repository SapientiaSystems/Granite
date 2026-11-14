//////////////////////////////////////////////////////////////////////////////
// GetClientsOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetClientsOutputReply
    extends GetOutputReply<ClientsOutput>
{
    public
    GetClientsOutputReply()
    {
        super();
    }

    public
    GetClientsOutputReply(GetClientsOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetClientsOutputReply
    setReplyId(UUID replyId)
    {
        return (GetClientsOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetClientsOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetClientsOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetClientsOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetClientsOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetClientsOutputReply
    setSuccess(boolean success)
    {
        return (GetClientsOutputReply)super.setSuccess(success);
    }

    @Override
    public GetClientsOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetClientsOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetClientsOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetClientsOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetClientsOutputReply
    setException(ExceptionData exception)
    {
        return (GetClientsOutputReply)super.setException(exception);
    }

    @Override
    public GetClientsOutputReply
    setOutput(ClientsOutput output)
    {
        return (GetClientsOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
