//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }    from "strata.frontendcontroller.service/RequestReply";
import type { UserData }   from "granite.user.service/RequestReply";

export
interface HomeOutput
    extends FrontEndOutput
{
    readonly userName:             string;
    readonly firstName:            string;
    readonly lastName:             string;
    readonly emailAddress:         string;
    readonly role:                 string;
    readonly totalUserCount: number;
    readonly recentUsers:    Array<UserData>;
}

//////////////////////////////////////////////////////////////////////////////
