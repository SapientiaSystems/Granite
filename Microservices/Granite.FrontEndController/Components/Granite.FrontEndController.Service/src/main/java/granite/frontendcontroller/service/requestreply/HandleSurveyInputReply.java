//////////////////////////////////////////////////////////////////////////////
// HandleSurveyInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleSurveyInputReply
    extends HandleInputReply<SurveyInput,SurveyOutput>
{
    public
    HandleSurveyInputReply()
    {
        super();
    }

    public
    HandleSurveyInputReply(HandleSurveyInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleSurveyInputReply
    setReplyId(UUID replyId)
    {
        return (HandleSurveyInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleSurveyInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleSurveyInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleSurveyInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleSurveyInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleSurveyInputReply
    setSuccess(boolean success)
    {
        return (HandleSurveyInputReply)super.setSuccess(success);
    }

    @Override
    public HandleSurveyInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleSurveyInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleSurveyInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleSurveyInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleSurveyInputReply
    setException(ExceptionData exception)
    {
        return (HandleSurveyInputReply)super.setException(exception);
    }

    @Override
    public HandleSurveyInputReply
    setInput(SurveyInput input)
    {
        return (HandleSurveyInputReply)super.setInput(input);
    }

    @Override
    public HandleSurveyInputReply
    setOutput(SurveyOutput output)
    {
        return (HandleSurveyInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
