//////////////////////////////////////////////////////////////////////////////
// GraniteUserQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.user.server.configuration;

import granite.user.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteUserQueryServerConfiguration
    extends AbstractGraniteUserServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/user-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
