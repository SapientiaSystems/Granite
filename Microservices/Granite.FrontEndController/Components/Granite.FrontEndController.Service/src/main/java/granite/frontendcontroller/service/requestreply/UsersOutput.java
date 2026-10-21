//////////////////////////////////////////////////////////////////////////////
// UsersOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.user.service.requestreply.UserData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.List;

public
class UsersOutput
    extends FrontEndOutput
{
    private List<UserData> users;
    private List<String>   failureMessages;

    public
    UsersOutput()
    {
        super();
        users           = new ArrayList<>();
        failureMessages = new ArrayList<>();
    }

    @Override
    public UsersOutput
    setUserId(Long id)
    {
        return (UsersOutput)super.setUserId(id);
    }

    @Override
    public UsersOutput
    setSource(String src)
    {
        return (UsersOutput)super.setSource(src);
    }

    @Override
    public UsersOutput
    setDestination(String dest)
    {
        return (UsersOutput)super.setDestination(dest);
    }

    @Override
    public UsersOutput
    setCommand(String cmd)
    {
        return (UsersOutput)super.setCommand(cmd);
    }

    public UsersOutput
    setUsers(List<UserData> users)
    {
        this.users = users;
        return this;
    }

    public UsersOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public List<UserData>
    getUsers() { return users; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
