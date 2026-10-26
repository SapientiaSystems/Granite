//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.health.FrontEndControllerHealthCheckConfiguration;
import granite.frontendcontroller.server.platform.ResponseCookieAdvice;
import granite.frontendcontroller.server.platform.GraniteFrontEndControllerServiceController;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
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
    @Named("Granite")
    public IGraniteFrontEndControllerService
    graniteFrontEndControllerService(
        @Named("FrontEndController")
        Executor    executor,
        ModelMapper mapper)
    {
        return serviceFactory().createService(mapper,executor);
    }

    @Bean
    @RequestScoped
    @Named("Granite")
    public GraniteFrontEndControllerServiceController
    graniteFrontEndControllerServiceController(
        @Named("Granite")
        IGraniteFrontEndControllerService service)
    {
        return new GraniteFrontEndControllerServiceController(service);
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
