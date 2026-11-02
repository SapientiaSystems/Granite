//////////////////////////////////////////////////////////////////////////////
// SurveyResponseData.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public
class SurveyResponseData
    implements Serializable
{
    private Long                 responseId;
    private Long                 userId;
    private Map<String,Integer>  answers;
    private String               comment;
    private SurveyResponseStatus status;
    private String               notes;
    private Instant              created;
    private Instant              updated;
    private Instant              sent;
    private Instant              received;

    public
    SurveyResponseData()
    {
        responseId = null;
        userId     = null;
        answers    = new LinkedHashMap<>();
        comment    = null;
        status     = null;
        notes      = null;
        created    = null;
        updated    = null;
        sent       = null;
        received   = null;
    }

    public SurveyResponseData
    setResponseId(Long value)
    {
        responseId = value;
        return this;
    }

    public SurveyResponseData
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public SurveyResponseData
    setAnswers(Map<String,Integer> value)
    {
        answers = value;
        return this;
    }

    public SurveyResponseData
    setComment(String value)
    {
        comment = value;
        return this;
    }

    public SurveyResponseData
    setStatus(SurveyResponseStatus value)
    {
        status = value;
        return this;
    }

    public SurveyResponseData
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public SurveyResponseData
    setCreated(Instant value)
    {
        created = value;
        return this;
    }

    public SurveyResponseData
    setUpdated(Instant value)
    {
        updated = value;
        return this;
    }

    public SurveyResponseData
    setSent(Instant value)
    {
        sent = value;
        return this;
    }

    public SurveyResponseData
    setReceived(Instant value)
    {
        received = value;
        return this;
    }

    public Long
    getResponseId() { return responseId; }

    public Long
    getUserId() { return userId; }

    public Map<String,Integer>
    getAnswers() { return answers; }

    public String
    getComment() { return comment; }

    public SurveyResponseStatus
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
