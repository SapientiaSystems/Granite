//////////////////////////////////////////////////////////////////////////////
// IssueDelegatedTokenRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class IssueDelegatedTokenRequest
    extends AbstractServiceRequest
{
    private String clientId;
    private String clientSecret;
    private Long   sessionId;
    private Long   userId;

    public
    IssueDelegatedTokenRequest()
    {
        super();
        clientId     = null;
        clientSecret = null;
        sessionId    = null;
        userId       = null;
    }

    @Override
    public IssueDelegatedTokenRequest
    setRequestId(UUID requestId)
    {
        return (IssueDelegatedTokenRequest)super.setRequestId(requestId);
    }

    @Override
    public IssueDelegatedTokenRequest
    setTimestamp(Instant timestamp)
    {
        return (IssueDelegatedTokenRequest)super.setTimestamp(timestamp);
    }

    @Override
    public IssueDelegatedTokenRequest
    addHeader(String name,Object value)
    {
        return (IssueDelegatedTokenRequest)super.addHeader(name,value);
    }

    @Override
    public IssueDelegatedTokenRequest
    clearHeaders()
    {
        return (IssueDelegatedTokenRequest)super.clearHeaders();
    }

    @Override
    public IssueDelegatedTokenRequest
    addCookie(Cookie cookie)
    {
        return (IssueDelegatedTokenRequest)super.addCookie(cookie);
    }

    @Override
    public IssueDelegatedTokenRequest
    clearCookies()
    {
        return (IssueDelegatedTokenRequest)super.clearCookies();
    }

    public IssueDelegatedTokenRequest
    setClientId(String clientId)
    {
        this.clientId = clientId;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public IssueDelegatedTokenRequest
    setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
        return this;
    }

    public String
    getClientSecret() { return clientSecret; }

    public IssueDelegatedTokenRequest
    setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }

    public IssueDelegatedTokenRequest
    setUserId(Long userId)
    {
        this.userId = userId;
        return this;
    }

    public Long
    getUserId() { return userId; }

    @Override
    public String
    toString()
    {
        return "IssueDelegatedTokenRequest{requestId=" + getRequestId() + ",clientId=" + clientId + ",sessionId=" + sessionId + ",userId=" + userId + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
