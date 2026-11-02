//////////////////////////////////////////////////////////////////////////////
// SurveyResponse.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import granite.survey.service.requestreply.SurveyResponseStatus;
import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public
class SurveyResponse
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            SurveyResponse,
            ISurveyResponseEvent,
            ISurveyResponseEventObserver>
    implements ISurveyResponseEventSource
{
    private Long                 userId;
    private Map<String,Integer>  answers;
    private String               comment;
    private SurveyResponseStatus status;
    private String               notes;
    private Instant              sent;
    private Instant              received;
    private Instant              submitted;
    private Instant              changed;

    public
    SurveyResponse()
    {
        super();
        userId    = null;
        answers   = new LinkedHashMap<>();
        comment   = null;
        status    = SurveyResponseStatus.NEW;
        notes     = null;
        sent      = null;
        received  = null;
        submitted = null;
        changed   = null;
    }

    @Override
    public SurveyResponse
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

    public SurveyResponse
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public SurveyResponse
    setAnswers(Map<String,Integer> value)
    {
        answers = value == null ? new LinkedHashMap<>() : new LinkedHashMap<>(value);
        return this;
    }

    public SurveyResponse
    setComment(String value)
    {
        comment = value;
        return this;
    }

    public SurveyResponse
    setStatus(SurveyResponseStatus value)
    {
        status = value;
        return this;
    }

    public SurveyResponse
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public SurveyResponse
    setSent(Instant time)
    {
        sent = time;
        return this;
    }

    public SurveyResponse
    setReceived(Instant time)
    {
        received = time;
        return this;
    }

    public SurveyResponse
    setSubmitted(Instant time)
    {
        submitted = time;
        return this;
    }

    public SurveyResponse
    setChanged(Instant time)
    {
        changed = time;
        return this;
    }

    public SurveyResponse
    changeStatus(SurveyResponseStatus value,Instant at)
    {
        if (value == null || value == status)
            return this;

        status = value;

        if (value == SurveyResponseStatus.SENT && sent == null)
            sent = at;

        if (value == SurveyResponseStatus.RECEIVED)
        {
            if (sent == null)
                sent = at;

            if (received == null)
                received = at;
        }

        return this;
    }

    public boolean
    canMarkSent() { return status == SurveyResponseStatus.NEW; }

    public boolean
    canMarkReceived() { return status == SurveyResponseStatus.SENT; }

    public Long
    getResponseId() { return super.getPrimaryId(); }

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
    getSent() { return sent; }

    public Instant
    getReceived() { return received; }

    public Instant
    getSubmitted() { return submitted; }

    public Instant
    getChanged() { return changed; }

    public SurveyResponse
    notifyCreated() { return super.notify(new SurveyResponseCreated(this)); }

    public SurveyResponse
    notifyUpdated() { return super.notify(new SurveyResponseUpdated(this)); }

    public SurveyResponse
    notifyDestroyed() { return super.notify(new SurveyResponseDestroyed(this)); }

    @Override
    protected SurveyResponse
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
