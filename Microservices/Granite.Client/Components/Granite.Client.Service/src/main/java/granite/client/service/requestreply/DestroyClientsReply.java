//////////////////////////////////////////////////////////////////////////////
// DestroyClientsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class DestroyClientsReply
    extends AbstractServiceReply
{
    private List<String> clientIds;

    public
    DestroyClientsReply()
    {
        super();
        clientIds = new ArrayList<>();
    }

    public
    DestroyClientsReply(DestroyClientsRequest originatingRequest)
    {
        super(originatingRequest);
        clientIds = new ArrayList<>();
    }

    @Override
    public DestroyClientsReply
    setReplyId(UUID replyId)
    {
        return (DestroyClientsReply)super.setReplyId(replyId);
    }

    @Override
    public DestroyClientsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (DestroyClientsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public DestroyClientsReply
    setTimestamp(Instant timestamp)
    {
        return (DestroyClientsReply)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyClientsReply
    setSuccess(boolean success)
    {
        return (DestroyClientsReply)super.setSuccess(success);
    }

    @Override
    public DestroyClientsReply
    setSuccessMessage(String successMessage)
    {
        return (DestroyClientsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public DestroyClientsReply
    setFailureMessage(String failureMessage)
    {
        return (DestroyClientsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public DestroyClientsReply
    setException(ExceptionData exception)
    {
        return (DestroyClientsReply)super.setException(exception);
    }

    public DestroyClientsReply
    setClientIds(List<String> value)
    {
        clientIds = value;
        return this;
    }

    public List<String>
    getClientIds() { return clientIds; }
}

//////////////////////////////////////////////////////////////////////////////
