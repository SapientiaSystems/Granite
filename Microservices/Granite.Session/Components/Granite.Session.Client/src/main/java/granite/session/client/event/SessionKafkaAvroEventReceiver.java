//////////////////////////////////////////////////////////////////////////////
// SessionKafkaAvroEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.event;

import granite.session.service.event.ISessionEventListener;
import granite.session.service.event.ISessionEventReceiver;
import granite.session.service.event.SessionEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventReceiver;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public
class SessionKafkaAvroEventReceiver
    extends AbstractKafkaAvroEventReceiver<SessionEvent,ISessionEventListener>
    implements ISessionEventReceiver
{
    public
    SessionKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic)
    {
        super(consumerProperties(provider,topic),SessionEvent.class,topic);
    }

    public
    SessionKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic,
        ISessionEventListener       listener)
    {
        super(consumerProperties(provider,topic),SessionEvent.class,topic);
        setListener(listener);
    }

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
