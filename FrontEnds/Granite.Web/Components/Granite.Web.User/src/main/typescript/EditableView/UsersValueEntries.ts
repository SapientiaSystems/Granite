//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { displayValue, findDuplicateKeys } from "./UsersChangeSet";

export interface IValueEntry
{
    id:    number;
    key:   string;
    value: unknown;
}

let nextEntryId: number = 1;

export function createEntry(key: string = "", value: unknown = ""): IValueEntry
{
    return { id: nextEntryId++, key, value };
}

export function toEntries(values: Record<string, unknown> | undefined): Array<IValueEntry>
{
    return Object.entries(values ?? {}).map(([key, value]) => createEntry(key, value));
}

export function toValues(entries: Array<IValueEntry>): Record<string, unknown>
{
    return Object.fromEntries(entries.map(e => [e.key, e.value]));
}

export function entryMessages(entries: Array<IValueEntry>): Array<string>
{
    const messages: Array<string> = [];

    if (entries.some(e => e.key.trim() === ""))
    {
        messages.push("Value keys cannot be blank");
    }

    const duplicates: Array<string> = findDuplicateKeys(entries.filter(e => e.key.trim() !== ""));

    if (duplicates.length > 0)
    {
        messages.push(`Value keys must be unique (${duplicates.join(", ")})`);
    }

    return messages;
}

export function validateEntries(entriesByKey: Record<string, Array<IValueEntry>>): Map<string, Array<string>>
{
    const result = new Map<string, Array<string>>();

    for (const [rowKey, entries] of Object.entries(entriesByKey))
    {
        const messages: Array<string> = entryMessages(entries);

        if (messages.length > 0)
        {
            result.set(rowKey, messages);
        }
    }

    return result;
}

export function entryDisplay(entry: IValueEntry): string
{
    return displayValue(entry.value);
}

//////////////////////////////////////////////////////////////////////////////
