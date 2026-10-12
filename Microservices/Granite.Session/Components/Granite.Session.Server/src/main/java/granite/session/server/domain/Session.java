//////////////////////////////////////////////////////////////////////////////
// Session.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class Session
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            Session,
            ISessionEvent,
            ISessionEventObserver>
    implements ISessionEventSource
{
    private Long                userId;
    private String              accessToken;
    private Instant             expires;
    private String              deviceId;
    private Map<String,Object>  values;
    private Set<String>         authorities;

    public
    Session()
    {
        super();
        userId      = null;
        accessToken = null;
        expires     = null;
        deviceId    = null;
        values      = new LinkedHashMap<>();
        authorities = new LinkedHashSet<>();
    }

    @Override
    public Session
    setPrimaryId(Long primaryId)
    {
        return super.setPrimaryId(primaryId);
    }

    @Override
    public Long
    getPrimaryId()
    {
        return super.getPrimaryId();
    }

    public Session
    setSessionId(Long id)
    {
        return super.setPrimaryId(id);
    }

    public Long
    getSessionId() { return super.getPrimaryId(); }

    public Session
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Session
    setAccessToken(String token)
    {
        accessToken = token;
        return this;
    }

    public String
    getAccessToken() { return accessToken; }

    public Session
    setExpires(Instant time)
    {
        expires = time;
        notifyUpdated();
        return this;
    }

    public Instant
    getExpires() { return expires; }

    public Session
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public String
    getDeviceId() { return deviceId; }

    public <T> Session
    setValue(String key,T value)
    {
        values.put(key,value);
        notifyUpdated();
        return this;
    }

    public <T> Optional<T>
    getValue(Class<T> type,String key)
    {
        Object stored = values.get(key);

        return
            type.isInstance(stored)
                ? Optional.of(type.cast(stored))
                : Optional.empty();
    }

    public boolean
    hasValue(String key)
    {
        return values.containsKey(key);
    }

    public Map<String,Object>
    getValues() { return values; }

    public Session
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public Set<String>
    getAuthorities() { return authorities; }

    public Session
    destroy()
    {
        return notifyDestroyed();
    }

    public Session
    notifyCreated() { return super.notify(new SessionCreated(this)); }

    public Session
    notifyUpdated() { return super.notify(new SessionUpdated(this)); }

    public Session
    notifyDestroyed() { return super.notify(new SessionDestroyed(this)); }

    @Override
    protected Session
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
