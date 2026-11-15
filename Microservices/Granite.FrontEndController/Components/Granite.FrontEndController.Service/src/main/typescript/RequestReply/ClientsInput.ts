//////////////////////////////////////////////////////////////////////////////
// Granite.FrontEndController.Service
//////////////////////////////////////////////////////////////////////////////

import type { FrontEndInput }                          from "strata.frontendcontroller.service/RequestReply";
import type { CreateClientData, UpdateClientData }     from "granite.client.service/RequestReply";

export
interface ClientsInput
    extends FrontEndInput
{
    clientsToCreate:   Array<CreateClientData>;
    clientsToUpdate:   Array<UpdateClientData>;
    clientIdsToDelete: Array<string>;
    clientIdToRotate?: string | null;
    secretExpiresAt?:  string | null;
}

//////////////////////////////////////////////////////////////////////////////
