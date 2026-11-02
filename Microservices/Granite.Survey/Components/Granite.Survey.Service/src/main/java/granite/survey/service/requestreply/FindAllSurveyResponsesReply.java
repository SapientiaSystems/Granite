//////////////////////////////////////////////////////////////////////////////
// FindAllSurveyResponsesReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindAllSurveyResponsesReply
    extends AbstractServiceReply
{
    private List<SurveyResponseData> responses;

    public
    FindAllSurveyResponsesReply()
    {
        super();
        responses = new ArrayList<>();
    }

    public
    FindAllSurveyResponsesReply(FindAllSurveyResponsesRequest originatingRequest)
    {
        super(originatingRequest);
        responses = new ArrayList<>();
    }

    @Override
    public FindAllSurveyResponsesReply
    setReplyId(UUID replyId)
    {
        return (FindAllSurveyResponsesReply)super.setReplyId(replyId);
    }

    @Override
    public FindAllSurveyResponsesReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAllSurveyResponsesReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAllSurveyResponsesReply
    setTimestamp(Instant timestamp)
    {
        return (FindAllSurveyResponsesReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAllSurveyResponsesReply
    setSuccess(boolean success)
    {
        return (FindAllSurveyResponsesReply)super.setSuccess(success);
    }

    @Override
    public FindAllSurveyResponsesReply
    setSuccessMessage(String successMessage)
    {
        return (FindAllSurveyResponsesReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAllSurveyResponsesReply
    setFailureMessage(String failureMessage)
    {
        return (FindAllSurveyResponsesReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAllSurveyResponsesReply
    setException(ExceptionData exception)
    {
        return (FindAllSurveyResponsesReply)super.setException(exception);
    }

    public FindAllSurveyResponsesReply
    setResponses(List<SurveyResponseData> value)
    {
        responses = value;
        return this;
    }

    public List<SurveyResponseData>
    getResponses() { return responses; }
}

//////////////////////////////////////////////////////////////////////////////
