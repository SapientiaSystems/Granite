//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.agent.server.application;

import granite.agent.server.domain.IConversationRepository;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;


@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScope
    public ITransactionalAgentQueryService
    agentQueryService(IConversationRepository repository,ModelMapper mapper)
    {
        return new AgentQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
