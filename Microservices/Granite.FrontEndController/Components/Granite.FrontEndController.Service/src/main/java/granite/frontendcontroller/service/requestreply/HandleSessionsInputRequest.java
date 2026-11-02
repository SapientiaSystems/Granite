//////////////////////////////////////////////////////////////////////////////
// HandleSessionsInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleSessionsInputRequest
    extends HandleInputRequest<SessionsInput>
{
    public
    HandleSessionsInputRequest()
    {
        super();
    }

    @Override
    public HandleSessionsInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleSessionsInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleSessionsInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleSessionsInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSessionsInputRequest
    addHeader(String name,Object value)
    {
        return (HandleSessionsInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleSessionsInputRequest
    clearHeaders()
    {
        return (HandleSessionsInputRequest)super.clearHeaders();
    }

    @Override
    public HandleSessionsInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleSessionsInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleSessionsInputRequest
    clearCookies()
    {
        return (HandleSessionsInputRequest)super.clearCookies();
    }

    @Override
    public HandleSessionsInputRequest
    setInput(SessionsInput input)
    {
        return (HandleSessionsInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
