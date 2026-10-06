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

    public
    UserData()
    {
        userId = null;
        foo = null;
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

    public Long
    getUserId() { return userId; }
    
    public String
    getFoo() { return foo; }
}

//////////////////////////////////////////////////////////////////////////////
