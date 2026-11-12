//////////////////////////////////////////////////////////////////////////////
// GraniteSurveyCommandServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.configuration;

import granite.survey.server.platform.CommandPlatformConfiguration;
import granite.survey.server.platform.OutboxEventSurveyEventSender;
import granite.survey.service.event.ISurveyEventSender;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.RequestScoped;
import strata.outbox.core.repository.IOutboxEventRepository;

@Configuration
@Import({CommandPlatformConfiguration.class})
public
class GraniteSurveyCommandServerConfiguration
    extends AbstractGraniteSurveyServerConfiguration
{
    @Bean
    @RequestScoped
    public ISurveyEventSender
    surveyEventSender(IOutboxEventRepository repository)
    {
        return new OutboxEventSurveyEventSender(repository);
    }

    @Override
    protected String
    getServicePathPattern() { return "/survey-command-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
