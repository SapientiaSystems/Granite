//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleSessionInputRequest } from "./HandleSessionInputRequest";
import type { HandleSessionInputReply } from "./HandleSessionInputReply";
import type { GetSessionOutputRequest } from "./GetSessionOutputRequest";
import type { GetSessionOutputReply } from "./GetSessionOutputReply";

export
interface ISessionFrontEndControllerService
    extends IFrontEndControllerService
{
    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>;

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
