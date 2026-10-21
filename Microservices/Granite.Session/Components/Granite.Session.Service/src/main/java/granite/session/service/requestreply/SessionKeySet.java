//////////////////////////////////////////////////////////////////////////////
// SessionKeySet.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public
class SessionKeySet
    implements Serializable, Comparable<SessionKeySet>
{
    public static final String SESSION_IDS = "sessionIds";
    public static final String USER_IDS = "userIds";

    private final String name;
    private final Set<String> values;

    @JsonCreator
    public SessionKeySet(
        @JsonProperty("name") String name,
        @JsonProperty("values") Set<String> values)
    {
        validate(name);
        this.name = name;
        this.values = new TreeSet<>(values);
    }

    @Override
    public int
    hashCode()
    {
        return Objects.hash(name,values);
    }

    @Override
    public boolean
    equals(Object other)
    {
        return
            (other instanceof SessionKeySet that) &&
                Objects.equals(this.name,that.name) &&
                Objects.equals(this.values,that.values);
    }

    @Override
    public String
    toString()
    {
        return
            String.format(
                "%s=%s",
                name,
                String.join(",",values));
    }

    @Override
    public int
    compareTo(SessionKeySet other)
    {
        int result = Objects.compare(name,other.name,String::compareTo);

        if (result != 0)
            return result;

        return
            Objects.compare(
                String.join(",",values),
                String.join(",",other.values),
                String::compareTo);
    }

    @JsonProperty("name")
    public String
    getName()
    {
        return name;
    }

    @JsonProperty("values")
    public Set<String>
    getValues()
    {
        return values;
    }

    @JsonIgnore
    public Set<Long>
    getValuesAsLongs()
        throws IllegalStateException
    {
        try
        {
            return
                values
                    .stream()
                    .map(Long::valueOf)
                    .collect(Collectors.toSet());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnore
    public boolean
    isEmpty() { return values.isEmpty(); }

    public static SessionKeySet
    empty(String name)
    {
        return new SessionKeySet(name,Set.of());
    }

    public static SessionKeySet
    ofSessionIds(Set<Long> ids)
    {
        return ofLongs(SESSION_IDS,ids);
    }

    public static SessionKeySet
    ofUserIds(Set<Long> ids)
    {
        return ofLongs(USER_IDS,ids);
    }

    public static SessionKeySet
    ofStrings(String key,Set<String> values)
    {
        return new SessionKeySet(key,values);
    }

    public static SessionKeySet
    ofLongs(String key,Set<Long> values)
    {
        return
            new SessionKeySet(
                key,
                values
                    .stream()
                    .map(value -> String.valueOf(value))
                    .collect(Collectors.toSet()));
    }

    private void
    validate(String name)
    {
        switch (name)
        {
            case SESSION_IDS:
            case USER_IDS:
                break;

            default:
                throw new IllegalArgumentException("Invalid key name: " + name);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
