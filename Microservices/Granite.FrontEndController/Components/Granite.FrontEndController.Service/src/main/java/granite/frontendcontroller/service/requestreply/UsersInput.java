//////////////////////////////////////////////////////////////////////////////
// UsersInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.UpdateUserData;
import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.ArrayList;
import java.util.List;

public
class UsersInput
    extends FrontEndInput
{
    private List<CreateUserData> usersToCreate;
    private List<UpdateUserData> usersToUpdate;
    private List<Long>           userIdsToDelete;

    public
    UsersInput()
    {
        super();
        usersToCreate   = new ArrayList<>();
        usersToUpdate   = new ArrayList<>();
        userIdsToDelete = new ArrayList<>();
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
    setUsersToCreate(List<CreateUserData> users)
    {
        usersToCreate = users;
        return this;
    }

    public UsersInput
    setUsersToUpdate(List<UpdateUserData> users)
    {
        usersToUpdate = users;
        return this;
    }

    public UsersInput
    setUserIdsToDelete(List<Long> ids)
    {
        userIdsToDelete = ids;
        return this;
    }

    public List<CreateUserData>
    getUsersToCreate() { return usersToCreate; }

    public List<UpdateUserData>
    getUsersToUpdate() { return usersToUpdate; }

    public List<Long>
    getUserIdsToDelete() { return userIdsToDelete; }
}

//////////////////////////////////////////////////////////////////////////////
