//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import type { UserData, CreateUserData, UpdateUserData } from "granite.user.service/RequestReply";

export interface IUserRow extends UserData
{
    rowKey:      string;
    isNew:       boolean;
    isDetailRow: boolean;
}

export interface IUsersChanges
{
    usersToCreate: Array<CreateUserData>;
    usersToUpdate: Array<UpdateUserData>;
}

export function toRows(users: Array<UserData>): Array<IUserRow>
{
    return users.map(u => ({ ...u, rowKey: `user-${u.userId}`, isNew: false, isDetailRow: false }));
}

export function normalizeAuthorities(values: Array<string>): Array<string>
{
    const result: Array<string> = [];

    for (const value of values)
    {
        const trimmed = value.trim();

        if (trimmed !== "" && !result.includes(trimmed))
        {
            result.push(trimmed);
        }
    }

    return result;
}

export function displayValue(value: unknown): string
{
    return value === null || value === undefined ? "" : String(value);
}

function sameAuthorities(a: Array<string> | undefined, b: Array<string> | undefined): boolean
{
    const left  = normalizeAuthorities(a ?? []);
    const right = normalizeAuthorities(b ?? []);

    return left.length === right.length && left.every(v => right.includes(v));
}

function sameValues(a: Record<string, unknown> | undefined, b: Record<string, unknown> | undefined): boolean
{
    const left  = a ?? {};
    const right = b ?? {};
    const keys  = Object.keys(left);

    return keys.length === Object.keys(right).length
        && keys.every(k => Object.hasOwn(right, k) && displayValue(left[k]) === displayValue(right[k]));
}

function sameEditable(a: UserData, b: UserData): boolean
{
    return sameAuthorities(a.authorities, b.authorities) && sameValues(a.values, b.values);
}

export function findDuplicateKeys(entries: Array<{ key: string; value: unknown }>): Array<string>
{
    const seen:       Set<string> = new Set();
    const duplicates: Set<string> = new Set();

    for (const entry of entries)
    {
        const key = entry.key.trim();

        if (seen.has(key))
        {
            duplicates.add(key);
        }

        seen.add(key);
    }

    return [...duplicates];
}

function trimKeys(values: Record<string, unknown>): Record<string, unknown>
{
    return Object.fromEntries(Object.entries(values).map(([k, v]) => [k.trim(), v]));
}

export function validateRows(rows: Array<IUserRow>): Map<string, Array<string>>
{
    const result = new Map<string, Array<string>>();

    for (const row of rows)
    {
        const messages: Array<string> = [];

        if (row.isNew)
        {
            if ((row.userName ?? "").trim() === "")
            {
                messages.push("User name is required");
            }

            if ((row.password ?? "").trim() === "")
            {
                messages.push("Password is required");
            }
        }

        const keys = Object.keys(row.values ?? {}).map(k => k.trim());

        if (keys.some(k => k === ""))
        {
            messages.push("Value keys cannot be blank");
        }

        const nonBlank = keys.filter(k => k !== "");

        if (new Set(nonBlank).size !== nonBlank.length)
        {
            messages.push("Value keys must be unique");
        }

        if (messages.length > 0)
        {
            result.set(row.rowKey, messages);
        }
    }

    return result;
}

export function computeChanges(baseline: Array<IUserRow>, rows: Array<IUserRow>): IUsersChanges
{
    const usersToCreate: Array<CreateUserData> = [];
    const usersToUpdate: Array<UpdateUserData> = [];

    for (const row of rows)
    {
        if (row.isNew)
        {
            usersToCreate.push({ userName:    (row.userName ?? "").trim(),
                                 password:    row.password,
                                 authorities: normalizeAuthorities(row.authorities ?? []),
                                 values:      trimKeys(row.values ?? {}) });
            continue;
        }

        const original = baseline.find(b => b.rowKey === row.rowKey);

        if (original !== undefined && !sameEditable(original, row))
        {
            usersToUpdate.push({ userId:      row.userId,
                                 authorities: normalizeAuthorities(row.authorities ?? []),
                                 values:      trimKeys(row.values ?? {}) });
        }
    }

    return { usersToCreate, usersToUpdate };
}

export function isRefreshFailure(result: { users: Array<UserData>; failureMessages: Array<string> }): boolean
{
    return result.users.length === 0 && (result.failureMessages ?? []).length > 0;
}

export function mergePending(refreshed: Array<UserData>, rows: Array<IUserRow>, baseline: Array<IUserRow>): Array<IUserRow>
{
    const merged = toRows(refreshed).map(server =>
    {
        const edited   = rows.find(r => r.rowKey === server.rowKey);
        const original = baseline.find(b => b.rowKey === server.rowKey);

        if (edited === undefined || original === undefined
            || sameEditable(original, edited) || sameEditable(server, edited))
        {
            return server;
        }

        return { ...server, authorities: edited.authorities, values: edited.values };
    });

    const baselineNames = new Set(baseline.map(b => (b.userName ?? "").trim()));
    const createdNames  = new Set(refreshed.map(u => (u.userName ?? "").trim()).filter(n => !baselineNames.has(n)));

    for (const row of rows)
    {
        if (row.isNew && !createdNames.has((row.userName ?? "").trim()))
        {
            merged.push(row);
        }
    }

    return merged;
}

//////////////////////////////////////////////////////////////////////////////
