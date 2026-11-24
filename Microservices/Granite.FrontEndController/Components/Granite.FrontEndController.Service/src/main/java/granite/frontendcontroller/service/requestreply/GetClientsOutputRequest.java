//////////////////////////////////////////////////////////////////////////////
// GetClientsOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetClientsOutputRequest
    extends GetOutputRequest
{
    public
    GetClientsOutputRequest()
    {
        super();
    }

    @Override
    public GetClientsOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetClientsOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetClientsOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetClientsOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetClientsOutputRequest
    addHeader(String name,Object value)
    {
        return (GetClientsOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetClientsOutputRequest
    clearHeaders()
    {
        return (GetClientsOutputRequest)super.clearHeaders();
    }

    @Override
    public GetClientsOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetClientsOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetClientsOutputRequest
    clearCookies()
    {
        return (GetClientsOutputRequest)super.clearCookies();
    }

    @Override
    public GetClientsOutputRequest
    setUserId(Long id)
    {
        return (GetClientsOutputRequest)super.setUserId(id);
    }

    @Override
    public GetClientsOutputRequest
    setSource(String s)
    {
        return (GetClientsOutputRequest)super.setSource(s);
    }

    @Override
    public GetClientsOutputRequest
    setCommand(String s)
    {
        return (GetClientsOutputRequest)super.setCommand(s);
    }

    @Override
    public GetClientsOutputRequest
    setDestination(String d)
    {
        return (GetClientsOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
