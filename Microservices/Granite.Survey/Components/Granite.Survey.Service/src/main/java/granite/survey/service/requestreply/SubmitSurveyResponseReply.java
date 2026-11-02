//////////////////////////////////////////////////////////////////////////////
// SubmitSurveyResponseReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class SubmitSurveyResponseReply
    extends AbstractServiceReply
{
    private SurveyResponseData submittedResponse;

    public
    SubmitSurveyResponseReply()
    {
        super();
        submittedResponse = null;
    }

    public
    SubmitSurveyResponseReply(SubmitSurveyResponseRequest originatingRequest)
    {
        super(originatingRequest);
        submittedResponse = null;
    }

    @Override
    public SubmitSurveyResponseReply
    setReplyId(UUID replyId)
    {
        return (SubmitSurveyResponseReply)super.setReplyId(replyId);
    }

    @Override
    public SubmitSurveyResponseReply
    setOriginatingRequestId(UUID requestId)
    {
        return (SubmitSurveyResponseReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public SubmitSurveyResponseReply
    setTimestamp(Instant timestamp)
    {
        return (SubmitSurveyResponseReply)super.setTimestamp(timestamp);
    }

    @Override
    public SubmitSurveyResponseReply
    setSuccess(boolean success)
    {
        return (SubmitSurveyResponseReply)super.setSuccess(success);
    }

    @Override
    public SubmitSurveyResponseReply
    setSuccessMessage(String successMessage)
    {
        return (SubmitSurveyResponseReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public SubmitSurveyResponseReply
    setFailureMessage(String failureMessage)
    {
        return (SubmitSurveyResponseReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public SubmitSurveyResponseReply
    setException(ExceptionData exception)
    {
        return (SubmitSurveyResponseReply)super.setException(exception);
    }

    public SubmitSurveyResponseReply
    setSubmittedResponse(SurveyResponseData value)
    {
        submittedResponse = value;
        return this;
    }

    public SurveyResponseData
    getSubmittedResponse() { return submittedResponse; }
}

//////////////////////////////////////////////////////////////////////////////
