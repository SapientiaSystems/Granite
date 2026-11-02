//////////////////////////////////////////////////////////////////////////////
// FeedbackData.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public
class FeedbackData
    implements Serializable
{
    private Long               feedbackId;
    private Long               userId;
    private FeedbackType       type;
    private String             summary;
    private Map<String,String> details;
    private FeedbackStatus     status;
    private String             notes;
    private Instant            created;
    private Instant            updated;
    private Instant            sent;
    private Instant            received;

    public
    FeedbackData()
    {
        feedbackId = null;
        userId     = null;
        type       = null;
        summary    = null;
        details    = new LinkedHashMap<>();
        status     = null;
        notes      = null;
        created    = null;
        updated    = null;
        sent       = null;
        received   = null;
    }

    public FeedbackData
    setFeedbackId(Long value)
    {
        feedbackId = value;
        return this;
    }

    public FeedbackData
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public FeedbackData
    setType(FeedbackType value)
    {
        type = value;
        return this;
    }

    public FeedbackData
    setSummary(String value)
    {
        summary = value;
        return this;
    }

    public FeedbackData
    setDetails(Map<String,String> value)
    {
        details = value;
        return this;
    }

    public FeedbackData
    setStatus(FeedbackStatus value)
    {
        status = value;
        return this;
    }

    public FeedbackData
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public FeedbackData
    setCreated(Instant value)
    {
        created = value;
        return this;
    }

    public FeedbackData
    setUpdated(Instant value)
    {
        updated = value;
        return this;
    }

    public FeedbackData
    setSent(Instant value)
    {
        sent = value;
        return this;
    }

    public FeedbackData
    setReceived(Instant value)
    {
        received = value;
        return this;
    }

    public Long
    getFeedbackId() { return feedbackId; }

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
    getCreated() { return created; }

    public Instant
    getUpdated() { return updated; }

    public Instant
    getSent() { return sent; }

    public Instant
    getReceived() { return received; }
}

//////////////////////////////////////////////////////////////////////////////
