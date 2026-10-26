//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleUsersInputRequest }    from "./HandleUsersInputRequest";
import type { HandleUsersInputReply }      from "./HandleUsersInputReply";
import type { GetUsersOutputRequest }      from "./GetUsersOutputRequest";
import type { GetUsersOutputReply }        from "./GetUsersOutputReply";
import type { HandleSessionInputRequest }  from "./HandleSessionInputRequest";
import type { HandleSessionInputReply }    from "./HandleSessionInputReply";
import type { GetSessionOutputRequest }    from "./GetSessionOutputRequest";
import type { GetSessionOutputReply }      from "./GetSessionOutputReply";

export
interface IGraniteFrontEndControllerService
    extends IFrontEndControllerService
{
    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>;

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>;

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>;

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
