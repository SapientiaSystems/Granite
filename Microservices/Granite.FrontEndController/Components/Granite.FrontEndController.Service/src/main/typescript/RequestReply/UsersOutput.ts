//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }    from "strata.frontendcontroller.service/RequestReply";
import type { UserData } from "granite.user.service/RequestReply";
import type { RoleData } from "./RoleData";

export
interface UsersOutput
    extends FrontEndOutput
{
    users:           Array<UserData>;
    failureMessages: Array<string>;
    roles:           Array<RoleData>;
}

//////////////////////////////////////////////////////////////////////////////
