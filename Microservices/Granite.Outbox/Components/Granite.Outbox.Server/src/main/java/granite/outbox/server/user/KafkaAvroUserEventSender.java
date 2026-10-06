//////////////////////////////////////////////////////////////////////////////
// KafkaAvroUserEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.user;

import granite.user.service.event.IUserEventSender;
import granite.user.service.event.UserEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroUserEventSender
    extends AbstractKafkaAvroEventSender<UserEvent>
    implements IUserEventSender
{
    public
    KafkaAvroUserEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            UserEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
