//////////////////////////////////////////////////////////////////////////////
// UpdateUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.UpdateUserData;
import granite.user.service.requestreply.UpdateUserRequest;

import java.util.Optional;

public
class UpdateUserContext
{
    private UpdateUserRequest request;
    private Optional<User>    updatedUser;

    public
    UpdateUserContext(UpdateUserRequest req)
    {
        request = req;
        updatedUser = Optional.empty();
    }

    public UpdateUserContext
    setUpdatedUser(Optional<User> updated)
    {
        updatedUser = updated;
        return this;
    }

    public UpdateUserContext
    setUpdatedUser(User updated)
    {
        return setUpdatedUser(Optional.of(updated));
    }

    public UpdateUserRequest
    getRequest() { return request; }

    public UpdateUserData
    getUserToUpdate() { return request.getUserToUpdate(); }

    public Optional<User>
    getUpdatedUser() { return updatedUser; }
}

//////////////////////////////////////////////////////////////////////////////
