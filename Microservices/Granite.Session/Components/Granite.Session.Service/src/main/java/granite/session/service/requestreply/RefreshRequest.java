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
    private String refreshToken;

    public
    RefreshRequest()
    {
        super();
        refreshToken = null;
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
    setRefreshToken(String refreshToken)
    {
        this.refreshToken = refreshToken;
        return this;
    }

    public String
    getRefreshToken() { return refreshToken; }
}

//////////////////////////////////////////////////////////////////////////////
