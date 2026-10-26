//////////////////////////////////////////////////////////////////////////////
// ISessionFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

import java.util.concurrent.CompletionStage;

public
interface ISessionFrontEndControllerService
    extends IFrontEndControllerService
{
    CompletionStage<HandleSessionInputReply>
    handleSessionInput(HandleSessionInputRequest request);

    CompletionStage<GetSessionOutputReply>
    getSessionOutput(GetSessionOutputRequest request);
}

//////////////////////////////////////////////////////////////////////////////
