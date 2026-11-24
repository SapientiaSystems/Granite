//////////////////////////////////////////////////////////////////////////////
// SessionOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public
class SessionOutput
    extends FrontEndOutput
{
    private String       status;
    private String       userName;
    private Set<String>  authorities;
    private String       returnTo;
    private boolean      passwordEnabled;
    private boolean      oktaEnabled;
    private String       redirectUrl;
    private boolean      loggedOut;
    private boolean      passwordSet;
    private List<String> failureMessages;

    public
    SessionOutput()
    {
        super();
        status          = null;
        userName        = null;
        authorities     = new HashSet<>();
        returnTo        = null;
        passwordEnabled = false;
        oktaEnabled     = false;
        redirectUrl     = null;
        loggedOut       = false;
        passwordSet     = false;
        failureMessages = new ArrayList<>();
    }

    @Override
    public SessionOutput
    setUserId(Long id)
    {
        return (SessionOutput)super.setUserId(id);
    }

    @Override
    public SessionOutput
    setSource(String src)
    {
        return (SessionOutput)super.setSource(src);
    }

    @Override
    public SessionOutput
    setDestination(String dest)
    {
        return (SessionOutput)super.setDestination(dest);
    }

    @Override
    public SessionOutput
    setCommand(String cmd)
    {
        return (SessionOutput)super.setCommand(cmd);
    }

    public SessionOutput
    setStatus(String value)
    {
        status = value;
        return this;
    }

    public SessionOutput
    setUserName(String value)
    {
        userName = value;
        return this;
    }

    public SessionOutput
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public SessionOutput
    setReturnTo(String value)
    {
        returnTo = value;
        return this;
    }

    public SessionOutput
    setPasswordEnabled(boolean value)
    {
        passwordEnabled = value;
        return this;
    }

    public SessionOutput
    setOktaEnabled(boolean value)
    {
        oktaEnabled = value;
        return this;
    }

    public SessionOutput
    setRedirectUrl(String value)
    {
        redirectUrl = value;
        return this;
    }

    public SessionOutput
    setLoggedOut(boolean value)
    {
        loggedOut = value;
        return this;
    }

    public SessionOutput
    setPasswordSet(boolean value)
    {
        passwordSet = value;
        return this;
    }

    public SessionOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public String
    getStatus() { return status; }

    public String
    getUserName() { return userName; }

    public Set<String>
    getAuthorities() { return authorities; }

    public String
    getReturnTo() { return returnTo; }

    public boolean
    isPasswordEnabled() { return passwordEnabled; }

    public boolean
    isOktaEnabled() { return oktaEnabled; }

    public String
    getRedirectUrl() { return redirectUrl; }

    public boolean
    isLoggedOut() { return loggedOut; }

    public boolean
    isPasswordSet() { return passwordSet; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
