//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }           from "strata.foundation.core/Concurrent";
import type { IFrontEndControllerService } from "strata.frontendcontroller.service/RequestReply";
import type { HandleUsersInputRequest }    from "./HandleUsersInputRequest";
import type { HandleUsersInputReply }      from "./HandleUsersInputReply";
import type { GetUsersOutputRequest }      from "./GetUsersOutputRequest";
import type { GetUsersOutputReply }        from "./GetUsersOutputReply";
import type { HandleSessionsInputRequest } from "./HandleSessionsInputRequest";
import type { HandleSessionsInputReply }   from "./HandleSessionsInputReply";
import type { GetSessionsOutputRequest }   from "./GetSessionsOutputRequest";
import type { GetSessionsOutputReply }     from "./GetSessionsOutputReply";
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

    handleSessionsInput(request: HandleSessionsInputRequest): ICompletionStage<HandleSessionsInputReply>;

    getSessionsOutput(request: GetSessionsOutputRequest): ICompletionStage<GetSessionsOutputReply>;

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>;

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>;

    handlePreferencesInput(request: HandlePreferencesInputRequest): ICompletionStage<HandlePreferencesInputReply>;

    getPreferencesOutput(request: GetPreferencesOutputRequest): ICompletionStage<GetPreferencesOutputReply>;
}

//////////////////////////////////////////////////////////////////////////////
