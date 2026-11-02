//////////////////////////////////////////////////////////////////////////////
// SendAgentMessageReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.UUID;

public
class SendAgentMessageReply
    extends AbstractServiceReply
{
    private ConversationData conversation;

    public
    SendAgentMessageReply()
    {
        super();
        conversation = null;
    }

    public
    SendAgentMessageReply(SendAgentMessageRequest originatingRequest)
    {
        super(originatingRequest);
        conversation = null;
    }

    @Override
    public SendAgentMessageReply
    setReplyId(UUID replyId)
    {
        return (SendAgentMessageReply)super.setReplyId(replyId);
    }

    @Override
    public SendAgentMessageReply
    setOriginatingRequestId(UUID requestId)
    {
        return (SendAgentMessageReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public SendAgentMessageReply
    setTimestamp(Instant timestamp)
    {
        return (SendAgentMessageReply)super.setTimestamp(timestamp);
    }

    @Override
    public SendAgentMessageReply
    setSuccess(boolean success)
    {
        return (SendAgentMessageReply)super.setSuccess(success);
    }

    @Override
    public SendAgentMessageReply
    setSuccessMessage(String successMessage)
    {
        return (SendAgentMessageReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public SendAgentMessageReply
    setFailureMessage(String failureMessage)
    {
        return (SendAgentMessageReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public SendAgentMessageReply
    setException(ExceptionData exception)
    {
        return (SendAgentMessageReply)super.setException(exception);
    }

    public SendAgentMessageReply
    setConversation(ConversationData value)
    {
        conversation = value;
        return this;
    }

    public ConversationData
    getConversation() { return conversation; }
}

//////////////////////////////////////////////////////////////////////////////
