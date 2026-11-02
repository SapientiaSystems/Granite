//////////////////////////////////////////////////////////////////////////////
// CreateFeedbackData.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public
class CreateFeedbackData
    implements Serializable
{
    private Long               userId;
    private FeedbackType       type;
    private String             summary;
    private Map<String,String> details;

    public
    CreateFeedbackData()
    {
        userId  = null;
        type    = null;
        summary = null;
        details = new LinkedHashMap<>();
    }

    public CreateFeedbackData
    setUserId(Long value)
    {
        userId = value;
        return this;
    }

    public CreateFeedbackData
    setType(FeedbackType value)
    {
        type = value;
        return this;
    }

    public CreateFeedbackData
    setSummary(String value)
    {
        summary = value;
        return this;
    }

    public CreateFeedbackData
    setDetails(Map<String,String> value)
    {
        details = value;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public FeedbackType
    getType() { return type; }

    public String
    getSummary() { return summary; }

    public Map<String,String>
    getDetails() { return details; }
}

//////////////////////////////////////////////////////////////////////////////
