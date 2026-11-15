//////////////////////////////////////////////////////////////////////////////
// HandleOwnedClientSecretInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleOwnedClientSecretInputRequest
    extends HandleInputRequest<OwnedClientSecretInput>
{
    public
    HandleOwnedClientSecretInputRequest()
    {
        super();
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleOwnedClientSecretInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleOwnedClientSecretInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    addHeader(String name,Object value)
    {
        return (HandleOwnedClientSecretInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    clearHeaders()
    {
        return (HandleOwnedClientSecretInputRequest)super.clearHeaders();
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleOwnedClientSecretInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    clearCookies()
    {
        return (HandleOwnedClientSecretInputRequest)super.clearCookies();
    }

    @Override
    public HandleOwnedClientSecretInputRequest
    setInput(OwnedClientSecretInput input)
    {
        return (HandleOwnedClientSecretInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
