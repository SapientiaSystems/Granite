//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                           from "strata.foundation.core/Concurrent";
import type { HandleUsersInputRequest, HandleUsersInputReply,
              GetUsersOutputRequest, GetUsersOutputReply }   from "granite.frontendcontroller.service/RequestReply";
import { AbstractGraniteFrontEndControllerServiceClient }  from "./AbstractGraniteFrontEndControllerServiceClient";
import type { FetchFunction }                              from "./FetchFunction";
import type { IUsersFrontEndControllerServiceClient }       from "./IUsersFrontEndControllerServiceClient";

export
class UsersFrontEndControllerServiceClient
    extends AbstractGraniteFrontEndControllerServiceClient
    implements IUsersFrontEndControllerServiceClient
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
}

//////////////////////////////////////////////////////////////////////////////
