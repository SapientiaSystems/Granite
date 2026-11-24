//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { ClientsInput, ClientsOutput } from "granite.frontendcontroller.service/RequestReply";
import type { IClientsResult }      from "../EditableView";
import type { ISecretReveal }       from "./SecretDialog";
import { toSecretExpiry }       from "../EditableView";

export function rotateFields(clientId: string,secretExpiryDate: string | null): Partial<ClientsInput>
{
    return { clientIdToRotate: clientId, secretExpiresAt: toSecretExpiry(secretExpiryDate) };
}

export function toResult(output: ClientsOutput): IClientsResult
{
    return { clients: output.clients ?? [], failureMessages: output.failureMessages ?? [] };
}

export function toReveal(output: ClientsOutput): ISecretReveal | null
{
    const clientId: string | null | undefined = output.revealedClientId;
    const secret:   string | null | undefined = output.revealedClientSecret;

    if (clientId === null || clientId === undefined || clientId === "" || secret === null || secret === undefined || secret === "")
        return null;

    const name: string | undefined = (output.clients ?? []).find(client => client.clientId === clientId)?.name;

    return { clientId, clientName: name ?? clientId, secret };
}

//////////////////////////////////////////////////////////////////////////////
