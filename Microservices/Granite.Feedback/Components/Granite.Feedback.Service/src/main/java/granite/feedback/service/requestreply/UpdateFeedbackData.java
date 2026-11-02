//////////////////////////////////////////////////////////////////////////////
// UpdateFeedbackData.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import java.io.Serializable;

public
class UpdateFeedbackData
    implements Serializable
{
    private Long           feedbackId;
    private FeedbackStatus status;
    private String         notes;

    public
    UpdateFeedbackData()
    {
        feedbackId = null;
        status     = null;
        notes      = null;
    }

    public UpdateFeedbackData
    setFeedbackId(Long value)
    {
        feedbackId = value;
        return this;
    }

    public UpdateFeedbackData
    setStatus(FeedbackStatus value)
    {
        status = value;
        return this;
    }

    public UpdateFeedbackData
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public Long
    getFeedbackId() { return feedbackId; }

    public FeedbackStatus
    getStatus() { return status; }

    public String
    getNotes() { return notes; }
}

//////////////////////////////////////////////////////////////////////////////
