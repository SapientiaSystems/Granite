//////////////////////////////////////////////////////////////////////////////
// ChangePasswordContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import granite.session.server.domain.Session;
import granite.session.service.requestreply.ChangePasswordRequest;

import java.util.ArrayList;
import java.util.List;

public
class ChangePasswordContext
{
    private ChangePasswordRequest request;
    private Session               loaded;
    private String                failureMessage;
    private List<Session>         closed;

    public
    ChangePasswordContext(ChangePasswordRequest req)
    {
        request        = req;
        loaded         = null;
        failureMessage = null;
        closed         = new ArrayList<>();
    }

    public ChangePasswordContext
    setLoaded(Session value)
    {
        loaded = value;
        return this;
    }

    public ChangePasswordContext
    setFailureMessage(String value)
    {
        failureMessage = value;
        return this;
    }

    public ChangePasswordContext
    setClosed(List<Session> value)
    {
        closed = value == null ? new ArrayList<>() : value;
        return this;
    }

    public ChangePasswordRequest
    getRequest() { return request; }

    public Long
    getSessionId() { return request.getSessionId(); }

    public Session
    getLoaded() { return loaded; }

    public String
    getFailureMessage() { return failureMessage; }

    public boolean
    isRefused() { return failureMessage != null; }

    public List<Session>
    getClosed() { return closed; }
}

//////////////////////////////////////////////////////////////////////////////
