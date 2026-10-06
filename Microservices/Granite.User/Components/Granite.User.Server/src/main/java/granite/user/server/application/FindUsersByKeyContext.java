//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeyContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.FindUsersByKeyRequest;
import granite.user.service.requestreply.UserKey;

import java.util.List;

public
class FindUsersByKeyContext
    extends AbstractFindUsersContext
{
    private FindUsersByKeyRequest request;

    public
    FindUsersByKeyContext(FindUsersByKeyRequest request)
    {
        this.request = request;
    }

    @Override
    public FindUsersByKeyContext
    setFoundUsers(List<User> found)
    {
        return (FindUsersByKeyContext)super.setFoundUsers(found);
    }

    public FindUsersByKeyRequest
    getRequest() { return request; }

    public UserKey
    getKey() { return request.getKey(); }
}

//////////////////////////////////////////////////////////////////////////////
