//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { displayValue, findDuplicateKeys } from "./UsersChangeSet";

export interface IValueEntry
{
    id:      number;
    key:     string;
    value:   unknown;
    locked?: boolean;
}

let nextEntryId: number = 1;

const RESERVED_VALUE_PREFIXES: Array<string> = ["preferences.", "granite."];

export function isReservedValueKey(key: string): boolean
{
    return RESERVED_VALUE_PREFIXES.some(prefix => key.startsWith(prefix));
}

export function createEntry(key: string = "", value: unknown = ""): IValueEntry
{
    return { id: nextEntryId++, key, value };
}

export function toEntries(values: Record<string, unknown> | undefined): Array<IValueEntry>
{
    return Object.entries(values ?? {}).map(([key, value]) =>
    {
        const entry: IValueEntry = createEntry(key, value);

        return isReservedValueKey(key) ? { ...entry, locked: true } : entry;
    });
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

    const reserved: Array<string> = entries.filter(e => !e.locked && isReservedValueKey(e.key.trim())).map(e => e.key.trim());

    if (reserved.length > 0)
    {
        messages.push(`Value keys cannot start with preferences. or granite. (${reserved.join(", ")})`);
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

export function removeOrphanEntries(entriesByKey: Record<string, Array<IValueEntry>>,
                                    rowKeys: Array<string>): Record<string, Array<IValueEntry>>
{
    return Object.fromEntries(Object.entries(entriesByKey).filter(([key]) => rowKeys.includes(key)));
}

export type UsersAction = "create" | "edit" | "save" | "undo" | "delete";

export interface IActionState
{
    busy:           boolean;
    isEditing:      boolean;
    hasSelection:   boolean;
    deleteDisabled: boolean;
}

export function isActionAllowed(action: UsersAction, state: IActionState): boolean
{
    if (state.busy)
    {
        return false;
    }

    switch (action)
    {
        case "create": return true;
        case "edit":   return state.hasSelection;
        case "save":
        case "undo":   return state.isEditing;
        case "delete": return state.hasSelection && !state.deleteDisabled;
    }
}

export function nextMacrotask(): Promise<void>
{
    return new Promise<void>(resolve => setTimeout(resolve, 0));
}

export function entryDisplay(entry: IValueEntry): string
{
    return displayValue(entry.value);
}

//////////////////////////////////////////////////////////////////////////////
