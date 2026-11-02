//////////////////////////////////////////////////////////////////////////////
// GetAgentOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetAgentOutputRequest
    extends GetOutputRequest
{
    public
    GetAgentOutputRequest()
    {
        super();
    }

    @Override
    public GetAgentOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetAgentOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetAgentOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetAgentOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetAgentOutputRequest
    addHeader(String name,Object value)
    {
        return (GetAgentOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetAgentOutputRequest
    clearHeaders()
    {
        return (GetAgentOutputRequest)super.clearHeaders();
    }

    @Override
    public GetAgentOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetAgentOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetAgentOutputRequest
    clearCookies()
    {
        return (GetAgentOutputRequest)super.clearCookies();
    }

    @Override
    public GetAgentOutputRequest
    setUserId(Long id)
    {
        return (GetAgentOutputRequest)super.setUserId(id);
    }

    @Override
    public GetAgentOutputRequest
    setSource(String s)
    {
        return (GetAgentOutputRequest)super.setSource(s);
    }

    @Override
    public GetAgentOutputRequest
    setCommand(String s)
    {
        return (GetAgentOutputRequest)super.setCommand(s);
    }

    @Override
    public GetAgentOutputRequest
    setDestination(String d)
    {
        return (GetAgentOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
