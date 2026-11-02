//////////////////////////////////////////////////////////////////////////////
// AgentTurnRequest.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import java.util.ArrayList;
import java.util.List;

public
class AgentTurnRequest
{
    private String                 history;
    private String                 userText;
    private List<AgentToolOutcome> toolOutcomes;
    private List<IAgentTool>       tools;
    private String                 systemPrompt;

    public
    AgentTurnRequest()
    {
        history      = null;
        userText     = null;
        toolOutcomes = new ArrayList<>();
        tools        = new ArrayList<>();
        systemPrompt = null;
    }

    public AgentTurnRequest
    setHistory(String value)
    {
        history = value;
        return this;
    }

    public AgentTurnRequest
    setUserText(String value)
    {
        userText = value;
        return this;
    }

    public AgentTurnRequest
    setToolOutcomes(List<AgentToolOutcome> value)
    {
        toolOutcomes = value;
        return this;
    }

    public AgentTurnRequest
    setTools(List<IAgentTool> value)
    {
        tools = value;
        return this;
    }

    public AgentTurnRequest
    setSystemPrompt(String value)
    {
        systemPrompt = value;
        return this;
    }

    public String
    getHistory() { return history; }

    public String
    getUserText() { return userText; }

    public List<AgentToolOutcome>
    getToolOutcomes() { return toolOutcomes; }

    public List<IAgentTool>
    getTools() { return tools; }

    public String
    getSystemPrompt() { return systemPrompt; }
}

//////////////////////////////////////////////////////////////////////////////
