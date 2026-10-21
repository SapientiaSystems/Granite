//////////////////////////////////////////////////////////////////////////////
// GraniteSessionQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.configuration;

import granite.session.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteSessionQueryServerConfiguration
    extends AbstractGraniteSessionServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/session-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
