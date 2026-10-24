//////////////////////////////////////////////////////////////////////////////
// LoginInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class LoginInput
    extends FrontEndInput
{
    private String userName;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public
    LoginInput()
    {
        super();
        userName = null;
        password = null;
    }

    @Override
    public LoginInput
    setUserId(Long id)
    {
        return (LoginInput)super.setUserId(id);
    }

    @Override
    public LoginInput
    setSource(String src)
    {
        return (LoginInput)super.setSource(src);
    }

    @Override
    public LoginInput
    setDestination(String dest)
    {
        return (LoginInput)super.setDestination(dest);
    }

    @Override
    public LoginInput
    setCommand(String cmd)
    {
        return (LoginInput)super.setCommand(cmd);
    }

    public LoginInput
    setUserName(String value)
    {
        userName = value;
        return this;
    }

    public LoginInput
    setPassword(String value)
    {
        password = value;
        return this;
    }

    public String
    getUserName() { return userName; }

    public String
    getPassword() { return password; }

    @Override
    public String
    toString()
    {
        return "LoginInput{userName=" + userName + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
