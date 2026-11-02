//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.platform;

import granite.agent.service.requestreply.IAgentQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@EnableTransactionManagement
public
class QueryPlatformConfiguration
    extends PlatformConfiguration
{
    @Bean
    @RequestScope
    public AgentQueryServiceController
    agentQueryServiceController(IAgentQueryService service)
    {
        return new AgentQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
