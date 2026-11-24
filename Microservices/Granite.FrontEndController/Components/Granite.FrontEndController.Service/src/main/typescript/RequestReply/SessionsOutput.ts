//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput } from "strata.frontendcontroller.service/RequestReply";
import type { SessionRowData } from "./SessionRowData";
import type { RoleData }       from "./RoleData";

export
interface SessionsOutput
    extends FrontEndOutput
{
    sessions:          Array<SessionRowData>;
    currentSessionId?: number | null;
    failureMessages:   Array<string>;
    roles:             Array<RoleData>;
}

//////////////////////////////////////////////////////////////////////////////
