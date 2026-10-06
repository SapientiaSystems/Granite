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

    public
    CreateUserData()
    {
        foo = null;
    }

    public CreateUserData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public String
    getFoo() { return foo; }
}

//////////////////////////////////////////////////////////////////////////////
