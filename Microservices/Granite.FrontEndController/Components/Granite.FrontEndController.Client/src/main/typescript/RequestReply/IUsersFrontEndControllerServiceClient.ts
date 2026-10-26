//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { IFrontEndControllerServiceClient }  from "strata.frontendcontroller.client/RequestReply";
import type { IUsersFrontEndControllerService }    from "granite.frontendcontroller.service/RequestReply";

export
interface IUsersFrontEndControllerServiceClient
    extends IFrontEndControllerServiceClient,
            IUsersFrontEndControllerService
{
}

//////////////////////////////////////////////////////////////////////////////
