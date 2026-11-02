//////////////////////////////////////////////////////////////////////////////
// ResolveAgentActionReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class ResolveAgentActionReply
    extends AbstractServiceReply
{
    private ConversationData conversation;

    public
    ResolveAgentActionReply()
    {
        super();
        conversation = null;
    }

    public
    ResolveAgentActionReply(ResolveAgentActionRequest originatingRequest)
    {
        super(originatingRequest);
        conversation = null;
    }

    @Override
    public ResolveAgentActionReply
    setReplyId(UUID replyId)
    {
        return (ResolveAgentActionReply)super.setReplyId(replyId);
    }

    @Override
    public ResolveAgentActionReply
    setOriginatingRequestId(UUID requestId)
    {
        return (ResolveAgentActionReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public ResolveAgentActionReply
    setTimestamp(Instant timestamp)
    {
        return (ResolveAgentActionReply)super.setTimestamp(timestamp);
    }

    @Override
    public ResolveAgentActionReply
    setSuccess(boolean success)
    {
        return (ResolveAgentActionReply)super.setSuccess(success);
    }

    @Override
    public ResolveAgentActionReply
    setSuccessMessage(String successMessage)
    {
        return (ResolveAgentActionReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public ResolveAgentActionReply
    setFailureMessage(String failureMessage)
    {
        return (ResolveAgentActionReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public ResolveAgentActionReply
    setException(ExceptionData exception)
    {
        return (ResolveAgentActionReply)super.setException(exception);
    }

    public ResolveAgentActionReply
    setConversation(ConversationData value)
    {
        conversation = value;
        return this;
    }

    public ConversationData
    getConversation() { return conversation; }
}

//////////////////////////////////////////////////////////////////////////////
