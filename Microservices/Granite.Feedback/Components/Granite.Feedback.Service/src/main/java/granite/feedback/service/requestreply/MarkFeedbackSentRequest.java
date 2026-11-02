//////////////////////////////////////////////////////////////////////////////
// MarkFeedbackSentRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkFeedbackSentRequest
    extends AbstractServiceRequest
{
    private Set<Long> feedbackIds;

    public
    MarkFeedbackSentRequest()
    {
        super();
        feedbackIds = new TreeSet<>();
    }

    @Override
    public MarkFeedbackSentRequest
    setRequestId(UUID requestId)
    {
        return (MarkFeedbackSentRequest)super.setRequestId(requestId);
    }

    @Override
    public MarkFeedbackSentRequest
    setTimestamp(Instant timestamp)
    {
        return (MarkFeedbackSentRequest)super.setTimestamp(timestamp);
    }

    @Override
    public MarkFeedbackSentRequest
    addHeader(String name,Object value)
    {
        return (MarkFeedbackSentRequest)super.addHeader(name,value);
    }

    @Override
    public MarkFeedbackSentRequest
    clearHeaders()
    {
        return (MarkFeedbackSentRequest)super.clearHeaders();
    }

    @Override
    public MarkFeedbackSentRequest
    addCookie(Cookie cookie)
    {
        return (MarkFeedbackSentRequest)super.addCookie(cookie);
    }

    @Override
    public MarkFeedbackSentRequest
    clearCookies()
    {
        return (MarkFeedbackSentRequest)super.clearCookies();
    }

    public MarkFeedbackSentRequest
    setFeedbackIds(Set<Long> value)
    {
        feedbackIds = value;
        return this;
    }

    public Set<Long>
    getFeedbackIds() { return feedbackIds; }
}

//////////////////////////////////////////////////////////////////////////////
