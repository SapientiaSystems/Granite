//////////////////////////////////////////////////////////////////////////////
// HandlePreferencesInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandlePreferencesInputRequest
    extends HandleInputRequest<PreferencesInput>
{
    public
    HandlePreferencesInputRequest()
    {
        super();
    }

    @Override
    public HandlePreferencesInputRequest
    setRequestId(UUID requestId)
    {
        return (HandlePreferencesInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandlePreferencesInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandlePreferencesInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandlePreferencesInputRequest
    addHeader(String name,Object value)
    {
        return (HandlePreferencesInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandlePreferencesInputRequest
    clearHeaders()
    {
        return (HandlePreferencesInputRequest)super.clearHeaders();
    }

    @Override
    public HandlePreferencesInputRequest
    addCookie(Cookie cookie)
    {
        return (HandlePreferencesInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandlePreferencesInputRequest
    clearCookies()
    {
        return (HandlePreferencesInputRequest)super.clearCookies();
    }

    @Override
    public HandlePreferencesInputRequest
    setInput(PreferencesInput input)
    {
        return (HandlePreferencesInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
