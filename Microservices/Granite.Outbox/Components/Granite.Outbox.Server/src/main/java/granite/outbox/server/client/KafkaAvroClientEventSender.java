//////////////////////////////////////////////////////////////////////////////
// KafkaAvroClientEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.client;

import granite.client.service.event.IClientEventSender;
import granite.client.service.event.ClientEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroClientEventSender
    extends AbstractKafkaAvroEventSender<ClientEvent>
    implements IClientEventSender
{
    public
    KafkaAvroClientEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            ClientEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
