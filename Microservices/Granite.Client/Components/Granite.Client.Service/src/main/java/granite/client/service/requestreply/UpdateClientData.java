//////////////////////////////////////////////////////////////////////////////
// UpdateClientData.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public
class UpdateClientData
    implements Serializable
{
    private String      clientId;
    private String      name;
    private String      description;
    private Set<String> authorities;
    private Set<Long>   owners;
    private boolean     enabled;
    private Instant     secretExpiresAt;
    private Long        version;

    public
    UpdateClientData()
    {
        clientId = null;
        name = null;
        description = null;
        authorities = new TreeSet<>();
        owners = new TreeSet<>();
        enabled = false;
        secretExpiresAt = null;
        version = null;
    }

    public UpdateClientData
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public UpdateClientData
    setName(String value)
    {
        name = value;
        return this;
    }

    public UpdateClientData
    setDescription(String value)
    {
        description = value;
        return this;
    }

    public UpdateClientData
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public UpdateClientData
    setOwners(Set<Long> value)
    {
        owners = value;
        return this;
    }

    public UpdateClientData
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public UpdateClientData
    setSecretExpiresAt(Instant value)
    {
        secretExpiresAt = value;
        return this;
    }

    public UpdateClientData
    setVersion(Long value)
    {
        version = value;
        return this;
    }

    public String
    getClientId() { return clientId; }

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

    public Long
    getVersion() { return version; }
}

//////////////////////////////////////////////////////////////////////////////
