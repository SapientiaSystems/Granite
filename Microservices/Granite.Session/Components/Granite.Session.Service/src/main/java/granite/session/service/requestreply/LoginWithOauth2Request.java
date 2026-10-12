//////////////////////////////////////////////////////////////////////////////
// LoginWithOauth2Request.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class LoginWithOauth2Request
    extends AbstractServiceRequest
{
    private String provider;
    private String idToken;
    private String deviceId;

    public
    LoginWithOauth2Request()
    {
        super();
        provider = null;
        idToken = null;
        deviceId = null;
    }

    @Override
    public LoginWithOauth2Request
    setRequestId(UUID requestId)
    {
        return (LoginWithOauth2Request)super.setRequestId(requestId);
    }

    @Override
    public LoginWithOauth2Request
    setTimestamp(Instant timestamp)
    {
        return (LoginWithOauth2Request)super.setTimestamp(timestamp);
    }

    @Override
    public LoginWithOauth2Request
    addHeader(String name,Object value)
    {
        return (LoginWithOauth2Request)super.addHeader(name,value);
    }

    @Override
    public LoginWithOauth2Request
    clearHeaders()
    {
        return (LoginWithOauth2Request)super.clearHeaders();
    }

    @Override
    public LoginWithOauth2Request
    addCookie(Cookie cookie)
    {
        return (LoginWithOauth2Request)super.addCookie(cookie);
    }

    @Override
    public LoginWithOauth2Request
    clearCookies()
    {
        return (LoginWithOauth2Request)super.clearCookies();
    }

    public LoginWithOauth2Request
    setProvider(String provider)
    {
        this.provider = provider;
        return this;
    }

    public String
    getProvider() { return provider; }

    public LoginWithOauth2Request
    setIdToken(String idToken)
    {
        this.idToken = idToken;
        return this;
    }

    public String
    getIdToken() { return idToken; }

    public LoginWithOauth2Request
    setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }
}

//////////////////////////////////////////////////////////////////////////////
