//////////////////////////////////////////////////////////////////////////////
// ResolveSessionRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class ResolveSessionRequest
    extends AbstractServiceRequest
{
    private String handle;

    public
    ResolveSessionRequest()
    {
        super();
        handle = null;
    }

    @Override
    public ResolveSessionRequest
    setRequestId(UUID requestId)
    {
        return (ResolveSessionRequest)super.setRequestId(requestId);
    }

    @Override
    public ResolveSessionRequest
    setTimestamp(Instant timestamp)
    {
        return (ResolveSessionRequest)super.setTimestamp(timestamp);
    }

    @Override
    public ResolveSessionRequest
    addHeader(String name,Object value)
    {
        return (ResolveSessionRequest)super.addHeader(name,value);
    }

    @Override
    public ResolveSessionRequest
    clearHeaders()
    {
        return (ResolveSessionRequest)super.clearHeaders();
    }

    @Override
    public ResolveSessionRequest
    addCookie(Cookie cookie)
    {
        return (ResolveSessionRequest)super.addCookie(cookie);
    }

    @Override
    public ResolveSessionRequest
    clearCookies()
    {
        return (ResolveSessionRequest)super.clearCookies();
    }

    public ResolveSessionRequest
    setHandle(String handle)
    {
        this.handle = handle;
        return this;
    }

    public String
    getHandle() { return handle; }

    @Override
    public String
    toString()
    {
        return "ResolveSessionRequest{requestId=" + getRequestId() + "}";
    }
}

//////////////////////////////////////////////////////////////////////////////
