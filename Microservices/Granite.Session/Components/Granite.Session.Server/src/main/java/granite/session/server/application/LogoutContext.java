//////////////////////////////////////////////////////////////////////////////
// LogoutContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.LogoutRequest;

public
class LogoutContext
{
    private LogoutRequest request;
    private Session       loaded;
    private boolean       notFound;

    public
    LogoutContext(LogoutRequest req)
    {
        request  = req;
        loaded   = null;
        notFound = false;
    }

    public LogoutContext
    setLoaded(Session value)
    {
        loaded = value;
        return this;
    }

    public LogoutContext
    setNotFound(boolean value)
    {
        notFound = value;
        return this;
    }

    public LogoutRequest
    getRequest() { return request; }

    public Long
    getSessionId() { return request.getSessionId(); }

    public Session
    getLoaded() { return loaded; }

    public boolean
    isNotFound() { return notFound; }
}

//////////////////////////////////////////////////////////////////////////////
