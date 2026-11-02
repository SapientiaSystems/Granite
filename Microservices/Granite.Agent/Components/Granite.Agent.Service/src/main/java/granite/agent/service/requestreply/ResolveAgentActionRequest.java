//////////////////////////////////////////////////////////////////////////////
// ResolveAgentActionRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class ResolveAgentActionRequest
    extends AbstractServiceRequest
{
    private Long        userId;
    private Long        sessionId;
    private Long        conversationId;
    private String      toolUseId;
    private Boolean     approved;
    private Set<String> authorities;

    public
    ResolveAgentActionRequest()
    {
        super();
        userId         = null;
        sessionId      = null;
        conversationId = null;
        toolUseId      = null;
        approved       = null;
        authorities    = new TreeSet<>();
    }

    @Override
    public ResolveAgentActionRequest
    setRequestId(UUID requestId)
    {
        return (ResolveAgentActionRequest)super.setRequestId(requestId);
    }

    @Override
    public ResolveAgentActionRequest
    setTimestamp(Instant timestamp)
    {
        return (ResolveAgentActionRequest)super.setTimestamp(timestamp);
    }

    @Override
    public ResolveAgentActionRequest
    addHeader(String name,Object value)
    {
        return (ResolveAgentActionRequest)super.addHeader(name,value);
    }

    @Override
    public ResolveAgentActionRequest
    clearHeaders()
    {
        return (ResolveAgentActionRequest)super.clearHeaders();
    }

    @Override
    public ResolveAgentActionRequest
    addCookie(Cookie cookie)
    {
        return (ResolveAgentActionRequest)super.addCookie(cookie);
    }

    @Override
    public ResolveAgentActionRequest
    clearCookies()
    {
        return (ResolveAgentActionRequest)super.clearCookies();
    }

    public ResolveAgentActionRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public ResolveAgentActionRequest
    setSessionId(Long value)
    {
        sessionId = value;
        return this;
    }

    public ResolveAgentActionRequest
    setConversationId(Long value)
    {
        conversationId = value;
        return this;
    }

    public ResolveAgentActionRequest
    setToolUseId(String value)
    {
        toolUseId = value;
        return this;
    }

    public ResolveAgentActionRequest
    setApproved(Boolean value)
    {
        approved = value;
        return this;
    }

    public ResolveAgentActionRequest
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Long
    getSessionId() { return sessionId; }

    public Long
    getConversationId() { return conversationId; }

    public String
    getToolUseId() { return toolUseId; }

    public Boolean
    getApproved() { return approved; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
