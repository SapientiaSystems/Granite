//////////////////////////////////////////////////////////////////////////////
// UserKey.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public
class UserKey
    implements Serializable, Comparable<UserKey>
{
    public static final String USER_ID = "userId";
    public static final String USER_FOO = "userFoo";

    private final String name;
    private final String value;

    @JsonCreator
    public
    UserKey(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value)
    {
        this.name = name;
        this.value = value;
    }

    @Override
    public int
    hashCode()
    {
        return Objects.hash(name,value);
    }

    @Override
    public boolean
    equals(Object other)
    {
        return
            (other instanceof UserKey that) &&
                Objects.equals(this.name,that.name) &&
                Objects.equals(this.value,that.value);
    }

    @Override
    public String
    toString()
    {
        return String.format("%s=%s",name,value);
    }

    @Override
    public int
    compareTo(UserKey other)
    {
        int result = Objects.compare(name,other.name,String::compareTo);

        if (result != 0)
            return result;

        return Objects.compare(value,other.value,String::compareTo);
    }

    @JsonProperty("name")
    public String
    getName()
    {
        return name;
    }

    @JsonProperty("value")
    public String
    getValue()
    {
        return value;
    }

    @JsonIgnore
    public Long
    getValueAsLong()
        throws IllegalStateException
    {
        try
        {
            return Long.valueOf(value);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalStateException(e);
        }
    }

    public static UserKey
    ofUserId(Long id)
    {
        return of(USER_ID, id);
    }

    public static UserKey
    ofUserFoo(String foo)
    {
        return of(USER_FOO,foo);
    }

    public static UserKey
    of(String key,String value)
    {
        return new UserKey(key,value);
    }

    public static UserKey
    of(String key,Long value)
    {
        return new UserKey(key,String.valueOf(value));
    }

    private void
    validate(String name)
        throws IllegalArgumentException
    {
        switch (name)
        {
            case USER_ID:
            case USER_FOO:
                break;

            default:
                throw new IllegalArgumentException("Invalid name " + name);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
