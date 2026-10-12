//////////////////////////////////////////////////////////////////////////////
// FindSessionByUserIdAndDeviceIdContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.FindSessionByUserIdAndDeviceIdRequest;

public
class FindSessionByUserIdAndDeviceIdContext
{
    private FindSessionByUserIdAndDeviceIdRequest request;
    private Session                               found;

    public
    FindSessionByUserIdAndDeviceIdContext(FindSessionByUserIdAndDeviceIdRequest req)
    {
        request = req;
        found   = null;
    }

    public FindSessionByUserIdAndDeviceIdContext
    setFound(Session value)
    {
        found = value;
        return this;
    }

    public FindSessionByUserIdAndDeviceIdRequest
    getRequest() { return request; }

    public Long
    getUserId() { return request.getUserId(); }

    public String
    getDeviceId() { return request.getDeviceId(); }

    public Session
    getFound() { return found; }
}

//////////////////////////////////////////////////////////////////////////////
