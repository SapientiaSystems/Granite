//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useRef, useState }   from "react";
import type { ReactElement }                      from "react";
import { ActionIcon, Alert, Button, Divider, Group, Loader,
         Modal, Stack, Switch, Text, Tooltip }    from "@mantine/core";
import { useDisclosure }                          from "@mantine/hooks";
import { AgGridReact }                            from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry,
         themeQuartz }                            from "ag-grid-community";
import type { ColDef, PostSortRowsParams,
              RowClassParams }                    from "ag-grid-community";
import type { ISessionsGridProps }                from "./ISessionsGridProps";
import { authoritiesLabel, canEndSelection, detailRowHeight, endableSessionIds, formatInstant,
         applicationLabel, isCurrent, toSessionRows, userLabel, withDetailRow } from "./SessionRows";
import type { ISessionRow }                       from "./SessionRows";
import { ExpandCellRenderer }                     from "./ExpandCellRenderer";
import { AuthoritiesCellRenderer }                from "./AuthoritiesCellRenderer";
import { SessionValuesDetail }                    from "./SessionValuesDetail";

ModuleRegistry.registerModules([AllCommunityModule]);

const masterGridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8",
                                                  selectedRowBackgroundColor: "#3264C8" });
const defaultColDef: ColDef<ISessionRow> = { sortable: true, resizable: true, filter: true };

const gridStyles: string = ".session-closed-row { color: var(--mantine-color-dimmed); }"
                         + " .session-current-row { font-weight: 600; }"
                         + " .session-expanded-parent { border-bottom: none; }";

