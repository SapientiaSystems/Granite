//////////////////////////////////////////////////////////////////////////////
// SessionEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.session.service.event.SessionEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class SessionEventToOutboxEventMapper
    implements ISessionEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    SessionEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(SessionEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(SessionEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(SessionEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
