//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useRef, useState }  from "react";
import type { ReactElement }                     from "react";
import { Stack, Group, ActionIcon, Loader, Tooltip,
         Text, Modal, Button, Alert }            from "@mantine/core";
import { AgGridReact }                           from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry,
         themeQuartz }                           from "ag-grid-community";
import type { ColDef, CellClassParams, CellValueChangedEvent,
              RowClassParams }                   from "ag-grid-community";
import { AuthoritiesCellEditor, AuthoritiesCellRenderer,
         removeRole }                            from "granite.web.user/EditableView";
import type { IClientsEditableViewProps, IClientsResult } from "./IClientsEditableViewProps";
import { clientRoles }                           from "./ClientsAuthorityOptions";
import { computeChanges, isRefreshFailure, mergePending, normalizeAuthorities,
         toRows, validateRows }                  from "./ClientsChangeSet";
import type { IClientRow }                       from "./ClientsChangeSet";
import { OwnersCellEditor }                      from "./OwnersCellEditor";
import { OwnersCellRenderer }                    from "./OwnersCellRenderer";
import { SecretExpiryCellEditor }                from "./SecretExpiryCellEditor";

ModuleRegistry.registerModules([AllCommunityModule]);

const masterGridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8",
                                                  selectedRowBackgroundColor: "#3264C8" });
const defaultColDef: ColDef<IClientRow> = { sortable: true, resizable: true, filter: true };

const gridStyles: string = ".client-editable-cell { background: #fff8e1; }"
                         + " .client-invalid-row { background: #fdecea !important; }";

