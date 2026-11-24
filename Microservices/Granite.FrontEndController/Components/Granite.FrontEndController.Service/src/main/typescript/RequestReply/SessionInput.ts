//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndInput } from "strata.frontendcontroller.service/RequestReply";

export
interface SessionInput
    extends FrontEndInput
{
    userName?: string | null;
    password?: string | null;
    returnTo?: string | null;
    code?:     string | null;
    state?:    string | null;
    application?: string | null;
    currentPassword?: string | null;
    newPassword?: string | null;
}

//////////////////////////////////////////////////////////////////////////////
