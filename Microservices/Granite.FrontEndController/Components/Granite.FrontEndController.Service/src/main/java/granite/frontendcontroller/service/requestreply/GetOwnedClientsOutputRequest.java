//////////////////////////////////////////////////////////////////////////////
// GetOwnedClientsOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetOwnedClientsOutputRequest
    extends GetOutputRequest
{
    public
    GetOwnedClientsOutputRequest()
    {
        super();
    }

    @Override
    public GetOwnedClientsOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetOwnedClientsOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetOwnedClientsOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetOwnedClientsOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetOwnedClientsOutputRequest
    addHeader(String name,Object value)
    {
        return (GetOwnedClientsOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetOwnedClientsOutputRequest
    clearHeaders()
    {
        return (GetOwnedClientsOutputRequest)super.clearHeaders();
    }

    @Override
    public GetOwnedClientsOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetOwnedClientsOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetOwnedClientsOutputRequest
    clearCookies()
    {
        return (GetOwnedClientsOutputRequest)super.clearCookies();
    }

    @Override
    public GetOwnedClientsOutputRequest
    setUserId(Long id)
    {
        return (GetOwnedClientsOutputRequest)super.setUserId(id);
    }

    @Override
    public GetOwnedClientsOutputRequest
    setSource(String s)
    {
        return (GetOwnedClientsOutputRequest)super.setSource(s);
    }

    @Override
    public GetOwnedClientsOutputRequest
    setCommand(String s)
    {
        return (GetOwnedClientsOutputRequest)super.setCommand(s);
    }

    @Override
    public GetOwnedClientsOutputRequest
    setDestination(String d)
    {
        return (GetOwnedClientsOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
