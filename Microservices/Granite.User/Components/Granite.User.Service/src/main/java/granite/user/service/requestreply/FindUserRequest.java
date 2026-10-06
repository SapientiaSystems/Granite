//////////////////////////////////////////////////////////////////////////////
// DestroyUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindUserRequest
    extends AbstractServiceRequest
{
    private Long   userId;

    public FindUserRequest()
    {
        super();
        userId = null;
    }

    @Override
    public FindUserRequest
    setRequestId(UUID requestId)
    {
        return (FindUserRequest)super.setRequestId(requestId);
    }

    @Override
    public FindUserRequest
    setTimestamp(Instant timestamp)
    {
        return (FindUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindUserRequest
    addHeader(String name,Object value)
    {
        return (FindUserRequest)super.addHeader(name,value);
    }

    @Override
    public FindUserRequest
    clearHeaders()
    {
        return (FindUserRequest)super.clearHeaders();
    }

    @Override
    public FindUserRequest
    addCookie(Cookie cookie)
    {
        return (FindUserRequest)super.addCookie(cookie);
    }

    @Override
    public FindUserRequest
    clearCookies()
    {
        return (FindUserRequest)super.clearCookies();
    }

    public FindUserRequest
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
