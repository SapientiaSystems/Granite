//////////////////////////////////////////////////////////////////////////////
// UpdateUsersContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUsersRequest;

import java.util.ArrayList;
import java.util.List;

public
class UpdateUsersContext
{
    private UpdateUsersRequest     request;
    private List<User>             updatedUsers;

    public
    UpdateUsersContext(UpdateUsersRequest req)
    {
        request = req;
        updatedUsers = new ArrayList<>();
    }

    public UpdateUsersContext
    addUpdatedUser(User updated)
    {
        updatedUsers.add(updated);
        return this;
    }

    public UpdateUsersContext
    setUpdatedUsers(List<User> updated)
    {
        updatedUsers = updated;
        return this;
    }

    public UpdateUsersRequest
    getRequest() { return request; }

    public List<UpdateUserData>
    getUsersToUpdate() { return request.getUsersToUpdate(); }

    public List<User>
    getUpdatedUsers() { return updatedUsers; }
}

//////////////////////////////////////////////////////////////////////////////
