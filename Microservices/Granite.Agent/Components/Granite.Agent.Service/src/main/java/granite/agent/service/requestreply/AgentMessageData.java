//////////////////////////////////////////////////////////////////////////////
// AgentMessageData.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.service.requestreply;

import java.io.Serializable;
import java.time.Instant;

public
class AgentMessageData
    implements Serializable
{
    private Integer          seq;
    private AgentMessageRole role;
    private String           text;
    private String           toolName;
    private Boolean          isError;
    private Instant          at;

    public
    AgentMessageData()
    {
        seq      = null;
        role     = null;
        text     = null;
        toolName = null;
        isError  = null;
        at       = null;
    }

    public AgentMessageData
    setSeq(Integer value)
    {
        seq = value;
        return this;
    }

    public AgentMessageData
    setRole(AgentMessageRole value)
    {
        role = value;
        return this;
    }

    public AgentMessageData
    setText(String value)
    {
        text = value;
        return this;
    }

    public AgentMessageData
    setToolName(String value)
    {
        toolName = value;
        return this;
    }

    public AgentMessageData
    setIsError(Boolean value)
    {
        isError = value;
        return this;
    }

    public AgentMessageData
    setAt(Instant value)
    {
        at = value;
        return this;
    }

    public Integer
    getSeq() { return seq; }

    public AgentMessageRole
    getRole() { return role; }

    public String
    getText() { return text; }

    public String
    getToolName() { return toolName; }

    public Boolean
    getIsError() { return isError; }

    public Instant
    getAt() { return at; }
}

//////////////////////////////////////////////////////////////////////////////
