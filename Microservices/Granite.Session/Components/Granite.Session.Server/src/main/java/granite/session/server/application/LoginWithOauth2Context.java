//////////////////////////////////////////////////////////////////////////////
// LoginWithOauth2Context.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.LoginWithOauth2Request;

import java.util.LinkedHashSet;
import java.util.Set;

public
class LoginWithOauth2Context
{
    private LoginWithOauth2Request  request;
    private Oauth2ValidatedIdentity validatedIdentity;
    private Long                    userId;
    private Set<String>             authorities;
    private Session                 created;
    private String                  jwt;
    private String                  refreshToken;
    private String                  sessionHandle;
    private LoginLifecycle          lifecycle;
    private String                  failureReason;

    public
    LoginWithOauth2Context(LoginWithOauth2Request req)
    {
        request           = req;
        validatedIdentity = null;
        userId            = null;
        authorities       = new LinkedHashSet<>();
        created           = null;
        jwt               = null;
        refreshToken      = null;
        sessionHandle     = null;
        lifecycle         = null;
        failureReason     = null;
    }

    public LoginWithOauth2Context
    setValidatedIdentity(Oauth2ValidatedIdentity value)
    {
        validatedIdentity = value;
        return this;
    }

    public LoginWithOauth2Context
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public LoginWithOauth2Context
    setAuthorities(Set<String> values)
    {
        authorities = values != null ? values : new LinkedHashSet<>();
        return this;
    }

    public LoginWithOauth2Context
    setCreated(Session value)
    {
        created = value;
        return this;
    }

    public LoginWithOauth2Context
    setJwt(String value)
    {
        jwt = value;
        return this;
    }

    public LoginWithOauth2Context
    setRefreshToken(String value)
    {
        refreshToken = value;
        return this;
    }

    public LoginWithOauth2Context
    setSessionHandle(String value)
    {
        sessionHandle = value;
        return this;
    }

    public LoginWithOauth2Context
    setFailureReason(String value)
    {
        failureReason = value;
        return this;
    }

    public LoginWithOauth2Context
    setLifecycle(LoginLifecycle value)
    {
        lifecycle = value;
        return this;
    }

    public LoginWithOauth2Request
    getRequest() { return request; }

    public String
    getProvider() { return request.getProvider(); }

    public String
    getIdToken() { return request.getIdToken(); }

    public String
    getDeviceId() { return request.getDeviceId(); }

    public Oauth2ValidatedIdentity
    getValidatedIdentity() { return validatedIdentity; }

    public Long
    getUserId() { return userId; }

    public Set<String>
    getAuthorities() { return authorities; }

    public Session
    getCreated() { return created; }

    public String
    getJwt() { return jwt; }

    public String
    getRefreshToken() { return refreshToken; }

    public String
    getSessionHandle() { return sessionHandle; }

    public LoginLifecycle
    getLifecycle() { return lifecycle; }

    public String
    getFailureReason() { return failureReason; }
}

//////////////////////////////////////////////////////////////////////////////
