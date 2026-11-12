//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import granite.agent.server.domain.IConversationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;


@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScoped
    public ITransactionalAgentQueryService
    agentQueryService(IConversationRepository repository,ModelMapper mapper)
    {
        return new AgentQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
