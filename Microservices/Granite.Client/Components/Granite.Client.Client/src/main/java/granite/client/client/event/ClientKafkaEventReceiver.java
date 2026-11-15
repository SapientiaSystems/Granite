//////////////////////////////////////////////////////////////////////////////
// ClientKafkaEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.client.event;

import granite.client.service.event.IClientEventListener;
import granite.client.service.event.IClientEventReceiver;
import granite.client.service.event.ClientEvent;
import strata.foundation.kafka.event.AbstractKafkaEventReceiver;

import java.util.Map;

public
class ClientKafkaEventReceiver
    extends AbstractKafkaEventReceiver<ClientEvent,IClientEventListener>
    implements IClientEventReceiver
{
    public
    ClientKafkaEventReceiver(Map<String,Object> p,String topic)
    {
        super(p,ClientEvent.class,topic);
    }
}

//////////////////////////////////////////////////////////////////////////////
