//////////////////////////////////////////////////////////////////////////////
// IssueServiceTokenRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class IssueServiceTokenRequest
    extends AbstractServiceRequest
{
    private String clientId;
    private String clientSecret;

    public
    IssueServiceTokenRequest()
    {
        super();
        clientId     = null;
        clientSecret = null;
    }

    @Override
    public IssueServiceTokenRequest
    setRequestId(UUID requestId)
    {
        return (IssueServiceTokenRequest)super.setRequestId(requestId);
    }

    @Override
    public IssueServiceTokenRequest
    setTimestamp(Instant timestamp)
    {
        return (IssueServiceTokenRequest)super.setTimestamp(timestamp);
    }

    @Override
    public IssueServiceTokenRequest
    addHeader(String name,Object value)
    {
        return (IssueServiceTokenRequest)super.addHeader(name,value);
    }

    @Override
    public IssueServiceTokenRequest
    clearHeaders()
    {
        return (IssueServiceTokenRequest)super.clearHeaders();
    }

    @Override
    public IssueServiceTokenRequest
    addCookie(Cookie cookie)
    {
        return (IssueServiceTokenRequest)super.addCookie(cookie);
    }

    @Override
    public IssueServiceTokenRequest
    clearCookies()
    {
        return (IssueServiceTokenRequest)super.clearCookies();
    }

    public IssueServiceTokenRequest
    setClientId(String clientId)
    {
        this.clientId = clientId;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public IssueServiceTokenRequest
    setClientSecret(String clientSecret)
    {
        this.clientSecret = clientSecret;
        return this;
    }

    public String
    getClientSecret() { return clientSecret; }

    @Override
    public String
    toString()
    {
        return "IssueServiceTokenRequest{requestId=" + getRequestId() + ",clientId=" + clientId + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
