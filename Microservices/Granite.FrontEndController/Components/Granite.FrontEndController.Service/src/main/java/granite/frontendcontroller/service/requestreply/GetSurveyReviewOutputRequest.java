//////////////////////////////////////////////////////////////////////////////
// GetSurveyReviewOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetSurveyReviewOutputRequest
    extends GetOutputRequest
{
    public
    GetSurveyReviewOutputRequest()
    {
        super();
    }

    @Override
    public GetSurveyReviewOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetSurveyReviewOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetSurveyReviewOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetSurveyReviewOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetSurveyReviewOutputRequest
    addHeader(String name,Object value)
    {
        return (GetSurveyReviewOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetSurveyReviewOutputRequest
    clearHeaders()
    {
        return (GetSurveyReviewOutputRequest)super.clearHeaders();
    }

    @Override
    public GetSurveyReviewOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetSurveyReviewOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetSurveyReviewOutputRequest
    clearCookies()
    {
        return (GetSurveyReviewOutputRequest)super.clearCookies();
    }

    @Override
    public GetSurveyReviewOutputRequest
    setUserId(Long id)
    {
        return (GetSurveyReviewOutputRequest)super.setUserId(id);
    }

    @Override
    public GetSurveyReviewOutputRequest
    setSource(String s)
    {
        return (GetSurveyReviewOutputRequest)super.setSource(s);
    }

    @Override
    public GetSurveyReviewOutputRequest
    setCommand(String s)
    {
        return (GetSurveyReviewOutputRequest)super.setCommand(s);
    }

    @Override
    public GetSurveyReviewOutputRequest
    setDestination(String d)
    {
        return (GetSurveyReviewOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
