//////////////////////////////////////////////////////////////////////////////
// FindSessionsByKeyContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.FindSessionsByKeyRequest;
import granite.session.service.requestreply.SessionKey;

import java.util.List;

public
class FindSessionsByKeyContext
{
    private FindSessionsByKeyRequest request;
    private List<Session>            found;

    public
    FindSessionsByKeyContext(FindSessionsByKeyRequest req)
    {
        request = req;
        found   = List.of();
    }

    public FindSessionsByKeyContext
    setFound(List<Session> value)
    {
        found = value;
        return this;
    }

    public FindSessionsByKeyRequest
    getRequest() { return request; }

    public SessionKey
    getKey() { return request.getKey(); }

    public List<Session>
    getFound() { return found; }
}

//////////////////////////////////////////////////////////////////////////////
