//////////////////////////////////////////////////////////////////////////////
// GraniteClientSettings.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import jakarta.ws.rs.client.ClientBuilder;

import java.util.Objects;

public
class GraniteClientSettings
{
    private final ClientBuilder builder;
    private final String        baseUrl;

    public
    GraniteClientSettings(ClientBuilder builder,String baseUrl)
    {
        this.builder = Objects.requireNonNull(builder,"builder");
        this.baseUrl = Objects.requireNonNull(baseUrl,"baseUrl");
    }

    public ClientBuilder
    getBuilder() { return builder; }

    public String
    getBaseUrl() { return baseUrl; }
}

//////////////////////////////////////////////////////////////////////////////
