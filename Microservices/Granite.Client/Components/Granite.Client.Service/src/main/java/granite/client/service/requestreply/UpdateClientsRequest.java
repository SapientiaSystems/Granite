//////////////////////////////////////////////////////////////////////////////
// UpdateClientsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class UpdateClientsRequest
    extends AbstractServiceRequest
{
    private List<UpdateClientData> clients;

    public
    UpdateClientsRequest()
    {
        super();
        clients = new ArrayList<>();
    }

    @Override
    public UpdateClientsRequest
    setRequestId(UUID requestId)
    {
        return (UpdateClientsRequest)super.setRequestId(requestId);
    }

    @Override
    public UpdateClientsRequest
    setTimestamp(Instant timestamp)
    {
        return (UpdateClientsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateClientsRequest
    addHeader(String name,Object value)
    {
        return (UpdateClientsRequest)super.addHeader(name,value);
    }

    @Override
    public UpdateClientsRequest
    clearHeaders()
    {
        return (UpdateClientsRequest)super.clearHeaders();
    }

    @Override
    public UpdateClientsRequest
    addCookie(Cookie cookie)
    {
        return (UpdateClientsRequest)super.addCookie(cookie);
    }

    @Override
    public UpdateClientsRequest
    clearCookies()
    {
        return (UpdateClientsRequest)super.clearCookies();
    }

    public UpdateClientsRequest
    setClients(List<UpdateClientData> value)
    {
        clients = value;
        return this;
    }

    public List<UpdateClientData>
    getClients() { return clients; }
}

//////////////////////////////////////////////////////////////////////////////
