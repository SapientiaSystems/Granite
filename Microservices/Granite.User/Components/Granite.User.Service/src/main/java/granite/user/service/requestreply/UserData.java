//////////////////////////////////////////////////////////////////////////////
// UserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public
class UserData
    implements Serializable
{
    private Long               userId;
    private String             userName;
    private String             externalIdentity;
    private Set<String>        authorities;
    private Map<String,Object> values;

    public
    UserData()
    {
        userId           = null;
        userName         = null;
        externalIdentity = null;
        authorities      = new LinkedHashSet<>();
        values           = new LinkedHashMap<>();
    }

    public UserData
    setUserId(Long id)
    {
        userId = id;
        return this;
    }

    public UserData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public UserData
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public UserData
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public UserData
    setValues(Map<String,Object> v)
    {
        values = v;
        return this;
    }

    public Long
    getUserId() { return userId; }

    public String
    getUserName() { return userName; }

    public String
    getExternalIdentity() { return externalIdentity; }

    public Set<String>
    getAuthorities() { return authorities; }

    public Map<String,Object>
    getValues() { return values; }
}

//////////////////////////////////////////////////////////////////////////////
