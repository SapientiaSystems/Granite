//////////////////////////////////////////////////////////////////////////////
// CapturingClientEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.application;

import granite.client.service.event.ClientEvent;
import granite.client.service.event.IClientEventSender;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class CapturingClientEventSender
    extends OutboxEventServiceEventSender<ClientEvent,IClientEventSender>
    implements IClientEventSender
{
    public
    CapturingClientEventSender(IOutboxEventRepository repository)
    {
        super(new CapturingClientEventMapper(),repository);
    }

    @Override
    protected IClientEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
