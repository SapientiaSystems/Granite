//////////////////////////////////////////////////////////////////////////////
// GraniteFrontEndControllerConfiguration.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.server.application.GraniteFrontEndControllerContext;
import granite.frontendcontroller.server.application.GraniteFrontEndControllerService;
import granite.frontendcontroller.server.platform.GraniteApplicationFrontEndControllerServiceController;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import jakarta.inject.Named;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import strata.foundation.spring.inject.RequestScoped;

@Configuration
public
class GraniteFrontEndControllerConfiguration
    extends AbstractGraniteFrontEndControllerConfiguration<IGraniteFrontEndControllerService>
{
    @Bean
    @RequestScoped
    @Named("Granite")
    @Override
    public GraniteApplicationFrontEndControllerServiceController
    frontEndControllerServiceController(
        @Named("Granite")
        IGraniteFrontEndControllerService service)
    {
        return (GraniteApplicationFrontEndControllerServiceController)super.frontEndControllerServiceController(service);
    }

    @Override
    protected IGraniteFrontEndControllerService
    createService(GraniteFrontEndControllerContext context)
    {
        return new GraniteFrontEndControllerService(context);
    }

    @Override
    protected Object
    createController(IGraniteFrontEndControllerService service)
    {
        return new GraniteApplicationFrontEndControllerServiceController(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
