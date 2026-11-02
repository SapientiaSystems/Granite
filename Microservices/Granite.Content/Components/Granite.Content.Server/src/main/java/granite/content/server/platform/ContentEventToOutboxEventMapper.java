//////////////////////////////////////////////////////////////////////////////
// ContentEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.content.service.event.ContentEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class ContentEventToOutboxEventMapper
    implements IContentEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    ContentEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(ContentEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(ContentEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(ContentEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
