//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import granite.feedback.service.requestreply.IFeedbackQueryService;
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
    public FeedbackQueryServiceController
    feedbackQueryServiceController(IFeedbackQueryService service)
    {
        return new FeedbackQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
