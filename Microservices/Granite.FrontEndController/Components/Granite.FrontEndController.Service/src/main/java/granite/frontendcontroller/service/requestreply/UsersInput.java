//////////////////////////////////////////////////////////////////////////////
// HomeInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.user.service.requestreply.UserData;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.ArrayList;
import java.util.List;

public
class UsersInput
    extends FrontEndInput
{
    private List<UserData> users;

    public UsersInput()
    {
        super();
        users = new ArrayList<>();
    }

    @Override
    public UsersInput
    setUserId(Long id)
    {
        return (UsersInput)super.setUserId(id);
    }

    @Override
    public UsersInput
    setSource(String src)
    {
        return (UsersInput)super.setSource(src);
    }

    @Override
    public UsersInput
    setDestination(String dest)
    {
        return (UsersInput)super.setDestination(dest);
    }

    @Override
    public UsersInput
    setCommand(String cmd)
    {
        return (UsersInput)super.setCommand(cmd);
    }

    public UsersInput
    setUsers(List<UserData> users)
    {
        this.users = users;
        return this;
    }

    public List<UserData>
    getUsers() { return users; }
}

//////////////////////////////////////////////////////////////////////////////
