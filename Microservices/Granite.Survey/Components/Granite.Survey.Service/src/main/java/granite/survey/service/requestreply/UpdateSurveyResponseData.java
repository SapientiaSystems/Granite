//////////////////////////////////////////////////////////////////////////////
// UpdateSurveyResponseData.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.io.Serializable;

public
class UpdateSurveyResponseData
    implements Serializable
{
    private Long                 responseId;
    private SurveyResponseStatus status;
    private String               notes;

    public
    UpdateSurveyResponseData()
    {
        responseId = null;
        status     = null;
        notes      = null;
    }

    public UpdateSurveyResponseData
    setResponseId(Long value)
    {
        responseId = value;
        return this;
    }

    public UpdateSurveyResponseData
    setStatus(SurveyResponseStatus value)
    {
        status = value;
        return this;
    }

    public UpdateSurveyResponseData
    setNotes(String value)
    {
        notes = value;
        return this;
    }

    public Long
    getResponseId() { return responseId; }

    public SurveyResponseStatus
    getStatus() { return status; }

    public String
    getNotes() { return notes; }
}

//////////////////////////////////////////////////////////////////////////////
