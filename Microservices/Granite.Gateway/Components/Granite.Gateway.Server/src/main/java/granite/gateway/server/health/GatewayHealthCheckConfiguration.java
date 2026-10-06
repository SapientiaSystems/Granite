//////////////////////////////////////////////////////////////////////////////
// GatewayHealthCheckConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.health;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.spring.inject.ScopeConfiguration;
import strata.foundation.spring.inject.SingletonScoped;

@Configuration
@Import({ScopeConfiguration.class})
public
class GatewayHealthCheckConfiguration
{
    @Bean
    @SingletonScoped
    public GatewayHealthCheckController
    healthCheckController()
    {
        return new GatewayHealthCheckController();
    }
}

//////////////////////////////////////////////////////////////////////////////
