//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.server.platform.SessionFrontEndControllerServiceController;
import granite.frontendcontroller.server.platform.UsersFrontEndControllerServiceController;
import granite.frontendcontroller.service.requestreply.ISessionFrontEndControllerService;
import granite.frontendcontroller.service.requestreply.IUsersFrontEndControllerService;
import jakarta.inject.Named;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import strata.foundation.core.concurrent.BoundedExecutor;
import strata.foundation.core.configuration.IConfiguration;
import strata.foundation.spring.inject.RequestScoped;
import strata.frontendcontroller.server.application.ApplicationConfiguration;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@Import({ApplicationConfiguration.class,FrontEndControllerHealthCheckConfiguration.class})
public
class GraniteFrontEndControllerConfiguration
{
    @Autowired
    private IConfiguration configuration;

    @Autowired
    private SslBundles     sslBundles;

    @Bean
    @Scope("singleton")
    @Named("FrontEndController")
    public Executor
    frontEndControllerExecutor()
    {
        return new BoundedExecutor(200,Duration.ofSeconds(10));
    }

    @Bean
    @RequestScoped
    @Named("Users")
    public IUsersFrontEndControllerService
    usersFrontEndControllerService(
        @Named("FrontEndController")
        Executor    executor,
        ModelMapper mapper)
    {
        return serviceFactory().createUsersService(mapper,executor);
    }

    @Bean
    @RequestScoped
    @Named("Session")
    public ISessionFrontEndControllerService
    sessionFrontEndControllerService(
        @Named("FrontEndController")
        Executor    executor,
        ModelMapper mapper)
    {
        return serviceFactory().createSessionService(mapper,executor);
    }

    @Bean
    @RequestScoped
    @Named("Users")
    public UsersFrontEndControllerServiceController
    usersFrontEndControllerServiceController(
        @Named("Users")
        IUsersFrontEndControllerService service)
    {
        return new UsersFrontEndControllerServiceController(service);
    }

    @Bean
    @RequestScoped
    @Named("Session")
    public SessionFrontEndControllerServiceController
    sessionFrontEndControllerServiceController(
        @Named("Session")
        ISessionFrontEndControllerService service)
    {
        return new SessionFrontEndControllerServiceController(service);
    }

    @Bean
    public ResponseCookieAdvice
    responseCookieAdvice()
    {
        return new ResponseCookieAdvice();
    }

    private GraniteFrontEndControllerServiceFactory
    serviceFactory()
    {
        return new GraniteFrontEndControllerServiceFactory(configuration,sslBundles);
    }
}

//////////////////////////////////////////////////////////////////////////////
