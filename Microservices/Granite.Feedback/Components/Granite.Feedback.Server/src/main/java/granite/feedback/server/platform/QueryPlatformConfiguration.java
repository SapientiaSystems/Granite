//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import granite.feedback.service.requestreply.IFeedbackQueryService;
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
    public FeedbackQueryServiceController
    feedbackQueryServiceController(IFeedbackQueryService service)
    {
        return new FeedbackQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
