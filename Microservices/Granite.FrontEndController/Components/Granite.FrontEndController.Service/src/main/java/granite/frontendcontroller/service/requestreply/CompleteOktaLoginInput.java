//////////////////////////////////////////////////////////////////////////////
// CompleteOktaLoginInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class CompleteOktaLoginInput
    extends FrontEndInput
{
    private String code;
    private String state;

    public
    CompleteOktaLoginInput()
    {
        super();
        code  = null;
        state = null;
    }

    @Override
    public CompleteOktaLoginInput
    setUserId(Long id)
    {
        return (CompleteOktaLoginInput)super.setUserId(id);
    }

    @Override
    public CompleteOktaLoginInput
    setSource(String src)
    {
        return (CompleteOktaLoginInput)super.setSource(src);
    }

    @Override
    public CompleteOktaLoginInput
    setDestination(String dest)
    {
        return (CompleteOktaLoginInput)super.setDestination(dest);
    }

    @Override
    public CompleteOktaLoginInput
    setCommand(String cmd)
    {
        return (CompleteOktaLoginInput)super.setCommand(cmd);
    }

    public CompleteOktaLoginInput
    setCode(String value)
    {
        code = value;
        return this;
    }

    public CompleteOktaLoginInput
    setState(String value)
    {
        state = value;
        return this;
    }

    public String
    getCode() { return code; }

    public String
    getState() { return state; }
}

//////////////////////////////////////////////////////////////////////////////
