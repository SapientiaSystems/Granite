//////////////////////////////////////////////////////////////////////////////
// OutboxEventSessionEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.service.event.ISessionEventSender;
import granite.session.service.event.SessionEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventSessionEventSender
    extends OutboxEventServiceEventSender<SessionEvent,ISessionEventSender>
    implements ISessionEventSender
{
    public
    OutboxEventSessionEventSender(IOutboxEventRepository repository)
    {
        super(new SessionEventToOutboxEventMapper(),repository);
    }

    @Override
    protected ISessionEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
