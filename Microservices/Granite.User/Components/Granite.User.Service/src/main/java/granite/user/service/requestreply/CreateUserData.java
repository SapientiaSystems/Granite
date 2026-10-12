//////////////////////////////////////////////////////////////////////////////
// CreateUserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.io.Serializable;

public
class CreateUserData
    implements Serializable
{
    private String foo;
    private String userName;
    private String password;
    private String externalIdentity;

    public
    CreateUserData()
    {
        foo              = null;
        userName         = null;
        password         = null;
        externalIdentity = null;
    }

    public CreateUserData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public CreateUserData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public CreateUserData
    setPassword(String plainText)
    {
        password = plainText;
        return this;
    }

    public CreateUserData
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public String
    getFoo() { return foo; }

    public String
    getUserName() { return userName; }

    public String
    getPassword() { return password; }

    public String
    getExternalIdentity() { return externalIdentity; }
}

//////////////////////////////////////////////////////////////////////////////
