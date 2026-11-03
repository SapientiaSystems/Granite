//////////////////////////////////////////////////////////////////////////////
// GetHomeOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetHomeOutputRequest
    extends GetOutputRequest
{
    public
    GetHomeOutputRequest()
    {
        super();
    }

    @Override
    public GetHomeOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetHomeOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetHomeOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetHomeOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetHomeOutputRequest
    addHeader(String name,Object value)
    {
        return (GetHomeOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetHomeOutputRequest
    clearHeaders()
    {
        return (GetHomeOutputRequest)super.clearHeaders();
    }

    @Override
    public GetHomeOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetHomeOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetHomeOutputRequest
    clearCookies()
    {
        return (GetHomeOutputRequest)super.clearCookies();
    }

    @Override
    public GetHomeOutputRequest
    setUserId(Long id)
    {
        return (GetHomeOutputRequest)super.setUserId(id);
    }

    @Override
    public GetHomeOutputRequest
    setSource(String s)
    {
        return (GetHomeOutputRequest)super.setSource(s);
    }

    @Override
    public GetHomeOutputRequest
    setCommand(String s)
    {
        return (GetHomeOutputRequest)super.setCommand(s);
    }

    @Override
    public GetHomeOutputRequest
    setDestination(String d)
    {
        return (GetHomeOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
