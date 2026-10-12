//////////////////////////////////////////////////////////////////////////////
// ValueMapConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public
class ValueMapConverter
    implements AttributeConverter<Map<String,Object>,String>
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String,Object>> TYPE = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(Map<String,Object> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize values map",e);
        }
    }

    @Override
    public Map<String,Object>
    convertToEntityAttribute(String dbData)
    {
        if (dbData == null || dbData.isBlank())
            return new LinkedHashMap<>();

        try
        {
            return OBJECT_MAPPER.readValue(dbData,TYPE);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to deserialize values map",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
