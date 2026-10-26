//////////////////////////////////////////////////////////////////////////////
// GetUsersOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetUsersOutputRequest
    extends GetOutputRequest
{
    public
    GetUsersOutputRequest()
    {
        super();
    }

    @Override
    public GetUsersOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetUsersOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetUsersOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetUsersOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetUsersOutputRequest
    addHeader(String name,Object value)
    {
        return (GetUsersOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetUsersOutputRequest
    clearHeaders()
    {
        return (GetUsersOutputRequest)super.clearHeaders();
    }

    @Override
    public GetUsersOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetUsersOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetUsersOutputRequest
    clearCookies()
    {
        return (GetUsersOutputRequest)super.clearCookies();
    }

    @Override
    public GetUsersOutputRequest
    setUserId(Long id)
    {
        return (GetUsersOutputRequest)super.setUserId(id);
    }

    @Override
    public GetUsersOutputRequest
    setSource(String s)
    {
        return (GetUsersOutputRequest)super.setSource(s);
    }

    @Override
    public GetUsersOutputRequest
    setCommand(String s)
    {
        return (GetUsersOutputRequest)super.setCommand(s);
    }

    @Override
    public GetUsersOutputRequest
    setDestination(String d)
    {
        return (GetUsersOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
