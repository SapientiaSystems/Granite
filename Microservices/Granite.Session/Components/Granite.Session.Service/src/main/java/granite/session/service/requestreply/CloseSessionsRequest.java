//////////////////////////////////////////////////////////////////////////////
// CloseSessionsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class CloseSessionsRequest
    extends AbstractServiceRequest
{
    private Set<Long> sessionIds;

    public
    CloseSessionsRequest()
    {
        super();
        sessionIds = new TreeSet<>();
    }

    @Override
    public CloseSessionsRequest
    setRequestId(UUID requestId)
    {
        return (CloseSessionsRequest)super.setRequestId(requestId);
    }

    @Override
    public CloseSessionsRequest
    setTimestamp(Instant timestamp)
    {
        return (CloseSessionsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public CloseSessionsRequest
    addHeader(String name,Object value)
    {
        return (CloseSessionsRequest)super.addHeader(name,value);
    }

    @Override
    public CloseSessionsRequest
    clearHeaders()
    {
        return (CloseSessionsRequest)super.clearHeaders();
    }

    @Override
    public CloseSessionsRequest
    addCookie(Cookie cookie)
    {
        return (CloseSessionsRequest)super.addCookie(cookie);
    }

    @Override
    public CloseSessionsRequest
    clearCookies()
    {
        return (CloseSessionsRequest)super.clearCookies();
    }

    public CloseSessionsRequest
    setSessionIds(Set<Long> ids)
    {
        sessionIds = ids;
        return this;
    }

    public Set<Long>
    getSessionIds() { return sessionIds; }
}

//////////////////////////////////////////////////////////////////////////////
