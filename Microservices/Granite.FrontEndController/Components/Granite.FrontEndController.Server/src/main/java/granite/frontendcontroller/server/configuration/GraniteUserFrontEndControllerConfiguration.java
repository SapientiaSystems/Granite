//////////////////////////////////////////////////////////////////////////////
// GraniteUserFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.control.user.UserRequestProcessor;
import com.fasterxml.jackson.databind.Module;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import jakarta.inject.Named;
import jakarta.ws.rs.client.ClientBuilder;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ForwardedBearerClientRequestFilter;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
import granite.user.client.requestreply.UserCommandServiceClient;
import granite.user.client.requestreply.UserQueryServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import strata.foundation.core.concurrent.BoundedExecutor;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.RequestScoped;
import strata.frontendcontroller.server.application.IRequestProcessor;
import strata.frontendcontroller.server.configuration.UserFrontEndControllerConfiguration;

import java.time.Duration;
import java.util.concurrent.Executor;

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

    @Override
    @Bean
    @Scope("singleton")
    @Named("User")
    public Executor
    userExecutor()
    {
        return new BoundedExecutor(200,Duration.ofSeconds(10));
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

    @Bean
    @RequestScoped
    public IUserCommandServiceClient
    userCommandServiceClient()
    {
        return
            new UserCommandServiceClient(
                createClientBuilder(),
                configuration.getProperty("user-command-service.base-url"));
    }

    @Bean
    public Module
    frontEndJacksonModule()
    {
        return new FrontEndJacksonModule();
    }

    private ClientBuilder
    createClientBuilder()
    {
        return
            ClientBuilder
                .newBuilder()
                .register(ForwardedBearerClientRequestFilter.fromCurrentRequest())
                .sslContext(
                    sslBundles
                        .getBundle("granite-services")
                        .createSslContext());
    }
}

//////////////////////////////////////////////////////////////////////////////
