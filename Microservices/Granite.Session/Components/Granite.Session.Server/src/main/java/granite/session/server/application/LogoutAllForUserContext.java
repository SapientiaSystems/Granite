//////////////////////////////////////////////////////////////////////////////
// LogoutAllForUserContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.LogoutAllForUserRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public
class LogoutAllForUserContext
{
    private LogoutAllForUserRequest request;
    private List<Session>           loaded;
    private Set<Long>               destroyed;

    public
    LogoutAllForUserContext(LogoutAllForUserRequest req)
    {
        request   = req;
        loaded    = Collections.emptyList();
        destroyed = new TreeSet<>();
    }

    public LogoutAllForUserContext
    setLoaded(List<Session> value)
    {
        loaded = value == null ? new ArrayList<>() : value;
        return this;
    }

    public LogoutAllForUserContext
    setDestroyed(Set<Long> value)
    {
        destroyed = value == null ? new TreeSet<>() : value;
        return this;
    }

    public LogoutAllForUserRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public List<Session>
    getLoaded() { return loaded; }

    public Set<Long>
    getDestroyed() { return destroyed; }
}

//////////////////////////////////////////////////////////////////////////////
