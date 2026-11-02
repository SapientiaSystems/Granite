//////////////////////////////////////////////////////////////////////////////
// FindSurveyResponsesByIdsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindSurveyResponsesByIdsReply
    extends AbstractServiceReply
{
    private List<SurveyResponseData> responses;

    public
    FindSurveyResponsesByIdsReply()
    {
        super();
        responses = new ArrayList<>();
    }

    public
    FindSurveyResponsesByIdsReply(FindSurveyResponsesByIdsRequest originatingRequest)
    {
        super(originatingRequest);
        responses = new ArrayList<>();
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setReplyId(UUID replyId)
    {
        return (FindSurveyResponsesByIdsReply)super.setReplyId(replyId);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindSurveyResponsesByIdsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setTimestamp(Instant timestamp)
    {
        return (FindSurveyResponsesByIdsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setSuccess(boolean success)
    {
        return (FindSurveyResponsesByIdsReply)super.setSuccess(success);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setSuccessMessage(String successMessage)
    {
        return (FindSurveyResponsesByIdsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setFailureMessage(String failureMessage)
    {
        return (FindSurveyResponsesByIdsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindSurveyResponsesByIdsReply
    setException(ExceptionData exception)
    {
        return (FindSurveyResponsesByIdsReply)super.setException(exception);
    }

    public FindSurveyResponsesByIdsReply
    setResponses(List<SurveyResponseData> value)
    {
        responses = value;
        return this;
    }

    public List<SurveyResponseData>
    getResponses() { return responses; }
}

//////////////////////////////////////////////////////////////////////////////
