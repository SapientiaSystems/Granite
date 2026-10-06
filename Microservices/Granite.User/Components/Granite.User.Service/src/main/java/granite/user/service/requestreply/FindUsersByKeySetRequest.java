//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeySetRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindUsersByKeySetRequest
    extends AbstractServiceRequest
{
    private UserKeySet keys;
    private boolean          returnIfFound;

    public
    FindUsersByKeySetRequest()
    {
        super();
        keys = null;
        returnIfFound = false;
    }

    @Override
    public FindUsersByKeySetRequest
    setRequestId(UUID requestId)
    {
        return (FindUsersByKeySetRequest)super.setRequestId(requestId);
    }

    @Override
    public FindUsersByKeySetRequest
    setTimestamp(Instant timestamp)
    {
        return (FindUsersByKeySetRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindUsersByKeySetRequest
    addHeader(String name,Object value)
    {
        return (FindUsersByKeySetRequest)super.addHeader(name,value);
    }

    @Override
    public FindUsersByKeySetRequest
    clearHeaders()
    {
        return (FindUsersByKeySetRequest)super.clearHeaders();
    }

    @Override
    public FindUsersByKeySetRequest
    addCookie(Cookie cookie)
    {
        return (FindUsersByKeySetRequest)super.addCookie(cookie);
    }

    @Override
    public FindUsersByKeySetRequest
    clearCookies()
    {
        return (FindUsersByKeySetRequest)super.clearCookies();
    }

    public FindUsersByKeySetRequest
    setKeys(UserKeySet value)
    {
        keys = value;
        return this;
    }

    public FindUsersByKeySetRequest
    setReturnIfFound(boolean value)
    {
        returnIfFound = value;
        return this;
    }

    public UserKeySet
    getKeys() { return keys; }

    public boolean
    getReturnIfFound() { return returnIfFound; }
}

//////////////////////////////////////////////////////////////////////////////
