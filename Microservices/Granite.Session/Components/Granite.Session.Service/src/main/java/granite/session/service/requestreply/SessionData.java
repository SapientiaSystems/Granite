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
import java.util.UUID;

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
    private UUID                continuityId;
    private SessionClosedReason closedReason;
    private Long                replacedBy;
    private Long                copiedFrom;
    private Instant             lastActivity;
    private SessionLoginMethod  loginMethod;
    private String              application;

    public
    SessionData()
    {
        sessionId    = null;
        userId       = null;
        created      = null;
        expires      = null;
        deviceId     = null;
        values       = new LinkedHashMap<>();
        authorities  = new LinkedHashSet<>();
        continuityId = null;
        closedReason = null;
        replacedBy   = null;
        copiedFrom   = null;
        lastActivity = null;
        loginMethod  = null;
        application  = null;
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

    public SessionData
    setContinuityId(UUID id)
    {
        continuityId = id;
        return this;
    }

    public SessionData
    setClosedReason(SessionClosedReason reason)
    {
        closedReason = reason;
        return this;
    }

    public SessionData
    setReplacedBy(Long id)
    {
        replacedBy = id;
        return this;
    }

    public SessionData
    setCopiedFrom(Long id)
    {
        copiedFrom = id;
        return this;
    }

    public SessionData
    setLastActivity(Instant time)
    {
        lastActivity = time;
        return this;
    }

    public SessionData
    setLoginMethod(SessionLoginMethod method)
    {
        loginMethod = method;
        return this;
    }

    public SessionData
    setApplication(String name)
    {
        application = name;
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

    public UUID
    getContinuityId() { return continuityId; }

    public SessionClosedReason
    getClosedReason() { return closedReason; }

    public Long
    getReplacedBy() { return replacedBy; }

    public Long
    getCopiedFrom() { return copiedFrom; }

    public Instant
    getLastActivity() { return lastActivity; }

    public SessionLoginMethod
    getLoginMethod() { return loginMethod; }

    public String
    getApplication() { return application; }
}

//////////////////////////////////////////////////////////////////////////////
