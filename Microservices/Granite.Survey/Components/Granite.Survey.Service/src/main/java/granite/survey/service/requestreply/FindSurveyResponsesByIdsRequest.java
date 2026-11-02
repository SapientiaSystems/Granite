//////////////////////////////////////////////////////////////////////////////
// FindSurveyResponsesByIdsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class FindSurveyResponsesByIdsRequest
    extends AbstractServiceRequest
{
    private Set<Long> responseIds;

    public
    FindSurveyResponsesByIdsRequest()
    {
        super();
        responseIds = new TreeSet<>();
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    setRequestId(UUID requestId)
    {
        return (FindSurveyResponsesByIdsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindSurveyResponsesByIdsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    addHeader(String name,Object value)
    {
        return (FindSurveyResponsesByIdsRequest)super.addHeader(name,value);
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    clearHeaders()
    {
        return (FindSurveyResponsesByIdsRequest)super.clearHeaders();
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    addCookie(Cookie cookie)
    {
        return (FindSurveyResponsesByIdsRequest)super.addCookie(cookie);
    }

    @Override
    public FindSurveyResponsesByIdsRequest
    clearCookies()
    {
        return (FindSurveyResponsesByIdsRequest)super.clearCookies();
    }

    public FindSurveyResponsesByIdsRequest
    setResponseIds(Set<Long> value)
    {
        responseIds = value;
        return this;
    }

    public Set<Long>
    getResponseIds() { return responseIds; }
}

//////////////////////////////////////////////////////////////////////////////
