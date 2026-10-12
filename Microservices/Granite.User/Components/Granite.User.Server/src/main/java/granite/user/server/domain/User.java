//////////////////////////////////////////////////////////////////////////////
// User.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.domain;

import strata.server.core.entity.AbstractEntityAndDomainEventSource;

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
    private String foo;
    private String userName;
    private String password;
    private String externalIdentity;

    public
    User()
    {
        super();
        foo              = null;
        userName         = null;
        password         = null;
        externalIdentity = null;
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
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public String
    getFoo() { return foo; }

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
