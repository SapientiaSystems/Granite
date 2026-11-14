//////////////////////////////////////////////////////////////////////////////
// FindAllClientsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindAllClientsRequest
    extends AbstractServiceRequest
{
    public
    FindAllClientsRequest()
    {
        super();
    }

    @Override
    public FindAllClientsRequest
    setRequestId(UUID requestId)
    {
        return (FindAllClientsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAllClientsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAllClientsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllClientsRequest
    addHeader(String name,Object value)
    {
        return (FindAllClientsRequest)super.addHeader(name,value);
    }

    @Override
    public FindAllClientsRequest
    clearHeaders()
    {
        return (FindAllClientsRequest)super.clearHeaders();
    }

    @Override
    public FindAllClientsRequest
    addCookie(Cookie cookie)
    {
        return (FindAllClientsRequest)super.addCookie(cookie);
    }

    @Override
    public FindAllClientsRequest
    clearCookies()
    {
        return (FindAllClientsRequest)super.clearCookies();
    }
}

//////////////////////////////////////////////////////////////////////////////
