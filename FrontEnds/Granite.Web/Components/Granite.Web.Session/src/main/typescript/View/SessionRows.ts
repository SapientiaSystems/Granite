//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import type { SessionRowData } from "granite.frontendcontroller.service/RequestReply";

export interface ISessionRow
    extends SessionRowData
{
    rowKey:      string;
    isDetailRow: boolean;
}

export interface ISessionValueEntry
{
    id:    number;
    key:   string;
    value: string;
}

const maxVisibleEntries: number = 8;

export function toSessionRows(sessions: Array<SessionRowData> | undefined): Array<ISessionRow>
{
    return (sessions ?? []).map(session => ({ ...session, rowKey: String(session.sessionId), isDetailRow: false }));
}

export function withDetailRow(rows: Array<ISessionRow>, expandedKey: string | null): Array<ISessionRow>
{
    const result: Array<ISessionRow> = [];

    for (const row of rows)
    {
        result.push(row);

        if (row.rowKey === expandedKey)
        {
            result.push({ ...row, isDetailRow: true });
        }
    }

    return result;
}

export function isOpen(row: SessionRowData): boolean
{
    return row.closedReason == null || row.closedReason === "";
}

export function isCurrent(row: SessionRowData, currentSessionId: number | null | undefined): boolean
{
    return currentSessionId != null && row.sessionId === currentSessionId;
}

export function endableSessionIds(selected: Array<SessionRowData>, currentSessionId: number | null | undefined): Array<number>
{
    return selected.filter(row => isOpen(row) && !isCurrent(row,currentSessionId)).map(row => row.sessionId);
}

export function canEndSelection(selected: Array<SessionRowData>, currentSessionId: number | null | undefined): boolean
{
    return !selected.some(row => isCurrent(row,currentSessionId)) && endableSessionIds(selected,currentSessionId).length > 0;
}

export function userLabel(row: SessionRowData): string
{
    if (row.userName != null && row.userName !== "")
    {
        return row.userName;
    }

    return row.userId == null ? "" : "#" + row.userId;
}

export function authoritiesLabel(row: SessionRowData): string
{
    return [...(row.authorities ?? [])].sort().join(", ");
}

export function displayValue(value: unknown): string
{
    if (value === null || value === undefined)
    {
        return "";
    }

    return typeof value === "object" ? JSON.stringify(value) : String(value);
}

export function valueEntries(values: Record<string, unknown> | undefined): Array<ISessionValueEntry>
{
    return Object.entries(values ?? {}).map(([key, value], index) => ({ id: index, key, value: displayValue(value) }));
}

export function formatInstant(value: string | null | undefined): string
{
    if (value == null || value === "")
    {
        return "";
    }

    const time: Date = new Date(value);

    return Number.isNaN(time.getTime()) ? value : time.toLocaleString();
}

export function detailRowHeight(entryCount: number): number
{
    return Math.min(Math.max(entryCount, 1), maxVisibleEntries) * 36 + 70;
}

//////////////////////////////////////////////////////////////////////////////
