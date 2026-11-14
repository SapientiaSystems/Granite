//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                                       from "react";
import type { ClientData, CreateClientData, UpdateClientData }     from "granite.client.service/RequestReply";
import type { ClientOwnerData, RoleData }                          from "granite.frontendcontroller.service/RequestReply";

export interface IClientsResult
{
    clients:         Array<ClientData>;
    failureMessages: Array<string>;
}

export interface IClientsEditableViewProps
{
    readonly clients?:         Array<ClientData>;
    readonly roles?:           Array<RoleData>;
    readonly users?:           Array<ClientOwnerData>;
    readonly editable?:        boolean;
    readonly showOwners?:      boolean;
    readonly loading?:         boolean;
    readonly onRenderToolbar?: (toolbar: ReactElement) => void;
    readonly onSave?:          (changes: { clientsToCreate: Array<CreateClientData>; clientsToUpdate: Array<UpdateClientData> }) => Promise<IClientsResult>;
    readonly onDelete?:        (clientIds: Array<string>) => Promise<IClientsResult>;
    readonly onRotate?:        (clientId: string) => Promise<IClientsResult>;
}

//////////////////////////////////////////////////////////////////////////////
