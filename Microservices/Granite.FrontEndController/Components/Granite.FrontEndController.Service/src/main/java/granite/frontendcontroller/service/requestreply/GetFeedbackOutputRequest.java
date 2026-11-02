//////////////////////////////////////////////////////////////////////////////
// GetFeedbackOutputRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.Cookie;
import strata.frontendcontroller.service.requestreply.GetOutputRequest;

import java.time.Instant;
import java.util.UUID;

public
class GetFeedbackOutputRequest
    extends GetOutputRequest
{
    public
    GetFeedbackOutputRequest()
    {
        super();
    }

    @Override
    public GetFeedbackOutputRequest
    setRequestId(UUID requestId)
    {
        return (GetFeedbackOutputRequest)super.setRequestId(requestId);
    }

    @Override
    public GetFeedbackOutputRequest
    setTimestamp(Instant timestamp)
    {
        return (GetFeedbackOutputRequest)super.setTimestamp(timestamp);
    }

    @Override
    public GetFeedbackOutputRequest
    addHeader(String name,Object value)
    {
        return (GetFeedbackOutputRequest)super.addHeader(name,value);
    }

    @Override
    public GetFeedbackOutputRequest
    clearHeaders()
    {
        return (GetFeedbackOutputRequest)super.clearHeaders();
    }

    @Override
    public GetFeedbackOutputRequest
    addCookie(Cookie cookie)
    {
        return (GetFeedbackOutputRequest)super.addCookie(cookie);
    }

    @Override
    public GetFeedbackOutputRequest
    clearCookies()
    {
        return (GetFeedbackOutputRequest)super.clearCookies();
    }

    @Override
    public GetFeedbackOutputRequest
    setUserId(Long id)
    {
        return (GetFeedbackOutputRequest)super.setUserId(id);
    }

    @Override
    public GetFeedbackOutputRequest
    setSource(String s)
    {
        return (GetFeedbackOutputRequest)super.setSource(s);
    }

    @Override
    public GetFeedbackOutputRequest
    setCommand(String s)
    {
        return (GetFeedbackOutputRequest)super.setCommand(s);
    }

    @Override
    public GetFeedbackOutputRequest
    setDestination(String d)
    {
        return (GetFeedbackOutputRequest)super.setDestination(d);
    }
}

//////////////////////////////////////////////////////////////////////////////
