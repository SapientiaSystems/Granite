//////////////////////////////////////////////////////////////////////////////
// AuthenticateUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class AuthenticateUserRequest
    extends AbstractServiceRequest
{
    private String         userName;
    private String         credential;
    private CredentialKind credentialKind;

    public
    AuthenticateUserRequest()
    {
        super();
        userName       = null;
        credential     = null;
        credentialKind = null;
    }

    @Override
    public AuthenticateUserRequest
    setRequestId(UUID requestId)
    {
        return (AuthenticateUserRequest)super.setRequestId(requestId);
    }

    @Override
    public AuthenticateUserRequest
    setTimestamp(Instant timestamp)
    {
        return (AuthenticateUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public AuthenticateUserRequest
    addHeader(String name,Object value)
    {
        return (AuthenticateUserRequest)super.addHeader(name,value);
    }

    @Override
    public AuthenticateUserRequest
    clearHeaders()
    {
        return (AuthenticateUserRequest)super.clearHeaders();
    }

    @Override
    public AuthenticateUserRequest
    addCookie(Cookie cookie)
    {
        return (AuthenticateUserRequest)super.addCookie(cookie);
    }

    @Override
    public AuthenticateUserRequest
    clearCookies()
    {
        return (AuthenticateUserRequest)super.clearCookies();
    }

    public AuthenticateUserRequest
    setUserName(String value)
    {
        userName = value;
        return this;
    }

    public AuthenticateUserRequest
    setCredential(String value)
    {
        credential = value;
        return this;
    }

    public AuthenticateUserRequest
    setCredentialKind(CredentialKind value)
    {
        credentialKind = value;
        return this;
    }

    public String
    getUserName() { return userName; }

    public String
    getCredential() { return credential; }

    public CredentialKind
    getCredentialKind() { return credentialKind; }
}

//////////////////////////////////////////////////////////////////////////////
