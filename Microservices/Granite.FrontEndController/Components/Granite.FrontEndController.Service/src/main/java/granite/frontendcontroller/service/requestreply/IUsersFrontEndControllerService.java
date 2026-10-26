//////////////////////////////////////////////////////////////////////////////
// IUsersFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

import java.util.concurrent.CompletionStage;

public
interface IUsersFrontEndControllerService
    extends IFrontEndControllerService
{
    CompletionStage<HandleUsersInputReply>
    handleUsersInput(HandleUsersInputRequest request);

    CompletionStage<GetUsersOutputReply>
    getUsersOutput(GetUsersOutputRequest request);
}

//////////////////////////////////////////////////////////////////////////////
