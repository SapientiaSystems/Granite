//////////////////////////////////////////////////////////////////////////////
// AgentEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.agent;

import granite.agent.service.event.IAgentEventSender;
import granite.agent.service.event.AgentEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class AgentEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<AgentEvent,IAgentEventSender>
{
    public
    AgentEventOutboxEventReceiver(IAgentEventSender sender)
        throws SendException
    {
        super(AgentEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
