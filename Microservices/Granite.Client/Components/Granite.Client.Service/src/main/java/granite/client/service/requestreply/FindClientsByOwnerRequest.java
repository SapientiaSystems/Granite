//////////////////////////////////////////////////////////////////////////////
// FindClientsByOwnerRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindClientsByOwnerRequest
    extends AbstractServiceRequest
{
    private Long userId;

    public
    FindClientsByOwnerRequest()
    {
        super();
        userId = null;
    }

    @Override
    public FindClientsByOwnerRequest
    setRequestId(UUID requestId)
    {
        return (FindClientsByOwnerRequest)super.setRequestId(requestId);
    }

    @Override
    public FindClientsByOwnerRequest
    setTimestamp(Instant timestamp)
    {
        return (FindClientsByOwnerRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindClientsByOwnerRequest
    addHeader(String name,Object value)
    {
        return (FindClientsByOwnerRequest)super.addHeader(name,value);
    }

    @Override
    public FindClientsByOwnerRequest
    clearHeaders()
    {
        return (FindClientsByOwnerRequest)super.clearHeaders();
    }

    @Override
    public FindClientsByOwnerRequest
    addCookie(Cookie cookie)
    {
        return (FindClientsByOwnerRequest)super.addCookie(cookie);
    }

    @Override
    public FindClientsByOwnerRequest
    clearCookies()
    {
        return (FindClientsByOwnerRequest)super.clearCookies();
    }

    public FindClientsByOwnerRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
