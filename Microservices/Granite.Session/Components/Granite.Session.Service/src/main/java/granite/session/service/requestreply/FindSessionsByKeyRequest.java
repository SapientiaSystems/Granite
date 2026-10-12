//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeyRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindSessionsByKeyRequest
    extends AbstractServiceRequest
{
    private SessionKey key;
    private boolean returnIfFound;

    public
    FindSessionsByKeyRequest()
    {
        super();
        key = null;
        returnIfFound = false;
    }

    @Override
    public FindSessionsByKeyRequest
    setRequestId(UUID requestId)
    {
        return (FindSessionsByKeyRequest)super.setRequestId(requestId);
    }

    @Override
    public FindSessionsByKeyRequest
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsByKeyRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsByKeyRequest
    addHeader(String name,Object value)
    {
        return (FindSessionsByKeyRequest)super.addHeader(name,value);
    }

    @Override
    public FindSessionsByKeyRequest
    clearHeaders()
    {
        return (FindSessionsByKeyRequest)super.clearHeaders();
    }

    @Override
    public FindSessionsByKeyRequest
    addCookie(Cookie cookie)
    {
        return (FindSessionsByKeyRequest)super.addCookie(cookie);
    }

    @Override
    public FindSessionsByKeyRequest
    clearCookies()
    {
        return (FindSessionsByKeyRequest)super.clearCookies();
    }

    public FindSessionsByKeyRequest
    setKey(SessionKey value)
    {
        key = value;
        return this;
    }

    public FindSessionsByKeyRequest
    setReturnIfFound(boolean value)
    {
        returnIfFound = value;
        return this;
    }

    public SessionKey
    getKey() { return key; }

    public boolean
    getReturnIfFound() { return returnIfFound; }
}

//////////////////////////////////////////////////////////////////////////////
