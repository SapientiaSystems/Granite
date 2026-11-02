//////////////////////////////////////////////////////////////////////////////
// OutboxEventContentEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.platform;

import granite.content.service.event.IContentEventSender;
import granite.content.service.event.ContentEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventContentEventSender
    extends OutboxEventServiceEventSender<ContentEvent,IContentEventSender>
    implements IContentEventSender
{
    public
    OutboxEventContentEventSender(IOutboxEventRepository repository)
    {
        super(new ContentEventToOutboxEventMapper(),repository);
    }

    @Override
    protected IContentEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
