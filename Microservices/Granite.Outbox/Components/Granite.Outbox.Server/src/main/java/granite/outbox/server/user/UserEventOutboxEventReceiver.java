//////////////////////////////////////////////////////////////////////////////
// UserEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.user;

import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class UserEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<UserEvent,IUserEventSender>
{
    public
    UserEventOutboxEventReceiver(IUserEventSender sender)
        throws SendException
    {
        super(UserEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
