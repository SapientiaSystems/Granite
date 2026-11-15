//////////////////////////////////////////////////////////////////////////////
// ClientData.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.service.requestreply;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public
class ClientData
    implements Serializable
{
    private String      clientId;
    private String      name;
    private String      description;
    private Set<String> authorities;
    private Set<Long>   owners;
    private boolean     enabled;
    private Instant     secretExpiresAt;
    private Instant     lastTokenIssuedAt;
    private Instant     createdAt;
    private Instant     updatedAt;
    private Long        version;

    public
    ClientData()
    {
        clientId = null;
        name = null;
        description = null;
        authorities = new TreeSet<>();
        owners = new TreeSet<>();
        enabled = false;
        secretExpiresAt = null;
        lastTokenIssuedAt = null;
        createdAt = null;
        updatedAt = null;
        version = null;
    }

    public ClientData
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public ClientData
    setName(String value)
    {
        name = value;
        return this;
    }

    public ClientData
    setDescription(String value)
    {
        description = value;
        return this;
    }

    public ClientData
    setAuthorities(Set<String> value)
    {
        authorities = value;
        return this;
    }

    public ClientData
    setOwners(Set<Long> value)
    {
        owners = value;
        return this;
    }

    public ClientData
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public ClientData
    setSecretExpiresAt(Instant value)
    {
        secretExpiresAt = value;
        return this;
    }

    public ClientData
    setLastTokenIssuedAt(Instant value)
    {
        lastTokenIssuedAt = value;
        return this;
    }

    public ClientData
    setCreatedAt(Instant value)
    {
        createdAt = value;
        return this;
    }

    public ClientData
    setUpdatedAt(Instant value)
    {
        updatedAt = value;
        return this;
    }

    public ClientData
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

    public Instant
    getLastTokenIssuedAt() { return lastTokenIssuedAt; }

    public Instant
    getCreatedAt() { return createdAt; }

    public Instant
    getUpdatedAt() { return updatedAt; }

    public Long
    getVersion() { return version; }
}

//////////////////////////////////////////////////////////////////////////////
