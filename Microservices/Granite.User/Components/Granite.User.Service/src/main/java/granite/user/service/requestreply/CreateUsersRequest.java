//////////////////////////////////////////////////////////////////////////////
// CreateUsersRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class CreateUsersRequest
    extends AbstractServiceRequest
{
    private List<CreateUserData> usersToCreate;

    public
    CreateUsersRequest()
    {
        super();
        usersToCreate = null;
    }

    @Override
    public CreateUsersRequest
    setRequestId(UUID requestId)
    {
        return (CreateUsersRequest)super.setRequestId(requestId);
    }

    @Override
    public CreateUsersRequest
    setTimestamp(Instant timestamp)
    {
        return (CreateUsersRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CreateUsersRequest
    addHeader(String name,Object value)
    {
        return (CreateUsersRequest)super.addHeader(name,value);
    }

    @Override
    public CreateUsersRequest
    clearHeaders()
    {
        return (CreateUsersRequest)super.clearHeaders();
    }

    @Override
    public CreateUsersRequest
    addCookie(Cookie cookie)
    {
        return (CreateUsersRequest)super.addCookie(cookie);
    }

    @Override
    public CreateUsersRequest
    clearCookies()
    {
        return (CreateUsersRequest)super.clearCookies();
    }

    public CreateUsersRequest
    setUsersToCreate(List<CreateUserData> entities)
    {
        usersToCreate = entities;
        return this;
    }

    public List<CreateUserData>
    getUsersToCreate() { return usersToCreate; }
}

//////////////////////////////////////////////////////////////////////////////

