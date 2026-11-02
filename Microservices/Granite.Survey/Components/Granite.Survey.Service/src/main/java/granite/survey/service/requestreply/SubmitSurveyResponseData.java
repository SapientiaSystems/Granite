//////////////////////////////////////////////////////////////////////////////
// SubmitSurveyResponseData.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public
class SubmitSurveyResponseData
    implements Serializable
{
    private Long                userId;
    private Map<String,Integer> answers;
    private String              comment;

    public
    SubmitSurveyResponseData()
    {
        userId  = null;
        answers = new LinkedHashMap<>();
        comment = null;
    }

    public SubmitSurveyResponseData
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public SubmitSurveyResponseData
    setAnswers(Map<String,Integer> value)
    {
        answers = value;
        return this;
    }

    public SubmitSurveyResponseData
    setComment(String value)
    {
        comment = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Map<String,Integer>
    getAnswers() { return answers; }

    public String
    getComment() { return comment; }
}

//////////////////////////////////////////////////////////////////////////////
