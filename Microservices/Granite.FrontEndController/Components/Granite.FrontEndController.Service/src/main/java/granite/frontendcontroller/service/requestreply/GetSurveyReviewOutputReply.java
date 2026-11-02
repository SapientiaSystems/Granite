//////////////////////////////////////////////////////////////////////////////
// GetSurveyReviewOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetSurveyReviewOutputReply
    extends GetOutputReply<SurveyReviewOutput>
{
    public
    GetSurveyReviewOutputReply()
    {
        super();
    }

    public
    GetSurveyReviewOutputReply(GetSurveyReviewOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetSurveyReviewOutputReply
    setReplyId(UUID replyId)
    {
        return (GetSurveyReviewOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetSurveyReviewOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetSurveyReviewOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetSurveyReviewOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetSurveyReviewOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetSurveyReviewOutputReply
    setSuccess(boolean success)
    {
        return (GetSurveyReviewOutputReply)super.setSuccess(success);
    }

    @Override
    public GetSurveyReviewOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetSurveyReviewOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetSurveyReviewOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetSurveyReviewOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetSurveyReviewOutputReply
    setException(ExceptionData exception)
    {
        return (GetSurveyReviewOutputReply)super.setException(exception);
    }

    @Override
    public GetSurveyReviewOutputReply
    setOutput(SurveyReviewOutput output)
    {
        return (GetSurveyReviewOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
