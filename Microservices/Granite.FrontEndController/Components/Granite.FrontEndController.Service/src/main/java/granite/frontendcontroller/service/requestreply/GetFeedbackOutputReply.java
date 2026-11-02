//////////////////////////////////////////////////////////////////////////////
// GetFeedbackOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetFeedbackOutputReply
    extends GetOutputReply<FeedbackOutput>
{
    public
    GetFeedbackOutputReply()
    {
        super();
    }

    public
    GetFeedbackOutputReply(GetFeedbackOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetFeedbackOutputReply
    setReplyId(UUID replyId)
    {
        return (GetFeedbackOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetFeedbackOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetFeedbackOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetFeedbackOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetFeedbackOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetFeedbackOutputReply
    setSuccess(boolean success)
    {
        return (GetFeedbackOutputReply)super.setSuccess(success);
    }

    @Override
    public GetFeedbackOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetFeedbackOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetFeedbackOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetFeedbackOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetFeedbackOutputReply
    setException(ExceptionData exception)
    {
        return (GetFeedbackOutputReply)super.setException(exception);
    }

    @Override
    public GetFeedbackOutputReply
    setOutput(FeedbackOutput output)
    {
        return (GetFeedbackOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
