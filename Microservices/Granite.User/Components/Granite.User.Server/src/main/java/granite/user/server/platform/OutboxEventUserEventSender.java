/// ///////////////////////////////////////////////////////////////////////////
// OutboxEventUserEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventUserEventSender
    extends OutboxEventServiceEventSender<UserEvent,IUserEventSender>
    implements IUserEventSender
{
    public
    OutboxEventUserEventSender(IOutboxEventRepository repository)
    {
        super(new UserEventToOutboxEventMapper(),repository);
    }

    @Override
    protected IUserEventSender
    getSelf()
    {
        return this;
    }

}

//////////////////////////////////////////////////////////////////////////////
