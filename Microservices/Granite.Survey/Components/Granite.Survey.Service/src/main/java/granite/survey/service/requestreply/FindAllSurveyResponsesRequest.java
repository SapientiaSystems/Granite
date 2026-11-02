//////////////////////////////////////////////////////////////////////////////
// FindAllSurveyResponsesRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindAllSurveyResponsesRequest
    extends AbstractServiceRequest
{
    private Set<SurveyResponseStatus> statuses;

    public
    FindAllSurveyResponsesRequest()
    {
        super();
        statuses = new TreeSet<>();
    }

    @Override
    public FindAllSurveyResponsesRequest
    setRequestId(UUID requestId)
    {
        return (FindAllSurveyResponsesRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAllSurveyResponsesRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAllSurveyResponsesRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllSurveyResponsesRequest
    addHeader(String name,Object value)
    {
        return (FindAllSurveyResponsesRequest)super.addHeader(name,value);
    }

    @Override
    public FindAllSurveyResponsesRequest
    clearHeaders()
    {
        return (FindAllSurveyResponsesRequest)super.clearHeaders();
    }

    @Override
    public FindAllSurveyResponsesRequest
    addCookie(Cookie cookie)
    {
        return (FindAllSurveyResponsesRequest)super.addCookie(cookie);
    }

    @Override
    public FindAllSurveyResponsesRequest
    clearCookies()
    {
        return (FindAllSurveyResponsesRequest)super.clearCookies();
    }

    public FindAllSurveyResponsesRequest
    setStatuses(Set<SurveyResponseStatus> value)
    {
        statuses = value;
        return this;
    }

    public Set<SurveyResponseStatus>
    getStatuses() { return statuses; }
}

//////////////////////////////////////////////////////////////////////////////
