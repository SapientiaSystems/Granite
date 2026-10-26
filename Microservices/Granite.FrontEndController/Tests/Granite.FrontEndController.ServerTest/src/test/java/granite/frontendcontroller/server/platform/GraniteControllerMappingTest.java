//////////////////////////////////////////////////////////////////////////////
// GraniteControllerMappingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;
import org.junit.jupiter.api.Tag;

@Tag("CommitStage")
public
class GraniteControllerMappingTest
    extends AbstractControllerMappingTest
{
    @Override
    protected Class<?>
    getServiceInterface() { return IGraniteFrontEndControllerService.class; }

    @Override
    protected Class<?>
    getControllerClass() { return GraniteFrontEndControllerServiceController.class; }
}

//////////////////////////////////////////////////////////////////////////////
