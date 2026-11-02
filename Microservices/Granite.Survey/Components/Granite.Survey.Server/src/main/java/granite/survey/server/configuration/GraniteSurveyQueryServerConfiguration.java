//////////////////////////////////////////////////////////////////////////////
// GraniteSurveyQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.survey.server.configuration;

import granite.survey.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteSurveyQueryServerConfiguration
    extends AbstractGraniteSurveyServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/survey-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
