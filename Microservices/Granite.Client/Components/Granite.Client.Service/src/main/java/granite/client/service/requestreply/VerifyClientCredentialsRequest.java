//////////////////////////////////////////////////////////////////////////////
// VerifyClientCredentialsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class VerifyClientCredentialsRequest
    extends AbstractServiceRequest
{
    private String clientId;
    private String clientSecret;

    public
    VerifyClientCredentialsRequest()
    {
        super();
        clientId = null;
        clientSecret = null;
    }

    @Override
    public VerifyClientCredentialsRequest
    setRequestId(UUID requestId)
    {
        return (VerifyClientCredentialsRequest)super.setRequestId(requestId);
    }

    @Override
    public VerifyClientCredentialsRequest
    setTimestamp(Instant timestamp)
    {
        return (VerifyClientCredentialsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public VerifyClientCredentialsRequest
    addHeader(String name,Object value)
    {
        return (VerifyClientCredentialsRequest)super.addHeader(name,value);
    }

    @Override
    public VerifyClientCredentialsRequest
    clearHeaders()
    {
        return (VerifyClientCredentialsRequest)super.clearHeaders();
    }

    @Override
    public VerifyClientCredentialsRequest
    addCookie(Cookie cookie)
    {
        return (VerifyClientCredentialsRequest)super.addCookie(cookie);
    }

    @Override
    public VerifyClientCredentialsRequest
    clearCookies()
    {
        return (VerifyClientCredentialsRequest)super.clearCookies();
    }

    public VerifyClientCredentialsRequest
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public VerifyClientCredentialsRequest
    setClientSecret(String value)
    {
        clientSecret = value;
        return this;
    }

    public String
    getClientId() { return clientId; }

    public String
    getClientSecret() { return clientSecret; }

    @Override
    public String
    toString()
    {
        return "VerifyClientCredentialsRequest{requestId=" + getRequestId() + ",clientId=" + clientId + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
