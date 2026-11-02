//////////////////////////////////////////////////////////////////////////////
// OutboxEventFeedbackEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import granite.feedback.service.event.IFeedbackEventSender;
import granite.feedback.service.event.FeedbackEvent;
import strata.outbox.core.repository.IOutboxEventRepository;
import strata.outbox.core.sender.OutboxEventServiceEventSender;

public
class OutboxEventFeedbackEventSender
    extends OutboxEventServiceEventSender<FeedbackEvent,IFeedbackEventSender>
    implements IFeedbackEventSender
{
    public
    OutboxEventFeedbackEventSender(IOutboxEventRepository repository)
    {
        super(new FeedbackEventToOutboxEventMapper(),repository);
    }

    @Override
    protected IFeedbackEventSender
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
