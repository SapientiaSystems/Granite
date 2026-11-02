//////////////////////////////////////////////////////////////////////////////
// GraniteContentQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.configuration;

import granite.content.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteContentQueryServerConfiguration
    extends AbstractGraniteContentServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/content-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
