//////////////////////////////////////////////////////////////////////////////
// SurveyEventToOutboxEventMapper.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import granite.survey.service.event.SurveyEvent;
import strata.outbox.core.repository.OutboxEvent;
import strata.outbox.core.shared.MappingException;
import strata.outbox.core.shared.ObjectMapperProvider;

public
class SurveyEventToOutboxEventMapper
    implements ISurveyEventToOutboxEventMapper
{
    private final ObjectMapper mapper;

    public
    SurveyEventToOutboxEventMapper()
    {
        this.mapper = new ObjectMapperProvider().get();
    }

    @Override
    public OutboxEvent
    map(SurveyEvent event)
        throws MappingException
    {
        try
        {
            return
                new OutboxEvent()
                    .setSourceType(SurveyEvent.class.getTypeName())
                    .setSourceId(event.getSourceId().toString())
                    .setEventType(SurveyEvent.class.getSimpleName())
                    .setEventPayload(mapper.writeValueAsString(event));
        }
        catch (JsonProcessingException e)
        {
            throw new MappingException(e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
