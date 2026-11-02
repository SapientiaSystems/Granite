//////////////////////////////////////////////////////////////////////////////
// SessionsInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

import java.util.ArrayList;
import java.util.List;

public
class SessionsInput
    extends FrontEndInput
{
    private List<Long> sessionIdsToClose;

    public
    SessionsInput()
    {
        super();
        sessionIdsToClose = new ArrayList<>();
    }

    @Override
    public SessionsInput
    setUserId(Long id)
    {
        return (SessionsInput)super.setUserId(id);
    }

    @Override
    public SessionsInput
    setSource(String src)
    {
        return (SessionsInput)super.setSource(src);
    }

    @Override
    public SessionsInput
    setDestination(String dest)
    {
        return (SessionsInput)super.setDestination(dest);
    }

    @Override
    public SessionsInput
    setCommand(String cmd)
    {
        return (SessionsInput)super.setCommand(cmd);
    }

    public SessionsInput
    setSessionIdsToClose(List<Long> ids)
    {
        sessionIdsToClose = ids;
        return this;
    }

    public List<Long>
    getSessionIdsToClose() { return sessionIdsToClose; }
}

//////////////////////////////////////////////////////////////////////////////
