//////////////////////////////////////////////////////////////////////////////
// ExtendedGatewayTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.env.Environment;

@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class ExtendedGatewayTestConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected RouteLocatorBuilder.Builder
    configureRoutes(RouteLocatorBuilder.Builder routes,Environment environment)
    {
        super.configureRoutes(routes,environment);
        return
            addRoute(
                routes,
                "order-command-route",
                "/order-command-service/**",
                "retail.order-command-service.host",
                environment);
    }
}

//////////////////////////////////////////////////////////////////////////////
