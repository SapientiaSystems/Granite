//////////////////////////////////////////////////////////////////////////////
// ClientOwnerData.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import java.io.Serializable;

public
class ClientOwnerData
    implements Serializable
{
    private Long   userId;
    private String userName;

    public
    ClientOwnerData()
    {
        userId   = null;
        userName = null;
    }

    public ClientOwnerData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public ClientOwnerData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getUserName() { return userName; }
}

//////////////////////////////////////////////////////////////////////////////
