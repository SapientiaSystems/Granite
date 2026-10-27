//////////////////////////////////////////////////////////////////////////////
// MergeUserValuesContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.application;

import granite.user.server.domain.User;
import granite.user.service.requestreply.MergeUserValuesRequest;

import java.util.Map;
import java.util.Optional;

public
class MergeUserValuesContext
{
    private MergeUserValuesRequest request;
    private Optional<User>         mergedUser;

    public
    MergeUserValuesContext(MergeUserValuesRequest req)
    {
        request = req;
        mergedUser = Optional.empty();
    }

    public MergeUserValuesContext
    setMergedUser(Optional<User> merged)
    {
        mergedUser = merged;
        return this;
    }

    public MergeUserValuesContext
    setMergedUser(User merged)
    {
        return setMergedUser(Optional.of(merged));
    }

    public MergeUserValuesRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public Map<String,Object>
    getValues() { return request.getValues(); }

    public Optional<User>
    getMergedUser() { return mergedUser; }
}

//////////////////////////////////////////////////////////////////////////////
