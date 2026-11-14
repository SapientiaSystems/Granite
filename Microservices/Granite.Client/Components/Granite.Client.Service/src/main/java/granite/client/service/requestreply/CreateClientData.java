//////////////////////////////////////////////////////////////////////////////
// CreateClientData.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public
class CreateClientData
    implements Serializable
{
    private String      name;
    private String      description;
    private Set<String> authorities;
    private Set<Long>   owners;
    private boolean     enabled;
    private Instant     secretExpiresAt;

    public
    CreateClientData()
    {
        name = null;
        description = null;
        authorities = new TreeSet<>();
        owners = new TreeSet<>();
        enabled = false;
        secretExpiresAt = null;
    }

    public CreateClientData
    setName(String value)
    {
        name = value;
        return this;
    }

    public CreateClientData
    setDescription(String value)
    {
        description = value;
        return this;
    }

    public CreateClientData
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public CreateClientData
    setOwners(Set<Long> value)
    {
        owners = value;
        return this;
    }

    public CreateClientData
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public CreateClientData
    setSecretExpiresAt(Instant value)
    {
        secretExpiresAt = value;
        return this;
    }

    public String
    getName() { return name; }

    public String
    getDescription() { return description; }

    public Set<String>
    getAuthorities() { return authorities; }

    public Set<Long>
    getOwners() { return owners; }

    public boolean
    isEnabled() { return enabled; }

    public Instant
    getSecretExpiresAt() { return secretExpiresAt; }
}

//////////////////////////////////////////////////////////////////////////////
