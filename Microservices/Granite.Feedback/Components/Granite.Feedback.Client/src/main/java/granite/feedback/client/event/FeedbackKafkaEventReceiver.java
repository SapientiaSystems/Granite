//////////////////////////////////////////////////////////////////////////////
// FeedbackKafkaEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.client.event;

import granite.feedback.service.event.IFeedbackEventListener;
import granite.feedback.service.event.IFeedbackEventReceiver;
import granite.feedback.service.event.FeedbackEvent;
import strata.foundation.kafka.event.AbstractKafkaEventReceiver;

import java.util.Map;

public
class FeedbackKafkaEventReceiver
    extends AbstractKafkaEventReceiver<FeedbackEvent,IFeedbackEventListener>
    implements IFeedbackEventReceiver
{
    public
    FeedbackKafkaEventReceiver(Map<String,Object> p,String topic)
    {
        super(p,FeedbackEvent.class,topic);
    }
}

//////////////////////////////////////////////////////////////////////////////
