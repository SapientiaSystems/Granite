//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                          from "strata.foundation.core/Concurrent";
import type { HandleUsersInputRequest, HandleUsersInputReply,
              GetUsersOutputRequest, GetUsersOutputReply,
              HandleSessionInputRequest, HandleSessionInputReply,
              GetSessionOutputRequest, GetSessionOutputReply }  from "granite.frontendcontroller.service/RequestReply";
import { AbstractGraniteFrontEndControllerServiceClient } from "./AbstractGraniteFrontEndControllerServiceClient";
import type { FetchFunction }                             from "./FetchFunction";
import type { IGraniteFrontEndControllerServiceClient }   from "./IGraniteFrontEndControllerServiceClient";

export
class GraniteFrontEndControllerServiceClient
    extends AbstractGraniteFrontEndControllerServiceClient
    implements IGraniteFrontEndControllerServiceClient
{
    constructor(baseUrl: string,fetchFunction?: FetchFunction)
    {
        super(baseUrl,fetchFunction);
    }

    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>
    {
        return this.doPost<HandleUsersInputReply,HandleUsersInputRequest>(
            "handle-users-input",
            this.initializeRequest(request));
    }

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>
    {
        return this.doPost<GetUsersOutputReply,GetUsersOutputRequest>(
            "get-users-output",
            this.initializeRequest(request));
    }

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>
    {
        return this.doPost<HandleSessionInputReply,HandleSessionInputRequest>(
            "handle-session-input",
            this.initializeRequest(request));
    }

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>
    {
        return this.doPost<GetSessionOutputReply,GetSessionOutputRequest>(
            "get-session-output",
            this.initializeRequest(request));
    }
}

//////////////////////////////////////////////////////////////////////////////
