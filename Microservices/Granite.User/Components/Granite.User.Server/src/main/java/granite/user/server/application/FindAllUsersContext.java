//////////////////////////////////////////////////////////////////////////////
// FindAllUsersContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.FindAllUsersRequest;

import java.util.List;

public
class FindAllUsersContext
    extends AbstractFindUsersContext
{
    private FindAllUsersRequest request;

    public
    FindAllUsersContext(FindAllUsersRequest request)
    {
        this.request = request;
    }

    @Override
    public FindAllUsersContext
    setFoundUsers(List<User> found)
    {
        return (FindAllUsersContext)super.setFoundUsers(found);
    }

    public FindAllUsersRequest
    getRequest() { return request; }
}

//////////////////////////////////////////////////////////////////////////////
