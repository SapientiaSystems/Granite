//////////////////////////////////////////////////////////////////////////////
// HandleSurveyReviewInputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.HandleInputRequest;

import java.time.Instant;
import java.util.UUID;

public
class HandleSurveyReviewInputRequest
    extends HandleInputRequest<SurveyReviewInput>
{
    public
    HandleSurveyReviewInputRequest()
    {
        super();
    }

    @Override
    public HandleSurveyReviewInputRequest
    setRequestId(UUID requestId)
    {
        return (HandleSurveyReviewInputRequest)super.setRequestId(requestId);
    }

    @Override
    public HandleSurveyReviewInputRequest
    setTimestamp(Instant timestamp)
    {
        return (HandleSurveyReviewInputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSurveyReviewInputRequest
    addHeader(String name,Object value)
    {
        return (HandleSurveyReviewInputRequest)super.addHeader(name,value);
    }

    @Override
    public HandleSurveyReviewInputRequest
    clearHeaders()
    {
        return (HandleSurveyReviewInputRequest)super.clearHeaders();
    }

    @Override
    public HandleSurveyReviewInputRequest
    addCookie(Cookie cookie)
    {
        return (HandleSurveyReviewInputRequest)super.addCookie(cookie);
    }

    @Override
    public HandleSurveyReviewInputRequest
    clearCookies()
    {
        return (HandleSurveyReviewInputRequest)super.clearCookies();
    }

    @Override
    public HandleSurveyReviewInputRequest
    setInput(SurveyReviewInput input)
    {
        return (HandleSurveyReviewInputRequest)super.setInput(input);
    }
}

//////////////////////////////////////////////////////////////////////////////
