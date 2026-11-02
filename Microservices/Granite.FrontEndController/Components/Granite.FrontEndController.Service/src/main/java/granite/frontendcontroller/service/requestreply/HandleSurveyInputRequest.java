//////////////////////////////////////////////////////////////////////////////
// HandleSurveyInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleSurveyInputRequest
    extends HandleInputRequest<SurveyInput>
{
    public
    HandleSurveyInputRequest()
    {
        super();
    }

    @Override
    public HandleSurveyInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleSurveyInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleSurveyInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleSurveyInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSurveyInputRequest
    addHeader(String name,Object value)
    {
        return (HandleSurveyInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleSurveyInputRequest
    clearHeaders()
    {
        return (HandleSurveyInputRequest)super.clearHeaders();
    }

    @Override
    public HandleSurveyInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleSurveyInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleSurveyInputRequest
    clearCookies()
    {
        return (HandleSurveyInputRequest)super.clearCookies();
    }

    @Override
    public HandleSurveyInputRequest
    setInput(SurveyInput input)
    {
        return (HandleSurveyInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
