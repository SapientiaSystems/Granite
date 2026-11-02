//////////////////////////////////////////////////////////////////////////////
// HandleSurveyReviewInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleSurveyReviewInputReply
    extends HandleInputReply<SurveyReviewInput,SurveyReviewOutput>
{
    public
    HandleSurveyReviewInputReply()
    {
        super();
    }

    public
    HandleSurveyReviewInputReply(HandleSurveyReviewInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleSurveyReviewInputReply
    setReplyId(UUID replyId)
    {
        return (HandleSurveyReviewInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleSurveyReviewInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleSurveyReviewInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleSurveyReviewInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleSurveyReviewInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSurveyReviewInputReply
    setSuccess(boolean success)
    {
        return (HandleSurveyReviewInputReply)super.setSuccess(success);
    }

    @Override
    public HandleSurveyReviewInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleSurveyReviewInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleSurveyReviewInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleSurveyReviewInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleSurveyReviewInputReply
    setException(ExceptionData exception)
    {
        return (HandleSurveyReviewInputReply)super.setException(exception);
    }

    @Override
    public HandleSurveyReviewInputReply
    setInput(SurveyReviewInput input)
    {
        return (HandleSurveyReviewInputReply)super.setInput(input);
    }

    @Override
    public HandleSurveyReviewInputReply
    setOutput(SurveyReviewOutput output)
    {
        return (HandleSurveyReviewInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
