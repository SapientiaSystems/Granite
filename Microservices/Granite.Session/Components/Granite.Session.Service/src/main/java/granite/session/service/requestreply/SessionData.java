//////////////////////////////////////////////////////////////////////////////
// SessionData.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public
class SessionData
    implements Serializable
{
    private Long                sessionId;
    private Long                userId;
    private String              accessToken;
    private Instant             created;
    private Instant             expires;
    private String              deviceId;
    private Map<String,String>  values;

    public
    SessionData()
    {
        sessionId   = null;
        userId      = null;
        accessToken = null;
        created     = null;
        expires     = null;
        deviceId    = null;
        values      = new HashMap<>();
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
    setAccessToken(String token)
    {
        accessToken = token;
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
    setValues(Map<String,String> v)
    {
        values = v;
        return this;
    }

    public Long
    getSessionId() { return sessionId; }

    public Long
    getUserId() { return userId; }

    public String
    getAccessToken() { return accessToken; }

    public Instant
    getCreated() { return created; }

    public Instant
    getExpires() { return expires; }

    public String
    getDeviceId() { return deviceId; }

    public Map<String,String>
    getValues() { return values; }
}

//////////////////////////////////////////////////////////////////////////////
