//////////////////////////////////////////////////////////////////////////////
// ClientEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.client.service.event.ClientEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class ClientEventToOutboxEventMapper
    implements IClientEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    ClientEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(ClientEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(ClientEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(ClientEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
