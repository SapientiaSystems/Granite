//////////////////////////////////////////////////////////////////////////////
// KafkaAvroContentEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.content;

import granite.content.service.event.IContentEventSender;
import granite.content.service.event.ContentEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroContentEventSender
    extends AbstractKafkaAvroEventSender<ContentEvent>
    implements IContentEventSender
{
    public
    KafkaAvroContentEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            ContentEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
