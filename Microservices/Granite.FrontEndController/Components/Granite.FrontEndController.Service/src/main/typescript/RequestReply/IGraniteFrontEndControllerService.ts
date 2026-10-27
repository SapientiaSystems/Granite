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
import type { HandlePreferencesInputRequest } from "./HandlePreferencesInputRequest";
import type { HandlePreferencesInputReply }   from "./HandlePreferencesInputReply";
import type { GetPreferencesOutputRequest }   from "./GetPreferencesOutputRequest";
import type { GetPreferencesOutputReply }     from "./GetPreferencesOutputReply";

export
interface IGraniteFrontEndControllerService
    extends IFrontEndControllerService
{
    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>;

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>;

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>;

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>;

    handlePreferencesInput(request: HandlePreferencesInputRequest): ICompletionStage<HandlePreferencesInputReply>;

    getPreferencesOutput(request: GetPreferencesOutputRequest): ICompletionStage<GetPreferencesOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
