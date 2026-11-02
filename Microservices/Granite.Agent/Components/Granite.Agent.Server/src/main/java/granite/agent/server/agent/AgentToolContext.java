//////////////////////////////////////////////////////////////////////////////
// AgentToolContext.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

import java.util.Objects;
import java.util.Set;

public
class AgentToolContext
{
    private final Long                  userId;
    private final Long                  sessionId;
    private final Set<String>           authorities;
    private final IDelegatedTokenSource tokens;
    private String                      delegatedToken;

    public
    AgentToolContext(Long userId,Long sessionId,Set<String> authorities,IDelegatedTokenSource tokens)
    {
        this.userId         = userId;
        this.sessionId      = sessionId;
        this.authorities    = authorities == null ? Set.of() : Set.copyOf(authorities);
        this.tokens         = tokens;
        this.delegatedToken = null;
    }

    public Long
    getUserId() { return userId; }

    public Long
    getSessionId() { return sessionId; }

    public Set<String>
    getAuthorities() { return authorities; }

    public String
    getDelegatedToken()
    {
        if (delegatedToken == null)
            delegatedToken = Objects.requireNonNull(tokens,"no delegated token source").issue(sessionId,userId);

        return delegatedToken;
    }
}

//////////////////////////////////////////////////////////////////////////////
