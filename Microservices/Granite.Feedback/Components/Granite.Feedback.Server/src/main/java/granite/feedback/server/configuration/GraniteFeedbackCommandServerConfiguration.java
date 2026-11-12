//////////////////////////////////////////////////////////////////////////////
// GraniteFeedbackCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.configuration;

import granite.feedback.server.platform.CommandPlatformConfiguration;
import granite.feedback.server.platform.OutboxEventFeedbackEventSender;
import granite.feedback.service.event.IFeedbackEventSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.RequestScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteFeedbackCommandServerConfiguration
    extends AbstractGraniteFeedbackServerConfiguration
{
    @Bean
    @RequestScoped
    public IFeedbackEventSender
    feedbackEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventFeedbackEventSender(repository);
    }

    @Override
    protected String
    getServicePathPattern() { return "/feedback-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
