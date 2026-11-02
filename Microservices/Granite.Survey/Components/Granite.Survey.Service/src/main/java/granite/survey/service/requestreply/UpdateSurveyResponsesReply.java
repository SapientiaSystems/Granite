//////////////////////////////////////////////////////////////////////////////
// UpdateSurveyResponsesReply.java
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
class UpdateSurveyResponsesReply
    extends AbstractServiceReply
{
    private Set<Long>                requestedResponseIds;
    private Set<Long>                failedResponseIds;
    private List<SurveyResponseData> updatedResponses;

    public
    UpdateSurveyResponsesReply()
    {
        super();
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    public
    UpdateSurveyResponsesReply(UpdateSurveyResponsesRequest originatingRequest)
    {
        super(originatingRequest);
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    @Override
    public UpdateSurveyResponsesReply
    setReplyId(UUID replyId)
    {
        return (UpdateSurveyResponsesReply)super.setReplyId(replyId);
    }

    @Override
    public UpdateSurveyResponsesReply
    setOriginatingRequestId(UUID requestId)
    {
        return (UpdateSurveyResponsesReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public UpdateSurveyResponsesReply
    setTimestamp(Instant timestamp)
    {
        return (UpdateSurveyResponsesReply)super.setTimestamp(timestamp);
    }

    @Override
    public UpdateSurveyResponsesReply
    setSuccess(boolean success)
    {
        return (UpdateSurveyResponsesReply)super.setSuccess(success);
    }

    @Override
    public UpdateSurveyResponsesReply
    setSuccessMessage(String successMessage)
    {
        return (UpdateSurveyResponsesReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public UpdateSurveyResponsesReply
    setFailureMessage(String failureMessage)
    {
        return (UpdateSurveyResponsesReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public UpdateSurveyResponsesReply
    setException(ExceptionData exception)
    {
        return (UpdateSurveyResponsesReply)super.setException(exception);
    }

    public UpdateSurveyResponsesReply
    setRequestedResponseIds(Set<Long> value)
    {
        requestedResponseIds = value;
        return this;
    }

    public UpdateSurveyResponsesReply
    setFailedResponseIds(Set<Long> value)
    {
        failedResponseIds = value;
        return this;
    }

    public UpdateSurveyResponsesReply
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
