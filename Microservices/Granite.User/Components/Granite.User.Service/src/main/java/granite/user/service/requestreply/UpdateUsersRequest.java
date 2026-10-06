//////////////////////////////////////////////////////////////////////////////
// UpdateUsersRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public
class UpdateUsersRequest
    extends AbstractServiceRequest
{
    private List<UpdateUserData> usersToUpdate;

    public
    UpdateUsersRequest()
    {
        super();
        usersToUpdate = null;
    }

    @Override
    public UpdateUsersRequest
    setRequestId(UUID requestId)
    {
        return (UpdateUsersRequest)super.setRequestId(requestId);
    }

    @Override
    public UpdateUsersRequest
    setTimestamp(Instant timestamp)
    {
        return (UpdateUsersRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateUsersRequest
    addHeader(String name,Object value)
    {
        return (UpdateUsersRequest)super.addHeader(name,value);
    }

    @Override
    public UpdateUsersRequest
    clearHeaders()
    {
        return (UpdateUsersRequest)super.clearHeaders();
    }

    @Override
    public UpdateUsersRequest
    addCookie(Cookie cookie)
    {
        return (UpdateUsersRequest)super.addCookie(cookie);
    }

    @Override
    public UpdateUsersRequest
    clearCookies()
    {
        return (UpdateUsersRequest)super.clearCookies();
    }

    public UpdateUsersRequest
    setUsersToUpdate(List<UpdateUserData> entities)
    {
        usersToUpdate = entities;
        return this;
    }

    public List<UpdateUserData>
    getUsersToUpdate() { return usersToUpdate; }
}

//////////////////////////////////////////////////////////////////////////////
