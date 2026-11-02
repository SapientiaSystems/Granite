//////////////////////////////////////////////////////////////////////////////
// FindFeedbackByIdsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindFeedbackByIdsRequest
    extends AbstractServiceRequest
{
    private Set<Long> feedbackIds;

    public
    FindFeedbackByIdsRequest()
    {
        super();
        feedbackIds = new TreeSet<>();
    }

    @Override
    public FindFeedbackByIdsRequest
    setRequestId(UUID requestId)
    {
        return (FindFeedbackByIdsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindFeedbackByIdsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindFeedbackByIdsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindFeedbackByIdsRequest
    addHeader(String name,Object value)
    {
        return (FindFeedbackByIdsRequest)super.addHeader(name,value);
    }

    @Override
    public FindFeedbackByIdsRequest
    clearHeaders()
    {
        return (FindFeedbackByIdsRequest)super.clearHeaders();
    }

    @Override
    public FindFeedbackByIdsRequest
    addCookie(Cookie cookie)
    {
        return (FindFeedbackByIdsRequest)super.addCookie(cookie);
    }

    @Override
    public FindFeedbackByIdsRequest
    clearCookies()
    {
        return (FindFeedbackByIdsRequest)super.clearCookies();
    }

    public FindFeedbackByIdsRequest
    setFeedbackIds(Set<Long> value)
    {
        feedbackIds = value;
        return this;
    }

    public Set<Long>
    getFeedbackIds() { return feedbackIds; }
}

//////////////////////////////////////////////////////////////////////////////
