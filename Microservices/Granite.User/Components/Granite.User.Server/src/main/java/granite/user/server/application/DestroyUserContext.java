//////////////////////////////////////////////////////////////////////////////
// UpdateUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.DestroyUserRequest;

import java.util.Optional;

public
class DestroyUserContext
{
    private DestroyUserRequest request;
    private Optional<User> destroyedUser;

    public
    DestroyUserContext(DestroyUserRequest req)
    {
        request = req;
        destroyedUser = Optional.empty();
    }

    public DestroyUserContext
    setDestroyedUser(Optional<User> updated)
    {
        destroyedUser = updated;
        return this;
    }

    public DestroyUserContext
    setDestroyedUser(User updated)
    {
        return setDestroyedUser(Optional.of(updated));
    }

    public DestroyUserRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public Optional<User>
    getDestroyedUser() { return destroyedUser; }
}

//////////////////////////////////////////////////////////////////////////////
