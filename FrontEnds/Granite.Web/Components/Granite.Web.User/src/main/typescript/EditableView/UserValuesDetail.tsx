//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { createContext, useContext, useEffect, useMemo, useRef } from "react";
import type { ReactElement }                                     from "react";
import { ActionIcon, Group, Text, Tooltip }                      from "@mantine/core";
import { AgGridReact }                                           from "ag-grid-react";
import type { CustomCellRendererProps }                          from "ag-grid-react";
import { themeQuartz }                                           from "ag-grid-community";
import type { CellValueChangedEvent, ColDef }                    from "ag-grid-community";
import type { IUserRow }                                         from "./UsersChangeSet";
import { createEntry, entryDisplay }                             from "./UsersValueEntries";
import type { IValueEntry }                                      from "./UsersValueEntries";

export interface IUserValuesContext
{
    entriesFor: (rowKey: string) => Array<IValueEntry>;
    setEntries: (rowKey: string, entries: Array<IValueEntry>) => void;
    isEditing:  (rowKey: string) => boolean;
}

export const UserValuesContext = createContext<IUserValuesContext>(
{
    entriesFor: () => [],
    setEntries: () => undefined,
    isEditing:  () => false,
});

const maxVisibleEntries: number = 8;
const detailGridTheme            = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#f1f1f1",
                                                            selectedRowBackgroundColor: "#3264C8" });

export function detailRowHeight(entryCount: number): number
{
    return Math.min(Math.max(entryCount, 1), maxVisibleEntries) * 36 + 102;
}

export function UserValuesDetail(props: CustomCellRendererProps<IUserRow>): ReactElement | null
{
    const values  = useContext(UserValuesContext);
    const gridRef = useRef<AgGridReact<IValueEntry>>(null);
    const rowKey: string = props.data?.rowKey ?? "";

    const entries: Array<IValueEntry> = values.entriesFor(rowKey);
    const editing: boolean            = values.isEditing(rowKey);
    const rowData: Array<IValueEntry> = useMemo(() => entries.map(e => ({ ...e })), [entries]);

    useEffect(() =>
    {
        props.node.setRowHeight(detailRowHeight(entries.length));
        props.api.onRowHeightChanged();
    }, [entries.length]);

    const columnDefs: Array<ColDef<IValueEntry>> = useMemo(() =>
    [
        { field: "key",   headerName: "Key",   flex: 1, editable: editing, headerTooltip: "Key"   },
        { colId: "value", headerName: "Value", flex: 2, editable: editing, headerTooltip: "Value",
          valueGetter: (p) => p.data != null ? entryDisplay(p.data) : "",
          valueSetter: (p) => { p.data.value = p.newValue; return true; } },
    ], [editing]);

    function handleAdd(): void
    {
        values.setEntries(rowKey, [...entries, createEntry()]);
    }

    function handleDelete(): void
    {
        const selected: Set<number> = new Set((gridRef.current?.api?.getSelectedRows() ?? []).map(e => e.id));

        if (selected.size > 0)
        {
            values.setEntries(rowKey, entries.filter(e => !selected.has(e.id)));
        }
    }

    function handleCellValueChanged(event: CellValueChangedEvent<IValueEntry>): void
    {
        const edited: IValueEntry = event.data;

        values.setEntries(rowKey, entries.map(e => e.id === edited.id ? { id: e.id, key: edited.key, value: edited.value } : e));
    }

    if (props.data == null)
    {
        return null;
    }

    return (
        <div style={{ display: "flex", flexDirection: "column", height: "100%",
                      padding: "8px 16px", boxSizing: "border-box" }}>
            <Group justify="space-between" align="center" gap="xs" style={{ height: 32, flexShrink: 0 }}>
                <Group gap="xs">
                    <Text size="xs" fw={600}>Values</Text>
                    <Tooltip label="Add value">
                        <ActionIcon variant="default" size="sm" onClick={handleAdd} disabled={!editing}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                                 fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <line x1="12" y1="5" x2="12" y2="19" />
                                <line x1="5" y1="12" x2="19" y2="12" />
                            </svg>
                        </ActionIcon>
                    </Tooltip>
                    <Tooltip label="Delete value">
                        <ActionIcon variant="default" size="sm" onClick={handleDelete} disabled={!editing}>
                            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
                                 fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                <polyline points="3 6 5 6 21 6" />
                                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                                <path d="M10 11v6" />
                                <path d="M14 11v6" />
                                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                            </svg>
                        </ActionIcon>
                    </Tooltip>
                </Group>
            </Group>
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                <AgGridReact<IValueEntry>
                    ref={gridRef}
                    theme={detailGridTheme}
                    suppressHorizontalScroll={true}
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={{ sortable: true, resizable: true }}
                    getRowId={(params) => String(params.data.id)}
                    rowSelection={{ mode: "multiRow", checkboxes: true, headerCheckbox: true, enableClickSelection: false }}
                    onCellValueChanged={handleCellValueChanged}
                />
            </div>
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
