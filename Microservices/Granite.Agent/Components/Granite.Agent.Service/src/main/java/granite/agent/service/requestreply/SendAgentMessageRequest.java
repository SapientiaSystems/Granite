//////////////////////////////////////////////////////////////////////////////
// SendAgentMessageRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceRequest;
import strata.foundation.core.transfer.Cookie;

import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class SendAgentMessageRequest
    extends AbstractServiceRequest
{
    private Long        userId;
    private Long        sessionId;
    private Long        conversationId;
    private String      text;
    private Set<String> authorities;

    public
    SendAgentMessageRequest()
    {
        super();
        userId         = null;
        sessionId      = null;
        conversationId = null;
        text           = null;
        authorities    = new TreeSet<>();
    }

    @Override
    public SendAgentMessageRequest
    setRequestId(UUID requestId)
    {
        return (SendAgentMessageRequest)super.setRequestId(requestId);
    }

    @Override
    public SendAgentMessageRequest
    setTimestamp(Instant timestamp)
    {
        return (SendAgentMessageRequest)super.setTimestamp(timestamp);
    }

    @Override
    public SendAgentMessageRequest
    addHeader(String name,Object value)
    {
        return (SendAgentMessageRequest)super.addHeader(name,value);
    }

    @Override
    public SendAgentMessageRequest
    clearHeaders()
    {
        return (SendAgentMessageRequest)super.clearHeaders();
    }

    @Override
    public SendAgentMessageRequest
    addCookie(Cookie cookie)
    {
        return (SendAgentMessageRequest)super.addCookie(cookie);
    }

    @Override
    public SendAgentMessageRequest
    clearCookies()
    {
        return (SendAgentMessageRequest)super.clearCookies();
    }

    public SendAgentMessageRequest
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public SendAgentMessageRequest
    setSessionId(Long value)
    {
        sessionId = value;
        return this;
    }

    public SendAgentMessageRequest
    setConversationId(Long value)
    {
        conversationId = value;
        return this;
    }

    public SendAgentMessageRequest
    setText(String value)
    {
        text = value;
        return this;
    }

    public SendAgentMessageRequest
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
    getText() { return text; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
