//////////////////////////////////////////////////////////////////////////////
// LogoutAllForUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class LogoutAllForUserRequest
    extends AbstractServiceRequest
{
    private Long userId;

    public
    LogoutAllForUserRequest()
    {
        super();
        userId = null;
    }

    @Override
    public LogoutAllForUserRequest
    setRequestId(UUID requestId)
    {
        return (LogoutAllForUserRequest)super.setRequestId(requestId);
    }

    @Override
    public LogoutAllForUserRequest
    setTimestamp(Instant timestamp)
    {
        return (LogoutAllForUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public LogoutAllForUserRequest
    addHeader(String name,Object value)
    {
        return (LogoutAllForUserRequest)super.addHeader(name,value);
    }

    @Override
    public LogoutAllForUserRequest
    clearHeaders()
    {
        return (LogoutAllForUserRequest)super.clearHeaders();
    }

    @Override
    public LogoutAllForUserRequest
    addCookie(Cookie cookie)
    {
        return (LogoutAllForUserRequest)super.addCookie(cookie);
    }

    @Override
    public LogoutAllForUserRequest
    clearCookies()
    {
        return (LogoutAllForUserRequest)super.clearCookies();
    }

    public LogoutAllForUserRequest
    setUserId(Long userId)
    {
        this.userId = userId;
        return this;
    }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