const svgProps = { xmlns: "http://www.w3.org/2000/svg", width: 20, height: 20, viewBox: "0 0 24 24",
                   fill: "none", stroke: "currentColor", strokeWidth: 2,
                   strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

const refreshIcon: ReactElement = (<><polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" />
                                     <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" /></>);
const endIcon:     ReactElement = (<><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" />
                                     <line x1="21" y1="12" x2="9" y2="12" /></>);

function errorText(error: unknown): string
{
    return error instanceof Error ? error.message : String(error);
}

function toolbarButton(label: string, text: string, icon: ReactElement, onClick: () => void,
                       disabled: boolean): ReactElement
{
    return (
        <Tooltip label={label}>
            <Stack align="center" gap={2}>
                <ActionIcon variant="default" size="lg" onClick={onClick} disabled={disabled}>
                    <svg {...svgProps}>{icon}</svg>
                </ActionIcon>
                <Text size="xs" c={disabled ? "dimmed" : undefined}>{text}</Text>
            </Stack>
        </Tooltip>
    );
}

function handlePostSortRows(params: PostSortRowsParams<ISessionRow>): void
{
    const nodes       = params.nodes;
    const detailIndex = nodes.findIndex(n => n.data?.isDetailRow === true);

    if (detailIndex < 0)
    {
        return;
    }

    const detailNode  = nodes[detailIndex];
    const parentKey   = detailNode.data?.rowKey;
    const parentIndex = nodes.findIndex(n => n.data?.isDetailRow === false && n.data?.rowKey === parentKey);

    if (parentIndex >= 0 && detailIndex !== parentIndex + 1)
    {
        nodes.splice(detailIndex, 1);
        nodes.splice(parentIndex + 1, 0, detailNode);
    }
}

export function SessionsGrid(props: ISessionsGridProps): ReactElement
{
    const [expandedKey, setExpandedKey]   = useState<string | null>(null);
    const [selectedKeys, setSelectedKeys] = useState<Array<string>>([]);
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const [endKeys, setEndKeys]           = useState<Array<string>>([]);
    const [endModalOpened, { open: openEndModal, close: closeEndModal }] = useDisclosure(false);
    const gridRef                         = useRef<AgGridReact<ISessionRow>>(null);

    const rows:        Array<ISessionRow> = useMemo(() => toSessionRows(props.sessions), [props.sessions]);
    const displayData: Array<ISessionRow> = useMemo(() => withDetailRow(rows,expandedKey), [rows,expandedKey]);
    const selected:    Array<ISessionRow> = rows.filter(r => selectedKeys.includes(r.rowKey));
    const loading:     boolean            = (props.loading ?? false) || busy;
    const showEnd:     boolean            = props.showEnd ?? true;
    const endDisabled: boolean            = busy || !canEndSelection(selected,props.currentSessionId);
    const includeClosed: boolean          = props.includeClosed ?? false;

    const actions = useRef({ refresh: () => {}, end: () => {}, toggleClosed: (_: boolean) => {} });

    actions.current =
    {
        refresh:      () => props.onRefresh?.(),
        toggleClosed: (value: boolean) => props.onIncludeClosedChange?.(value),
        end:          () =>
        {
            setEndKeys(endableSessionIds(selected,props.currentSessionId).map(String));
            openEndModal();
        },
    };

    useEffect(() =>
    {
        const keys: Set<string> = new Set(rows.map(r => r.rowKey));

        setSelectedKeys(previous => previous.filter(k => keys.has(k)));

        if (expandedKey !== null && !keys.has(expandedKey))
        {
            setExpandedKey(null);
        }
    }, [rows]);

    async function confirmEnd(): Promise<void>
    {
        const ids: Array<number> = endKeys.map(Number);

        closeEndModal();

        if (props.onEnd == null || ids.length === 0)
        {
            return;
        }

        setBusy(true);

        try
        {
            const result = await props.onEnd(ids);

            setMessages(result.failureMessages ?? []);
            gridRef.current?.api?.deselectAll();
        }
        catch (error)
        {
            setMessages([errorText(error)]);
        }
        finally
        {
            setBusy(false);
        }
    }

    function toggleExpand(rowKey: string): void
    {
        setExpandedKey(previous => previous === rowKey ? null : rowKey);
    }

    const columnDefs: Array<ColDef<ISessionRow>> =
    [
        { colId: "expand", headerName: "", width: 50, maxWidth: 50, sortable: false, filter: false, resizable: false,
          cellRenderer: ExpandCellRenderer },
        { field: "sessionId",    headerName: "Session",       width: 110, filter: "agNumberColumnFilter", headerTooltip: "Session id" },
        { colId: "user",         headerName: "User",          flex: 2, headerTooltip: "User name (user id)",
          valueGetter: (p) => p.data == null ? "" : userLabel(p.data) },
        { colId: "authorities",  headerName: "Authorities",   flex: 2, headerTooltip: "Authorities",
          valueGetter: (p) => p.data == null ? "" : authoritiesLabel(p.data,props.roles),
          cellRenderer: AuthoritiesCellRenderer },
        { field: "userId",       headerName: "User Id",       width: 100, hide: true, filter: "agNumberColumnFilter" },
        { field: "application",  headerName: "Application",   width: 130, headerTooltip: "Web application the session is bound to",
          valueGetter: (p) => p.data == null ? "" : applicationLabel(p.data) },
        { field: "deviceId",     headerName: "Device",        flex: 2, headerTooltip: "Device id" },
        { field: "loginMethod",  headerName: "Login Method",  width: 130 },
        { field: "created",      headerName: "Created",       flex: 2, valueFormatter: (p) => formatInstant(p.value) },
        { field: "lastActivity", headerName: "Last Activity", flex: 2, sort: "desc", valueFormatter: (p) => formatInstant(p.value) },
        { field: "expires",      headerName: "Expires",       flex: 2, valueFormatter: (p) => formatInstant(p.value) },
        { field: "closedReason", headerName: "Closed Reason", width: 140, hide: !includeClosed },
        { field: "continuityId", headerName: "Continuity",    flex: 2, hide: true },
        { field: "replacedBy",   headerName: "Replaced By",   width: 120, hide: !includeClosed, filter: "agNumberColumnFilter" },
        { field: "copiedFrom",   headerName: "Copied From",   width: 120, filter: "agNumberColumnFilter" },
    ];

    const gridContext = { expandedKey, toggleExpand, roles: props.roles };

    const isGrowMode: boolean = props.height === "grow" || props.height == null;
    const isDetached: boolean = isGrowMode && props.onRenderToolbar != null;
    const gridHeight: string | number =
        props.height === undefined || props.height === "grow"
            ? "100%"
            : (props.height as number) * 36 + 48;

    const toolbarContent: ReactElement = (
        <Group gap="sm" wrap="nowrap" style={{ flex: 1 }}>
            {toolbarButton("Refresh Sessions", "Refresh", refreshIcon, () => actions.current.refresh(), busy)}
            <Switch label="Include closed" checked={includeClosed} disabled={busy}
                    onChange={(event) => actions.current.toggleClosed(event.currentTarget.checked)} />
            {showEnd && <Divider orientation="vertical" mx="md" />}
            {showEnd && toolbarButton("End Selected Sessions", "End", endIcon, () => actions.current.end(), endDisabled)}
            <div style={{ flex: 1 }} />
        </Group>
    );

    useEffect(() =>
    {
        if (props.onRenderToolbar != null)
        {
            props.onRenderToolbar(toolbarContent);
        }
    }, [endDisabled, busy, showEnd, includeClosed]);

    const gridPanel: ReactElement = (
        <>
            <style>{gridStyles}</style>
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="sm" my={6}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%",
                                                     height: isDetached ? undefined : gridHeight }}>
                <AgGridReact<ISessionRow>
                    ref={gridRef}
                    theme={masterGridTheme}
                    loading={loading}
                    overlayLoadingTemplate="Retrieving Sessions..."
                    overlayNoRowsTemplate="No sessions"
                    rowData={displayData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    context={gridContext}
                    alwaysPassFilter={(node) => node.isSelected() === true}
                    rowClassRules={{
                        "session-expanded-parent": (params: RowClassParams<ISessionRow>) =>
                            params.data?.isDetailRow === false && params.data.rowKey === expandedKey,
                        "session-closed-row": (params: RowClassParams<ISessionRow>) =>
                            params.data?.isDetailRow === false && params.data.closedReason != null,
                        "session-current-row": (params: RowClassParams<ISessionRow>) =>
                            params.data?.isDetailRow === false && isCurrent(params.data,props.currentSessionId),
                    }}
                    rowSelection={{ mode: "multiRow", checkboxes: true, headerCheckbox: true,
                                    selectAll: "filtered", enableClickSelection: false,
                                    isRowSelectable: (node) => node.data?.isDetailRow !== true }}
                    onSelectionChanged={(event) =>
                        setSelectedKeys(event.api.getSelectedRows().filter(r => !r.isDetailRow).map(r => r.rowKey))}
                    getRowId={(params) => params.data.isDetailRow ? "detail-" + params.data.rowKey : params.data.rowKey}
                    getRowHeight={(params) =>
                        params.data?.isDetailRow === true
                            ? detailRowHeight(Object.keys(params.data.values ?? {}).length)
                            : undefined}
                    isFullWidthRow={(params) => params.rowNode.data?.isDetailRow === true}
                    fullWidthCellRenderer={SessionValuesDetail}
                    postSortRows={handlePostSortRows}
                />
            </div>
            <Modal opened={endModalOpened} onClose={closeEndModal} title="Confirm End Sessions" centered>
                <Text size="sm">End the following sessions? Their users will have to log in again.</Text>
                <ul>
                    {rows.filter(r => endKeys.includes(r.rowKey))
                         .map(r => <li key={r.rowKey}>{r.sessionId}: {userLabel(r)} ({r.deviceId ?? ""})</li>)}
                </ul>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={closeEndModal}>Cancel</Button>
                    <Button color="red" onClick={() => { void confirmEnd(); }}>End Sessions</Button>
                </Group>
            </Modal>
        </>
    );

    const loader: ReactElement | false = loading && (
        <div style={{ position: "absolute", inset: 0, display: "flex",
                      alignItems: "center", justifyContent: "center", zIndex: 10 }}>
            <Loader size="lg" />
        </div>
    );

    if (isDetached)
    {
        return (
            <div style={{ flex: 1, minHeight: 0, position: "relative", display: "flex", flexDirection: "column" }}>
                {loader}
                {gridPanel}
            </div>
        );
    }

    return (
        <Stack gap={0} style={{ position: "relative" as const, height: isGrowMode ? "100%" : undefined }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbarContent}
            </Group>
            {loader}
            {gridPanel}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
