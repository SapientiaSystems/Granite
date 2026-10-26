//////////////////////////////////////////////////////////////////////////////
// HandleUsersInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleUsersInputRequest
    extends HandleInputRequest<UsersInput>
{
    public
    HandleUsersInputRequest()
    {
        super();
    }

    @Override
    public HandleUsersInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleUsersInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleUsersInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleUsersInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleUsersInputRequest
    addHeader(String name,Object value)
    {
        return (HandleUsersInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleUsersInputRequest
    clearHeaders()
    {
        return (HandleUsersInputRequest)super.clearHeaders();
    }

    @Override
    public HandleUsersInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleUsersInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleUsersInputRequest
    clearCookies()
    {
        return (HandleUsersInputRequest)super.clearCookies();
    }

    @Override
    public HandleUsersInputRequest
    setInput(UsersInput input)
    {
        return (HandleUsersInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
