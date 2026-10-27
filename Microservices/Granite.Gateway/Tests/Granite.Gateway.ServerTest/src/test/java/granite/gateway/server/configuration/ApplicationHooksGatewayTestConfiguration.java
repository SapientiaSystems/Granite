//////////////////////////////////////////////////////////////////////////////
// ApplicationHooksGatewayTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;

@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class ApplicationHooksGatewayTestConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected Builder
    configureApplicationRoutes(Builder routes,Environment environment)
    {
        return
            addRoute(
                routes,
                "promotion-query-route",
                "/promotion-query-service/**",
                "granite.user-query-service.host",
                environment);
    }

    @Override
    protected AuthorizeExchangeSpec
    configureApplicationAuthorization(AuthorizeExchangeSpec exchanges,Environment environment)
    {
        return
            exchanges
                .pathMatchers(HttpMethod.POST,"/frontendcontroller-user-service/get-promotions-output")
                    .hasAuthority("ROLE_PROMOTIONS_REVIEWER")
                .pathMatchers("/frontendcontroller-user-service/**")
                    .hasAuthority("ROLE_PROMOTIONS_REVIEWER")
                .pathMatchers("/user-command-service/**")
                    .permitAll()
                .pathMatchers("/session-command-service/resolve-session")
                    .permitAll();
    }
}

//////////////////////////////////////////////////////////////////////////////
