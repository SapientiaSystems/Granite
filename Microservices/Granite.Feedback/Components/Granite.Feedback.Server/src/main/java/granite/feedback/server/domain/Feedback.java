//////////////////////////////////////////////////////////////////////////////
// Feedback.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import granite.feedback.service.requestreply.FeedbackStatus;
import granite.feedback.service.requestreply.FeedbackType;
import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public
class Feedback
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            Feedback,
            IFeedbackEvent,
            IFeedbackEventObserver>
    implements IFeedbackEventSource
{
    private Long               userId;
    private FeedbackType       type;
    private String             summary;
    private Map<String,String> details;
    private FeedbackStatus     status;
    private String             notes;
    private Instant            sent;
    private Instant            received;
    private Instant            submitted;
    private Instant            changed;

    public
    Feedback()
    {
        super();
        userId    = null;
        type      = null;
        summary   = null;
        details   = new LinkedHashMap<>();
        status    = FeedbackStatus.NEW;
        notes     = null;
        sent      = null;
        received  = null;
        submitted = null;
        changed   = null;
    }

    @Override
    public Feedback
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

    public Feedback
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Feedback
    setType(FeedbackType value)
    {
        type = value;
        return this;
    }

    public Feedback
    setSummary(String value)
    {
        summary = value;
        return this;
    }

    public Feedback
    setDetails(Map<String,String> value)
    {
        details = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        return this;
    }

    public Feedback
    setStatus(FeedbackStatus value)
    {
        status = value;
        return this;
    }

    public Feedback
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public Feedback
    setSent(Instant time)
    {
        sent = time;
        return this;
    }

    public Feedback
    setReceived(Instant time)
    {
        received = time;
        return this;
    }

    public Feedback
    setSubmitted(Instant time)
    {
        submitted = time;
        return this;
    }

    public Feedback
    setChanged(Instant time)
    {
        changed = time;
        return this;
    }

    public Feedback
    changeStatus(FeedbackStatus value,Instant at)
    {
        if (value == null || value == status)
            return this;

        status = value;

        if (value == FeedbackStatus.SENT && sent == null)
            sent = at;

        if (value == FeedbackStatus.RECEIVED)
        {
            if (sent == null)
                sent = at;

            if (received == null)
                received = at;
        }

        return this;
    }

    public boolean
    canMarkSent() { return status == FeedbackStatus.NEW; }

    public boolean
    canMarkReceived() { return status == FeedbackStatus.SENT; }

    public Long
    getFeedbackId() { return super.getPrimaryId(); }

    public Long
    getUserId() { return userId; }

    public FeedbackType
    getType() { return type; }

    public String
    getSummary() { return summary; }

    public Map<String,String>
    getDetails() { return details; }

    public FeedbackStatus
    getStatus() { return status; }

    public String
    getNotes() { return notes; }

    public Instant
    getSent() { return sent; }

    public Instant
    getReceived() { return received; }

    public Instant
    getSubmitted() { return submitted; }

    public Instant
    getChanged() { return changed; }

    public Feedback
    notifyCreated() { return super.notify(new FeedbackCreated(this)); }

    public Feedback
    notifyUpdated() { return super.notify(new FeedbackUpdated(this)); }

    public Feedback
    notifyDestroyed() { return super.notify(new FeedbackDestroyed(this)); }

    @Override
    protected Feedback
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
