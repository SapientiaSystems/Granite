//////////////////////////////////////////////////////////////////////////////
// CreateClientRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class CreateClientRequest
    extends AbstractServiceRequest
{
    private CreateClientData client;

    public
    CreateClientRequest()
    {
        super();
        client = null;
    }

    @Override
    public CreateClientRequest
    setRequestId(UUID requestId)
    {
        return (CreateClientRequest)super.setRequestId(requestId);
    }

    @Override
    public CreateClientRequest
    setTimestamp(Instant timestamp)
    {
        return (CreateClientRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CreateClientRequest
    addHeader(String name,Object value)
    {
        return (CreateClientRequest)super.addHeader(name,value);
    }

    @Override
    public CreateClientRequest
    clearHeaders()
    {
        return (CreateClientRequest)super.clearHeaders();
    }

    @Override
    public CreateClientRequest
    addCookie(Cookie cookie)
    {
        return (CreateClientRequest)super.addCookie(cookie);
    }

    @Override
    public CreateClientRequest
    clearCookies()
    {
        return (CreateClientRequest)super.clearCookies();
    }

    public CreateClientRequest
    setClient(CreateClientData value)
    {
        client = value;
        return this;
    }

    public CreateClientData
    getClient() { return client; }
}

//////////////////////////////////////////////////////////////////////////////
