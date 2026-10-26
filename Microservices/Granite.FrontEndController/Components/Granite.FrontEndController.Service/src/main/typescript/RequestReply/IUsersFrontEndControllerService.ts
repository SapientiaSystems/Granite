//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleUsersInputRequest } from "./HandleUsersInputRequest";
import type { HandleUsersInputReply } from "./HandleUsersInputReply";
import type { GetUsersOutputRequest } from "./GetUsersOutputRequest";
import type { GetUsersOutputReply } from "./GetUsersOutputReply";

export
interface IUsersFrontEndControllerService
    extends IFrontEndControllerService
{
    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>;

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
