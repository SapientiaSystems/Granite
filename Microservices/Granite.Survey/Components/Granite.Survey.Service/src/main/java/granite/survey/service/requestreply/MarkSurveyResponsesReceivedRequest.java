//////////////////////////////////////////////////////////////////////////////
// MarkSurveyResponsesReceivedRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkSurveyResponsesReceivedRequest
    extends AbstractServiceRequest
{
    private Set<Long> responseIds;

    public
    MarkSurveyResponsesReceivedRequest()
    {
        super();
        responseIds = new TreeSet<>();
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    setRequestId(UUID requestId)
    {
        return (MarkSurveyResponsesReceivedRequest)super.setRequestId(requestId);
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    setTimestamp(Instant timestamp)
    {
        return (MarkSurveyResponsesReceivedRequest)super.setTimestamp(timestamp);
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    addHeader(String name,Object value)
    {
        return (MarkSurveyResponsesReceivedRequest)super.addHeader(name,value);
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    clearHeaders()
    {
        return (MarkSurveyResponsesReceivedRequest)super.clearHeaders();
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    addCookie(Cookie cookie)
    {
        return (MarkSurveyResponsesReceivedRequest)super.addCookie(cookie);
    }

    @Override
    public MarkSurveyResponsesReceivedRequest
    clearCookies()
    {
        return (MarkSurveyResponsesReceivedRequest)super.clearCookies();
    }

    public MarkSurveyResponsesReceivedRequest
    setResponseIds(Set<Long> value)
    {
        responseIds = value;
        return this;
    }

    public Set<Long>
    getResponseIds() { return responseIds; }
}

//////////////////////////////////////////////////////////////////////////////
