//////////////////////////////////////////////////////////////////////////////
// AgentPendingActionData.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import java.io.Serializable;

public
class AgentPendingActionData
    implements Serializable
{
    private String toolUseId;
    private String toolName;
    private String summary;
    private String input;

    public
    AgentPendingActionData()
    {
        toolUseId = null;
        toolName  = null;
        summary   = null;
        input     = null;
    }

    public AgentPendingActionData
    setToolUseId(String value)
    {
        toolUseId = value;
        return this;
    }

    public AgentPendingActionData
    setToolName(String value)
    {
        toolName = value;
        return this;
    }

    public AgentPendingActionData
    setSummary(String value)
    {
        summary = value;
        return this;
    }

    public AgentPendingActionData
    setInput(String value)
    {
        input = value;
        return this;
    }

    public String
    getToolUseId() { return toolUseId; }

    public String
    getToolName() { return toolName; }

    public String
    getSummary() { return summary; }

    public String
    getInput() { return input; }
}

//////////////////////////////////////////////////////////////////////////////
