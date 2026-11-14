//////////////////////////////////////////////////////////////////////////////
// OutboxEventClientEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.service.event.IClientEventSender;
import granite.client.service.event.ClientEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventClientEventSender
    extends OutboxEventServiceEventSender<ClientEvent,IClientEventSender>
    implements IClientEventSender
{
    public
    OutboxEventClientEventSender(IOutboxEventRepository repository)
    {
        super(new ClientEventToOutboxEventMapper(),repository);
    }

    @Override
    protected IClientEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
