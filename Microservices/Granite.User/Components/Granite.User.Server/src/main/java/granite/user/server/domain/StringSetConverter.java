//////////////////////////////////////////////////////////////////////////////
// StringSetConverter.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

@Converter
public
class StringSetConverter
    implements AttributeConverter<Set<String>,String>
{
    @Override
    public String
    convertToDatabaseColumn(Set<String> attribute)
    {
        return
            attribute != null
                ? attribute
                      .stream()
                      .collect(Collectors.joining(","))
                : null;
    }

    @Override
    public Set<String>
    convertToEntityAttribute(String dbData)
    {
        Set<String> attribute = new LinkedHashSet<>();

        if (dbData != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(dbData.trim());

            while (tokenizer.hasMoreTokens())
                attribute.add(tokenizer.nextToken(","));
        }

        return attribute;
    }
}

//////////////////////////////////////////////////////////////////////////////
