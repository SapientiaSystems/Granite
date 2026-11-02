//////////////////////////////////////////////////////////////////////////////
// FindAgentConversationsReply.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import strata.foundation.core.transfer.AbstractServiceReply;
import strata.foundation.core.transfer.ExceptionData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public
class FindAgentConversationsReply
    extends AbstractServiceReply
{
    private List<ConversationData> conversations;

    public
    FindAgentConversationsReply()
    {
        super();
        conversations = new ArrayList<>();
    }

    public
    FindAgentConversationsReply(FindAgentConversationsRequest originatingRequest)
    {
        super(originatingRequest);
        conversations = new ArrayList<>();
    }

    @Override
    public FindAgentConversationsReply
    setReplyId(UUID replyId)
    {
        return (FindAgentConversationsReply)super.setReplyId(replyId);
    }

    @Override
    public FindAgentConversationsReply
    setOriginatingRequestId(UUID requestId)
    {
        return (FindAgentConversationsReply)super.setOriginatingRequestId(requestId);
    }

    @Override
    public FindAgentConversationsReply
    setTimestamp(Instant timestamp)
    {
        return (FindAgentConversationsReply)super.setTimestamp(timestamp);
    }

    @Override
    public FindAgentConversationsReply
    setSuccess(boolean success)
    {
        return (FindAgentConversationsReply)super.setSuccess(success);
    }

    @Override
    public FindAgentConversationsReply
    setSuccessMessage(String successMessage)
    {
        return (FindAgentConversationsReply)super.setSuccessMessage(successMessage);
    }

    @Override
    public FindAgentConversationsReply
    setFailureMessage(String failureMessage)
    {
        return (FindAgentConversationsReply)super.setFailureMessage(failureMessage);
    }

    @Override
    public FindAgentConversationsReply
    setException(ExceptionData exception)
    {
        return (FindAgentConversationsReply)super.setException(exception);
    }

    public FindAgentConversationsReply
    setConversations(List<ConversationData> value)
    {
        conversations = value;
        return this;
    }

    public List<ConversationData>
    getConversations() { return conversations; }
}

//////////////////////////////////////////////////////////////////////////////
