//////////////////////////////////////////////////////////////////////////////
// HandleContentInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleContentInputRequest
    extends HandleInputRequest<ContentInput>
{
    public
    HandleContentInputRequest()
    {
        super();
    }

    @Override
    public HandleContentInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleContentInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleContentInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleContentInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleContentInputRequest
    addHeader(String name,Object value)
    {
        return (HandleContentInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleContentInputRequest
    clearHeaders()
    {
        return (HandleContentInputRequest)super.clearHeaders();
    }

    @Override
    public HandleContentInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleContentInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleContentInputRequest
    clearCookies()
    {
        return (HandleContentInputRequest)super.clearCookies();
    }

    @Override
    public HandleContentInputRequest
    setInput(ContentInput input)
    {
        return (HandleContentInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
