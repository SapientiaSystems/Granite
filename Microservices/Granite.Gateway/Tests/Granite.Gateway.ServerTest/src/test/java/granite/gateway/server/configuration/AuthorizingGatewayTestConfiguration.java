//////////////////////////////////////////////////////////////////////////////
// AuthorizingGatewayTestConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.gateway.server.configuration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;

@SpringBootConfiguration
@EnableAutoConfiguration(
    exclude = {
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        KafkaAutoConfiguration.class})
public
class AuthorizingGatewayTestConfiguration
    extends GraniteGatewayServerConfiguration
{
    @Override
    protected ServerHttpSecurity.AuthorizeExchangeSpec
    configureApplicationAuthorization(ServerHttpSecurity.AuthorizeExchangeSpec exchanges,Environment environment)
    {
        return
            exchanges
                .pathMatchers(HttpMethod.POST,"/user-command-service/**")
                    .hasAuthority("ROLE_ADMIN")
                .pathMatchers("/session-query-service/**")
                    .hasAnyAuthority("ROLE_ADMIN","SCOPE_internal");
    }
}

//////////////////////////////////////////////////////////////////////////////
