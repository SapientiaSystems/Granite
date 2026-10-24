//////////////////////////////////////////////////////////////////////////////
// LogoutOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

public
class LogoutOutput
    extends FrontEndOutput
{
    private boolean loggedOut;
    private String  endSessionUrl;

    public
    LogoutOutput()
    {
        super();
        loggedOut     = false;
        endSessionUrl = null;
    }

    @Override
    public LogoutOutput
    setUserId(Long id)
    {
        return (LogoutOutput)super.setUserId(id);
    }

    @Override
    public LogoutOutput
    setSource(String src)
    {
        return (LogoutOutput)super.setSource(src);
    }

    @Override
    public LogoutOutput
    setDestination(String dest)
    {
        return (LogoutOutput)super.setDestination(dest);
    }

    @Override
    public LogoutOutput
    setCommand(String cmd)
    {
        return (LogoutOutput)super.setCommand(cmd);
    }

    public LogoutOutput
    setLoggedOut(boolean value)
    {
        loggedOut = value;
        return this;
    }

    public LogoutOutput
    setEndSessionUrl(String value)
    {
        endSessionUrl = value;
        return this;
    }

    public boolean
    isLoggedOut() { return loggedOut; }

    public String
    getEndSessionUrl() { return endSessionUrl; }
}

//////////////////////////////////////////////////////////////////////////////
