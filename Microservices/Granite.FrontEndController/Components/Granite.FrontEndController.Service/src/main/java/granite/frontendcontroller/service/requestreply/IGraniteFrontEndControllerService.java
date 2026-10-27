//////////////////////////////////////////////////////////////////////////////
// IGraniteFrontEndControllerService.java
//////////////////////////////////////////////////////////////////////////////

package granite.frontendcontroller.service.requestreply;

import strata.frontendcontroller.service.requestreply.IFrontEndControllerService;

import java.util.concurrent.CompletionStage;

public
interface IGraniteFrontEndControllerService
    extends IFrontEndControllerService
{
    CompletionStage<HandleUsersInputReply>
    handleUsersInput(HandleUsersInputRequest request);

    CompletionStage<GetUsersOutputReply>
    getUsersOutput(GetUsersOutputRequest request);

    CompletionStage<HandleSessionInputReply>
    handleSessionInput(HandleSessionInputRequest request);

    CompletionStage<GetSessionOutputReply>
    getSessionOutput(GetSessionOutputRequest request);

    CompletionStage<HandlePreferencesInputReply>
    handlePreferencesInput(HandlePreferencesInputRequest request);

    CompletionStage<GetPreferencesOutputReply>
    getPreferencesOutput(GetPreferencesOutputRequest request);
}

//////////////////////////////////////////////////////////////////////////////
