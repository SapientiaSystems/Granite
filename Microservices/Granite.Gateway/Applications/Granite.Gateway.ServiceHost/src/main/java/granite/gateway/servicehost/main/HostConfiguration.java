//////////////////////////////////////////////////////////////////////////////
// HostConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.servicehost.main;

import granite.gateway.server.health.GatewayHealthCheckConfiguration;
import granite.gateway.server.route.RouteConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@Import({
    RouteConfiguration.class,
    GatewayHealthCheckConfiguration.class})
public
class HostConfiguration
{
    @Bean
    public InitializingBean
    initialize()
    {
        return
            () ->
                SecurityContextHolder
                    .setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public SecurityWebFilterChain
    securityWebFilterChain(ServerHttpSecurity http)
    {
        return
            http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }
}

//////////////////////////////////////////////////////////////////////////////
