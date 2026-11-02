//////////////////////////////////////////////////////////////////////////////
// SubmitSurveyResponseRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class SubmitSurveyResponseRequest
    extends AbstractServiceRequest
{
    private SubmitSurveyResponseData response;

    public
    SubmitSurveyResponseRequest()
    {
        super();
        response = null;
    }

    @Override
    public SubmitSurveyResponseRequest
    setRequestId(UUID requestId)
    {
        return (SubmitSurveyResponseRequest)super.setRequestId(requestId);
    }

    @Override
    public SubmitSurveyResponseRequest
    setTimestamp(Instant timestamp)
    {
        return (SubmitSurveyResponseRequest)super.setTimestamp(timestamp);
    }

    @Override
    public SubmitSurveyResponseRequest
    addHeader(String name,Object value)
    {
        return (SubmitSurveyResponseRequest)super.addHeader(name,value);
    }

    @Override
    public SubmitSurveyResponseRequest
    clearHeaders()
    {
        return (SubmitSurveyResponseRequest)super.clearHeaders();
    }

    @Override
    public SubmitSurveyResponseRequest
    addCookie(Cookie cookie)
    {
        return (SubmitSurveyResponseRequest)super.addCookie(cookie);
    }

    @Override
    public SubmitSurveyResponseRequest
    clearCookies()
    {
        return (SubmitSurveyResponseRequest)super.clearCookies();
    }

    public SubmitSurveyResponseRequest
    setResponse(SubmitSurveyResponseData value)
    {
        response = value;
        return this;
    }

    public SubmitSurveyResponseData
    getResponse() { return response; }
}

//////////////////////////////////////////////////////////////////////////////
