//////////////////////////////////////////////////////////////////////////////
// UserKeySet.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public
class UserKeySet
    implements Serializable, Comparable<UserKeySet>
{
    public static final String USER_IDS = "userIds";
    public static final String USER_FOOS = "userFoos";

    private final String name;
    private final Set<String> values;

    @JsonCreator
    public UserKeySet(
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
            (other instanceof UserKeySet that) &&
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
    compareTo(UserKeySet other)
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

    public static UserKeySet
    empty(String name)
    {
        return new UserKeySet(name,Set.of());
    }

    public static UserKeySet
    ofIds(Set<Long> ids)
    {
        return ofLongs(USER_IDS,ids);
    }

    public static UserKeySet
    ofFoos(Set<String> foos)
    {
        return ofStrings(USER_FOOS,foos);
    }

    public static UserKeySet
    ofStrings(String key,Set<String> values)
    {
        return new UserKeySet(key,values);
    }

    public static UserKeySet
    ofLongs(String key,Set<Long> values)
    {
        return
            new UserKeySet(
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
            case USER_IDS:
            case USER_FOOS:
                break;

            default:
                throw new IllegalArgumentException("Invalid key name: " + name);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
