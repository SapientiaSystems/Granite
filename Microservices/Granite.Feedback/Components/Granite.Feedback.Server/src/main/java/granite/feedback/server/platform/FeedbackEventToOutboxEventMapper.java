//////////////////////////////////////////////////////////////////////////////
// FeedbackEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.feedback.service.event.FeedbackEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class FeedbackEventToOutboxEventMapper
    implements IFeedbackEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    FeedbackEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(FeedbackEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(FeedbackEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(FeedbackEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
