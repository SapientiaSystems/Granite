//////////////////////////////////////////////////////////////////////////////
// SessionControllerMappingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.ISessionFrontEndControllerService;
import org.junit.jupiter.api.Tag;

@Tag("CommitStage")
public
class SessionControllerMappingTest
    extends AbstractControllerMappingTest
{
    @Override
    protected Class<?>
    getServiceInterface() { return ISessionFrontEndControllerService.class; }

    @Override
    protected Class<?>
    getControllerClass() { return SessionFrontEndControllerServiceController.class; }
}

//////////////////////////////////////////////////////////////////////////////
