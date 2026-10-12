//////////////////////////////////////////////////////////////////////////////
// UpdateUserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.util.Optional;

public
class UpdateUserData
{
    private Long   userId;
    private String foo;
    private String userName;
    private String password;
    private String externalIdentity;

    public
    UpdateUserData()
    {
        userId           = null;
        foo              = null;
        userName         = null;
        password         = null;
        externalIdentity = null;
    }

    public UpdateUserData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public UpdateUserData
    setFoo(String f)
    {
        foo = f;
        return this;
    }

    public UpdateUserData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public UpdateUserData
    setPassword(String plainText)
    {
        password = plainText;
        return this;
    }

    public UpdateUserData
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Optional<String>
    getFoo() { return Optional.ofNullable(foo); }

    public Optional<String>
    getUserName() { return Optional.ofNullable(userName); }

    public Optional<String>
    getPassword() { return Optional.ofNullable(password); }

    public Optional<String>
    getExternalIdentity() { return Optional.ofNullable(externalIdentity); }
}

//////////////////////////////////////////////////////////////////////////////
