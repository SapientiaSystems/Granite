//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.session.server.platform;

import granite.session.service.requestreply.ISessionQueryService;
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
    public SessionQueryServiceController
    sessionQueryServiceController(ISessionQueryService service)
    {
        return new SessionQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
