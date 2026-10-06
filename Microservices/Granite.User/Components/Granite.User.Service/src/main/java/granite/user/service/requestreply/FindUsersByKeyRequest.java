//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeyRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindUsersByKeyRequest
    extends AbstractServiceRequest
{
    private UserKey key;
    private boolean       returnIfFound;

    public
    FindUsersByKeyRequest()
    {
        super();
        key = null;
        returnIfFound = false;
    }

    @Override
    public FindUsersByKeyRequest
    setRequestId(UUID requestId)
    {
        return (FindUsersByKeyRequest)super.setRequestId(requestId);
    }

    @Override
    public FindUsersByKeyRequest
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByKeyRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByKeyRequest
    addHeader(String name,Object value)
    {
        return (FindUsersByKeyRequest)super.addHeader(name,value);
    }

    @Override
    public FindUsersByKeyRequest
    clearHeaders()
    {
        return (FindUsersByKeyRequest)super.clearHeaders();
    }

    @Override
    public FindUsersByKeyRequest
    addCookie(Cookie cookie)
    {
        return (FindUsersByKeyRequest)super.addCookie(cookie);
    }

    @Override
    public FindUsersByKeyRequest
    clearCookies()
    {
        return (FindUsersByKeyRequest)super.clearCookies();
    }

    public FindUsersByKeyRequest
    setKey(UserKey value)
    {
        key = value;
        return this;
    }

    public FindUsersByKeyRequest
    setReturnIfFound(boolean value)
    {
        returnIfFound = value;
        return this;
    }

    public UserKey
    getKey() { return key; }

    public boolean
    getReturnIfFound() { return returnIfFound; }
}

//////////////////////////////////////////////////////////////////////////////
