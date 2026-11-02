//////////////////////////////////////////////////////////////////////////////
// MarkSurveyResponsesSentRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkSurveyResponsesSentRequest
    extends AbstractServiceRequest
{
    private Set<Long> responseIds;

    public
    MarkSurveyResponsesSentRequest()
    {
        super();
        responseIds = new TreeSet<>();
    }

    @Override
    public MarkSurveyResponsesSentRequest
    setRequestId(UUID requestId)
    {
        return (MarkSurveyResponsesSentRequest)super.setRequestId(requestId);
    }

    @Override
    public MarkSurveyResponsesSentRequest
    setTimestamp(Instant timestamp)
    {
        return (MarkSurveyResponsesSentRequest)super.setTimestamp(timestamp);
    }

    @Override
    public MarkSurveyResponsesSentRequest
    addHeader(String name,Object value)
    {
        return (MarkSurveyResponsesSentRequest)super.addHeader(name,value);
    }

    @Override
    public MarkSurveyResponsesSentRequest
    clearHeaders()
    {
        return (MarkSurveyResponsesSentRequest)super.clearHeaders();
    }

    @Override
    public MarkSurveyResponsesSentRequest
    addCookie(Cookie cookie)
    {
        return (MarkSurveyResponsesSentRequest)super.addCookie(cookie);
    }

    @Override
    public MarkSurveyResponsesSentRequest
    clearCookies()
    {
        return (MarkSurveyResponsesSentRequest)super.clearCookies();
    }

    public MarkSurveyResponsesSentRequest
    setResponseIds(Set<Long> value)
    {
        responseIds = value;
        return this;
    }

    public Set<Long>
    getResponseIds() { return responseIds; }
}

//////////////////////////////////////////////////////////////////////////////
