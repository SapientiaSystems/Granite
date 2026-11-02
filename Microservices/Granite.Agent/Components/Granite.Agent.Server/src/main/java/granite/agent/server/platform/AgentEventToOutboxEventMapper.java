//////////////////////////////////////////////////////////////////////////////
// AgentEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.agent.service.event.AgentEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class AgentEventToOutboxEventMapper
    implements IAgentEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    AgentEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(AgentEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(AgentEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(AgentEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
