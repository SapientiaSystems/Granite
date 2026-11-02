//////////////////////////////////////////////////////////////////////////////
// IntegerMapConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.LinkedHashMap;
import java.util.Map;

@Converter
public
class IntegerMapConverter
    implements AttributeConverter<Map<String,Integer>,String>
{
    private static final ObjectMapper                       OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String,Integer>> TYPE          = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(Map<String,Integer> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize answers map",e);
        }
    }

    @Override
    public Map<String,Integer>
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
            throw new PersistenceException("Unable to deserialize answers map",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
