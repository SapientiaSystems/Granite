//////////////////////////////////////////////////////////////////////////////
// SessionsOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndOutput;

import java.util.ArrayList;
import java.util.List;

public
class SessionsOutput
    extends FrontEndOutput
{
    private List<SessionRowData> sessions;
    private Long                 currentSessionId;
    private List<String>         failureMessages;

    public
    SessionsOutput()
    {
        super();
        sessions         = new ArrayList<>();
        currentSessionId = null;
        failureMessages  = new ArrayList<>();
    }

    @Override
    public SessionsOutput
    setUserId(Long id)
    {
        return (SessionsOutput)super.setUserId(id);
    }

    @Override
    public SessionsOutput
    setSource(String src)
    {
        return (SessionsOutput)super.setSource(src);
    }

    @Override
    public SessionsOutput
    setDestination(String dest)
    {
        return (SessionsOutput)super.setDestination(dest);
    }

    @Override
    public SessionsOutput
    setCommand(String cmd)
    {
        return (SessionsOutput)super.setCommand(cmd);
    }

    public SessionsOutput
    setSessions(List<SessionRowData> rows)
    {
        sessions = rows;
        return this;
    }

    public SessionsOutput
    setCurrentSessionId(Long id)
    {
        currentSessionId = id;
        return this;
    }

    public SessionsOutput
    setFailureMessages(List<String> messages)
    {
        failureMessages = messages;
        return this;
    }

    public List<SessionRowData>
    getSessions() { return sessions; }

    public Long
    getCurrentSessionId() { return currentSessionId; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
