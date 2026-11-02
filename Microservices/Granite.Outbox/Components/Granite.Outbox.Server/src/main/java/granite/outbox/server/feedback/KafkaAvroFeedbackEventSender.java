//////////////////////////////////////////////////////////////////////////////
// KafkaAvroFeedbackEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.feedback;

import granite.feedback.service.event.IFeedbackEventSender;
import granite.feedback.service.event.FeedbackEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroFeedbackEventSender
    extends AbstractKafkaAvroEventSender<FeedbackEvent>
    implements IFeedbackEventSender
{
    public
    KafkaAvroFeedbackEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            FeedbackEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
