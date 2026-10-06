//////////////////////////////////////////////////////////////////////////////
// DestroyUsersContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.DestroyUsersRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public
class DestroyUsersContext
{
    private DestroyUsersRequest    request;
    private List<User>             destroyedUsers;

    public
    DestroyUsersContext(DestroyUsersRequest req)
    {
        request = req;
        destroyedUsers = new ArrayList<>();
    }

    public DestroyUsersContext
    addDestroyedUser(User destroyed)
    {
        destroyedUsers.add(destroyed);
        return this;
    }

    public DestroyUsersContext
    setDestroyedUsers(List<User> destroyed)
    {
        destroyedUsers = destroyed;
        return this;
    }

    public DestroyUsersRequest
    getRequest() { return request; }

    public Set<Long>
    getUserIds() { return request.getUserIds(); }

    public List<User>
    getDestroyedUsers() { return destroyedUsers; }
}

//////////////////////////////////////////////////////////////////////////////


