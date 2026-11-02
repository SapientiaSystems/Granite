//////////////////////////////////////////////////////////////////////////////
// MarkSurveyResponsesReceivedReply.java
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
class MarkSurveyResponsesReceivedReply
    extends AbstractServiceReply
{
    private Set<Long>                requestedResponseIds;
    private Set<Long>                failedResponseIds;
    private List<SurveyResponseData> updatedResponses;

    public
    MarkSurveyResponsesReceivedReply()
    {
        super();
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    public
    MarkSurveyResponsesReceivedReply(MarkSurveyResponsesReceivedRequest originatingRequest)
    {
        super(originatingRequest);
        requestedResponseIds = new TreeSet<>();
        failedResponseIds    = new TreeSet<>();
        updatedResponses     = new ArrayList<>();
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setReplyId(UUID replyId)
    {
        return (MarkSurveyResponsesReceivedReply)super.setReplyId(replyId);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setOriginatingRequestId(UUID requestId)
    {
        return (MarkSurveyResponsesReceivedReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setTimestamp(Instant timestamp)
    {
        return (MarkSurveyResponsesReceivedReply)super.setTimestamp(timestamp);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setSuccess(boolean success)
    {
        return (MarkSurveyResponsesReceivedReply)super.setSuccess(success);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setSuccessMessage(String successMessage)
    {
        return (MarkSurveyResponsesReceivedReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setFailureMessage(String failureMessage)
    {
        return (MarkSurveyResponsesReceivedReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public MarkSurveyResponsesReceivedReply
    setException(ExceptionData exception)
    {
        return (MarkSurveyResponsesReceivedReply)super.setException(exception);
    }

    public MarkSurveyResponsesReceivedReply
    setRequestedResponseIds(Set<Long> value)
    {
        requestedResponseIds = value;
        return this;
    }

    public MarkSurveyResponsesReceivedReply
    setFailedResponseIds(Set<Long> value)
    {
        failedResponseIds = value;
        return this;
    }

    public MarkSurveyResponsesReceivedReply
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
