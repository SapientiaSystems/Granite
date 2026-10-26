//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { IFrontEndControllerServiceClient }  from "strata.frontendcontroller.client/RequestReply";
import type { ISessionFrontEndControllerService }    from "granite.frontendcontroller.service/RequestReply";

export
interface ISessionFrontEndControllerServiceClient
    extends IFrontEndControllerServiceClient,
            ISessionFrontEndControllerService
{
}

//////////////////////////////////////////////////////////////////////////////
