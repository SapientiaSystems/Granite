//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import { useMemo }                     from "react";
import type { ReactElement }           from "react";
import { Text }                        from "@mantine/core";
import { AgGridReact }                 from "ag-grid-react";
import type { CustomCellRendererProps } from "ag-grid-react";
import { themeQuartz }                 from "ag-grid-community";
import type { ColDef }                 from "ag-grid-community";
import { valueEntries }                from "./SessionRows";
import type { ISessionRow, ISessionValueEntry } from "./SessionRows";

const detailGridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#f1f1f1" });

const columnDefs: Array<ColDef<ISessionValueEntry>> =
[
    { field: "key",   headerName: "Key",   flex: 1, headerTooltip: "Key" },
    { field: "value", headerName: "Value", flex: 2, headerTooltip: "Value" },
];

export function SessionValuesDetail(props: CustomCellRendererProps<ISessionRow>): ReactElement | null
{
    const rowData: Array<ISessionValueEntry> = useMemo(() => valueEntries(props.data?.values), [props.data?.values]);

    if (props.data == null)
    {
        return null;
    }

    return (
        <div style={{ display: "flex", flexDirection: "column", height: "100%",
                      padding: "8px 16px", boxSizing: "border-box" }}>
            <Text size="xs" fw={600} style={{ height: 24, flexShrink: 0 }}>
                {rowData.length === 0 ? "Values (none)" : "Values"}
            </Text>
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                <AgGridReact<ISessionValueEntry>
                    theme={detailGridTheme}
                    suppressHorizontalScroll={true}
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={{ sortable: true, resizable: true }}
                    getRowId={(params) => String(params.data.id)}
                />
            </div>
        </div>
    );
}

//////////////////////////////////////////////////////////////////////////////
