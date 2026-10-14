//////////////////////////////////////////////////////////////////////////////
// User.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import granite.foundation.core.mapper.StringMapperRegistry;
import strata.foundation.core.mapper.IMapper;
import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class User
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            User,
            IUserEvent,
            IUserEventObserver>
    implements IUserEventSource
{
    private String                     userName;
    private String                     password;
    private String                     externalIdentity;
    private Set<String>                authorities;
    private Map<String,Object>         values;

    private static final StringMapperRegistry registry = StringMapperRegistry.of();

    public
    User()
    {
        super();
        userName         = null;
        password         = null;
        externalIdentity = null;
        authorities      = new LinkedHashSet<>();
        values           = new LinkedHashMap<>();
    }

    @Override
    public User
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

    public User
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public User
    setPassword(String hashed)
    {
        password = hashed;
        return this;
    }

    public User
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public User
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public <T> User
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

    public String
    getUserName() { return userName; }

    public String
    getPassword() { return password; }

    public String
    getExternalIdentity() { return externalIdentity; }

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

    public User
    notifyCreated() { return super.notify(new UserCreated(this)); }

    public User
    notifyUpdated() { return super.notify(new UserUpdated(this)); }

    public User
    notifyDestroyed() { return super.notify(new UserDestroyed(this)); }

    public static StringMapperRegistry
    getRegistry() { return registry; }

    @Override
    protected User
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
