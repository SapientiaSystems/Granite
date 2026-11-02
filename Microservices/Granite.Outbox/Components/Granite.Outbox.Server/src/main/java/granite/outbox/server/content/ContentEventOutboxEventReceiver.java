//////////////////////////////////////////////////////////////////////////////
// ContentEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.content;

import granite.content.service.event.IContentEventSender;
import granite.content.service.event.ContentEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class ContentEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<ContentEvent,IContentEventSender>
{
    public
    ContentEventOutboxEventReceiver(IContentEventSender sender)
        throws SendException
    {
        super(ContentEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
