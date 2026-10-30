//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";

export
interface SessionOutput
    extends FrontEndOutput
{
    status:           string;
    userName?:        string | null;
    authorities?:     Array<string> | null;
    returnTo?:        string | null;
    passwordEnabled?: boolean;
    oktaEnabled?:     boolean;
    redirectUrl?:     string | null;
    loggedOut?:       boolean;
}

//////////////////////////////////////////////////////////////////////////////
