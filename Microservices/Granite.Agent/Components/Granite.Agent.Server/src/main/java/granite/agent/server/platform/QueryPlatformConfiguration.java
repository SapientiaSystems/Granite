//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.platform;

import granite.agent.service.requestreply.IAgentQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;

@Configuration
@EnableTransactionManagement
public
class QueryPlatformConfiguration
    extends PlatformConfiguration
{
    @Bean
    @RequestScoped
    public AgentQueryServiceController
    agentQueryServiceController(IAgentQueryService service)
    {
        return new AgentQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
