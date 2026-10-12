//////////////////////////////////////////////////////////////////////////////
// RefreshContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.RefreshRequest;

public
class RefreshContext
{
    private RefreshRequest request;
    private Session        loaded;
    private boolean        notFound;
    private boolean        expired;
    private String         jwt;

    public
    RefreshContext(RefreshRequest req)
    {
        request  = req;
        loaded   = null;
        notFound = false;
        expired  = false;
        jwt      = null;
    }

    public RefreshContext
    setLoaded(Session value)
    {
        loaded = value;
        return this;
    }

    public RefreshContext
    setNotFound(boolean value)
    {
        notFound = value;
        return this;
    }

    public RefreshContext
    setExpired(boolean value)
    {
        expired = value;
        return this;
    }

    public RefreshContext
    setJwt(String value)
    {
        jwt = value;
        return this;
    }

    public RefreshRequest
    getRequest() { return request; }

    public Long
    getSessionId() { return request.getSessionId(); }

    public Session
    getLoaded() { return loaded; }

    public boolean
    isNotFound() { return notFound; }

    public boolean
    isExpired() { return expired; }

    public String
    getJwt() { return jwt; }
}

//////////////////////////////////////////////////////////////////////////////
