//////////////////////////////////////////////////////////////////////////////
// FindUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.FindUserRequest;

import java.util.Optional;

public
class FindUserContext
{
    private FindUserRequest request;
    private Optional<User>  foundUser;

    public
    FindUserContext(FindUserRequest req)
    {
        request = req;
        foundUser = Optional.empty();
    }

    public FindUserContext
    setFoundUser(Optional<User> updated)
    {
        foundUser = updated;
        return this;
    }

    public FindUserRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public Optional<User>
    getFoundUser() { return foundUser; }
}

//////////////////////////////////////////////////////////////////////////////
