//////////////////////////////////////////////////////////////////////////////
// GraniteUserFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.control.user.UserRequestProcessor;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientBuilder;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.RequestScoped;
import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.server.configuration.UserFrontEndControllerConfiguration;

@Configuration
@Import({FrontEndControllerHealthCheckConfiguration.class})
public
class GraniteUserFrontEndControllerConfiguration
    extends UserFrontEndControllerConfiguration
{
    @Autowired
    private IConfiguration configuration;

    @Autowired
    private SslBundles     sslBundles;

    @Override
    @Bean
    @RequestScoped
    @Named("User")
    public IRequestProcessor
    userRequestProcessor()
    {
        return new UserRequestProcessor();
    }

    @Bean
    @RequestScoped
    public IUserQueryServiceClient
    userQueryServiceClient()
    {
        return
            new UserQueryServiceClient(
                createClientBuilder(),
                configuration.getProperty("user-query-service.base-url"));
    }

    private ClientBuilder
    createClientBuilder()
    {
        return
            ClientBuilder
                .newBuilder()
                .sslContext(
                    sslBundles
                        .getBundle("granite-services")
                        .createSslContext());
    }
}

//////////////////////////////////////////////////////////////////////////////
