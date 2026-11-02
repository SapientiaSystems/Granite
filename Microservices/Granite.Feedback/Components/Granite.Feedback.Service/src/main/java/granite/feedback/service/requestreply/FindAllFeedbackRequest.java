//////////////////////////////////////////////////////////////////////////////
// FindAllFeedbackRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindAllFeedbackRequest
    extends AbstractServiceRequest
{
    private Set<FeedbackStatus> statuses;

    public
    FindAllFeedbackRequest()
    {
        super();
        statuses = new TreeSet<>();
    }

    @Override
    public FindAllFeedbackRequest
    setRequestId(UUID requestId)
    {
        return (FindAllFeedbackRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAllFeedbackRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAllFeedbackRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllFeedbackRequest
    addHeader(String name,Object value)
    {
        return (FindAllFeedbackRequest)super.addHeader(name,value);
    }

    @Override
    public FindAllFeedbackRequest
    clearHeaders()
    {
        return (FindAllFeedbackRequest)super.clearHeaders();
    }

    @Override
    public FindAllFeedbackRequest
    addCookie(Cookie cookie)
    {
        return (FindAllFeedbackRequest)super.addCookie(cookie);
    }

    @Override
    public FindAllFeedbackRequest
    clearCookies()
    {
        return (FindAllFeedbackRequest)super.clearCookies();
    }

    public FindAllFeedbackRequest
    setStatuses(Set<FeedbackStatus> value)
    {
        statuses = value;
        return this;
    }

    public Set<FeedbackStatus>
    getStatuses() { return statuses; }
}

//////////////////////////////////////////////////////////////////////////////
