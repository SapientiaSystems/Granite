//////////////////////////////////////////////////////////////////////////////
// FindUsersByExternalIdentityContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.FindUsersByExternalIdentityRequest;

import java.util.List;

public
class FindUsersByExternalIdentityContext
    extends AbstractFindUsersContext
{
    private FindUsersByExternalIdentityRequest request;

    public
    FindUsersByExternalIdentityContext(FindUsersByExternalIdentityRequest request)
    {
        this.request = request;
    }

    @Override
    public FindUsersByExternalIdentityContext
    setFoundUsers(List<User> found)
    {
        return (FindUsersByExternalIdentityContext)super.setFoundUsers(found);
    }

    public FindUsersByExternalIdentityRequest
    getRequest() { return request; }

    public String
    getCompositeIdentity()
    {
        return request.getProvider() + ":" + request.getExternalId();
    }
}

//////////////////////////////////////////////////////////////////////////////
