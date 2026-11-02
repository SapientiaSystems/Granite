//////////////////////////////////////////////////////////////////////////////
// GetSessionsOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetSessionsOutputRequest
    extends GetOutputRequest
{
    public
    GetSessionsOutputRequest()
    {
        super();
    }

    @Override
    public GetSessionsOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetSessionsOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetSessionsOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetSessionsOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetSessionsOutputRequest
    addHeader(String name,Object value)
    {
        return (GetSessionsOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetSessionsOutputRequest
    clearHeaders()
    {
        return (GetSessionsOutputRequest)super.clearHeaders();
    }

    @Override
    public GetSessionsOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetSessionsOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetSessionsOutputRequest
    clearCookies()
    {
        return (GetSessionsOutputRequest)super.clearCookies();
    }

    @Override
    public GetSessionsOutputRequest
    setUserId(Long id)
    {
        return (GetSessionsOutputRequest)super.setUserId(id);
    }

    @Override
    public GetSessionsOutputRequest
    setSource(String s)
    {
        return (GetSessionsOutputRequest)super.setSource(s);
    }

    @Override
    public GetSessionsOutputRequest
    setCommand(String s)
    {
        return (GetSessionsOutputRequest)super.setCommand(s);
    }

    @Override
    public GetSessionsOutputRequest
    setDestination(String d)
    {
        return (GetSessionsOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
