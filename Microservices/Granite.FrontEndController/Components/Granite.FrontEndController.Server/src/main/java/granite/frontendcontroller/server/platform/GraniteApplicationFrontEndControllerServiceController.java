//////////////////////////////////////////////////////////////////////////////
// GraniteApplicationFrontEndControllerServiceController.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("${granite.frontendcontroller.service-path}")
public
class GraniteApplicationFrontEndControllerServiceController
    extends GraniteFrontEndControllerServiceController<IGraniteFrontEndControllerService>
{
    public
    GraniteApplicationFrontEndControllerServiceController(IGraniteFrontEndControllerService service)
    {
        super(service);
    }
}

//////////////////////////////////////////////////////////////////////////////
