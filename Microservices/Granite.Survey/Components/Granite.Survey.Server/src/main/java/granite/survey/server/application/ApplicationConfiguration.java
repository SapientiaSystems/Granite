//////////////////////////////////////////////////////////////////////////////
// ApplicationConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.application;

import granite.survey.server.domain.ISurveyResponseRepository;
import granite.survey.service.event.ISurveyEventSender;
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
    public ITransactionalSurveyCommandService
    surveyCommandService(
        ISurveyResponseRepository   repository,
        ISurveyEventSender  sender,
        ModelMapper        mapper,
        Clock              clock)
    {
        return new SurveyCommandService(repository,sender,mapper,clock);
    }

    @Bean
    @RequestScoped
    public ITransactionalSurveyQueryService
    surveyQueryService(ISurveyResponseRepository repository,ModelMapper mapper)
    {
        return new SurveyQueryService(repository,mapper);
    }
}

//////////////////////////////////////////////////////////////////////////////
