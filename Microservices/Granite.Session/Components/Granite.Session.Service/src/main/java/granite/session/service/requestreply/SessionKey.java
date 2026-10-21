//////////////////////////////////////////////////////////////////////////////
// SessionKey.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

public
class SessionKey
    implements Serializable, Comparable<SessionKey>
{
    public static final String SESSION_ID = "sessionId";
    public static final String USER_ID = "userId";

    private final String name;
    private final String value;

    @JsonCreator
    public
    SessionKey(
        @JsonProperty("name") String name,
        @JsonProperty("value") String value)
    {
        validate(name);
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
            (other instanceof SessionKey that) &&
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
    compareTo(SessionKey other)
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

    public static SessionKey
    ofSessionId(Long id)
    {
        return of(SESSION_ID,id);
    }

    public static SessionKey
    ofUserId(Long id)
    {
        return of(USER_ID,id);
    }

    public static SessionKey
    of(String key,String value)
    {
        return new SessionKey(key,value);
    }

    public static SessionKey
    of(String key,Long value)
    {
        return new SessionKey(key,String.valueOf(value));
    }

    private void
    validate(String name)
        throws IllegalArgumentException
    {
        switch (name)
        {
            case SESSION_ID:
            case USER_ID:
                break;

            default:
                throw new IllegalArgumentException("Invalid name " + name);
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
