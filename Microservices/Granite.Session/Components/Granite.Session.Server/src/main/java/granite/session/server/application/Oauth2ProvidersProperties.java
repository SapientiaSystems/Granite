//////////////////////////////////////////////////////////////////////////////
// Oauth2ProvidersProperties.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.application;

import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.LinkedHashMap;
import java.util.Map;

@ConfigurationProperties("granite.session.oauth2")
@Validated
public
class Oauth2ProvidersProperties
{
    @Valid
    private Map<String,Oauth2ProviderConfig> providers;

    public
    Oauth2ProvidersProperties()
    {
        providers = new LinkedHashMap<>();
    }

    public Oauth2ProvidersProperties
    setProviders(Map<String,Oauth2ProviderConfig> value)
    {
        providers = value == null ? new LinkedHashMap<>() : value;
        return this;
    }

    public Map<String,Oauth2ProviderConfig>
    getProviders() { return providers; }
}

//////////////////////////////////////////////////////////////////////////////
