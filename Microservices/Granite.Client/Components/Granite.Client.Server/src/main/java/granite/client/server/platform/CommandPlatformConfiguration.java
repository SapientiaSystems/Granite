//////////////////////////////////////////////////////////////////////////////
// CommandPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.service.requestreply.IClientCommandService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;
import strata.outbox.core.repository.RequestScopedRepositoryConfiguration;

@Configuration
@EnableTransactionManagement
@Import({RequestScopedRepositoryConfiguration.class})
public
class CommandPlatformConfiguration
    extends PlatformConfiguration
{
    @Bean
    @RequestScoped
    public ClientCommandServiceController
    clientCommandServiceController(IClientCommandService service)
    {
        return new ClientCommandServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
