//////////////////////////////////////////////////////////////////////////////
// LoginMethodsOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

public
class LoginMethodsOutput
    extends FrontEndOutput
{
    private boolean passwordEnabled;
    private boolean oktaEnabled;

    public
    LoginMethodsOutput()
    {
        super();
        passwordEnabled = false;
        oktaEnabled     = false;
    }

    @Override
    public LoginMethodsOutput
    setUserId(Long id)
    {
        return (LoginMethodsOutput)super.setUserId(id);
    }

    @Override
    public LoginMethodsOutput
    setSource(String src)
    {
        return (LoginMethodsOutput)super.setSource(src);
    }

    @Override
    public LoginMethodsOutput
    setDestination(String dest)
    {
        return (LoginMethodsOutput)super.setDestination(dest);
    }

    @Override
    public LoginMethodsOutput
    setCommand(String cmd)
    {
        return (LoginMethodsOutput)super.setCommand(cmd);
    }

    public LoginMethodsOutput
    setPasswordEnabled(boolean value)
    {
        passwordEnabled = value;
        return this;
    }

    public LoginMethodsOutput
    setOktaEnabled(boolean value)
    {
        oktaEnabled = value;
        return this;
    }

    public boolean
    isPasswordEnabled() { return passwordEnabled; }

    public boolean
    isOktaEnabled() { return oktaEnabled; }
}

//////////////////////////////////////////////////////////////////////////////
