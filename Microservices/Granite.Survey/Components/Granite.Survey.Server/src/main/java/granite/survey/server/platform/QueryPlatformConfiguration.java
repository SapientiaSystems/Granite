//////////////////////////////////////////////////////////////////////////////
// QueryPlatformConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.platform;

import granite.survey.service.requestreply.ISurveyQueryService;
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
    public SurveyQueryServiceController
    surveyQueryServiceController(ISurveyQueryService service)
    {
        return new SurveyQueryServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
