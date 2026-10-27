//////////////////////////////////////////////////////////////////////////////
// GraniteClientConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import jakarta.ws.rs.client.ClientBuilder;

public
class GraniteClientConfiguration
{
    private final ClientBuilder builder;
    private final String        baseUrl;

    public
    GraniteClientConfiguration(ClientBuilder builder,String baseUrl)
    {
        this.builder = builder;
        this.baseUrl = baseUrl;
    }

    public ClientBuilder
    getBuilder() { return builder; }

    public String
    getBaseUrl() { return baseUrl; }
}

//////////////////////////////////////////////////////////////////////////////
