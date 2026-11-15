//////////////////////////////////////////////////////////////////////////////
// ClientEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.client;

import granite.client.service.event.IClientEventSender;
import granite.client.service.event.ClientEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class ClientEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<ClientEvent,IClientEventSender>
{
    public
    ClientEventOutboxEventReceiver(IClientEventSender sender)
        throws SendException
    {
        super(ClientEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
