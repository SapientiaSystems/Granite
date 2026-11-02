//////////////////////////////////////////////////////////////////////////////
// GetContentOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetContentOutputRequest
    extends GetOutputRequest
{
    public
    GetContentOutputRequest()
    {
        super();
    }

    @Override
    public GetContentOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetContentOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetContentOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetContentOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetContentOutputRequest
    addHeader(String name,Object value)
    {
        return (GetContentOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetContentOutputRequest
    clearHeaders()
    {
        return (GetContentOutputRequest)super.clearHeaders();
    }

    @Override
    public GetContentOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetContentOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetContentOutputRequest
    clearCookies()
    {
        return (GetContentOutputRequest)super.clearCookies();
    }

    @Override
    public GetContentOutputRequest
    setUserId(Long id)
    {
        return (GetContentOutputRequest)super.setUserId(id);
    }

    @Override
    public GetContentOutputRequest
    setSource(String s)
    {
        return (GetContentOutputRequest)super.setSource(s);
    }

    @Override
    public GetContentOutputRequest
    setCommand(String s)
    {
        return (GetContentOutputRequest)super.setCommand(s);
    }

    @Override
    public GetContentOutputRequest
    setDestination(String d)
    {
        return (GetContentOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
