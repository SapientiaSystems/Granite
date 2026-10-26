//////////////////////////////////////////////////////////////////////////////
// GetSessionOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetSessionOutputRequest
    extends GetOutputRequest
{
    public
    GetSessionOutputRequest()
    {
        super();
    }

    @Override
    public GetSessionOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetSessionOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetSessionOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetSessionOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetSessionOutputRequest
    addHeader(String name,Object value)
    {
        return (GetSessionOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetSessionOutputRequest
    clearHeaders()
    {
        return (GetSessionOutputRequest)super.clearHeaders();
    }

    @Override
    public GetSessionOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetSessionOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetSessionOutputRequest
    clearCookies()
    {
        return (GetSessionOutputRequest)super.clearCookies();
    }

    @Override
    public GetSessionOutputRequest
    setUserId(Long id)
    {
        return (GetSessionOutputRequest)super.setUserId(id);
    }

    @Override
    public GetSessionOutputRequest
    setSource(String s)
    {
        return (GetSessionOutputRequest)super.setSource(s);
    }

    @Override
    public GetSessionOutputRequest
    setCommand(String s)
    {
        return (GetSessionOutputRequest)super.setCommand(s);
    }

    @Override
    public GetSessionOutputRequest
    setDestination(String d)
    {
        return (GetSessionOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
