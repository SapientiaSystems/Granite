//////////////////////////////////////////////////////////////////////////////
// StringListConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.ArrayList;
import java.util.List;

@Converter
public
class StringListConverter
    implements AttributeConverter<List<String>,String>
{
    private static final ObjectMapper                OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> TYPE          = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(List<String> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize string list",e);
        }
    }

    @Override
    public List<String>
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
            throw new PersistenceException("Unable to deserialize string list",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
