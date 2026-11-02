//////////////////////////////////////////////////////////////////////////////
// GetFeedbackReviewOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetFeedbackReviewOutputRequest
    extends GetOutputRequest
{
    public
    GetFeedbackReviewOutputRequest()
    {
        super();
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetFeedbackReviewOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetFeedbackReviewOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    addHeader(String name,Object value)
    {
        return (GetFeedbackReviewOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    clearHeaders()
    {
        return (GetFeedbackReviewOutputRequest)super.clearHeaders();
    }

    @Override
    public GetFeedbackReviewOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetFeedbackReviewOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    clearCookies()
    {
        return (GetFeedbackReviewOutputRequest)super.clearCookies();
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setUserId(Long id)
    {
        return (GetFeedbackReviewOutputRequest)super.setUserId(id);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setSource(String s)
    {
        return (GetFeedbackReviewOutputRequest)super.setSource(s);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setCommand(String s)
    {
        return (GetFeedbackReviewOutputRequest)super.setCommand(s);
    }

    @Override
    public GetFeedbackReviewOutputRequest
    setDestination(String d)
    {
        return (GetFeedbackReviewOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
