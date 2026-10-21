//////////////////////////////////////////////////////////////////////////////
// Session.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.domain;

import granite.foundation.core.mapper.StringMapperRegistry;
import strata.foundation.core.mapper.IMapper;
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
    private Long                       userId;
    private String                     accessToken;
    private String                     refreshTokenHash;
    private String                     previousRefreshTokenHash;
    private Instant                    expires;
    private String                     deviceId;
    private Set<String>                authorities;
    private Map<String,Object>         values;

    private static final StringMapperRegistry registry = StringMapperRegistry.of();

    public
    Session()
    {
        super();
        userId                   = null;
        accessToken              = null;
        refreshTokenHash         = null;
        previousRefreshTokenHash = null;
        expires                  = null;
        deviceId                 = null;
        authorities              = new LinkedHashSet<>();
        values                   = new LinkedHashMap<>();
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

    public Session
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public Session
    setAccessToken(String token)
    {
        accessToken = token;
        return this;
    }

    public Session
    setRefreshTokenHash(String hash)
    {
        refreshTokenHash = hash;
        return this;
    }

    public Session
    setPreviousRefreshTokenHash(String hash)
    {
        previousRefreshTokenHash = hash;
        return this;
    }

    public Session
    rotateRefreshTokenHash(String hash)
    {
        previousRefreshTokenHash = refreshTokenHash;
        refreshTokenHash         = hash;
        return this;
    }

    public Session
    setExpires(Instant time)
    {
        expires = time;
        notifyUpdated();
        return this;
    }

    public Session
    setDeviceId(String id)
    {
        deviceId = id;
        return this;
    }

    public Session
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public <T> Session
    setValue(String key,T value)
    {
        Optional<IMapper<String,T>> mapper =
            registry.get(String.class,(Class<T>)value.getClass());

        values.put(
            key,
            mustStringify(value)
                ? mapper
                    .map(m -> m.map(value))
                    .orElse(value.toString())
                : value);
        return this;
    }

    public Long
    getSessionId() { return super.getPrimaryId(); }

    public Long
    getUserId() { return userId; }

    public String
    getAccessToken() { return accessToken; }

    public String
    getRefreshTokenHash() { return refreshTokenHash; }

    public String
    getPreviousRefreshTokenHash() { return previousRefreshTokenHash; }

    public Instant
    getExpires() { return expires; }

    public String
    getDeviceId() { return deviceId; }

    public Set<String>
    getAuthorities() { return authorities; }

    public Map<String,Object>
    getValues() { return values; }

    public <T> Optional<T>
    getValue(Class<T> type,String key)
    {
        Optional<IMapper<T,String>> mapper = registry.get(type,String.class);
        Object                      stored = values.get(key);

        if (stored instanceof String s && !type.equals(String.class))
            return mapper.map(m -> m.map(s));

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

    public Session
    destroy() { return notifyDestroyed(); }

    public Session
    notifyCreated() { return super.notify(new SessionCreated(this)); }

    public Session
    notifyUpdated() { return super.notify(new SessionUpdated(this)); }

    public Session
    notifyDestroyed() { return super.notify(new SessionDestroyed(this)); }

    public static StringMapperRegistry
    getRegistry() { return registry; }

    @Override
    protected Session
    getSelf()
    {
        return this;
    }

    private <T> boolean
    mustStringify(T value)
    {
        if (value instanceof String)
            return false;

        if (value instanceof Number)
            return false;

        if (value.getClass().isPrimitive())
            return false;

        return true;
    }
}

//////////////////////////////////////////////////////////////////////////////
