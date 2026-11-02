//////////////////////////////////////////////////////////////////////////////
// OutboxEventAgentEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.platform;

import granite.agent.service.event.IAgentEventSender;
import granite.agent.service.event.AgentEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventAgentEventSender
    extends OutboxEventServiceEventSender<AgentEvent,IAgentEventSender>
    implements IAgentEventSender
{
    public
    OutboxEventAgentEventSender(IOutboxEventRepository repository)
    {
        super(new AgentEventToOutboxEventMapper(),repository);
    }

    @Override
    protected IAgentEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
