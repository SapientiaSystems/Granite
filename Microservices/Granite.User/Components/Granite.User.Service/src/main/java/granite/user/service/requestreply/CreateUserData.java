//////////////////////////////////////////////////////////////////////////////
// CreateUserData.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.service.requestreply;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public
class CreateUserData
    implements Serializable
{
    private String      userName;
    private String      password;
    private String      externalIdentity;
    private Set<String> authorities;
    private Map<String,Object> values;

    public
    CreateUserData()
    {
        userName         = null;
        password         = null;
        externalIdentity = null;
        authorities      = new LinkedHashSet<>();
        values           = new LinkedHashMap<>();
    }

    public CreateUserData
    setUserName(String name)
    {
        userName = name;
        return this;
    }

    public CreateUserData
    setPassword(String plainText)
    {
        password = plainText;
        return this;
    }

    public CreateUserData
    setExternalIdentity(String identity)
    {
        externalIdentity = identity;
        return this;
    }

    public CreateUserData
    setAuthorities(Set<String> values)
    {
        authorities = values;
        return this;
    }

    public CreateUserData
    setValues(Map<String,Object> v)
    {
        values = v;
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
}

//////////////////////////////////////////////////////////////////////////////
