//////////////////////////////////////////////////////////////////////////////
// DestroyClientsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class DestroyClientsRequest
    extends AbstractServiceRequest
{
    private List<String> clientIds;

    public
    DestroyClientsRequest()
    {
        super();
        clientIds = new ArrayList<>();
    }

    @Override
    public DestroyClientsRequest
    setRequestId(UUID requestId)
    {
        return (DestroyClientsRequest)super.setRequestId(requestId);
    }

    @Override
    public DestroyClientsRequest
    setTimestamp(Instant timestamp)
    {
        return (DestroyClientsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyClientsRequest
    addHeader(String name,Object value)
    {
        return (DestroyClientsRequest)super.addHeader(name,value);
    }

    @Override
    public DestroyClientsRequest
    clearHeaders()
    {
        return (DestroyClientsRequest)super.clearHeaders();
    }

    @Override
    public DestroyClientsRequest
    addCookie(Cookie cookie)
    {
        return (DestroyClientsRequest)super.addCookie(cookie);
    }

    @Override
    public DestroyClientsRequest
    clearCookies()
    {
        return (DestroyClientsRequest)super.clearCookies();
    }

    public DestroyClientsRequest
    setClientIds(List<String> value)
    {
        clientIds = value;
        return this;
    }

    public List<String>
    getClientIds() { return clientIds; }
}

//////////////////////////////////////////////////////////////////////////////
