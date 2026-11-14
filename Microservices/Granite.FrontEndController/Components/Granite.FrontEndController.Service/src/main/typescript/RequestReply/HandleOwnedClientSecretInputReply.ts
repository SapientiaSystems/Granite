//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { HandleInputReply } from "strata.frontendcontroller.service/RequestReply";
import type { OwnedClientSecretInput } from "./OwnedClientSecretInput";
import type { ClientsOutput } from "./ClientsOutput";

export
interface HandleOwnedClientSecretInputReply
    extends HandleInputReply<OwnedClientSecretInput,ClientsOutput>
{
}

//////////////////////////////////////////////////////////////////////////////
