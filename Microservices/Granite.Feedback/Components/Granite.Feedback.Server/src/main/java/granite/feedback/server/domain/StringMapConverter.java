//////////////////////////////////////////////////////////////////////////////
// StringMapConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public
class StringMapConverter
    implements AttributeConverter<Map<String,String>,String>
{
    private static final ObjectMapper                      OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String,String>> TYPE          = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(Map<String,String> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize details map",e);
        }
    }

    @Override
    public Map<String,String>
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
            throw new PersistenceException("Unable to deserialize details map",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
