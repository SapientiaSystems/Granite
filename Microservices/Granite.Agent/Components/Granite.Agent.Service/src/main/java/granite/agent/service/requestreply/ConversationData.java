//////////////////////////////////////////////////////////////////////////////
// ConversationData.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public
class ConversationData
    implements Serializable
{
    private Long                   conversationId;
    private Long                   userId;
    private String                 title;
    private List<AgentMessageData> messages;
    private AgentPendingActionData pendingAction;
    private Long                   inputTokens;
    private Long                   outputTokens;
    private Instant                created;
    private Instant                updated;

    public
    ConversationData()
    {
        conversationId = null;
        userId         = null;
        title          = null;
        messages       = new ArrayList<>();
        pendingAction  = null;
        inputTokens    = null;
        outputTokens   = null;
        created        = null;
        updated        = null;
    }

    public ConversationData
    setConversationId(Long value)
    {
        conversationId = value;
        return this;
    }

    public ConversationData
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public ConversationData
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public ConversationData
    setMessages(List<AgentMessageData> value)
    {
        messages = value;
        return this;
    }

    public ConversationData
    setPendingAction(AgentPendingActionData value)
    {
        pendingAction = value;
        return this;
    }

    public ConversationData
    setInputTokens(Long value)
    {
        inputTokens = value;
        return this;
    }

    public ConversationData
    setOutputTokens(Long value)
    {
        outputTokens = value;
        return this;
    }

    public ConversationData
    setCreated(Instant value)
    {
        created = value;
        return this;
    }

    public ConversationData
    setUpdated(Instant value)
    {
        updated = value;
        return this;
    }

    public Long
    getConversationId() { return conversationId; }

    public Long
    getUserId() { return userId; }

    public String
    getTitle() { return title; }

    public List<AgentMessageData>
    getMessages() { return messages; }

    public AgentPendingActionData
    getPendingAction() { return pendingAction; }

    public Long
    getInputTokens() { return inputTokens; }

    public Long
    getOutputTokens() { return outputTokens; }

    public Instant
    getCreated() { return created; }

    public Instant
    getUpdated() { return updated; }
}

//////////////////////////////////////////////////////////////////////////////
