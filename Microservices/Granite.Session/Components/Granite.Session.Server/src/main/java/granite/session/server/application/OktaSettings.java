//////////////////////////////////////////////////////////////////////////////
// OktaSettings.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

public
class OktaSettings
{
    private boolean               enabled;
    private String                issuer;
    private String                clientId;
    private String                redirectUri;
    private String                postLogoutRedirectUri;
    private String                scope;
    private OktaRoleAssignment    roleAssignment;
    private OktaUnmappedGroups    unmappedGroups;
    private Map<String,String>    groupMapping;
    private Duration              transactionTtl;
    private Duration              discoveryCacheTtl;
    private Duration              clockSkew;
    private Duration              httpTimeout;

    public
    OktaSettings()
    {
        enabled               = false;
        issuer                = null;
        clientId              = null;
        redirectUri           = null;
        postLogoutRedirectUri = null;
        scope                 = "openid profile email groups";
        roleAssignment        = OktaRoleAssignment.ON_CREATE;
        unmappedGroups        = OktaUnmappedGroups.DEFAULT_ROLE;
        groupMapping          = new LinkedHashMap<>();
        transactionTtl        = Duration.ofMinutes(5);
        discoveryCacheTtl     = Duration.ofHours(1);
        clockSkew             = Duration.ofSeconds(60);
        httpTimeout           = Duration.ofSeconds(5);
    }

    public OktaSettings
    setEnabled(boolean value)
    {
        enabled = value;
        return this;
    }

    public OktaSettings
    setIssuer(String value)
    {
        issuer = value;
        return this;
    }

    public OktaSettings
    setClientId(String value)
    {
        clientId = value;
        return this;
    }

    public OktaSettings
    setRedirectUri(String value)
    {
        redirectUri = value;
        return this;
    }

    public OktaSettings
    setPostLogoutRedirectUri(String value)
    {
        postLogoutRedirectUri = value;
        return this;
    }

    public OktaSettings
    setRoleAssignment(OktaRoleAssignment value)
    {
        roleAssignment = value != null ? value : OktaRoleAssignment.ON_CREATE;
        return this;
    }

    public OktaSettings
    setUnmappedGroups(OktaUnmappedGroups value)
    {
        unmappedGroups = value != null ? value : OktaUnmappedGroups.DEFAULT_ROLE;
        return this;
    }

    public OktaSettings
    setGroupMapping(Map<String,String> value)
    {
        groupMapping = value != null ? new LinkedHashMap<>(value) : new LinkedHashMap<>();
        return this;
    }

    public OktaSettings
    setTransactionTtl(Duration value)
    {
        transactionTtl = value;
        return this;
    }

    public OktaSettings
    setDiscoveryCacheTtl(Duration value)
    {
        discoveryCacheTtl = value;
        return this;
    }

    public OktaSettings
    setClockSkew(Duration value)
    {
        clockSkew = value;
        return this;
    }

    public OktaSettings
    setHttpTimeout(Duration value)
    {
        httpTimeout = value;
        return this;
    }

    public boolean
    isEnabled() { return enabled; }

    public String
    getIssuer() { return issuer; }

    public String
    getClientId() { return clientId; }

    public String
    getRedirectUri() { return redirectUri; }

    public String
    getPostLogoutRedirectUri() { return postLogoutRedirectUri; }

    public String
    getScope() { return scope; }

    public OktaRoleAssignment
    getRoleAssignment() { return roleAssignment; }

    public OktaUnmappedGroups
    getUnmappedGroups() { return unmappedGroups; }

    public Map<String,String>
    getGroupMapping() { return groupMapping; }

    public Duration
    getTransactionTtl() { return transactionTtl; }

    public Duration
    getDiscoveryCacheTtl() { return discoveryCacheTtl; }

    public Duration
    getClockSkew() { return clockSkew; }

    public Duration
    getHttpTimeout() { return httpTimeout; }
}

//////////////////////////////////////////////////////////////////////////////
