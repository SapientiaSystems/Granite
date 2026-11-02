//////////////////////////////////////////////////////////////////////////////
// FindSessionsContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.FindSessionsRequest;

import java.util.List;

public
class FindSessionsContext
{
    private FindSessionsRequest request;
    private List<Session>       found;

    public
    FindSessionsContext(FindSessionsRequest req)
    {
        request = req;
        found   = List.of();
    }

    public FindSessionsContext
    setFound(List<Session> value)
    {
        found = value;
        return this;
    }

    public FindSessionsRequest
    getRequest() { return request; }

    public boolean
    isIncludeClosed() { return request.isIncludeClosed(); }

    public List<Session>
    getFound() { return found; }
}

//////////////////////////////////////////////////////////////////////////////
