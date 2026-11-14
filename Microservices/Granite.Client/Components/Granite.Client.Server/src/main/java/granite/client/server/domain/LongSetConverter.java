//////////////////////////////////////////////////////////////////////////////
// LongSetConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public
class LongSetConverter
    implements AttributeConverter<Set<Long>,String>
{
    @Override
    public String
    convertToDatabaseColumn(Set<Long> attribute)
    {
        return
            attribute == null
                ? null
                : attribute.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override
    public Set<Long>
    convertToEntityAttribute(String dbData)
    {
        if (dbData == null || dbData.isBlank())
            return new LinkedHashSet<>();

        return
            Arrays
                .stream(dbData.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}

//////////////////////////////////////////////////////////////////////////////
