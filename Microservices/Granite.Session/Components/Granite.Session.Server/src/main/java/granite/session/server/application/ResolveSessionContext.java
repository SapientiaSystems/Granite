//////////////////////////////////////////////////////////////////////////////
// ResolveSessionContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.ResolveSessionRequest;

public
class ResolveSessionContext
{
    private ResolveSessionRequest request;
    private Session               loaded;
    private boolean               rejected;
    private boolean               closed;
    private boolean               renewed;
    private boolean               activityRecorded;

    public
    ResolveSessionContext(ResolveSessionRequest req)
    {
        request          = req;
        loaded           = null;
        rejected         = false;
        closed           = false;
        renewed          = false;
        activityRecorded = false;
    }

    public ResolveSessionContext
    setLoaded(Session value)
    {
        loaded = value;
        return this;
    }

    public ResolveSessionContext
    setRejected(boolean value)
    {
        rejected = value;
        return this;
    }

    public ResolveSessionContext
    setClosed(boolean value)
    {
        closed = value;
        return this;
    }

    public ResolveSessionContext
    setRenewed(boolean value)
    {
        renewed = value;
        return this;
    }

    public ResolveSessionContext
    setActivityRecorded(boolean value)
    {
        activityRecorded = value;
        return this;
    }

    public ResolveSessionRequest
    getRequest() { return request; }

    public String
    getHandle() { return request.getHandle(); }

    public Session
    getLoaded() { return loaded; }

    public boolean
    isRejected() { return rejected || loaded == null; }

    public boolean
    isClosed() { return closed; }

    public boolean
    isRenewed() { return renewed; }

    public boolean
    isActivityRecorded() { return activityRecorded; }

    public boolean
    isChanged() { return closed || renewed || activityRecorded; }
}

//////////////////////////////////////////////////////////////////////////////
