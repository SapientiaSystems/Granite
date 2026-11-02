//////////////////////////////////////////////////////////////////////////////
// StringSetConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import jakarta.persistence.PersistenceException;

import java.util.Set;
import java.util.TreeSet;

@Converter
public
class StringSetConverter
    implements AttributeConverter<Set<String>,String>
{
    private static final ObjectMapper                   OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<TreeSet<String>> TYPE          = new TypeReference<>() {};

    @Override
    public String
    convertToDatabaseColumn(Set<String> attribute)
    {
        if (attribute == null)
            return null;

        try
        {
            return OBJECT_MAPPER.writeValueAsString(new TreeSet<>(attribute));
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to serialize string set",e);
        }
    }

    @Override
    public Set<String>
    convertToEntityAttribute(String dbData)
    {
        if (dbData == null || dbData.isBlank())
            return new TreeSet<>();

        try
        {
            return OBJECT_MAPPER.readValue(dbData,TYPE);
        }
        catch (Exception e)
        {
            throw new PersistenceException("Unable to deserialize string set",e);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
