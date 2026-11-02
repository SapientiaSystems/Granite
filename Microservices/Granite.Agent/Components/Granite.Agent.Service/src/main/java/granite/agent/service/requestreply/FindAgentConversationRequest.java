//////////////////////////////////////////////////////////////////////////////
// FindAgentConversationRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.UUID;

public
class FindAgentConversationRequest
    extends AbstractServiceRequest
{
    private Long userId;
    private Long conversationId;

    public
    FindAgentConversationRequest()
    {
        super();
        userId         = null;
        conversationId = null;
    }

    @Override
    public FindAgentConversationRequest
    setRequestId(UUID requestId)
    {
        return (FindAgentConversationRequest)super.setRequestId(requestId);
    }

    @Override
    public FindAgentConversationRequest
    setTimestamp(Instant timestamp)
    {
        return (FindAgentConversationRequest)super.setTimestamp(timestamp);
    }

    @Override
    public FindAgentConversationRequest
    addHeader(String name,Object value)
    {
        return (FindAgentConversationRequest)super.addHeader(name,value);
    }

    @Override
    public FindAgentConversationRequest
    clearHeaders()
    {
        return (FindAgentConversationRequest)super.clearHeaders();
    }

    @Override
    public FindAgentConversationRequest
    addCookie(Cookie cookie)
    {
        return (FindAgentConversationRequest)super.addCookie(cookie);
    }

    @Override
    public FindAgentConversationRequest
    clearCookies()
    {
        return (FindAgentConversationRequest)super.clearCookies();
    }

    public FindAgentConversationRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public FindAgentConversationRequest
    setConversationId(Long value)
    {
        conversationId = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Long
    getConversationId() { return conversationId; }
}

//////////////////////////////////////////////////////////////////////////////
