//////////////////////////////////////////////////////////////////////////////
// SessionData.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public
class SessionData
    implements Serializable
{
    private Long                sessionId;
    private Long                userId;
    private Instant             created;
    private Instant             expires;
    private String              deviceId;
    private Map<String,Object>  values;
    private Set<String>         authorities;

    public
    SessionData()
    {
        sessionId   = null;
        userId      = null;
        created     = null;
        expires     = null;
        deviceId    = null;
        values      = new LinkedHashMap<>();
        authorities = new LinkedHashSet<>();
    }

    public SessionData
    setSessionId(Long id)
    {
        sessionId = id;
        return this;
    }

    public SessionData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public SessionData
    setCreated(Instant time)
    {
        created = time;
        return this;
    }

    public SessionData
    setExpires(Instant time)
    {
        expires = time;
        return this;
    }

    public SessionData
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public SessionData
    setValues(Map<String,Object> v)
    {
        values = v;
        return this;
    }

    public SessionData
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }

    public Long
    getUserId() { return userId; }

    public Instant
    getCreated() { return created; }

    public Instant
    getExpires() { return expires; }

    public String
    getDeviceId() { return deviceId; }

    public Map<String,Object>
    getValues() { return values; }

    public Set<String>
    getAuthorities() { return authorities; }
}

//////////////////////////////////////////////////////////////////////////////
