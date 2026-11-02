//////////////////////////////////////////////////////////////////////////////
// FeedbackKafkaAvroEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.client.event;

import granite.feedback.service.event.IFeedbackEventListener;
import granite.feedback.service.event.IFeedbackEventReceiver;
import granite.feedback.service.event.FeedbackEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventReceiver;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public
class FeedbackKafkaAvroEventReceiver
    extends AbstractKafkaAvroEventReceiver<FeedbackEvent,IFeedbackEventListener>
    implements IFeedbackEventReceiver
{
    public
    FeedbackKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic)
    {
        super(consumerProperties(provider,topic),FeedbackEvent.class,topic);
    }

    public
    FeedbackKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic,
        IFeedbackEventListener    listener)
    {
        super(consumerProperties(provider,topic),FeedbackEvent.class,topic);
        setListener(listener);
    }

    // The provider is producer-oriented and does not emit consumer-only keys.
    // Apply safe consumer defaults so Kafka's ConsumerConfig does not reject
    // the construction (e.g. "enable.auto.commit cannot be set to true when
    // default group id (null) is used"). Caller-supplied values always win.
    private static Map<String,Object>
    consumerProperties(IKafkaConfigurationProvider provider,String topic)
    {
        Map<String,Object> properties = new HashMap<>(provider.get());

        properties.putIfAbsent("group.id",topic);
        properties.putIfAbsent("enable.auto.commit","false");
        properties.putIfAbsent("auto.offset.reset","earliest");
        return properties;
    }
}

//////////////////////////////////////////////////////////////////////////////
