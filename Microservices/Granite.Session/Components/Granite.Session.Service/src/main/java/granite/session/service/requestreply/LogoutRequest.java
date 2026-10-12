//////////////////////////////////////////////////////////////////////////////
// LogoutRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class LogoutRequest
    extends AbstractServiceRequest
{
    private Long sessionId;

    public
    LogoutRequest()
    {
        super();
        sessionId = null;
    }

    @Override
    public LogoutRequest
    setRequestId(UUID requestId)
    {
        return (LogoutRequest)super.setRequestId(requestId);
    }

    @Override
    public LogoutRequest
    setTimestamp(Instant timestamp)
    {
        return (LogoutRequest)super.setTimestamp(timestamp);
    }

    @Override
    public LogoutRequest
    addHeader(String name,Object value)
    {
        return (LogoutRequest)super.addHeader(name,value);
    }

    @Override
    public LogoutRequest
    clearHeaders()
    {
        return (LogoutRequest)super.clearHeaders();
    }

    @Override
    public LogoutRequest
    addCookie(Cookie cookie)
    {
        return (LogoutRequest)super.addCookie(cookie);
    }

    @Override
    public LogoutRequest
    clearCookies()
    {
        return (LogoutRequest)super.clearCookies();
    }

    public LogoutRequest
    setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }
}

//////////////////////////////////////////////////////////////////////////////
