//////////////////////////////////////////////////////////////////////////////
// GetSurveyOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetSurveyOutputRequest
    extends GetOutputRequest
{
    public
    GetSurveyOutputRequest()
    {
        super();
    }

    @Override
    public GetSurveyOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetSurveyOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetSurveyOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetSurveyOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetSurveyOutputRequest
    addHeader(String name,Object value)
    {
        return (GetSurveyOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetSurveyOutputRequest
    clearHeaders()
    {
        return (GetSurveyOutputRequest)super.clearHeaders();
    }

    @Override
    public GetSurveyOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetSurveyOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetSurveyOutputRequest
    clearCookies()
    {
        return (GetSurveyOutputRequest)super.clearCookies();
    }

    @Override
    public GetSurveyOutputRequest
    setUserId(Long id)
    {
        return (GetSurveyOutputRequest)super.setUserId(id);
    }

    @Override
    public GetSurveyOutputRequest
    setSource(String s)
    {
        return (GetSurveyOutputRequest)super.setSource(s);
    }

    @Override
    public GetSurveyOutputRequest
    setCommand(String s)
    {
        return (GetSurveyOutputRequest)super.setCommand(s);
    }

    @Override
    public GetSurveyOutputRequest
    setDestination(String d)
    {
        return (GetSurveyOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
