//////////////////////////////////////////////////////////////////////////////
// UpdateFeedbacksRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class UpdateFeedbacksRequest
    extends AbstractServiceRequest
{
    private List<UpdateFeedbackData> feedback;

    public
    UpdateFeedbacksRequest()
    {
        super();
        feedback = new ArrayList<>();
    }

    @Override
    public UpdateFeedbacksRequest
    setRequestId(UUID requestId)
    {
        return (UpdateFeedbacksRequest)super.setRequestId(requestId);
    }

    @Override
    public UpdateFeedbacksRequest
    setTimestamp(Instant timestamp)
    {
        return (UpdateFeedbacksRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateFeedbacksRequest
    addHeader(String name,Object value)
    {
        return (UpdateFeedbacksRequest)super.addHeader(name,value);
    }

    @Override
    public UpdateFeedbacksRequest
    clearHeaders()
    {
        return (UpdateFeedbacksRequest)super.clearHeaders();
    }

    @Override
    public UpdateFeedbacksRequest
    addCookie(Cookie cookie)
    {
        return (UpdateFeedbacksRequest)super.addCookie(cookie);
    }

    @Override
    public UpdateFeedbacksRequest
    clearCookies()
    {
        return (UpdateFeedbacksRequest)super.clearCookies();
    }

    public UpdateFeedbacksRequest
    setFeedback(List<UpdateFeedbackData> value)
    {
        feedback = value;
        return this;
    }

    public List<UpdateFeedbackData>
    getFeedback() { return feedback; }
}

//////////////////////////////////////////////////////////////////////////////
