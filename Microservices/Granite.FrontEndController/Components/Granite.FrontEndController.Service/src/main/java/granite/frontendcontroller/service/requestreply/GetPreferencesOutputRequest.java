//////////////////////////////////////////////////////////////////////////////
// GetPreferencesOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetPreferencesOutputRequest
    extends GetOutputRequest
{
    public
    GetPreferencesOutputRequest()
    {
        super();
    }

    @Override
    public GetPreferencesOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetPreferencesOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetPreferencesOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetPreferencesOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetPreferencesOutputRequest
    addHeader(String name,Object value)
    {
        return (GetPreferencesOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetPreferencesOutputRequest
    clearHeaders()
    {
        return (GetPreferencesOutputRequest)super.clearHeaders();
    }

    @Override
    public GetPreferencesOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetPreferencesOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetPreferencesOutputRequest
    clearCookies()
    {
        return (GetPreferencesOutputRequest)super.clearCookies();
    }

    @Override
    public GetPreferencesOutputRequest
    setUserId(Long id)
    {
        return (GetPreferencesOutputRequest)super.setUserId(id);
    }

    @Override
    public GetPreferencesOutputRequest
    setSource(String s)
    {
        return (GetPreferencesOutputRequest)super.setSource(s);
    }

    @Override
    public GetPreferencesOutputRequest
    setCommand(String s)
    {
        return (GetPreferencesOutputRequest)super.setCommand(s);
    }

    @Override
    public GetPreferencesOutputRequest
    setDestination(String d)
    {
        return (GetPreferencesOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
