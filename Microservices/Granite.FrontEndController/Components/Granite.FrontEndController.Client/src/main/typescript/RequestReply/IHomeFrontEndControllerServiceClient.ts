//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { IFrontEndControllerServiceClient }  from "strata.frontendcontroller.client/RequestReply";
import type { IHomeFrontEndControllerService }    from "granite.frontendcontroller.service/RequestReply";

export
interface IHomeFrontEndControllerServiceClient
    extends IFrontEndControllerServiceClient,
            IHomeFrontEndControllerService
{
}

//////////////////////////////////////////////////////////////////////////////
