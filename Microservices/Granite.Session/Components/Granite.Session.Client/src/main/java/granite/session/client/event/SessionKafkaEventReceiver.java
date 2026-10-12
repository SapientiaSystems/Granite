//////////////////////////////////////////////////////////////////////////////
// SessionKafkaEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.client.event;

import granite.session.service.event.ISessionEventListener;
import granite.session.service.event.ISessionEventReceiver;
import granite.session.service.event.SessionEvent;
import strata.foundation.kafka.event.AbstractKafkaEventReceiver;

import java.util.Map;

public
class SessionKafkaEventReceiver
    extends AbstractKafkaEventReceiver<SessionEvent,ISessionEventListener>
    implements ISessionEventReceiver
{
    public
    SessionKafkaEventReceiver(Map<String,Object> p,String topic)
    {
        super(p,SessionEvent.class,topic);
    }
}

//////////////////////////////////////////////////////////////////////////////
