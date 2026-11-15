//////////////////////////////////////////////////////////////////////////////
// FindClientsByIdsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindClientsByIdsRequest
    extends AbstractServiceRequest
{
    private Set<String> clientIds;

    public
    FindClientsByIdsRequest()
    {
        super();
        clientIds = new TreeSet<>();
    }

    @Override
    public FindClientsByIdsRequest
    setRequestId(UUID requestId)
    {
        return (FindClientsByIdsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindClientsByIdsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindClientsByIdsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindClientsByIdsRequest
    addHeader(String name,Object value)
    {
        return (FindClientsByIdsRequest)super.addHeader(name,value);
    }

    @Override
    public FindClientsByIdsRequest
    clearHeaders()
    {
        return (FindClientsByIdsRequest)super.clearHeaders();
    }

    @Override
    public FindClientsByIdsRequest
    addCookie(Cookie cookie)
    {
        return (FindClientsByIdsRequest)super.addCookie(cookie);
    }

    @Override
    public FindClientsByIdsRequest
    clearCookies()
    {
        return (FindClientsByIdsRequest)super.clearCookies();
    }

    public FindClientsByIdsRequest
    setClientIds(Set<String> value)
    {
        clientIds = value;
        return this;
    }

    public Set<String>
    getClientIds() { return clientIds; }
}

//////////////////////////////////////////////////////////////////////////////
