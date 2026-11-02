//////////////////////////////////////////////////////////////////////////////
// HandleAgentInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleAgentInputRequest
    extends HandleInputRequest<AgentInput>
{
    public
    HandleAgentInputRequest()
    {
        super();
    }

    @Override
    public HandleAgentInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleAgentInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleAgentInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleAgentInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleAgentInputRequest
    addHeader(String name,Object value)
    {
        return (HandleAgentInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleAgentInputRequest
    clearHeaders()
    {
        return (HandleAgentInputRequest)super.clearHeaders();
    }

    @Override
    public HandleAgentInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleAgentInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleAgentInputRequest
    clearCookies()
    {
        return (HandleAgentInputRequest)super.clearCookies();
    }

    @Override
    public HandleAgentInputRequest
    setInput(AgentInput input)
    {
        return (HandleAgentInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
