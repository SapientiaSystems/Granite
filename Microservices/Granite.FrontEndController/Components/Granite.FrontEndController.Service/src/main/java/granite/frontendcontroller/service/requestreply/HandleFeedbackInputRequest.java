//////////////////////////////////////////////////////////////////////////////
// HandleFeedbackInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleFeedbackInputRequest
    extends HandleInputRequest<FeedbackInput>
{
    public
    HandleFeedbackInputRequest()
    {
        super();
    }

    @Override
    public HandleFeedbackInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleFeedbackInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleFeedbackInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleFeedbackInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleFeedbackInputRequest
    addHeader(String name,Object value)
    {
        return (HandleFeedbackInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleFeedbackInputRequest
    clearHeaders()
    {
        return (HandleFeedbackInputRequest)super.clearHeaders();
    }

    @Override
    public HandleFeedbackInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleFeedbackInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleFeedbackInputRequest
    clearCookies()
    {
        return (HandleFeedbackInputRequest)super.clearCookies();
    }

    @Override
    public HandleFeedbackInputRequest
    setInput(FeedbackInput input)
    {
        return (HandleFeedbackInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
