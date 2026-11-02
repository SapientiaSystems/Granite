//////////////////////////////////////////////////////////////////////////////
// IAgentTool.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import java.util.List;
import java.util.Map;
import java.util.Set;

public
interface IAgentTool
{
    String
    getName();

    String
    getDescription();

    Map<String,Object>
    getProperties();

    List<String>
    getRequired();

    Set<String>
    getRequiredAuthorities();

    boolean
    isMutating();

    String
    summarize(Map<String,Object> input);

    AgentToolResult
    execute(Map<String,Object> input,AgentToolContext context);
}

//////////////////////////////////////////////////////////////////////////////
