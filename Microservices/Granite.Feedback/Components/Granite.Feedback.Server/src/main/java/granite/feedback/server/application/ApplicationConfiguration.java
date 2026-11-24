//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.application;

import granite.feedback.server.domain.IFeedbackRepository;
import granite.feedback.service.event.IFeedbackEventSender;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import strata.foundation.spring.inject.RequestScoped;

import java.time.Clock;

@Configuration
@EnableTransactionManagement
public
class ApplicationConfiguration
{
    @Bean
    @RequestScoped
    public ITransactionalFeedbackCommandService
    feedbackCommandService(
        IFeedbackRepository   repository,
        IFeedbackEventSender  sender,
        ModelMapper        mapper,
        Clock              clock)
    {
        return new FeedbackCommandService(repository,sender,mapper,clock);
    }

    @Bean
    @RequestScoped
    public ITransactionalFeedbackQueryService
    feedbackQueryService(IFeedbackRepository repository,ModelMapper mapper)
    {
        return new FeedbackQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
