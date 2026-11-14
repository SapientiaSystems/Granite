//////////////////////////////////////////////////////////////////////////////
// FindClientsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindClientsReply
    extends AbstractServiceReply
{
    private List<ClientData> clients;

    public
    FindClientsReply()
    {
        super();
        clients = new ArrayList<>();
    }

    public
    FindClientsReply(FindAllClientsRequest originatingRequest)
    {
        super(originatingRequest);
        clients = new ArrayList<>();
    }

    @Override
    public FindClientsReply
    setReplyId(UUID replyId)
    {
        return (FindClientsReply)super.setReplyId(replyId);
    }

    @Override
    public FindClientsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindClientsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindClientsReply
    setTimestamp(Instant timestamp)
    {
        return (FindClientsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindClientsReply
    setSuccess(boolean success)
    {
        return (FindClientsReply)super.setSuccess(success);
    }

    @Override
    public FindClientsReply
    setSuccessMessage(String successMessage)
    {
        return (FindClientsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindClientsReply
    setFailureMessage(String failureMessage)
    {
        return (FindClientsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindClientsReply
    setException(ExceptionData exception)
    {
        return (FindClientsReply)super.setException(exception);
    }

    public FindClientsReply
    setClients(List<ClientData> value)
    {
        clients = value;
        return this;
    }

    public List<ClientData>
    getClients() { return clients; }
}

//////////////////////////////////////////////////////////////////////////////
