//////////////////////////////////////////////////////////////////////////////
// AgentTurnStep.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import java.util.ArrayList;
import java.util.List;

public
class AgentTurnStep
{
    private String              history;
    private String              text;
    private List<AgentToolCall> toolCalls;
    private String              stopReason;
    private long                inputTokens;
    private long                outputTokens;

    public
    AgentTurnStep()
    {
        history      = null;
        text         = null;
        toolCalls    = new ArrayList<>();
        stopReason   = null;
        inputTokens  = 0;
        outputTokens = 0;
    }

    public AgentTurnStep
    setHistory(String value)
    {
        history = value;
        return this;
    }

    public AgentTurnStep
    setText(String value)
    {
        text = value;
        return this;
    }

    public AgentTurnStep
    setToolCalls(List<AgentToolCall> value)
    {
        toolCalls = value;
        return this;
    }

    public AgentTurnStep
    setStopReason(String value)
    {
        stopReason = value;
        return this;
    }

    public AgentTurnStep
    setInputTokens(long value)
    {
        inputTokens = value;
        return this;
    }

    public AgentTurnStep
    setOutputTokens(long value)
    {
        outputTokens = value;
        return this;
    }

    public String
    getHistory() { return history; }

    public String
    getText() { return text; }

    public List<AgentToolCall>
    getToolCalls() { return toolCalls; }

    public String
    getStopReason() { return stopReason; }

    public long
    getInputTokens() { return inputTokens; }

    public long
    getOutputTokens() { return outputTokens; }
}

//////////////////////////////////////////////////////////////////////////////
