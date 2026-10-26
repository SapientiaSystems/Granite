//////////////////////////////////////////////////////////////////////////////
// HandleSessionInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleSessionInputRequest
    extends HandleInputRequest<SessionInput>
{
    public
    HandleSessionInputRequest()
    {
        super();
    }

    @Override
    public HandleSessionInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleSessionInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleSessionInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleSessionInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSessionInputRequest
    addHeader(String name,Object value)
    {
        return (HandleSessionInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleSessionInputRequest
    clearHeaders()
    {
        return (HandleSessionInputRequest)super.clearHeaders();
    }

    @Override
    public HandleSessionInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleSessionInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleSessionInputRequest
    clearCookies()
    {
        return (HandleSessionInputRequest)super.clearCookies();
    }

    @Override
    public HandleSessionInputRequest
    setInput(SessionInput input)
    {
        return (HandleSessionInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
