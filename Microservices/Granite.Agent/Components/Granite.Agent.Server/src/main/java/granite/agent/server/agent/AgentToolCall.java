//////////////////////////////////////////////////////////////////////////////
// AgentToolCall.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import java.util.LinkedHashMap;
import java.util.Map;

public
class AgentToolCall
{
    private String             id;
    private String             name;
    private Map<String,Object> input;

    public
    AgentToolCall()
    {
        id    = null;
        name  = null;
        input = new LinkedHashMap<>();
    }

    public AgentToolCall
    setId(String value)
    {
        id = value;
        return this;
    }

    public AgentToolCall
    setName(String value)
    {
        name = value;
        return this;
    }

    public AgentToolCall
    setInput(Map<String,Object> value)
    {
        input = value;
        return this;
    }

    public String
    getId() { return id; }

    public String
    getName() { return name; }

    public Map<String,Object>
    getInput() { return input; }
}

//////////////////////////////////////////////////////////////////////////////
