//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleHomeInputRequest } from "./HandleHomeInputRequest";
import type { HandleHomeInputReply } from "./HandleHomeInputReply";
import type { GetHomeOutputRequest } from "./GetHomeOutputRequest";
import type { GetHomeOutputReply } from "./GetHomeOutputReply";

export
interface IHomeFrontEndControllerService
    extends IFrontEndControllerService
{
    handleHomeInput(request: HandleHomeInputRequest): ICompletionStage<HandleHomeInputReply>;

    getHomeOutput(request: GetHomeOutputRequest): ICompletionStage<GetHomeOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
