//////////////////////////////////////////////////////////////////////////////
// GraniteFeedbackQueryServerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.feedback.server.configuration;

import granite.feedback.server.platform.QueryPlatformConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({QueryPlatformConfiguration.class})
public
class GraniteFeedbackQueryServerConfiguration
    extends AbstractGraniteFeedbackServerConfiguration
{
    @Override
    protected String
    getServicePathPattern() { return "/feedback-query-service/**"; }
}

//////////////////////////////////////////////////////////////////////////////
