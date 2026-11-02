//////////////////////////////////////////////////////////////////////////////
// AgentToolOutcome.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

public
class AgentToolOutcome
{
    private String  toolUseId;
    private String  toolName;
    private String  content;
    private boolean error;

    public
    AgentToolOutcome()
    {
        toolUseId = null;
        toolName  = null;
        content   = null;
        error     = false;
    }

    public AgentToolOutcome
    setToolUseId(String value)
    {
        toolUseId = value;
        return this;
    }

    public AgentToolOutcome
    setToolName(String value)
    {
        toolName = value;
        return this;
    }

    public AgentToolOutcome
    setContent(String value)
    {
        content = value;
        return this;
    }

    public AgentToolOutcome
    setError(boolean value)
    {
        error = value;
        return this;
    }

    public String
    getToolUseId() { return toolUseId; }

    public String
    getToolName() { return toolName; }

    public String
    getContent() { return content; }

    public boolean
    isError() { return error; }
}

//////////////////////////////////////////////////////////////////////////////
