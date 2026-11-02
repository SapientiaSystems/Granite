//////////////////////////////////////////////////////////////////////////////
// UpdateSurveyResponsesRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class UpdateSurveyResponsesRequest
    extends AbstractServiceRequest
{
    private List<UpdateSurveyResponseData> responses;

    public
    UpdateSurveyResponsesRequest()
    {
        super();
        responses = new ArrayList<>();
    }

    @Override
    public UpdateSurveyResponsesRequest
    setRequestId(UUID requestId)
    {
        return (UpdateSurveyResponsesRequest)super.setRequestId(requestId);
    }

    @Override
    public UpdateSurveyResponsesRequest
    setTimestamp(Instant timestamp)
    {
        return (UpdateSurveyResponsesRequest)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateSurveyResponsesRequest
    addHeader(String name,Object value)
    {
        return (UpdateSurveyResponsesRequest)super.addHeader(name,value);
    }

    @Override
    public UpdateSurveyResponsesRequest
    clearHeaders()
    {
        return (UpdateSurveyResponsesRequest)super.clearHeaders();
    }

    @Override
    public UpdateSurveyResponsesRequest
    addCookie(Cookie cookie)
    {
        return (UpdateSurveyResponsesRequest)super.addCookie(cookie);
    }

    @Override
    public UpdateSurveyResponsesRequest
    clearCookies()
    {
        return (UpdateSurveyResponsesRequest)super.clearCookies();
    }

    public UpdateSurveyResponsesRequest
    setResponses(List<UpdateSurveyResponseData> value)
    {
        responses = value;
        return this;
    }

    public List<UpdateSurveyResponseData>
    getResponses() { return responses; }
}

//////////////////////////////////////////////////////////////////////////////
