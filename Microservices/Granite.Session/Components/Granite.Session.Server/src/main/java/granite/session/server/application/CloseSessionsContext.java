//////////////////////////////////////////////////////////////////////////////
// CloseSessionsContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.CloseSessionsRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class CloseSessionsContext
{
    private CloseSessionsRequest        request;
    private Map<Long,Optional<Session>> loaded;
    private List<Session>               closed;

    public
    CloseSessionsContext(CloseSessionsRequest req)
    {
        request = req;
        loaded  = new LinkedHashMap<>();
        closed  = new ArrayList<>();
    }

    public CloseSessionsContext
    setLoaded(Map<Long,Optional<Session>> value)
    {
        loaded = value == null ? new LinkedHashMap<>() : value;
        return this;
    }

    public CloseSessionsContext
    setClosed(List<Session> value)
    {
        closed = value == null ? new ArrayList<>() : value;
        return this;
    }

    public CloseSessionsRequest
    getRequest() { return request; }

    public Set<Long>
    getSessionIds() { return request.getSessionIds(); }

    public Map<Long,Optional<Session>>
    getLoaded() { return loaded; }

    public List<Session>
    getClosed() { return closed; }
}

//////////////////////////////////////////////////////////////////////////////
