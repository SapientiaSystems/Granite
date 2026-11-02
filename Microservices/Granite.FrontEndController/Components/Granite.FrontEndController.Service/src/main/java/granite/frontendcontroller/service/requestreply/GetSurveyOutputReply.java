//////////////////////////////////////////////////////////////////////////////
// GetSurveyOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetSurveyOutputReply
    extends GetOutputReply<SurveyOutput>
{
    public
    GetSurveyOutputReply()
    {
        super();
    }

    public
    GetSurveyOutputReply(GetSurveyOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetSurveyOutputReply
    setReplyId(UUID replyId)
    {
        return (GetSurveyOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetSurveyOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetSurveyOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetSurveyOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetSurveyOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetSurveyOutputReply
    setSuccess(boolean success)
    {
        return (GetSurveyOutputReply)super.setSuccess(success);
    }

    @Override
    public GetSurveyOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetSurveyOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetSurveyOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetSurveyOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetSurveyOutputReply
    setException(ExceptionData exception)
    {
        return (GetSurveyOutputReply)super.setException(exception);
    }

    @Override
    public GetSurveyOutputReply
    setOutput(SurveyOutput output)
    {
        return (GetSurveyOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
