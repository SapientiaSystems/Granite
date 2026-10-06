//////////////////////////////////////////////////////////////////////////////
// DestroyUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class DestroyUserRequest
    extends AbstractServiceRequest
{
    private Long   userId;

    public
    DestroyUserRequest()
    {
        super();
        userId = null;
    }

    @Override
    public DestroyUserRequest
    setRequestId(UUID requestId)
    {
        return (DestroyUserRequest)super.setRequestId(requestId);
    }

    @Override
    public DestroyUserRequest
    setTimestamp(Instant timestamp)
    {
        return (DestroyUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public DestroyUserRequest
    addHeader(String name,Object value)
    {
        return (DestroyUserRequest)super.addHeader(name,value);
    }

    @Override
    public DestroyUserRequest
    clearHeaders()
    {
        return (DestroyUserRequest)super.clearHeaders();
    }

    @Override
    public DestroyUserRequest
    addCookie(Cookie cookie)
    {
        return (DestroyUserRequest)super.addCookie(cookie);
    }

    @Override
    public DestroyUserRequest
    clearCookies()
    {
        return (DestroyUserRequest)super.clearCookies();
    }

    public DestroyUserRequest
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
