//////////////////////////////////////////////////////////////////////////////
// UserKafkaEventReceiver.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.client.event;

import granite.user.service.event.IUserEventListener;
import granite.user.service.event.IUserEventReceiver;
import granite.user.service.event.UserEvent;
import strata.foundation.kafka.event.AbstractKafkaEventReceiver;

import java.util.Map;

public
class UserKafkaEventReceiver
    extends AbstractKafkaEventReceiver<UserEvent,IUserEventListener>
    implements IUserEventReceiver
{
    public
    UserKafkaEventReceiver(Map<String,Object> p,String topic)
    {
        super(p,UserEvent.class,topic);
    }
}

//////////////////////////////////////////////////////////////////////////////
