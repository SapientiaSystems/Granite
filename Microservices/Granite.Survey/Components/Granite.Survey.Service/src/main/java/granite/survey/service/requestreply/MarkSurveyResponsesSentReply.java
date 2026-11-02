//////////////////////////////////////////////////////////////////////////////
// MarkSurveyResponsesSentReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public
class MarkSurveyResponsesSentReply
    extends AbstractServiceReply
{
    private Set<Long>                requestedResponseIds;
    private Set<Long>                failedResponseIds;
    private List<SurveyResponseData> updatedResponses;

    public
    MarkSurveyResponsesSentReply()
    {
        super();
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    public
    MarkSurveyResponsesSentReply(MarkSurveyResponsesSentRequest originatingRequest)
    {
        super(originatingRequest);
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    @Override
    public MarkSurveyResponsesSentReply
    setReplyId(UUID replyId)
    {
        return (MarkSurveyResponsesSentReply)super.setReplyId(replyId);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setOriginatingRequestId(UUID requestId)
    {
        return (MarkSurveyResponsesSentReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setTimestamp(Instant timestamp)
    {
        return (MarkSurveyResponsesSentReply)super.setTimestamp(timestamp);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setSuccess(boolean success)
    {
        return (MarkSurveyResponsesSentReply)super.setSuccess(success);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setSuccessMessage(String successMessage)
    {
        return (MarkSurveyResponsesSentReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setFailureMessage(String failureMessage)
    {
        return (MarkSurveyResponsesSentReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public MarkSurveyResponsesSentReply
    setException(ExceptionData exception)
    {
        return (MarkSurveyResponsesSentReply)super.setException(exception);
    }

    public MarkSurveyResponsesSentReply
    setRequestedResponseIds(Set<Long> value)
    {
        requestedResponseIds = value;
        return this;
    }

    public MarkSurveyResponsesSentReply
    setFailedResponseIds(Set<Long> value)
    {
        failedResponseIds = value;
        return this;
    }

    public MarkSurveyResponsesSentReply
    setUpdatedResponses(List<SurveyResponseData> value)
    {
        updatedResponses = value;
        return this;
    }

    public Set<Long>
    getRequestedResponseIds() { return requestedResponseIds; }

    public Set<Long>
    getFailedResponseIds() { return failedResponseIds; }

    public List<SurveyResponseData>
    getUpdatedResponses() { return updatedResponses; }
}

//////////////////////////////////////////////////////////////////////////////
