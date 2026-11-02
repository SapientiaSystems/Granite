//////////////////////////////////////////////////////////////////////////////
// Conversation.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

import granite.agent.service.requestreply.AgentMessageData;
import granite.agent.service.requestreply.AgentMessageRole;
import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public
class Conversation
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            Conversation,
            IConversationEvent,
            IConversationEventObserver>
    implements IConversationEventSource
{
    public static final int MAX_TITLE_LENGTH = 80;

    private Long                   userId;
    private String                 title;
    private List<AgentMessageData> transcript;
    private String                 history;
    private String                 pendingCalls;
    private String                 collectedResults;
    private String                 pendingSummary;
    private Long                   inputTokens;
    private Long                   outputTokens;
    private Instant                started;
    private Instant                changed;

    public
    Conversation()
    {
        super();
        userId           = null;
        title            = null;
        transcript       = new ArrayList<>();
        history          = null;
        pendingCalls     = null;
        collectedResults = null;
        pendingSummary   = null;
        inputTokens      = 0L;
        outputTokens     = 0L;
        started          = null;
        changed          = null;
    }

    @Override
    public Conversation
    setPrimaryId(Long primaryId)
    {
        return super.setPrimaryId(primaryId);
    }

    @Override
    public Long
    getPrimaryId()
    {
        return super.getPrimaryId();
    }

    public Conversation
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Conversation
    setTitle(String value)
    {
        title = value;
        return this;
    }

    public Conversation
    setTranscript(List<AgentMessageData> value)
    {
        transcript = value == null ? new ArrayList<>() : new ArrayList<>(value);
        return this;
    }

    public Conversation
    setHistory(String value)
    {
        history = value;
        return this;
    }

    public Conversation
    setPendingCalls(String value)
    {
        pendingCalls = value;
        return this;
    }

    public Conversation
    setCollectedResults(String value)
    {
        collectedResults = value;
        return this;
    }

    public Conversation
    setPendingSummary(String value)
    {
        pendingSummary = value;
        return this;
    }

    public Conversation
    setInputTokens(Long value)
    {
        inputTokens = value == null ? 0L : value;
        return this;
    }

    public Conversation
    setOutputTokens(Long value)
    {
        outputTokens = value == null ? 0L : value;
        return this;
    }

    public Conversation
    setStarted(Instant time)
    {
        started = time;
        return this;
    }

    public Conversation
    setChanged(Instant time)
    {
        changed = time;
        return this;
    }

    public Conversation
    append(AgentMessageRole role,String text,String toolName,boolean error,Instant at)
    {
        List<AgentMessageData> next = new ArrayList<>(transcript);

        next.add(
            new AgentMessageData()
                .setSeq(next.size() + 1)
                .setRole(role)
                .setText(text)
                .setToolName(toolName)
                .setIsError(error)
                .setAt(at));
        transcript = next;

        if (title == null && role == AgentMessageRole.USER && text != null)
            title = text.length() > MAX_TITLE_LENGTH ? text.substring(0,MAX_TITLE_LENGTH - 1) + "…" : text;

        return this;
    }

    public Conversation
    addUsage(long input,long output)
    {
        inputTokens  = inputTokens + input;
        outputTokens = outputTokens + output;
        return this;
    }

    public boolean
    hasPendingAction() { return pendingCalls != null && !pendingCalls.isBlank(); }

    public Long
    getConversationId() { return super.getPrimaryId(); }

    public Long
    getUserId() { return userId; }

    public String
    getTitle() { return title; }

    public List<AgentMessageData>
    getTranscript() { return transcript; }

    public String
    getHistory() { return history; }

    public String
    getPendingCalls() { return pendingCalls; }

    public String
    getCollectedResults() { return collectedResults; }

    public String
    getPendingSummary() { return pendingSummary; }

    public Long
    getInputTokens() { return inputTokens; }

    public Long
    getOutputTokens() { return outputTokens; }

    public Instant
    getStarted() { return started; }

    public Instant
    getChanged() { return changed; }

    public Conversation
    notifyCreated() { return super.notify(new ConversationCreated(this)); }

    public Conversation
    notifyUpdated() { return super.notify(new ConversationUpdated(this)); }

    public Conversation
    notifyDestroyed() { return super.notify(new ConversationDestroyed(this)); }

    @Override
    protected Conversation
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
