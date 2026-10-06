//////////////////////////////////////////////////////////////////////////////
// CreateUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class CreateUserRequest
    extends AbstractServiceRequest
{
    private CreateUserData userToCreate;

    public
    CreateUserRequest()
    {
        super();
        userToCreate = null;
    }

    @Override
    public CreateUserRequest
    setRequestId(UUID requestId)
    {
        return (CreateUserRequest)super.setRequestId(requestId);
    }

    @Override
    public CreateUserRequest
    setTimestamp(Instant timestamp)
    {
        return (CreateUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CreateUserRequest
    addHeader(String name,Object value)
    {
        return (CreateUserRequest)super.addHeader(name,value);
    }

    @Override
    public CreateUserRequest
    clearHeaders()
    {
        return (CreateUserRequest)super.clearHeaders();
    }

    @Override
    public CreateUserRequest
    addCookie(Cookie cookie)
    {
        return (CreateUserRequest)super.addCookie(cookie);
    }

    @Override
    public CreateUserRequest
    clearCookies()
    {
        return (CreateUserRequest)super.clearCookies();
    }

    public CreateUserRequest
    setUserToCreate(CreateUserData entity)
    {
        userToCreate = entity;
        return this;
    }

    public CreateUserData
    getUserToCreate() { return userToCreate; }
}

//////////////////////////////////////////////////////////////////////////////
