//////////////////////////////////////////////////////////////////////////////
// SessionOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.HashSet;
import java.util.Set;

public
class SessionOutput
    extends FrontEndOutput
{
    private String      userName;
    private Set<String> authorities;
    private String      status;

    public
    SessionOutput()
    {
        super();
        userName    = null;
        authorities = new HashSet<>();
        status      = null;
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
    setStatus(String value)
    {
        status = value;
        return this;
    }

    public String
    getUserName() { return userName; }

    public Set<String>
    getAuthorities() { return authorities; }

    public String
    getStatus() { return status; }
}

//////////////////////////////////////////////////////////////////////////////
