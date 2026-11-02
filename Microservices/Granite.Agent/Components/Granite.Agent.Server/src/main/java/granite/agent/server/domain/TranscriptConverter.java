//////////////////////////////////////////////////////////////////////////////
// TranscriptConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import granite.agent.service.requestreply.AgentMessageData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;

@Converter
public
class TranscriptConverter
    implements AttributeConverter<List<AgentMessageData>,String>
{
    private static final ObjectMapper                          OBJECT_MAPPER =
        new ObjectMapper().findAndRegisterModules().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final TypeReference<List<AgentMessageData>> TYPE          = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(List<AgentMessageData> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize transcript",e);
        }
    }

    @Override
    public List<AgentMessageData>
    convertToEntityAttribute(String dbData)
    {
        if (dbData == null || dbData.isBlank())
            return new ArrayList<>();

        try
        {
            return OBJECT_MAPPER.readValue(dbData,TYPE);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to deserialize transcript",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
