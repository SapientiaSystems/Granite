//////////////////////////////////////////////////////////////////////////////
// UserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.io.Serializable;

public
class UserData
    implements Serializable
{
    private Long   userId;
    private String foo;
    private String userName;
    private String externalIdentity;

    public
    UserData()
    {
        userId           = null;
        foo              = null;
        userName         = null;
        externalIdentity = null;
    }

    public UserData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public UserData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public UserData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public UserData
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getFoo() { return foo; }

    public String
    getUserName() { return userName; }

    public String
    getExternalIdentity() { return externalIdentity; }
}

//////////////////////////////////////////////////////////////////////////////
