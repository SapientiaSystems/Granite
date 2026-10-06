//////////////////////////////////////////////////////////////////////////////
// CreateUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.CreateUserData;
import granite.user.service.requestreply.CreateUserRequest;

import java.util.Optional;

public
class CreateUserContext
{
    private CreateUserRequest request;
    private Optional<User>    createdUser;

    public
    CreateUserContext(CreateUserRequest req)
    {
        request = req;
        createdUser = Optional.empty();
    }

    public CreateUserContext
    setCreatedUser(Optional<User> created)
    {
        createdUser = created;
        return this;
    }

    public CreateUserContext
    setCreatedUser(User created)
    {
        return setCreatedUser(Optional.of(created));
    }

    public CreateUserRequest
    getRequest() { return request; }

    public CreateUserData
    getUserToCreate() { return request.getUserToCreate(); }

    public Optional<User>
    getCreatedUser() { return createdUser; }
}

//////////////////////////////////////////////////////////////////////////////

