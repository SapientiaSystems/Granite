//////////////////////////////////////////////////////////////////////////////
// ClientKafkaAvroEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.client.event;

import granite.client.service.event.IClientEventListener;
import granite.client.service.event.IClientEventReceiver;
import granite.client.service.event.ClientEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventReceiver;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

import java.util.HashMap;
import java.util.Map;

public
class ClientKafkaAvroEventReceiver
    extends AbstractKafkaAvroEventReceiver<ClientEvent,IClientEventListener>
    implements IClientEventReceiver
{
    public
    ClientKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic)
    {
        super(consumerProperties(provider,topic),ClientEvent.class,topic);
    }

    public
    ClientKafkaAvroEventReceiver(
        IKafkaConfigurationProvider provider,
        String                      topic,
        IClientEventListener      listener)
    {
        super(consumerProperties(provider,topic),ClientEvent.class,topic);
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
