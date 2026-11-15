//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndOutput }    from "strata.frontendcontroller.service/RequestReply";
import type { ClientData }        from "granite.client.service/RequestReply";
import type { RoleData }          from "./RoleData";
import type { ClientOwnerData }   from "./ClientOwnerData";

export
interface ClientsOutput
    extends FrontEndOutput
{
    clients:               Array<ClientData>;
    roles:                 Array<RoleData>;
    users:                 Array<ClientOwnerData>;
    revealedClientId?:     string | null;
    revealedClientSecret?: string | null;
    failureMessages:       Array<string>;
}

//////////////////////////////////////////////////////////////////////////////
