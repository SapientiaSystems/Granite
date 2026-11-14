//////////////////////////////////////////////////////////////////////////////
// Client.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.domain;

import strata.server.core.entity.AbstractEntityAndDomainEventSource;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public
class Client
    extends
        AbstractEntityAndDomainEventSource<
            Long,
            Client,
            IClientEvent,
            IClientEventObserver>
    implements IClientEventSource
{
    private String      clientId;
    private String      name;
    private String      description;
    private Set<String> authorities;
    private Set<Long>   owners;
    private boolean     enabled;
    private byte[]      secretSha256;
    private Instant     secretExpiresAt;
    private Instant     lastTokenIssuedAt;

    public
    Client()
    {
        super();
        clientId          = null;
        name              = null;
        description       = null;
        authorities       = new LinkedHashSet<>();
        owners            = new LinkedHashSet<>();
        enabled           = true;
        secretSha256      = null;
        secretExpiresAt   = null;
        lastTokenIssuedAt = null;
    }

    @Override
    public Client
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

    public Client
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public Client
    setName(String value)
    {
        name = value;
        return this;
    }

    public Client
    setDescription(String value)
    {
        description = value;
        return this;
    }

    public Client
    setAuthorities(Set<String> value)
    {
        authorities = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value);
        return this;
    }

    public Client
    setOwners(Set<Long> value)
    {
        owners = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value);
        return this;
    }

    public Client
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public Client
    setSecretSha256(byte[] value)
    {
        secretSha256 = value;
        return this;
    }

    public Client
    setSecretExpiresAt(Instant value)
    {
        secretExpiresAt = value;
        return this;
    }

    public Client
    setLastTokenIssuedAt(Instant value)
    {
        lastTokenIssuedAt = value;
        return this;
    }

    public boolean
    isOwnedBy(Long userId)
    {
        return userId != null && owners.contains(userId);
    }

    public boolean
    isSecretCurrent(Instant now)
    {
        return secretExpiresAt == null || now.isBefore(secretExpiresAt);
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

    public byte[]
    getSecretSha256() { return secretSha256; }

    public Instant
    getSecretExpiresAt() { return secretExpiresAt; }

    public Instant
    getLastTokenIssuedAt() { return lastTokenIssuedAt; }

    public Client
    notifyCreated() { return super.notify(new ClientCreated(this)); }

    public Client
    notifyUpdated() { return super.notify(new ClientUpdated(this)); }

    public Client
    notifyDestroyed() { return super.notify(new ClientDestroyed(this)); }

    @Override
    protected Client
    getSelf()
    {
        return this;
    }
}

//////////////////////////////////////////////////////////////////////////////
