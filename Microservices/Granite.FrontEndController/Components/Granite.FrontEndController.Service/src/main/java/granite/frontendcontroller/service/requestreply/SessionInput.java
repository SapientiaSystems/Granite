//////////////////////////////////////////////////////////////////////////////
// SessionInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class SessionInput
    extends FrontEndInput
{
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String returnTo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    private String state;

    private String application;

    public
    SessionInput()
    {
        super();
        userName = null;
        password = null;
        returnTo = null;
        code     = null;
        state    = null;
        application = null;
    }

    @Override
    public SessionInput
    setUserId(Long id)
    {
        return (SessionInput)super.setUserId(id);
    }

    @Override
    public SessionInput
    setSource(String src)
    {
        return (SessionInput)super.setSource(src);
    }

    @Override
    public SessionInput
    setDestination(String dest)
    {
        return (SessionInput)super.setDestination(dest);
    }

    @Override
    public SessionInput
    setCommand(String cmd)
    {
        return (SessionInput)super.setCommand(cmd);
    }

    public SessionInput
    setUserName(String value)
    {
        userName = value;
        return this;
    }

    public SessionInput
    setPassword(String value)
    {
        password = value;
        return this;
    }

    public SessionInput
    setReturnTo(String value)
    {
        returnTo = value;
        return this;
    }

    public SessionInput
    setCode(String value)
    {
        code = value;
        return this;
    }

    public SessionInput
    setState(String value)
    {
        state = value;
        return this;
    }

    public SessionInput
    setApplication(String value)
    {
        application = value;
        return this;
    }

    public String
    getUserName() { return userName; }

    public String
    getPassword() { return password; }

    public String
    getReturnTo() { return returnTo; }

    public String
    getCode() { return code; }

    public String
    getState() { return state; }

    public String
    getApplication() { return application; }

    @Override
    public String
    toString()
    {
        return "SessionInput{key=" + getKey() + ",userName=" + userName + ",returnTo=" + returnTo + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
