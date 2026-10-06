/// ///////////////////////////////////////////////////////////////////////////
// UserEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.platform;

import granite.user.service.event.UserEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class UserEventToOutboxEventMapper
    implements IUserEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    UserEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(UserEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(
                        UserEvent
                            .class
                            .getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(
                        UserEvent
                            .class
                            .getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
