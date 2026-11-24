//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import type { ClientData, CreateClientData, UpdateClientData } from "granite.client.service/RequestReply";

export const MAX_NAME_LENGTH: number = 100;

const DAY_MILLISECONDS: number = 24 * 60 * 60 * 1000;
const DATE_PATTERN:     RegExp = /^\d{4}-\d{2}-\d{2}$/;

export interface IClientRow
{
    clientId:           string;
    name:               string;
    description:        string;
    authorities:        Array<string>;
    owners:             Array<number>;
    enabled:            boolean;
    secretExpiryDate:   string | null;
    lastTokenIssuedAt?: string;
    createdAt?:         string;
    version?:           number;
    rowKey:             string;
    isNew:              boolean;
    isDetailRow:        boolean;
}

export interface IClientsChanges
{
    clientsToCreate: Array<CreateClientData>;
    clientsToUpdate: Array<UpdateClientData>;
}

export function isValidDate(date: string | null | undefined): boolean
{
    if (date === null || date === undefined || !DATE_PATTERN.test(date))
        return false;

    const parsed: Date = new Date(date + "T00:00:00Z");

    return !Number.isNaN(parsed.getTime()) && parsed.toISOString().slice(0,10) === date;
}

export function toSecretExpiry(date: string | null): string | null
{
    if (date === null || !isValidDate(date))
        return null;

    const next: Date = new Date(new Date(date + "T00:00:00Z").getTime() + DAY_MILLISECONDS);

    return next.toISOString().slice(0,19) + "Z";
}

export function fromSecretExpiry(instant: string | null): string | null
{
    if (instant === null || instant === undefined || instant === "")
        return null;

    const parsed: Date = new Date(instant);

    return Number.isNaN(parsed.getTime()) ? null : new Date(parsed.getTime() - DAY_MILLISECONDS).toISOString().slice(0,10);
}

export function normalizeAuthorities(values: Array<string>): Array<string>
{
    const result: Array<string> = [];

    for (const value of values)
    {
        const trimmed: string = value.trim();

        if (trimmed !== "" && !result.includes(trimmed))
            result.push(trimmed);
    }

    return result;
}

export function toRows(clients: Array<ClientData>): Array<IClientRow>
{
    return clients.map(client => (
        {
            clientId:          client.clientId ?? "",
            name:              client.name ?? "",
            description:       client.description ?? "",
            authorities:       client.authorities ?? [],
            owners:            client.owners ?? [],
            enabled:           client.enabled ?? true,
            secretExpiryDate:  fromSecretExpiry(client.secretExpiresAt ?? null),
            lastTokenIssuedAt: client.lastTokenIssuedAt,
            createdAt:         client.createdAt,
            version:           client.version,
            rowKey:            client.clientId ?? "",
            isNew:             false,
            isDetailRow:       false
        }));
}

function sameSet<T>(a: Array<T>,b: Array<T>): boolean
{
    return a.length === b.length && a.every(value => b.includes(value));
}

function sameEditable(a: IClientRow,b: IClientRow): boolean
{
    return a.name.trim() === b.name.trim()
        && a.description === b.description
        && a.enabled === b.enabled
        && (a.secretExpiryDate ?? null) === (b.secretExpiryDate ?? null)
        && sameSet(normalizeAuthorities(a.authorities),normalizeAuthorities(b.authorities))
        && sameSet(a.owners,b.owners);
}

export function computeChanges(baseline: Array<IClientRow>,rows: Array<IClientRow>): IClientsChanges
{
    const clientsToCreate: Array<CreateClientData> = [];
    const clientsToUpdate: Array<UpdateClientData> = [];

    for (const row of rows)
    {
        const secretExpiresAt: string | undefined = toSecretExpiry(row.secretExpiryDate) ?? undefined;

        if (row.isNew)
        {
            clientsToCreate.push({ name:        row.name.trim(),
                                   description: row.description,
                                   authorities: normalizeAuthorities(row.authorities),
                                   owners:      row.owners,
                                   enabled:     row.enabled,
                                   secretExpiresAt });
            continue;
        }

        const original: IClientRow | undefined = baseline.find(b => b.rowKey === row.rowKey);

        if (original !== undefined && !sameEditable(original,row))
            clientsToUpdate.push({ clientId:    row.clientId,
                                   name:        row.name.trim(),
                                   description: row.description,
                                   authorities: normalizeAuthorities(row.authorities),
                                   owners:      row.owners,
                                   enabled:     row.enabled,
                                   secretExpiresAt,
                                   version:     row.version });
    }

    return { clientsToCreate, clientsToUpdate };
}

export function gridCopies(rows: Array<IClientRow>): Array<IClientRow>
{
    return rows.map(row => ({ ...row, authorities: [...row.authorities], owners: [...row.owners] }));
}

export function pendingEditKeys(refreshed: Array<IClientRow>,merged: Array<IClientRow>): Set<string>
{
    const pending: IClientsChanges = computeChanges(refreshed,merged);

    return new Set([ ...merged.filter(r => r.isNew).map(r => r.rowKey),
                     ...pending.clientsToUpdate.map(c => c.clientId ?? "") ]);
}

export function validateRows(rows: Array<IClientRow>): Map<string,Array<string>>
{
    const result: Map<string,Array<string>> = new Map<string,Array<string>>();
    const counts: Map<string,number>        = new Map<string,number>();

    for (const row of rows)
    {
        const key: string = row.name.trim().toLowerCase();

        counts.set(key,(counts.get(key) ?? 0) + 1);
    }

    for (const row of rows)
    {
        const messages: Array<string> = [];
        const name:     string        = row.name.trim();

        if (name === "")
            messages.push("Name is required");
        else if (name.length > MAX_NAME_LENGTH)
            messages.push("Name must be at most " + MAX_NAME_LENGTH + " characters");
        else if ((counts.get(name.toLowerCase()) ?? 0) > 1)
            messages.push("Name must be unique");

        if (row.secretExpiryDate !== null && row.secretExpiryDate !== "" && !isValidDate(row.secretExpiryDate))
            messages.push("Secret expires must be a valid date");

        if (messages.length > 0)
            result.set(row.rowKey,messages);
    }

    return result;
}

export function isRefreshFailure(result: { clients: Array<ClientData>; failureMessages: Array<string> }): boolean
{
    return result.clients.length === 0 && (result.failureMessages ?? []).length > 0;
}

export function mergePending(refreshed: Array<ClientData>,rows: Array<IClientRow>,baseline: Array<IClientRow>): Array<IClientRow>
{
    const merged: Array<IClientRow> = toRows(refreshed).map(server =>
    {
        const edited:   IClientRow | undefined = rows.find(r => r.rowKey === server.rowKey);
        const original: IClientRow | undefined = baseline.find(b => b.rowKey === server.rowKey);

        if (edited === undefined || original === undefined || sameEditable(original,edited) || sameEditable(server,edited))
            return server;

        return { ...edited, version: server.version, lastTokenIssuedAt: server.lastTokenIssuedAt, createdAt: server.createdAt };
    });

    const baselineNames: Set<string> = new Set(baseline.map(b => b.name.trim()));
    const createdNames:  Set<string> = new Set(merged.map(m => m.name.trim()).filter(n => !baselineNames.has(n)));

    for (const row of rows)
    {
        if (row.isNew && !createdNames.has(row.name.trim()))
            merged.push(row);
    }

    return merged;
}

//////////////////////////////////////////////////////////////////////////////
