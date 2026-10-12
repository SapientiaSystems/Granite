//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeySetContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.FindSessionsByKeySetRequest;
import granite.session.service.requestreply.SessionKeySet;

import java.util.List;

public
class FindSessionsByKeySetContext
{
    private FindSessionsByKeySetRequest request;
    private List<Session>               found;

    public
    FindSessionsByKeySetContext(FindSessionsByKeySetRequest req)
    {
        request = req;
        found   = List.of();
    }

    public FindSessionsByKeySetContext
    setFound(List<Session> value)
    {
        found = value;
        return this;
    }

    public FindSessionsByKeySetRequest
    getRequest() { return request; }

    public SessionKeySet
    getKeySet() { return request.getKeySet(); }

    public List<Session>
    getFound() { return found; }
}

//////////////////////////////////////////////////////////////////////////////
