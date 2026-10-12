//////////////////////////////////////////////////////////////////////////////
// LoginRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import granite.user.service.requestreply.CredentialKind;
import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class LoginRequest
    extends AbstractServiceRequest
{
    private String userName;
    private String credential;
    private CredentialKind credentialKind;
    private String deviceId;

    public
    LoginRequest()
    {
        super();
        userName = null;
        credential = null;
        credentialKind = null;
        deviceId = null;
    }

    @Override
    public LoginRequest
    setRequestId(UUID requestId)
    {
        return (LoginRequest)super.setRequestId(requestId);
    }

    @Override
    public LoginRequest
    setTimestamp(Instant timestamp)
    {
        return (LoginRequest)super.setTimestamp(timestamp);
    }

    @Override
    public LoginRequest
    addHeader(String name,Object value)
    {
        return (LoginRequest)super.addHeader(name,value);
    }

    @Override
    public LoginRequest
    clearHeaders()
    {
        return (LoginRequest)super.clearHeaders();
    }

    @Override
    public LoginRequest
    addCookie(Cookie cookie)
    {
        return (LoginRequest)super.addCookie(cookie);
    }

    @Override
    public LoginRequest
    clearCookies()
    {
        return (LoginRequest)super.clearCookies();
    }

    public LoginRequest
    setUserName(String userName)
    {
        this.userName = userName;
        return this;
    }

    public String
    getUserName() { return userName; }

    public LoginRequest
    setCredential(String credential)
    {
        this.credential = credential;
        return this;
    }

    public String
    getCredential() { return credential; }

    public LoginRequest
    setCredentialKind(CredentialKind credentialKind)
    {
        this.credentialKind = credentialKind;
        return this;
    }

    public CredentialKind
    getCredentialKind() { return credentialKind; }

    public LoginRequest
    setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }
}

//////////////////////////////////////////////////////////////////////////////
