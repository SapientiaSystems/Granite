//////////////////////////////////////////////////////////////////////////////
// IExtendedFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.server.configuration;

import granite.frontendcontroller.service.requestreply.GetSessionOutputReply;
import granite.frontendcontroller.service.requestreply.GetSessionOutputRequest;
import granite.frontendcontroller.service.requestreply.IGraniteFrontEndControllerService;

import java.util.concurrent.CompletionStage;

public
interface IExtendedFrontEndControllerService
    extends IGraniteFrontEndControllerService
{
    CompletionStage<GetSessionOutputReply>
    getEchoOutput(GetSessionOutputRequest request);
}

//////////////////////////////////////////////////////////////////////////////
