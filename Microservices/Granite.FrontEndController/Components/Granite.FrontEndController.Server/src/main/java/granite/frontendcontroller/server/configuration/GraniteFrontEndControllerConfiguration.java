//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.foundation.core.security.ApplicationRoles;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerService;
import granite.frontendcontroller.server.platform.GraniteApplicationFrontEndControllerServiceController;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import jakarta.inject.Named;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import strata.foundation.spring.inject.RequestScoped;

import java.util.concurrent.Executor;

@Configuration
public
class GraniteFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IGraniteFrontEndControllerService>
{
    @Bean
    @RequestScoped
    @Named("Granite")
    public IGraniteFrontEndControllerService
    frontEndControllerService(
        @Named("FrontEndController")
        Executor         executor,
        ModelMapper      mapper,
        ApplicationRoles roles)
    {
        return createService(createContext(mapper,executor,roles));
    }

    @Bean
    @RequestScoped
    @Named("Granite")
    public GraniteApplicationFrontEndControllerServiceController
    frontEndControllerServiceController(
        @Named("Granite")
        IGraniteFrontEndControllerService service)
    {
        return new GraniteApplicationFrontEndControllerServiceController(service);
    }

    @Override
    protected IGraniteFrontEndControllerService
    createService(GraniteFrontEndControllerContext context)
    {
        return new GraniteFrontEndControllerService(context);
    }
}

//////////////////////////////////////////////////////////////////////////////
