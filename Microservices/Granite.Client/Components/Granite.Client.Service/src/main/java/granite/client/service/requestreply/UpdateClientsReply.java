//////////////////////////////////////////////////////////////////////////////
// UpdateClientsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class UpdateClientsReply
    extends AbstractServiceReply
{
    private List<ClientData> clients;

    public
    UpdateClientsReply()
    {
        super();
        clients = new ArrayList<>();
    }

    public
    UpdateClientsReply(UpdateClientsRequest originatingRequest)
    {
        super(originatingRequest);
        clients = new ArrayList<>();
    }

    @Override
    public UpdateClientsReply
    setReplyId(UUID replyId)
    {
        return (UpdateClientsReply)super.setReplyId(replyId);
    }

    @Override
    public UpdateClientsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpdateClientsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpdateClientsReply
    setTimestamp(Instant timestamp)
    {
        return (UpdateClientsReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateClientsReply
    setSuccess(boolean success)
    {
        return (UpdateClientsReply)super.setSuccess(success);
    }

    @Override
    public UpdateClientsReply
    setSuccessMessage(String successMessage)
    {
        return (UpdateClientsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpdateClientsReply
    setFailureMessage(String failureMessage)
    {
        return (UpdateClientsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpdateClientsReply
    setException(ExceptionData exception)
    {
        return (UpdateClientsReply)super.setException(exception);
    }

    public UpdateClientsReply
    setClients(List<ClientData> value)
    {
        clients = value;
        return this;
    }

    public List<ClientData>
    getClients() { return clients; }
}

//////////////////////////////////////////////////////////////////////////////
