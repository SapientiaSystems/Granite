//////////////////////////////////////////////////////////////////////////////
// UpdateUserRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class UpdateUserRequest
    extends AbstractServiceRequest
{
    private UpdateUserData userToUpdate;

    public
    UpdateUserRequest()
    {
        super();
        userToUpdate = null;
    }

    @Override
    public UpdateUserRequest
    setRequestId(UUID requestId)
    {
        return (UpdateUserRequest)super.setRequestId(requestId);
    }

    @Override
    public UpdateUserRequest
    setTimestamp(Instant timestamp)
    {
        return (UpdateUserRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateUserRequest
    addHeader(String name,Object value)
    {
        return (UpdateUserRequest)super.addHeader(name,value);
    }

    @Override
    public UpdateUserRequest
    clearHeaders()
    {
        return (UpdateUserRequest)super.clearHeaders();
    }

    @Override
    public UpdateUserRequest
    addCookie(Cookie cookie)
    {
        return (UpdateUserRequest)super.addCookie(cookie);
    }

    @Override
    public UpdateUserRequest
    clearCookies()
    {
        return (UpdateUserRequest)super.clearCookies();
    }

    public UpdateUserRequest
    setUserToUpdate(UpdateUserData entity)
    {
        userToUpdate = entity;
        return this;
    }

    public UpdateUserData
    getUserToUpdate() { return userToUpdate; }
}

//////////////////////////////////////////////////////////////////////////////
