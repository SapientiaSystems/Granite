//////////////////////////////////////////////////////////////////////////////
// StartOktaLoginInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class StartOktaLoginInput
    extends FrontEndInput
{
    private String returnTo;

    public
    StartOktaLoginInput()
    {
        super();
        returnTo = null;
    }

    @Override
    public StartOktaLoginInput
    setUserId(Long id)
    {
        return (StartOktaLoginInput)super.setUserId(id);
    }

    @Override
    public StartOktaLoginInput
    setSource(String src)
    {
        return (StartOktaLoginInput)super.setSource(src);
    }

    @Override
    public StartOktaLoginInput
    setDestination(String dest)
    {
        return (StartOktaLoginInput)super.setDestination(dest);
    }

    @Override
    public StartOktaLoginInput
    setCommand(String cmd)
    {
        return (StartOktaLoginInput)super.setCommand(cmd);
    }

    public StartOktaLoginInput
    setReturnTo(String value)
    {
        returnTo = value;
        return this;
    }

    public String
    getReturnTo() { return returnTo; }
}

//////////////////////////////////////////////////////////////////////////////
