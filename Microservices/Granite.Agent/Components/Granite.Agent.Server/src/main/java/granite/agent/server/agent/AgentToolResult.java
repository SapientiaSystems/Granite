//////////////////////////////////////////////////////////////////////////////
// AgentToolResult.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

public
class AgentToolResult
{
    private final String  content;
    private final boolean error;

    public
    AgentToolResult(String content,boolean error)
    {
        this.content = content == null ? "" : content;
        this.error   = error;
    }

    public static AgentToolResult
    ok(String content) { return new AgentToolResult(content,false); }

    public static AgentToolResult
    error(String content) { return new AgentToolResult(content,true); }

    public String
    getContent() { return content; }

    public boolean
    isError() { return error; }
}

//////////////////////////////////////////////////////////////////////////////
