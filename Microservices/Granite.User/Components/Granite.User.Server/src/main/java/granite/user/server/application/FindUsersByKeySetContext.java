//////////////////////////////////////////////////////////////////////////////
// FindUsersByKeySetContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.FindUsersByKeySetRequest;
import granite.user.service.requestreply.UserKeySet;

import java.util.List;

public
class FindUsersByKeySetContext
    extends AbstractFindUsersContext
{
    private FindUsersByKeySetRequest request;
    private UserKeySet foundKeys;
    private UserKeySet missingKeys;

    public
    FindUsersByKeySetContext(FindUsersByKeySetRequest request)
    {
        this.request = request;
    }

    @Override
    public FindUsersByKeySetContext
    setFoundUsers(List<User> found)
    {
        return (FindUsersByKeySetContext)super.setFoundUsers(found);
    }

    public FindUsersByKeySetContext
    setFoundKeys(UserKeySet foundKeys)
    {
        this.foundKeys = foundKeys;
        return this;
    }

    public FindUsersByKeySetContext
    setMissingKeys(UserKeySet missingKeys)
    {
        this.missingKeys = missingKeys;
        return this;
    }

    public FindUsersByKeySetRequest
    getRequest() { return request; }

    public UserKeySet
    getRequestedKeys() { return request.getKeys(); }

    public UserKeySet
    getFoundKeys() { return foundKeys; }

    public UserKeySet
    getMissingKeys() { return missingKeys; }
}

//////////////////////////////////////////////////////////////////////////////