const svgProps = { xmlns: "http://www.w3.org/2000/svg", width: 20, height: 20, viewBox: "0 0 24 24",
                   fill: "none", stroke: "currentColor", strokeWidth: 2,
                   strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

const createIcon: ReactElement = (<><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></>);
const saveIcon:   ReactElement = (<><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                                     <polyline points="17 21 17 13 7 13 7 21" /><polyline points="7 3 7 8 15 8" /></>);
const deleteIcon: ReactElement = (<><polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                                     <path d="M10 11v6" /><path d="M14 11v6" /><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" /></>);
const rotateIcon: ReactElement = (<><polyline points="23 4 23 10 17 10" /><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" /></>);
const copyIcon:   ReactElement = (<><rect x="9" y="9" width="13" height="13" rx="2" ry="2" /><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" /></>);

function errorText(error: unknown): string
{
    return error instanceof Error ? error.message : String(error);
}

function rowLabel(row: IClientRow): string
{
    return row.name.trim() === "" ? "(new client)" : row.name;
}

function createNewRow(rowKey: string): IClientRow
{
    return { clientId: "", name: "", description: "", authorities: [], owners: [], enabled: true,
             secretExpiryDate: null, rowKey, isNew: true, isDetailRow: false };
}

function formatInstant(value: string | undefined): string
{
    if (value === undefined || value === "")
        return "";

    const parsed: Date = new Date(value);

    return Number.isNaN(parsed.getTime()) ? value : parsed.toLocaleString();
}

function toolbarButton(label: string,text: string,icon: ReactElement,onClick: () => void,disabled: boolean): ReactElement
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

function ClientIdCellRenderer(props: { data?: IClientRow }): ReactElement | null
{
    const clientId: string = props.data?.clientId ?? "";

    if (clientId === "")
        return null;

    function copy(): void
    {
        try
        {
            void navigator.clipboard?.writeText(clientId);
        }
        catch
        {
            return;
        }
    }

    return (
        <Group gap={4} wrap="nowrap">
            <span style={{ fontFamily: "monospace" }}>{clientId}</span>
            <Tooltip label="Copy Client ID">
                <ActionIcon variant="subtle" size="sm" aria-label="Copy Client ID" onClick={copy}>
                    <svg {...svgProps} width={14} height={14}>{copyIcon}</svg>
                </ActionIcon>
            </Tooltip>
        </Group>
    );
}

export function ClientsEditableView(props: IClientsEditableViewProps): ReactElement
{
    const editable:   boolean   = props.editable ?? false;
    const showOwners: boolean   = props.showOwners ?? false;
    const canRotate:  boolean   = props.onRotate !== undefined;
    const [rows, setRows]                 = useState<Array<IClientRow>>(() => toRows(props.clients ?? []));
    const [baseline, setBaseline]         = useState<Array<IClientRow>>(() => toRows(props.clients ?? []));
    const [selectedKeys, setSelectedKeys] = useState<Array<string>>([]);
    const [invalidKeys, setInvalidKeys]   = useState<Set<string>>(new Set());
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const [deleteKeys, setDeleteKeys]     = useState<Array<string>>([]);
    const [rotateKey, setRotateKey]       = useState<string | null>(null);
    const gridRef                         = useRef<AgGridReact<IClientRow>>(null);
    const rowsRef                         = useRef<Array<IClientRow>>(rows);
    const baselineRef                     = useRef<Array<IClientRow>>(baseline);
    const busyRef                         = useRef<boolean>(false);
    const newRowCounter                   = useRef<number>(0);
    const actions                         = useRef<{ create: () => void; save: () => void; remove: () => void; rotate: () => void }>(
                                                { create: () => undefined, save: () => undefined, remove: () => undefined, rotate: () => undefined });

    rowsRef.current     = rows;
    baselineRef.current = baseline;

    const loading:       boolean = (props.loading ?? false) || busy;
    const hasSelection:  boolean = selectedKeys.length > 0;
    const hasNewRow:     boolean = rows.some(r => r.isNew);
    const rotateTarget:  IClientRow | undefined = selectedKeys.length === 1 ? rows.find(r => r.rowKey === selectedKeys[0] && !r.isNew) : undefined;
    const roles                  = useMemo(() => clientRoles(props.roles),[props.roles]);

    useEffect(() =>
    {
        if (props.clients != null)
        {
            const fresh: Array<IClientRow> = toRows(props.clients);

            commitRows(fresh);
            baselineRef.current = fresh;
            setBaseline(fresh);
            setInvalidKeys(new Set());
        }
    },[props.clients]);

    useEffect(() =>
    {
        gridRef.current?.api?.redrawRows();
    },[invalidKeys,busy]);

    function commitRows(next: Array<IClientRow>): void
    {
        rowsRef.current = next;
        setRows(next);
    }

    function commitBusy(next: boolean): void
    {
        busyRef.current = next;
        setBusy(next);
    }

    function updateRow(rowKey: string,patch: Partial<IClientRow>): void
    {
        if (!busyRef.current)
            commitRows(rowsRef.current.map(r => r.rowKey === rowKey ? { ...r, ...patch } : r));
    }

    function applyResult(result: IClientsResult,current: Array<IClientRow>): void
    {
        if (isRefreshFailure(result))
        {
            setMessages(result.failureMessages);
            return;
        }

        const refreshed: Array<IClientRow> = toRows(result.clients);

        commitRows(mergePending(result.clients,current,baselineRef.current));
        baselineRef.current = refreshed;
        setBaseline(refreshed);
        setSelectedKeys([]);
        setInvalidKeys(new Set());
        setMessages(result.failureMessages ?? []);
    }

    function handleCreate(): void
    {
        if (!editable || busyRef.current || rowsRef.current.some(r => r.isNew))
            return;

        const rowKey: string = `new-${++newRowCounter.current}`;

        commitRows([...rowsRef.current,createNewRow(rowKey)]);
    }

    async function handleSave(): Promise<void>
    {
        if (!editable || busyRef.current)
            return;

        gridRef.current?.api?.stopEditing();

        const current:  Array<IClientRow>           = rowsRef.current;
        const problems: Map<string,Array<string>>   = validateRows(current);

        if (problems.size > 0)
        {
            const lines: Array<string> = [];

            for (const [rowKey,rowProblems] of problems)
            {
                const row: IClientRow | undefined = current.find(r => r.rowKey === rowKey);

                lines.push(...rowProblems.map(p => `${row === undefined ? rowKey : rowLabel(row)}: ${p}`));
            }

            setInvalidKeys(new Set(problems.keys()));
            setMessages(lines);
            return;
        }

        const changes = computeChanges(baselineRef.current,current);

        if (changes.clientsToCreate.length === 0 && changes.clientsToUpdate.length === 0)
        {
            setInvalidKeys(new Set());
            setMessages([]);
            return;
        }

        if (changes.clientsToCreate.length > 1)
        {
            setMessages(["Only one new client can be saved at a time"]);
            return;
        }

        if (props.onSave === undefined)
            return;

        commitBusy(true);
        setInvalidKeys(new Set());

        try
        {
            applyResult(await props.onSave(changes),rowsRef.current);
        }
        catch (error)
        {
            setMessages([errorText(error)]);
        }
        finally
        {
            commitBusy(false);
        }
    }

    function handleDelete(): void
    {
        if (editable && hasSelection && !busyRef.current)
            setDeleteKeys(selectedKeys);
    }

    async function confirmDelete(): Promise<void>
    {
        const keys:      Set<string>       = new Set(deleteKeys);
        const doomed:    Array<IClientRow> = rowsRef.current.filter(r => keys.has(r.rowKey));
        const remaining: Array<IClientRow> = rowsRef.current.filter(r => !keys.has(r.rowKey));
        const ids:       Array<string>     = doomed.filter(r => !r.isNew).map(r => r.clientId);

        setDeleteKeys([]);

        if (busyRef.current)
            return;

        if (ids.length === 0)
        {
            commitRows(remaining);
            setSelectedKeys([]);
            return;
        }

        if (props.onDelete === undefined)
            return;

        commitBusy(true);

        try
        {
            applyResult(await props.onDelete(ids),remaining);
        }
        catch (error)
        {
            setMessages([errorText(error)]);
        }
        finally
        {
            commitBusy(false);
        }
    }

    function handleRotate(): void
    {
        if (canRotate && rotateTarget !== undefined && !busyRef.current)
            setRotateKey(rotateTarget.rowKey);
    }

    async function confirmRotate(): Promise<void>
    {
        const target: IClientRow | undefined = rowsRef.current.find(r => r.rowKey === rotateKey);

        setRotateKey(null);

        if (target === undefined || props.onRotate === undefined || busyRef.current)
            return;

        commitBusy(true);

        try
        {
            const saved: IClientRow | undefined = baselineRef.current.find(r => r.rowKey === target.rowKey);

            applyResult(await props.onRotate(target.clientId,saved?.secretExpiryDate ?? null),rowsRef.current);
        }
        catch (error)
        {
            setMessages([errorText(error)]);
        }
        finally
        {
            commitBusy(false);
        }
    }

    actions.current = { create: handleCreate, save: () => { void handleSave(); }, remove: handleDelete, rotate: handleRotate };

    function handleCellValueChanged(event: CellValueChangedEvent<IClientRow>): void
    {
        const edited: IClientRow = event.data;

        updateRow(edited.rowKey,{ name:             edited.name ?? "",
                                  description:      edited.description ?? "",
                                  authorities:      normalizeAuthorities(edited.authorities ?? []),
                                  owners:           edited.owners ?? [],
                                  enabled:          edited.enabled,
                                  secretExpiryDate: edited.secretExpiryDate ?? null });
    }

    const isCellEditable = (row: IClientRow | undefined): boolean => editable && row !== undefined && !busyRef.current;

    const editableRules: Record<string,(params: CellClassParams<IClientRow>) => boolean> =
    {
        "client-editable-cell": (params) => isCellEditable(params.data),
    };

    const columnDefs: Array<ColDef<IClientRow>> =
    [
        { field: "name", headerName: "Name", flex: 1, headerTooltip: "Name",
          editable: (p) => isCellEditable(p.data), cellClassRules: editableRules },
        { field: "clientId", headerName: "Client ID", width: 330, headerTooltip: "Client ID",
          cellRenderer: ClientIdCellRenderer },
        { field: "description", headerName: "Description", flex: 1, headerTooltip: "Description",
          editable: (p) => isCellEditable(p.data), cellClassRules: editableRules },
        { field: "authorities", headerName: "Authorities", flex: 2, headerTooltip: "Authorities", cellDataType: false,
          editable: (p) => isCellEditable(p.data),
          cellEditor: AuthoritiesCellEditor, cellEditorPopup: true,
          suppressKeyboardEvent: (p) => p.editing && p.event.key === "Enter",
          cellRenderer: AuthoritiesCellRenderer,
          filterValueGetter: (p) => (p.data?.authorities ?? []).join(", "),
          comparator: (a: Array<string> | undefined,b: Array<string> | undefined) =>
              (a ?? []).join(", ").localeCompare((b ?? []).join(", ")),
          cellClassRules: editableRules },
        { field: "owners", headerName: "Owners", flex: 1, headerTooltip: "Owners", cellDataType: false, hide: !showOwners,
          editable: (p) => isCellEditable(p.data),
          cellEditor: OwnersCellEditor, cellEditorPopup: true,
          suppressKeyboardEvent: (p) => p.editing && p.event.key === "Enter",
          cellRenderer: OwnersCellRenderer,
          sortable: false, filter: false,
          cellClassRules: editableRules },
        { field: "enabled", headerName: "Enabled", width: 110, headerTooltip: "Enabled", cellDataType: "boolean",
          editable: (p) => isCellEditable(p.data), cellClassRules: editableRules },
        { field: "secretExpiryDate", headerName: "Secret expires", width: 150, headerTooltip: "Secret expires", cellDataType: false,
          editable: (p) => isCellEditable(p.data),
          cellEditor: SecretExpiryCellEditor, cellEditorPopup: true,
          valueFormatter: (p) => p.value === null || p.value === undefined || p.value === "" ? "No expiry" : String(p.value),
          cellClassRules: editableRules },
        { field: "lastTokenIssuedAt", headerName: "Last token issued", width: 190, headerTooltip: "Last token issued",
          valueFormatter: (p) => formatInstant(p.value) },
        { field: "createdAt", headerName: "Created", width: 190, headerTooltip: "Created",
          valueFormatter: (p) => formatInstant(p.value) },
    ];

    const gridContext =
    {
        roles:           roles,
        users:           props.users,
        isEditing:       () => editable && !busy,
        removeAuthority: (rowKey: string,authority: string) =>
        {
            const row: IClientRow | undefined = rowsRef.current.find(r => r.rowKey === rowKey);

            updateRow(rowKey,{ authorities: removeRole(row?.authorities ?? [],authority,roles) });
        },
    };

    const toolbarContent: ReactElement = (
        <Group gap="sm" wrap="nowrap" style={{ flex: 1 }}>
            {editable   && toolbarButton("Add Client","Add",createIcon,() => actions.current.create(),busy || hasNewRow)}
            {editable   && toolbarButton("Delete Clients","Delete",deleteIcon,() => actions.current.remove(),!hasSelection || busy)}
            {canRotate  && toolbarButton("Rotate secret","Rotate secret",rotateIcon,() => actions.current.rotate(),rotateTarget === undefined || busy)}
            {editable   && toolbarButton("Save Clients","Save",saveIcon,() => actions.current.save(),busy)}
            <div style={{ flex: 1 }} />
        </Group>
    );

    useEffect(() =>
    {
        if (props.onRenderToolbar != null)
            props.onRenderToolbar(toolbarContent);
    },[hasSelection,hasNewRow,busy,editable,canRotate,rotateTarget?.rowKey]);

    const detached: boolean = props.onRenderToolbar != null;

    const gridPanel: ReactElement = (
        <>
            <style>{gridStyles}</style>
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="sm" my={6}>
                {messages.map((m,i) => <div key={i}>{m}</div>)}
            </Alert>}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%", height: detached ? undefined : "100%" }}>
                <AgGridReact<IClientRow>
                    ref={gridRef}
                    theme={masterGridTheme}
                    loading={loading}
                    overlayLoadingTemplate="Retrieving Clients..."
                    rowData={rows}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    context={gridContext}
                    rowClassRules={{
                        "client-invalid-row": (params: RowClassParams<IClientRow>) =>
                            params.data != null && invalidKeys.has(params.data.rowKey),
                    }}
                    rowSelection={editable
                        ? { mode: "multiRow", checkboxes: true, headerCheckbox: true, enableClickSelection: false }
                        : { mode: "singleRow", checkboxes: false, enableClickSelection: canRotate }}
                    onSelectionChanged={(event) => setSelectedKeys(event.api.getSelectedRows().map(r => r.rowKey))}
                    onCellValueChanged={handleCellValueChanged}
                    getRowId={(params) => params.data.rowKey}
                />
            </div>
            <Modal opened={deleteKeys.length > 0} onClose={() => setDeleteKeys([])} title="Confirm Delete" centered>
                <Text size="sm">Are you sure you want to delete the following clients?</Text>
                <ul>
                    {rows.filter(r => deleteKeys.includes(r.rowKey)).map(r => <li key={r.rowKey}>{rowLabel(r)}</li>)}
                </ul>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={() => setDeleteKeys([])}>Cancel</Button>
                    <Button color="red" onClick={() => { void confirmDelete(); }}>Delete</Button>
                </Group>
            </Modal>
            <Modal opened={rotateKey !== null} onClose={() => setRotateKey(null)} title="Rotate secret" centered>
                <Text size="sm">
                    Rotate the secret of {rows.find(r => r.rowKey === rotateKey)?.name ?? "this client"}? The current secret stops working immediately.
                </Text>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={() => setRotateKey(null)}>Cancel</Button>
                    <Button color="red" onClick={() => { void confirmRotate(); }}>Rotate</Button>
                </Group>
            </Modal>
        </>
    );

    const loader: ReactElement | false = loading && (
        <div style={{ position: "absolute", inset: 0, display: "flex", alignItems: "center", justifyContent: "center", zIndex: 10 }}>
            <Loader size="lg" />
        </div>
    );

    if (detached)
    {
        return (
            <div style={{ flex: 1, minHeight: 0, position: "relative", display: "flex", flexDirection: "column" }}>
                {loader}
                {gridPanel}
            </div>
        );
    }

    return (
        <Stack gap={0} style={{ position: "relative", height: "100%" }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbarContent}
            </Group>
            {loader}
            {gridPanel}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
