//////////////////////////////////////////////////////////////////////////////
// Oauth2ProviderConfig.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import jakarta.validation.constraints.NotBlank;

public
class Oauth2ProviderConfig
{
    private String name;
    @NotBlank
    private String issuerUri;
    @NotBlank
    private String audience;
    @NotBlank
    private String jwksUri;

    public
    Oauth2ProviderConfig()
    {
        name      = null;
        issuerUri = null;
        audience  = null;
        jwksUri   = null;
    }

    public Oauth2ProviderConfig
    setName(String value)
    {
        name = value;
        return this;
    }

    public String
    getName() { return name; }

    public Oauth2ProviderConfig
    setIssuerUri(String value)
    {
        issuerUri = value;
        return this;
    }

    public String
    getIssuerUri() { return issuerUri; }

    public Oauth2ProviderConfig
    setAudience(String value)
    {
        audience = value;
        return this;
    }

    public String
    getAudience() { return audience; }

    public Oauth2ProviderConfig
    setJwksUri(String value)
    {
        jwksUri = value;
        return this;
    }

    public String
    getJwksUri() { return jwksUri; }
}

//////////////////////////////////////////////////////////////////////////////
