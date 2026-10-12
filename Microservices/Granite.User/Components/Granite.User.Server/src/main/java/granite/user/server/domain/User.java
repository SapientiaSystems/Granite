//////////////////////////////////////////////////////////////////////////////
// User.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

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
    private String             userName;
    private String             password;
    private String             externalIdentity;
    private Set<String>        authorities;
    private Map<String,Object> values;

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

    public String
    getUserName() { return userName; }

    public User
    setPassword(String hashed)
    {
        password = hashed;
        return this;
    }

    public String
    getPassword() { return password; }

    public User
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public String
    getExternalIdentity() { return externalIdentity; }

    public User
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public Set<String>
    getAuthorities() { return authorities; }

    public <T> User
    setValue(String key,T value)
    {
        values.put(key,value);
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

    public User
    notifyCreated() { return super.notify(new UserCreated(this)); }

    public User
    notifyUpdated() { return super.notify(new UserUpdated(this)); }

    public User
    notifyDestroyed() { return super.notify(new UserDestroyed(this)); }

    @Override
    protected User
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
