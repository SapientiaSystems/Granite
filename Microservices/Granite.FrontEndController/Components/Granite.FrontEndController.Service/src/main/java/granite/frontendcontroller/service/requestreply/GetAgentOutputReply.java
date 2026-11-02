//////////////////////////////////////////////////////////////////////////////
// GetAgentOutputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.GetOutputReply;

import java.time.Instant;
import java.util.UUID;

public
class GetAgentOutputReply
    extends GetOutputReply<AgentOutput>
{
    public
    GetAgentOutputReply()
    {
        super();
    }

    public
    GetAgentOutputReply(GetAgentOutputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public GetAgentOutputReply
    setReplyId(UUID replyId)
    {
        return (GetAgentOutputReply)super.setReplyId(replyId);
    }

    @Override
    public GetAgentOutputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (GetAgentOutputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public GetAgentOutputReply
    setTimestamp(Instant timestamp)
    {
        return (GetAgentOutputReply)super.setTimestamp(timestamp);
    }

    @Override
    public GetAgentOutputReply
    setSuccess(boolean success)
    {
        return (GetAgentOutputReply)super.setSuccess(success);
    }

    @Override
    public GetAgentOutputReply
    setSuccessMessage(String successMessage)
    {
        return (GetAgentOutputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public GetAgentOutputReply
    setFailureMessage(String failureMessage)
    {
        return (GetAgentOutputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public GetAgentOutputReply
    setException(ExceptionData exception)
    {
        return (GetAgentOutputReply)super.setException(exception);
    }

    @Override
    public GetAgentOutputReply
    setOutput(AgentOutput output)
    {
        return (GetAgentOutputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
