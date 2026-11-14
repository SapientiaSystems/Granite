//////////////////////////////////////////////////////////////////////////////
// HandleClientsInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleClientsInputRequest
    extends HandleInputRequest<ClientsInput>
{
    public
    HandleClientsInputRequest()
    {
        super();
    }

    @Override
    public HandleClientsInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleClientsInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleClientsInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleClientsInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleClientsInputRequest
    addHeader(String name,Object value)
    {
        return (HandleClientsInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleClientsInputRequest
    clearHeaders()
    {
        return (HandleClientsInputRequest)super.clearHeaders();
    }

    @Override
    public HandleClientsInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleClientsInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleClientsInputRequest
    clearCookies()
    {
        return (HandleClientsInputRequest)super.clearCookies();
    }

    @Override
    public HandleClientsInputRequest
    setInput(ClientsInput input)
    {
        return (HandleClientsInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
