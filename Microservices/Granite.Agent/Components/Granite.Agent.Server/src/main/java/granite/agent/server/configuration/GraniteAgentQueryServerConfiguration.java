//////////////////////////////////////////////////////////////////////////////
// GraniteAgentQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.configuration;

import granite.agent.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteAgentQueryServerConfiguration
    extends AbstractGraniteAgentServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/agent-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
