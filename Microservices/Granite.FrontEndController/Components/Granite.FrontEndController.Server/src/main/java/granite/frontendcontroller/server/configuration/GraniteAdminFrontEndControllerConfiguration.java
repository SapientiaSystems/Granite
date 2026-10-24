//////////////////////////////////////////////////////////////////////////////
// GraniteUserFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import com.fasterxml.jackson.databind.Module;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.service.requestreply.FrontEndJacksonModule;
import jakarta.inject.Named;
import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.user.client.requestreply.IUserCommandServiceClient;
import granite.user.client.requestreply.IUserQueryServiceClient;
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
import strata.frontendcontroller.server.configuration.AdminFrontEndControllerConfiguration;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@Import({FrontEndControllerHealthCheckConfiguration.class})
public
class GraniteAdminFrontEndControllerConfiguration
    extends AdminFrontEndControllerConfiguration
{
    @Autowired
    private IConfiguration configuration;

    @Autowired
    private SslBundles     sslBundles;

    @Override
    @Bean
    @RequestScoped
    @Named("Admin")
    public IRequestProcessor
    adminRequestProcessor()
    {
        return requestProcessorFactory().create();
    }

    @Override
    @Bean
    @Scope("singleton")
    @Named("Admin")
    public Executor
    adminExecutor()
    {
        return new BoundedExecutor(200,Duration.ofSeconds(10));
    }

    @Bean
    @RequestScoped
    public IUserQueryServiceClient
    userQueryServiceClient()
    {
        return requestProcessorFactory().userQueryClient();
    }

    @Bean
    @RequestScoped
    public IUserCommandServiceClient
    userCommandServiceClient()
    {
        return requestProcessorFactory().userCommandClient();
    }

    @Bean
    public Module
    frontEndJacksonModule()
    {
        return new FrontEndJacksonModule();
    }

    @Bean
    public ResponseCookieAdvice
    responseCookieAdvice()
    {
        return new ResponseCookieAdvice();
    }

    private GraniteRequestProcessorFactory
    requestProcessorFactory()
    {
        return new GraniteRequestProcessorFactory(configuration,sslBundles);
    }
}

//////////////////////////////////////////////////////////////////////////////
