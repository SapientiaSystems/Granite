//////////////////////////////////////////////////////////////////////////////
// FrontEndControllerHealthCheckConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.ScopeConfiguration;
import strata.foundation.spring.inject.SingletonScoped;

@Configuration
@Import({ScopeConfiguration.class})
public
class FrontEndControllerHealthCheckConfiguration
{
    @Bean
    @SingletonScoped
    public FrontEndControllerHealthCheckController
    healthCheckController()
    {
        return new FrontEndControllerHealthCheckController();
    }
}

//////////////////////////////////////////////////////////////////////////////
