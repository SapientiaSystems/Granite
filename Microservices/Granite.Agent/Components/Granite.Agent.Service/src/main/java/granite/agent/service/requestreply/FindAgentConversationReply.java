//////////////////////////////////////////////////////////////////////////////
// FindAgentConversationReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class FindAgentConversationReply
    extends AbstractServiceReply
{
    private ConversationData conversation;

    public
    FindAgentConversationReply()
    {
        super();
        conversation = null;
    }

    public
    FindAgentConversationReply(FindAgentConversationRequest originatingRequest)
    {
        super(originatingRequest);
        conversation = null;
    }

    @Override
    public FindAgentConversationReply
    setReplyId(UUID replyId)
    {
        return (FindAgentConversationReply)super.setReplyId(replyId);
    }

    @Override
    public FindAgentConversationReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAgentConversationReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAgentConversationReply
    setTimestamp(Instant timestamp)
    {
        return (FindAgentConversationReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAgentConversationReply
    setSuccess(boolean success)
    {
        return (FindAgentConversationReply)super.setSuccess(success);
    }

    @Override
    public FindAgentConversationReply
    setSuccessMessage(String successMessage)
    {
        return (FindAgentConversationReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAgentConversationReply
    setFailureMessage(String failureMessage)
    {
        return (FindAgentConversationReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAgentConversationReply
    setException(ExceptionData exception)
    {
        return (FindAgentConversationReply)super.setException(exception);
    }

    public FindAgentConversationReply
    setConversation(ConversationData value)
    {
        conversation = value;
        return this;
    }

    public ConversationData
    getConversation() { return conversation; }
}

//////////////////////////////////////////////////////////////////////////////
