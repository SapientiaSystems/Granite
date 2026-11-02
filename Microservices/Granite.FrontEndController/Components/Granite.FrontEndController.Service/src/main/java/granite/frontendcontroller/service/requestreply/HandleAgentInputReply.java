//////////////////////////////////////////////////////////////////////////////
// HandleAgentInputReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.foundation.core.transfer.ExceptionData;
import strata.frontendcontroller.service.requestreply.HandleInputReply;

import java.time.Instant;
import java.util.UUID;

public
class HandleAgentInputReply
    extends HandleInputReply<AgentInput,AgentOutput>
{
    public
    HandleAgentInputReply()
    {
        super();
    }

    public
    HandleAgentInputReply(HandleAgentInputRequest originatingRequest)
    {
        super(originatingRequest);
    }

    @Override
    public HandleAgentInputReply
    setReplyId(UUID replyId)
    {
        return (HandleAgentInputReply)super.setReplyId(replyId);
    }

    @Override
    public HandleAgentInputReply
    setOriginatingRequestId(UUID requestId)
    {
        return (HandleAgentInputReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public HandleAgentInputReply
    setTimestamp(Instant timestamp)
    {
        return (HandleAgentInputReply)super.setTimestamp(timestamp);
    }

    @Override
    public HandleAgentInputReply
    setSuccess(boolean success)
    {
        return (HandleAgentInputReply)super.setSuccess(success);
    }

    @Override
    public HandleAgentInputReply
    setSuccessMessage(String successMessage)
    {
        return (HandleAgentInputReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public HandleAgentInputReply
    setFailureMessage(String failureMessage)
    {
        return (HandleAgentInputReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public HandleAgentInputReply
    setException(ExceptionData exception)
    {
        return (HandleAgentInputReply)super.setException(exception);
    }

    @Override
    public HandleAgentInputReply
    setInput(AgentInput input)
    {
        return (HandleAgentInputReply)super.setInput(input);
    }

    @Override
    public HandleAgentInputReply
    setOutput(AgentOutput output)
    {
        return (HandleAgentInputReply)super.setOutput(output);
    }
}

//////////////////////////////////////////////////////////////////////////////
