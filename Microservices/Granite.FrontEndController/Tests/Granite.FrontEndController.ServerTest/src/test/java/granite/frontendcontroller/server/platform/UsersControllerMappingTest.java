//////////////////////////////////////////////////////////////////////////////
// UsersControllerMappingTest.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.platform;

import granite.frontendcontroller.service.requestreply.IUsersFrontEndControllerService;
import org.junit.jupiter.api.Tag;

@Tag("CommitStage")
public
class UsersControllerMappingTest
    extends AbstractControllerMappingTest
{
    @Override
    protected Class<?>
    getServiceInterface() { return IUsersFrontEndControllerService.class; }

    @Override
    protected Class<?>
    getControllerClass() { return UsersFrontEndControllerServiceController.class; }
}

//////////////////////////////////////////////////////////////////////////////
