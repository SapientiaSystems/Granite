//////////////////////////////////////////////////////////////////////////////
// SessionCookieGatewayTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import granite.gateway.server.security.IServiceTokenSupplier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class SessionCookieGatewayTestConfiguration
    extends GraniteRoleRulesGatewayServerConfiguration
{
    @Bean
    @Override
    public IServiceTokenSupplier
    serviceTokenSupplier(Environment environment)
    {
        return () -> Mono.fromSupplier(() -> environment.getRequiredProperty("test.gateway.service-token"));
    }
}

//////////////////////////////////////////////////////////////////////////////
