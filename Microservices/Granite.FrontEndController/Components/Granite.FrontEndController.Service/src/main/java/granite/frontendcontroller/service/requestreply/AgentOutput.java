//////////////////////////////////////////////////////////////////////////////
// AgentOutput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import granite.agent.service.requestreply.ConversationData;
import strata.frontendcontroller.service.requestreply.FrontEndOutput;
import java.util.ArrayList;
import java.util.List;

public
class AgentOutput
    extends FrontEndOutput
{
    private ConversationData       conversation;
    private List<ConversationData> conversations;
    private List<String>           failureMessages;

    public
    AgentOutput()
    {
        super();
        conversation    = null;
        conversations   = new ArrayList<>();
        failureMessages = new ArrayList<>();
    }

    @Override
    public AgentOutput
    setUserId(Long id)
    {
        return (AgentOutput)super.setUserId(id);
    }

    @Override
    public AgentOutput
    setSource(String src)
    {
        return (AgentOutput)super.setSource(src);
    }

    @Override
    public AgentOutput
    setDestination(String dest)
    {
        return (AgentOutput)super.setDestination(dest);
    }

    @Override
    public AgentOutput
    setCommand(String cmd)
    {
        return (AgentOutput)super.setCommand(cmd);
    }

    public AgentOutput
    setConversation(ConversationData value)
    {
        conversation = value;
        return this;
    }

    public AgentOutput
    setConversations(List<ConversationData> value)
    {
        conversations = value;
        return this;
    }

    public AgentOutput
    setFailureMessages(List<String> value)
    {
        failureMessages = value;
        return this;
    }

    public ConversationData
    getConversation() { return conversation; }

    public List<ConversationData>
    getConversations() { return conversations; }

    public List<String>
    getFailureMessages() { return failureMessages; }
}

//////////////////////////////////////////////////////////////////////////////
