//////////////////////////////////////////////////////////////////////////////
// KafkaAvroAgentEventSender.java
//////////////////////////////////////////////////////////////////////////////

package granite.outbox.server.agent;

import granite.agent.service.event.IAgentEventSender;
import granite.agent.service.event.AgentEvent;
import strata.foundation.kafka.event.AbstractKafkaAvroEventSender;
import strata.foundation.kafka.event.IKafkaConfigurationProvider;

public
class KafkaAvroAgentEventSender
    extends AbstractKafkaAvroEventSender<AgentEvent>
    implements IAgentEventSender
{
    public
    KafkaAvroAgentEventSender(IKafkaConfigurationProvider provider,String topic)
    {
        super(
            provider.get(),
            e -> e.getSourceId().toString(),
            AgentEvent.class,
            topic);
    }

}

//////////////////////////////////////////////////////////////////////////////
