//////////////////////////////////////////////////////////////////////////////
// LoginContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.LoginRequest;
import granite.user.service.requestreply.CredentialKind;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public
class LoginContext
{
    private LoginRequest       request;
    private Long               validatedUserId;
    private Set<String>        validatedAuthorities;
    private Map<String,Object> validatedValues;
    private boolean            unsupportedKind;
    private Session            created;
    private String             jwt;
    private String             refreshToken;
    private String             sessionHandle;
    private LoginLifecycle     lifecycle;

    public
    LoginContext(LoginRequest req)
    {
        request              = req;
        validatedUserId      = null;
        validatedAuthorities = new LinkedHashSet<>();
        validatedValues      = new LinkedHashMap<>();
        unsupportedKind      = false;
        created              = null;
        jwt                  = null;
        refreshToken         = null;
        sessionHandle        = null;
        lifecycle            = null;
    }

    public LoginContext
    setValidatedUserId(Long value)
    {
        validatedUserId = value;
        return this;
    }

    public LoginContext
    setValidatedAuthorities(Set<String> values)
    {
        validatedAuthorities = values != null ? values : new LinkedHashSet<>();
        return this;
    }

    public LoginContext
    setValidatedValues(Map<String,Object> values)
    {
        validatedValues = values != null ? values : new LinkedHashMap<>();
        return this;
    }

    public LoginContext
    setUnsupportedKind(boolean value)
    {
        unsupportedKind = value;
        return this;
    }

    public LoginContext
    setCreated(Session value)
    {
        created = value;
        return this;
    }

    public LoginContext
    setJwt(String value)
    {
        jwt = value;
        return this;
    }

    public LoginContext
    setRefreshToken(String value)
    {
        refreshToken = value;
        return this;
    }

    public LoginContext
    setSessionHandle(String value)
    {
        sessionHandle = value;
        return this;
    }

    public LoginContext
    setLifecycle(LoginLifecycle value)
    {
        lifecycle = value;
        return this;
    }

    public LoginRequest
    getRequest() { return request; }

    public String
    getUserName() { return request.getUserName(); }

    public String
    getCredential() { return request.getCredential(); }

    public CredentialKind
    getCredentialKind() { return request.getCredentialKind(); }

    public String
    getDeviceId() { return request.getDeviceId(); }

    public Long
    getValidatedUserId() { return validatedUserId; }

    public Set<String>
    getValidatedAuthorities() { return validatedAuthorities; }

    public Map<String,Object>
    getValidatedValues() { return validatedValues; }

    public boolean
    isUnsupportedKind() { return unsupportedKind; }

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
}

//////////////////////////////////////////////////////////////////////////////
