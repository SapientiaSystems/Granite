//////////////////////////////////////////////////////////////////////////////
// FindAllUsersRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindAllUsersRequest
    extends AbstractServiceRequest
{
    public
    FindAllUsersRequest()
    {
        super();
    }

    @Override
    public FindAllUsersRequest
    setRequestId(UUID requestId)
    {
        return (FindAllUsersRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAllUsersRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAllUsersRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllUsersRequest
    addHeader(String name,Object value)
    {
        return (FindAllUsersRequest)super.addHeader(name,value);
    }

    @Override
    public FindAllUsersRequest
    clearHeaders()
    {
        return (FindAllUsersRequest)super.clearHeaders();
    }

    @Override
    public FindAllUsersRequest
    addCookie(Cookie cookie)
    {
        return (FindAllUsersRequest)super.addCookie(cookie);
    }

    @Override
    public FindAllUsersRequest
    clearCookies()
    {
        return (FindAllUsersRequest)super.clearCookies();
    }
}

//////////////////////////////////////////////////////////////////////////////
