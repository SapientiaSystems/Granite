//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeySetRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindSessionsByKeySetRequest
    extends AbstractServiceRequest
{
    private SessionKeySet keySet;

    public
    FindSessionsByKeySetRequest()
    {
        super();
        keySet = null;
    }

    @Override
    public FindSessionsByKeySetRequest
    setRequestId(UUID requestId)
    {
        return (FindSessionsByKeySetRequest)super.setRequestId(requestId);
    }

    @Override
    public FindSessionsByKeySetRequest
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsByKeySetRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsByKeySetRequest
    addHeader(String name,Object value)
    {
        return (FindSessionsByKeySetRequest)super.addHeader(name,value);
    }

    @Override
    public FindSessionsByKeySetRequest
    clearHeaders()
    {
        return (FindSessionsByKeySetRequest)super.clearHeaders();
    }

    @Override
    public FindSessionsByKeySetRequest
    addCookie(Cookie cookie)
    {
        return (FindSessionsByKeySetRequest)super.addCookie(cookie);
    }

    @Override
    public FindSessionsByKeySetRequest
    clearCookies()
    {
        return (FindSessionsByKeySetRequest)super.clearCookies();
    }

    public FindSessionsByKeySetRequest
    setKeySet(SessionKeySet value)
    {
        keySet = value;
        return this;
    }

    public SessionKeySet
    getKeySet() { return keySet; }
}

//////////////////////////////////////////////////////////////////////////////
