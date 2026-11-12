//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.platform;

import granite.survey.service.requestreply.ISurveyQueryService;
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
    public SurveyQueryServiceController
    surveyQueryServiceController(ISurveyQueryService service)
    {
        return new SurveyQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
