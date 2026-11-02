//////////////////////////////////////////////////////////////////////////////
// AgentOutputs.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.control.agent;

import granite.frontendcontroller.service.requestreply.AgentInput;
import granite.frontendcontroller.service.requestreply.AgentOutput;

import java.util.ArrayList;
import java.util.List;

public
class AgentOutputs
{
    public static final String NOT_AUTHENTICATED = "not authenticated";

    private
    AgentOutputs() {}

    public static AgentOutput
    failure(String message)
    {
        return new AgentOutput().setFailureMessages(new ArrayList<>(List.of(message)));
    }

    public static AgentOutput
    describe(AgentInput input,AgentOutput output,Long caller)
    {
        return
            output
                .setUserId(caller)
                .setSource(input.getSource())
                .setCommand(input.getCommand())
                .setDestination(input.getDestination().orElse(null));
    }

    public static String
    reason(String message)
    {
        return message == null || message.isBlank() ? "unknown error" : message;
    }
}

//////////////////////////////////////////////////////////////////////////////
