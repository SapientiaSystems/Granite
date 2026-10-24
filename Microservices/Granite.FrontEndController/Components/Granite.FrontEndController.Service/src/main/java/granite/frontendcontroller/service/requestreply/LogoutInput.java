//////////////////////////////////////////////////////////////////////////////
// LogoutInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class LogoutInput
    extends FrontEndInput
{
    private boolean logout;

    public
    LogoutInput()
    {
        super();
        logout = true;
    }

    @Override
    public LogoutInput
    setUserId(Long id)
    {
        return (LogoutInput)super.setUserId(id);
    }

    @Override
    public LogoutInput
    setSource(String src)
    {
        return (LogoutInput)super.setSource(src);
    }

    @Override
    public LogoutInput
    setDestination(String dest)
    {
        return (LogoutInput)super.setDestination(dest);
    }

    @Override
    public LogoutInput
    setCommand(String cmd)
    {
        return (LogoutInput)super.setCommand(cmd);
    }

    public LogoutInput
    setLogout(boolean value)
    {
        logout = value;
        return this;
    }

    public boolean
    isLogout() { return logout; }
}

//////////////////////////////////////////////////////////////////////////////
