//////////////////////////////////////////////////////////////////////////////
// HandleFeedbackReviewInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleFeedbackReviewInputRequest
    extends HandleInputRequest<FeedbackReviewInput>
{
    public
    HandleFeedbackReviewInputRequest()
    {
        super();
    }

    @Override
    public HandleFeedbackReviewInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleFeedbackReviewInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleFeedbackReviewInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleFeedbackReviewInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleFeedbackReviewInputRequest
    addHeader(String name,Object value)
    {
        return (HandleFeedbackReviewInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleFeedbackReviewInputRequest
    clearHeaders()
    {
        return (HandleFeedbackReviewInputRequest)super.clearHeaders();
    }

    @Override
    public HandleFeedbackReviewInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleFeedbackReviewInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleFeedbackReviewInputRequest
    clearCookies()
    {
        return (HandleFeedbackReviewInputRequest)super.clearCookies();
    }

    @Override
    public HandleFeedbackReviewInputRequest
    setInput(FeedbackReviewInput input)
    {
        return (HandleFeedbackReviewInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
