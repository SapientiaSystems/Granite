//////////////////////////////////////////////////////////////////////////////
// GraniteClientQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.configuration;

import granite.client.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteClientQueryServerConfiguration
    extends AbstractGraniteClientServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/client-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
