//////////////////////////////////////////////////////////////////////////////
// CommandPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.platform;

import granite.feedback.service.requestreply.IFeedbackCommandService;
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
    public FeedbackCommandServiceController
    feedbackCommandServiceController(IFeedbackCommandService service)
    {
        return new FeedbackCommandServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
