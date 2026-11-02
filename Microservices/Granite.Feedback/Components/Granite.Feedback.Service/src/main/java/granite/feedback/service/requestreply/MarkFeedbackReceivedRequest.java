//////////////////////////////////////////////////////////////////////////////
// MarkFeedbackReceivedRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkFeedbackReceivedRequest
    extends AbstractServiceRequest
{
    private Set<Long> feedbackIds;

    public
    MarkFeedbackReceivedRequest()
    {
        super();
        feedbackIds = new TreeSet<>();
    }

    @Override
    public MarkFeedbackReceivedRequest
    setRequestId(UUID requestId)
    {
        return (MarkFeedbackReceivedRequest)super.setRequestId(requestId);
    }

    @Override
    public MarkFeedbackReceivedRequest
    setTimestamp(Instant timestamp)
    {
        return (MarkFeedbackReceivedRequest)super.setTimestamp(timestamp);
    }

    @Override
    public MarkFeedbackReceivedRequest
    addHeader(String name,Object value)
    {
        return (MarkFeedbackReceivedRequest)super.addHeader(name,value);
    }

    @Override
    public MarkFeedbackReceivedRequest
    clearHeaders()
    {
        return (MarkFeedbackReceivedRequest)super.clearHeaders();
    }

    @Override
    public MarkFeedbackReceivedRequest
    addCookie(Cookie cookie)
    {
        return (MarkFeedbackReceivedRequest)super.addCookie(cookie);
    }

    @Override
    public MarkFeedbackReceivedRequest
    clearCookies()
    {
        return (MarkFeedbackReceivedRequest)super.clearCookies();
    }

    public MarkFeedbackReceivedRequest
    setFeedbackIds(Set<Long> value)
    {
        feedbackIds = value;
        return this;
    }

    public Set<Long>
    getFeedbackIds() { return feedbackIds; }
}

//////////////////////////////////////////////////////////////////////////////
