//////////////////////////////////////////////////////////////////////////////
// FeedbackEventOutboxEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.feedback;

import granite.feedback.service.event.IFeedbackEventSender;
import granite.feedback.service.event.FeedbackEvent;
import strata.outbox.core.receiver.ServiceEventOutboxEventReceiver;
import strata.outbox.core.sender.SendException;

public
class FeedbackEventOutboxEventReceiver
    extends ServiceEventOutboxEventReceiver<FeedbackEvent,IFeedbackEventSender>
{
    public
    FeedbackEventOutboxEventReceiver(IFeedbackEventSender sender)
        throws SendException
    {
        super(FeedbackEvent.class,sender);
    }
}

//////////////////////////////////////////////////////////////////////////////
