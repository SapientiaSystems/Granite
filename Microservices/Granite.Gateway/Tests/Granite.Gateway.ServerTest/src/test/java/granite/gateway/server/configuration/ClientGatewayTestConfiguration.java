//////////////////////////////////////////////////////////////////////////////
// ClientGatewayTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.foundation.core.security.ApplicationRolesManager;
import granite.foundation.core.security.GraniteAuthorities;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.core.env.Environment;
import org.springframework.security.config.web.server.ServerHttpSecurity.AuthorizeExchangeSpec;

import java.util.List;

@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class ClientGatewayTestConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected List<String>
    getUsersReaderAuthorities(Environment environment)
    {
        return List.of(ApplicationRolesManager.ADMIN,"ROLE_AUDITOR",GraniteAuthorities.SCOPE_INTERNAL);
    }

    @Override
    protected Builder
    configureApplicationRoutes(Builder routes,Environment environment)
    {
        return
            addRoute(
                routes,
                "poplar-route",
                "/poplar-service/**",
                "granite.user-query-service.host",
                environment);
    }

    @Override
    protected AuthorizeExchangeSpec
    configureApplicationAuthorization(AuthorizeExchangeSpec exchanges,Environment environment)
    {
        return
            exchanges
                .pathMatchers("/poplar-service/**")
                    .hasAuthority("ROLE_USER");
    }
}

//////////////////////////////////////////////////////////////////////////////
