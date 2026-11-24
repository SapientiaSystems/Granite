//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.content.server.platform;

import granite.content.service.requestreply.IContentQueryService;
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
    public ContentQueryServiceController
    contentQueryServiceController(IContentQueryService service)
    {
        return new ContentQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
