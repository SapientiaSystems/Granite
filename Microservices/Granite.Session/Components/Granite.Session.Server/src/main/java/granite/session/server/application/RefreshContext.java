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
    private boolean        reused;
    private boolean        expired;
    private String         jwt;
    private String         refreshToken;

    public
    RefreshContext(RefreshRequest req)
    {
        request      = req;
        loaded       = null;
        notFound     = false;
        reused       = false;
        expired      = false;
        jwt          = null;
        refreshToken = null;
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
    setReused(boolean value)
    {
        reused = value;
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

    public RefreshContext
    setRefreshToken(String value)
    {
        refreshToken = value;
        return this;
    }

    public RefreshRequest
    getRequest() { return request; }

    public String
    getPresentedRefreshToken() { return request.getRefreshToken(); }

    public Session
    getLoaded() { return loaded; }

    public boolean
    isNotFound() { return notFound; }

    public boolean
    isReused() { return reused; }

    public boolean
    isExpired() { return expired; }

    public boolean
    isRejected() { return notFound || reused || expired || loaded == null; }

    public String
    getJwt() { return jwt; }

    public String
    getRefreshToken() { return refreshToken; }
}

//////////////////////////////////////////////////////////////////////////////
