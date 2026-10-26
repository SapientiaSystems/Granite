//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Client
//////////////////////////////////////////////////////////////////////////////

import type { IFrontEndControllerServiceClient }  from "strata.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerService } from "granite.frontendcontroller.service/RequestReply";

export
interface IGraniteFrontEndControllerServiceClient
    extends IFrontEndControllerServiceClient,
            IGraniteFrontEndControllerService
{
}

//////////////////////////////////////////////////////////////////////////////
