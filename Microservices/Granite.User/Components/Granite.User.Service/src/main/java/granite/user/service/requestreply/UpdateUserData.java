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

    public
    UpdateUserData()
    {
        userId = null;
        foo = null;
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

    public Long
    getUserId() { return userId; }

    public Optional<String>
    getFoo() { return Optional.ofNullable(foo); }
}

//////////////////////////////////////////////////////////////////////////////
