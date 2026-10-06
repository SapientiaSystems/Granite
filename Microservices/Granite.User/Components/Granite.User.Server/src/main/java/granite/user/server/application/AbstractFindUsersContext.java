//////////////////////////////////////////////////////////////////////////////
// AbstractFindUsersContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;

import java.util.ArrayList;
import java.util.List;

public abstract
class AbstractFindUsersContext
    implements IFindUsersContext
{
    private List<User> foundUsers;

    protected
    AbstractFindUsersContext()
    {
        foundUsers = new ArrayList<>();
    }

    @Override
    public IFindUsersContext
    setFoundUsers(List<User> found)
    {
        foundUsers = found;
        return this;
    }

    @Override
    public List<User>
    getFoundUsers()
    {
        return foundUsers;
    }
}

//////////////////////////////////////////////////////////////////////////////
