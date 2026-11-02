//////////////////////////////////////////////////////////////////////////////
// AgentInput.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.FrontEndInput;

public
class AgentInput
    extends FrontEndInput
{
    private Long   conversationId;
    private String text;
    private String toolUseId;

    public
    AgentInput()
    {
        super();
        conversationId = null;
        text           = null;
        toolUseId      = null;
    }

    @Override
    public AgentInput
    setUserId(Long id)
    {
        return (AgentInput)super.setUserId(id);
    }

    @Override
    public AgentInput
    setSource(String src)
    {
        return (AgentInput)super.setSource(src);
    }

    @Override
    public AgentInput
    setDestination(String dest)
    {
        return (AgentInput)super.setDestination(dest);
    }

    @Override
    public AgentInput
    setCommand(String cmd)
    {
        return (AgentInput)super.setCommand(cmd);
    }

    public AgentInput
    setConversationId(Long value)
    {
        conversationId = value;
        return this;
    }

    public AgentInput
    setText(String value)
    {
        text = value;
        return this;
    }

    public AgentInput
    setToolUseId(String value)
    {
        toolUseId = value;
        return this;
    }

    public Long
    getConversationId() { return conversationId; }

    public String
    getText() { return text; }

    public String
    getToolUseId() { return toolUseId; }
}

//////////////////////////////////////////////////////////////////////////////
