//////////////////////////////////////////////////////////////////////////////
// CreateUsersContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUsersRequest;

import java.util.ArrayList;
import java.util.List;

public
class CreateUsersContext
{
    private CreateUsersRequest     request;
    private List<User>             createdUsers;

    public
    CreateUsersContext(CreateUsersRequest req)
    {
        request = req;
        createdUsers = new ArrayList<>();
    }

    public CreateUsersContext
    addCreatedUser(User created)
    {
        createdUsers.add(created);
        return this;
    }

    public CreateUsersContext
    setCreatedUsers(List<User> created)
    {
        createdUsers = created;
        return this;
    }

    public CreateUsersRequest
    getRequest() { return request; }

    public List<CreateUserData>
    getUsersToCreate() { return request.getUsersToCreate(); }


    public List<User>
    getCreatedUsers() { return createdUsers; }
}

//////////////////////////////////////////////////////////////////////////////

