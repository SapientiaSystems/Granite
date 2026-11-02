//////////////////////////////////////////////////////////////////////////////
// FindSessionsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindSessionsRequest
    extends AbstractServiceRequest
{
    private boolean includeClosed;

    public
    FindSessionsRequest()
    {
        super();
        includeClosed = false;
    }

    @Override
    public FindSessionsRequest
    setRequestId(UUID requestId)
    {
        return (FindSessionsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindSessionsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindSessionsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindSessionsRequest
    addHeader(String name,Object value)
    {
        return (FindSessionsRequest)super.addHeader(name,value);
    }

    @Override
    public FindSessionsRequest
    clearHeaders()
    {
        return (FindSessionsRequest)super.clearHeaders();
    }

    @Override
    public FindSessionsRequest
    addCookie(Cookie cookie)
    {
        return (FindSessionsRequest)super.addCookie(cookie);
    }

    @Override
    public FindSessionsRequest
    clearCookies()
    {
        return (FindSessionsRequest)super.clearCookies();
    }

    public FindSessionsRequest
    setIncludeClosed(boolean value)
    {
        includeClosed = value;
        return this;
    }

    public boolean
    isIncludeClosed() { return includeClosed; }
}

//////////////////////////////////////////////////////////////////////////////
