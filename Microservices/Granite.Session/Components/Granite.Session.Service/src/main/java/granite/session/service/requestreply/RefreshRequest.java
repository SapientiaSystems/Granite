//////////////////////////////////////////////////////////////////////////////
// RefreshRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class RefreshRequest
    extends AbstractServiceRequest
{
    private Long sessionId;

    public
    RefreshRequest()
    {
        super();
        sessionId = null;
    }

    @Override
    public RefreshRequest
    setRequestId(UUID requestId)
    {
        return (RefreshRequest)super.setRequestId(requestId);
    }

    @Override
    public RefreshRequest
    setTimestamp(Instant timestamp)
    {
        return (RefreshRequest)super.setTimestamp(timestamp);
    }

    @Override
    public RefreshRequest
    addHeader(String name,Object value)
    {
        return (RefreshRequest)super.addHeader(name,value);
    }

    @Override
    public RefreshRequest
    clearHeaders()
    {
        return (RefreshRequest)super.clearHeaders();
    }

    @Override
    public RefreshRequest
    addCookie(Cookie cookie)
    {
        return (RefreshRequest)super.addCookie(cookie);
    }

    @Override
    public RefreshRequest
    clearCookies()
    {
        return (RefreshRequest)super.clearCookies();
    }

    public RefreshRequest
    setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }
}

//////////////////////////////////////////////////////////////////////////////
