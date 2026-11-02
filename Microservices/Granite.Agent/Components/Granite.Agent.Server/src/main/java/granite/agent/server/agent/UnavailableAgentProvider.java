//////////////////////////////////////////////////////////////////////////////
// UnavailableAgentProvider.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.agent;

public
class UnavailableAgentProvider
    implements IAgentProvider
{
    public static final String MESSAGE = "The virtual assistant is not configured. Ask your administrator to provide a Claude API key.";

    @Override
    public AgentTurnStep
    step(AgentTurnRequest request)
    {
        return
            new AgentTurnStep()
                .setHistory(request.getHistory())
                .setText(MESSAGE)
                .setStopReason("unavailable");
    }
}

//////////////////////////////////////////////////////////////////////////////
