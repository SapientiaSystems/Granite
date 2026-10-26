//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage }                           from "strata.foundation.core/Concurrent";
import type { HandleSessionInputRequest, HandleSessionInputReply,
              GetSessionOutputRequest, GetSessionOutputReply }   from "granite.frontendcontroller.service/RequestReply";
import { AbstractGraniteFrontEndControllerServiceClient }  from "./AbstractGraniteFrontEndControllerServiceClient";
import type { FetchFunction }                              from "./FetchFunction";
import type { ISessionFrontEndControllerServiceClient }       from "./ISessionFrontEndControllerServiceClient";

export
class SessionFrontEndControllerServiceClient
    extends AbstractGraniteFrontEndControllerServiceClient
    implements ISessionFrontEndControllerServiceClient
{
    constructor(baseUrl: string,fetchFunction?: FetchFunction)
    {
        super(baseUrl,fetchFunction);
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
