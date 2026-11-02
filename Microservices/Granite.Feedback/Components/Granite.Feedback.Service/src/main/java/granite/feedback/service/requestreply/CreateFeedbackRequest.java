//////////////////////////////////////////////////////////////////////////////
// CreateFeedbackRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class CreateFeedbackRequest
    extends AbstractServiceRequest
{
    private CreateFeedbackData feedback;

    public
    CreateFeedbackRequest()
    {
        super();
        feedback = null;
    }

    @Override
    public CreateFeedbackRequest
    setRequestId(UUID requestId)
    {
        return (CreateFeedbackRequest)super.setRequestId(requestId);
    }

    @Override
    public CreateFeedbackRequest
    setTimestamp(Instant timestamp)
    {
        return (CreateFeedbackRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CreateFeedbackRequest
    addHeader(String name,Object value)
    {
        return (CreateFeedbackRequest)super.addHeader(name,value);
    }

    @Override
    public CreateFeedbackRequest
    clearHeaders()
    {
        return (CreateFeedbackRequest)super.clearHeaders();
    }

    @Override
    public CreateFeedbackRequest
    addCookie(Cookie cookie)
    {
        return (CreateFeedbackRequest)super.addCookie(cookie);
    }

    @Override
    public CreateFeedbackRequest
    clearCookies()
    {
        return (CreateFeedbackRequest)super.clearCookies();
    }

    public CreateFeedbackRequest
    setFeedback(CreateFeedbackData value)
    {
        feedback = value;
        return this;
    }

    public CreateFeedbackData
    getFeedback() { return feedback; }
}

//////////////////////////////////////////////////////////////////////////////
