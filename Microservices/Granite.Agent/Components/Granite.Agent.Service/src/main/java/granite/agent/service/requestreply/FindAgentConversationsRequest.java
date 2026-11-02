//////////////////////////////////////////////////////////////////////////////
// FindAgentConversationsRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindAgentConversationsRequest
    extends AbstractServiceRequest
{
    private Long userId;

    public
    FindAgentConversationsRequest()
    {
        super();
        userId = null;
    }

    @Override
    public FindAgentConversationsRequest
    setRequestId(UUID requestId)
    {
        return (FindAgentConversationsRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAgentConversationsRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAgentConversationsRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAgentConversationsRequest
    addHeader(String name,Object value)
    {
        return (FindAgentConversationsRequest)super.addHeader(name,value);
    }

    @Override
    public FindAgentConversationsRequest
    clearHeaders()
    {
        return (FindAgentConversationsRequest)super.clearHeaders();
    }

    @Override
    public FindAgentConversationsRequest
    addCookie(Cookie cookie)
    {
        return (FindAgentConversationsRequest)super.addCookie(cookie);
    }

    @Override
    public FindAgentConversationsRequest
    clearCookies()
    {
        return (FindAgentConversationsRequest)super.clearCookies();
    }

    public FindAgentConversationsRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public Long
    getUserId() { return userId; }
}

//////////////////////////////////////////////////////////////////////////////
