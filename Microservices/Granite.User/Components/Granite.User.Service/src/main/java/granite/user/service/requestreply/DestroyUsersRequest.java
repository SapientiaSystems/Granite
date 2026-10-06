//////////////////////////////////////////////////////////////////////////////
// DestroyUsersRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public
class DestroyUsersRequest
    extends AbstractServiceRequest
{
    private Set<Long> userIds;

    public
    DestroyUsersRequest()
    {
        super();
        userIds = null;
    }

    @Override
    public DestroyUsersRequest
    setRequestId(UUID requestId)
    {
        return (DestroyUsersRequest)super.setRequestId(requestId);
    }

    @Override
    public DestroyUsersRequest
    setTimestamp(Instant timestamp)
    {
        return (DestroyUsersRequest)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyUsersRequest
    addHeader(String name,Object value)
    {
        return (DestroyUsersRequest)super.addHeader(name,value);
    }

    @Override
    public DestroyUsersRequest
    clearHeaders()
    {
        return (DestroyUsersRequest)super.clearHeaders();
    }

    @Override
    public DestroyUsersRequest
    addCookie(Cookie cookie)
    {
        return (DestroyUsersRequest)super.addCookie(cookie);
    }

    @Override
    public DestroyUsersRequest
    clearCookies()
    {
        return (DestroyUsersRequest)super.clearCookies();
    }

    public DestroyUsersRequest
    setUserIds(Set<Long> ids)
    {
        userIds = ids;
        return this;
    }

    public Set<Long>
    getUserIds() { return userIds; }
}

//////////////////////////////////////////////////////////////////////////////

