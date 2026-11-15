//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.client.server.platform;

import granite.client.service.requestreply.IClientQueryService;
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
    public ClientQueryServiceController
    clientQueryServiceController(IClientQueryService service)
    {
        return new ClientQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
