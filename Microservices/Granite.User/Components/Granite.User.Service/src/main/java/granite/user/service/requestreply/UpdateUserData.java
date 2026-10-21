//////////////////////////////////////////////////////////////////////////////
// UpdateUserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public
class UpdateUserData
{
    private Long        userId;
    private String      userName;
    private String      password;
    private String      externalIdentity;
    private Set<String> authorities;
    private Map<String,Object> values;

    public
    UpdateUserData()
    {
        userId           = null;
        userName         = null;
        password         = null;
        externalIdentity = null;
        authorities      = null;
        values           = null;
    }

    public UpdateUserData
    setUserId(Long id)
    {
        userId = id;
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

    public UpdateUserData
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public UpdateUserData
    setValues(Map<String,Object> v)
    {
        values = v;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public Optional<String>
    getUserName() { return Optional.ofNullable(userName); }

    public Optional<String>
    getPassword() { return Optional.ofNullable(password); }

    public Optional<String>
    getExternalIdentity() { return Optional.ofNullable(externalIdentity); }

    public Optional<Set<String>>
    getAuthorities() { return Optional.ofNullable(authorities); }

    public Optional<Map<String,Object>>
    getValues() { return Optional.ofNullable(values); }
}

//////////////////////////////////////////////////////////////////////////////
