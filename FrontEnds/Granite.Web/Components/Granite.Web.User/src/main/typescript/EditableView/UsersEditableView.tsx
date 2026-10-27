//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useRef, useState }  from "react";
import type { ReactElement }                     from "react";
import { Stack, Group, ActionIcon, Loader, Tooltip,
         Text, Modal, Button, Divider, Alert }   from "@mantine/core";
import { useDisclosure }                         from "@mantine/hooks";
import { AgGridReact }                           from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry,
         themeQuartz }                           from "ag-grid-community";
import type { ColDef, CellClassParams, CellKeyDownEvent,
              CellValueChangedEvent, PostSortRowsParams,
              RowClassParams }                   from "ag-grid-community";
import type { IUsersEditableViewProps, IUsersResult } from "./IUsersEditableViewProps";
import { computeChanges, isRefreshFailure, mergePending, normalizeAuthorities,
         toRows, validateRows }                  from "./UsersChangeSet";
import type { IUserRow }                         from "./UsersChangeSet";
import { toEntries, toValues, validateEntries, entryMessages,
         removeOrphanEntries, isActionAllowed,
         nextMacrotask, createDetailGridRegistry }  from "./UsersValueEntries";
import type { IValueEntry, UsersAction }         from "./UsersValueEntries";
import { ExpandCellRenderer }                    from "./ExpandCellRenderer";
import { AuthoritiesCellRenderer }               from "./AuthoritiesCellRenderer";
import { AuthoritiesCellEditor }                 from "./AuthoritiesCellEditor";
import { PasswordCellEditor }                    from "./PasswordCellEditor";
import { removeRole }                           from "./RoleCascade";
import { UserValuesDetail, UserValuesContext,
         detailRowHeight }                       from "./UserValuesDetail";
import type { IUserValuesContext }               from "./UserValuesDetail";

ModuleRegistry.registerModules([AllCommunityModule]);

const masterGridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8",
                                                  selectedRowBackgroundColor: "#3264C8" });
const defaultColDef: ColDef<IUserRow> = { sortable: true, resizable: true, filter: true };

const gridStyles: string = ".user-editable-cell { background: #fff8e1; }"
                         + " .user-invalid-row { background: #fdecea !important; }"
                         + " .user-expanded-parent { border-bottom: none; }";

const svgProps = { xmlns: "http://www.w3.org/2000/svg", width: 20, height: 20, viewBox: "0 0 24 24",
                   fill: "none", stroke: "currentColor", strokeWidth: 2,
                   strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

function errorText(error: unknown): string
{
    return error instanceof Error ? error.message : String(error);
}

function rowLabel(row: IUserRow): string
{
    return (row.userName ?? "").trim() === "" ? "(new user)" : (row.userName ?? "");
}

function createNewRow(rowKey: string): IUserRow
{
    return { userId: 0, userName: "", password: "", authorities: [], values: {},
             rowKey, isNew: true, isDetailRow: false };
}

function seedEntries(rowKey: string | null, rows: Array<IUserRow>): Record<string, Array<IValueEntry>>
{
    const row: IUserRow | undefined = rows.find(r => r.rowKey === rowKey);

    return rowKey !== null && row !== undefined ? { [rowKey]: toEntries(row.values) } : {};
}

function handlePostSortRows(params: PostSortRowsParams<IUserRow>): void
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

function toolbarButton(label: string, text: string, icon: ReactElement, onClick: () => void,
                       disabled: boolean, active: boolean = false): ReactElement
{
    return (
        <Tooltip label={label}>
            <Stack align="center" gap={2}>
                <ActionIcon variant={active ? "filled" : "default"} color={active ? "blue" : undefined}
                            size="lg" onClick={onClick} disabled={disabled}>
                    <svg {...svgProps}>{icon}</svg>
                </ActionIcon>
                <Text size="xs" c={disabled ? "dimmed" : undefined}>{text}</Text>
            </Stack>
        </Tooltip>
    );
}

const createIcon: ReactElement = (<><line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" /></>);
const editIcon:   ReactElement = (<><path d="M17 3a2.85 2.85 0 0 1 4 4L7.5 20.5 2 22l1.5-5.5Z" /><path d="m15 5 4 4" /></>);
const saveIcon:   ReactElement = (<><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                                     <polyline points="17 21 17 13 7 13 7 21" /><polyline points="7 3 7 8 15 8" /></>);
const undoIcon:   ReactElement = (<><polyline points="1 4 1 10 7 10" /><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" /></>);
const deleteIcon: ReactElement = (<><polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                                     <path d="M10 11v6" /><path d="M14 11v6" /><path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" /></>);

export function UsersEditableView(props: IUsersEditableViewProps): ReactElement
{
    const [rows, setRows]                 = useState<Array<IUserRow>>(() => toRows(props.users ?? []));
    const [baseline, setBaseline]         = useState<Array<IUserRow>>(() => toRows(props.users ?? []));
    const [editingKeys, setEditingKeys]   = useState<Set<string>>(new Set());
    const [expandedKey, setExpandedKey]   = useState<string | null>(null);
    const [entriesByKey, setEntriesByKey] = useState<Record<string, Array<IValueEntry>>>({});
    const [selectedKeys, setSelectedKeys] = useState<Array<string>>([]);
    const [invalidKeys, setInvalidKeys]   = useState<Set<string>>(new Set());
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [busy, setBusy]                 = useState<boolean>(false);
    const [deleteKeys, setDeleteKeys]     = useState<Array<string>>([]);
    const [deleteModalOpened, { open: openDeleteModal, close: closeDeleteModal }] = useDisclosure(false);
    const gridRef                         = useRef<AgGridReact<IUserRow>>(null);
    const rowsRef                         = useRef<Array<IUserRow>>(rows);
    const baselineRef                     = useRef<Array<IUserRow>>(baseline);
    const entriesRef                      = useRef<Record<string, Array<IValueEntry>>>(entriesByKey);
    const expandedRef                     = useRef<string | null>(expandedKey);
    const busyRef                         = useRef<boolean>(false);
    const settlingRef                     = useRef<boolean>(false);
    const detailGrids                     = useRef(createDetailGridRegistry());
    const newRowCounter                   = useRef<number>(0);
    const pendingVisibleKey               = useRef<string | null>(null);
    const actions                         = useRef<{ create: () => void; edit: () => void; save: () => void;
                                                     undo: () => void; remove: () => void }>(
                                                { create: () => undefined, edit: () => undefined, save: () => undefined,
                                                  undo: () => undefined, remove: () => undefined });

    rowsRef.current     = rows;
    baselineRef.current = baseline;
    entriesRef.current  = entriesByKey;
    expandedRef.current = expandedKey;

    const loading:   boolean = (props.loading ?? false) || busy;
    const isEditing: boolean = editingKeys.size > 0;
    const hasSelection: boolean = selectedKeys.length > 0;

    const deleteDisabled: boolean = !hasSelection || busy
        || rows.some(r => selectedKeys.includes(r.rowKey) && !r.isNew && r.userId === props.currentUserId);

    useEffect(() =>
    {
        if (props.users != null)
        {
            const fresh: Array<IUserRow> = toRows(props.users);

            commitRows(fresh);
            baselineRef.current = fresh;
            setBaseline(fresh);
            setEditingKeys(new Set());
            setInvalidKeys(new Set());
            commitEntries(seedEntries(expandedRef.current, fresh));
        }
    }, [props.users]);

    useEffect(() =>
    {
        gridRef.current?.api?.redrawRows();
    }, [editingKeys, invalidKeys]);

    useEffect(() =>
    {
        gridRef.current?.api?.refreshCells({ force: true, columns: ["expand"] });
    }, [expandedKey]);

    const displayData = useMemo<Array<IUserRow>>(() =>
    {
        const result: Array<IUserRow> = [];

        for (const row of rows)
        {
            result.push({ ...row, isDetailRow: false });

            if (row.rowKey === expandedKey)
            {
                result.push({ ...row, isDetailRow: true });
            }
        }

        return result;
    }, [rows, expandedKey]);

    function commitRows(next: Array<IUserRow>): void
    {
        rowsRef.current = next;
        setRows(next);
    }

    function commitEntries(next: Record<string, Array<IValueEntry>>): void
    {
        entriesRef.current = next;
        setEntriesByKey(next);
    }

    function commitBusy(next: boolean): void
    {
        busyRef.current = next;
        setBusy(next);
    }

    function updateRow(rowKey: string, patch: Partial<IUserRow>): void
    {
        if (busyRef.current)
        {
            return;
        }

        commitRows(rowsRef.current.map(r => r.rowKey === rowKey ? { ...r, ...patch } : r));
    }

    function setEntries(rowKey: string, entries: Array<IValueEntry>): void
    {
        if (busyRef.current)
        {
            return;
        }

        commitEntries({ ...entriesRef.current, [rowKey]: entries });

        if (entryMessages(entries).length === 0)
        {
            updateRow(rowKey, { values: toValues(entries) });
        }
    }

    function toggleExpand(rowKey: string): void
    {
        if (rowKey === expandedRef.current)
        {
            expandedRef.current = null;
            setExpandedKey(null);
            return;
        }

        expandedRef.current = rowKey;
        setExpandedKey(rowKey);

        if (entriesRef.current[rowKey] === undefined)
        {
            commitEntries({ ...entriesRef.current, ...seedEntries(rowKey, rowsRef.current) });
        }
    }

    function applyResult(result: IUsersResult, current: Array<IUserRow>): void
    {
        if (isRefreshFailure(result))
        {
            setMessages(result.failureMessages);
            return;
        }

        const refreshed: Array<IUserRow> = toRows(result.users);
        const merged:    Array<IUserRow> = mergePending(result.users, current, baselineRef.current);
        const pending                    = computeChanges(refreshed, merged);
        const pendingKeys: Set<string>   = new Set([ ...merged.filter(r => r.isNew).map(r => r.rowKey),
                                                     ...pending.usersToUpdate.map(u => `user-${u.userId}`) ]);
        const nextExpanded: string | null = merged.some(r => r.rowKey === expandedRef.current) ? expandedRef.current : null;

        commitRows(merged);
        baselineRef.current = refreshed;
        setBaseline(refreshed);
        setEditingKeys(pendingKeys);
        expandedRef.current = nextExpanded;
        setExpandedKey(nextExpanded);
        commitEntries(seedEntries(nextExpanded, merged));
        setInvalidKeys(new Set());
        setMessages(result.failureMessages ?? []);
    }

    function collectProblems(): Map<string, Array<string>>
    {
        const problems: Map<string, Array<string>> = validateRows(rowsRef.current);

        for (const [rowKey, entryProblems] of validateEntries(entriesRef.current))
        {
            const existing: Array<string> = problems.get(rowKey) ?? [];

            problems.set(rowKey, [...new Set([...existing, ...entryProblems])]);
        }

        return problems;
    }

    function actionAllowed(action: UsersAction): boolean
    {
        return isActionAllowed(action, { busy: busyRef.current || settlingRef.current, isEditing, hasSelection, deleteDisabled });
    }

    async function settleOpenEdits(): Promise<void>
    {
        settlingRef.current = true;

        try
        {
            gridRef.current?.api?.stopEditing();
            detailGrids.current.stopEditing();
            await nextMacrotask();
        }
        finally
        {
            settlingRef.current = false;
        }
    }

    function handleCreate(): void
    {
        if (!actionAllowed("create"))
        {
            return;
        }

        const rowKey: string = `new-${++newRowCounter.current}`;

        pendingVisibleKey.current = rowKey;
        commitRows([...rowsRef.current, createNewRow(rowKey)]);
        setEditingKeys(prev => new Set([...prev, rowKey]));
    }

    function handleEdit(): void
    {
        if (actionAllowed("edit"))
        {
            setEditingKeys(prev => new Set([...prev, ...selectedKeys]));
        }
    }

    async function handleSave(): Promise<void>
    {
        if (!actionAllowed("save"))
        {
            return;
        }

        await settleOpenEdits();

        if (!actionAllowed("save"))
        {
            return;
        }

        const rows:    Array<IUserRow> = rowsRef.current;
        const baseline: Array<IUserRow> = baselineRef.current;
        const problems: Map<string, Array<string>> = collectProblems();

        if (problems.size > 0)
        {
            const lines: Array<string> = [];

            for (const [rowKey, rowProblems] of problems)
            {
                const row: IUserRow | undefined = rows.find(r => r.rowKey === rowKey);

                lines.push(...rowProblems.map(p => `${row === undefined ? rowKey : rowLabel(row)}: ${p}`));
            }

            setInvalidKeys(new Set(problems.keys()));
            setMessages(lines);
            return;
        }

        const changes = computeChanges(baseline, rows);

        if (changes.usersToCreate.length === 0 && changes.usersToUpdate.length === 0)
        {
            setEditingKeys(new Set());
            setInvalidKeys(new Set());
            setMessages([]);
            return;
        }

        if (props.onSave === undefined)
        {
            return;
        }

        commitBusy(true);
        setInvalidKeys(new Set());

        try
        {
            const result: IUsersResult = await props.onSave(changes);

            applyResult(result, rowsRef.current);
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

    function handleUndo(): void
    {
        if (!actionAllowed("undo"))
        {
            return;
        }

        const baseline: Array<IUserRow> = baselineRef.current;
        const keepKey:  string | null   = baseline.some(r => r.rowKey === expandedRef.current) ? expandedRef.current : null;

        commitRows(baseline);
        setEditingKeys(new Set());
        setInvalidKeys(new Set());
        setMessages([]);
        expandedRef.current = keepKey;
        setExpandedKey(keepKey);
        commitEntries(seedEntries(keepKey, baseline));
    }

    function handleDelete(): void
    {
        if (actionAllowed("delete"))
        {
            setDeleteKeys(selectedKeys);
            openDeleteModal();
        }
    }

    async function confirmDelete(): Promise<void>
    {
        closeDeleteModal();

        if (busyRef.current || settlingRef.current)
        {
            return;
        }

        await settleOpenEdits();

        if (busyRef.current)
        {
            return;
        }

        const keys:     Set<string>     = new Set(deleteKeys);
        const doomed:    Array<IUserRow> = rowsRef.current.filter(r => keys.has(r.rowKey));
        const remaining: Array<IUserRow> = rowsRef.current.filter(r => !keys.has(r.rowKey));
        const ids:       Array<number>   = doomed.filter(r => !r.isNew).map(r => r.userId);

        setDeleteKeys([]);

        if (ids.length === 0)
        {
            commitRows(remaining);
            commitEntries(removeOrphanEntries(entriesRef.current, remaining.map(r => r.rowKey)));
            setEditingKeys(prev => new Set([...prev].filter(k => !keys.has(k))));

            if (expandedRef.current !== null && keys.has(expandedRef.current))
            {
                expandedRef.current = null;
                setExpandedKey(null);
            }

            return;
        }

        if (props.onDelete === undefined)
        {
            return;
        }

        commitBusy(true);

        try
        {
            const result: IUsersResult = await props.onDelete(ids);

            applyResult(result, rowsRef.current.filter(r => !keys.has(r.rowKey)));
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

    actions.current = { create: handleCreate, edit: handleEdit, save: () => { void handleSave(); },
                        undo: handleUndo, remove: handleDelete };

    function handleCellValueChanged(event: CellValueChangedEvent<IUserRow>): void
    {
        const edited: IUserRow = event.data;

        if (!edited.isDetailRow)
        {
            updateRow(edited.rowKey, { userName:    edited.userName,
                                       password:    edited.password,
                                       authorities: normalizeAuthorities(edited.authorities ?? []) });
        }
    }

    function handleMasterKeyDown(event: CellKeyDownEvent<IUserRow>): void
    {
        const e = event.event as KeyboardEvent;

        if (e.ctrlKey && e.key === "z" && (props.showUndo ?? true)) { e.preventDefault(); event.api.undoCellEditing(); return; }
        if (e.ctrlKey && e.key === "y" && (props.showUndo ?? true)) { e.preventDefault(); event.api.redoCellEditing(); return; }
        if (e.ctrlKey && e.key === "e" && (props.showEdit ?? true)) { e.preventDefault(); handleEdit();   return; }
        if (e.ctrlKey && e.key === "n" && (props.showCreate ?? true)) { e.preventDefault(); handleCreate(); return; }

        if (e.ctrlKey && e.key === "s" && isEditing && (props.showSave ?? true))
        {
            e.preventDefault();
            void handleSave();
            return;
        }

        if (e.key === "Delete" && (props.showDelete ?? true) && event.api.getEditingCells().length === 0)
        {
            handleDelete();
        }
    }

    const isMasterEditable = (row: IUserRow | undefined): boolean =>
        row !== undefined && !row.isDetailRow && editingKeys.has(row.rowKey) && !busyRef.current;

    const newRowEditableRules: Record<string, (params: CellClassParams<IUserRow>) => boolean> =
    {
        "user-editable-cell": (params) => params.data?.isDetailRow === false && params.data.isNew,
    };

    const authoritiesEditableRules: Record<string, (params: CellClassParams<IUserRow>) => boolean> =
    {
        "user-editable-cell": (params) => isMasterEditable(params.data),
    };

    const columnDefs: Array<ColDef<IUserRow>> =
    [
        { colId: "expand", headerName: "", width: 50, sortable: false, filter: false, resizable: false,
          suppressMovable: true, cellRenderer: ExpandCellRenderer },
        { colId: "id", headerName: "Id", width: 110, headerTooltip: "User Id",
          valueGetter: (p) => p.data == null || p.data.isNew ? "" : p.data.userId },
        { field: "userName", headerName: "User Name", flex: 1, headerTooltip: "User Name",
          editable: (p) => p.data?.isDetailRow === false && p.data.isNew && !busyRef.current,
          cellClassRules: newRowEditableRules },
        { field: "externalIdentity", headerName: "External Identity", flex: 1, headerTooltip: "External Identity" },
        { field: "password", headerName: "Password", width: 160, headerTooltip: "Password",
          sortable: false, filter: false,
          editable: (p) => p.data?.isDetailRow === false && p.data.isNew && !busyRef.current,
          cellEditor: PasswordCellEditor,
          valueFormatter: (p) => p.data?.isNew ? "•".repeat((p.value ?? "").length) : "********",
          cellClassRules: newRowEditableRules },
        { field: "authorities", headerName: "Authorities", flex: 2, headerTooltip: "Authorities", cellDataType: false,
          editable: (p) => isMasterEditable(p.data),
          cellEditor: AuthoritiesCellEditor, cellEditorPopup: true,
          suppressKeyboardEvent: (p) => p.editing && p.event.key === "Enter",
          cellRenderer: AuthoritiesCellRenderer,
          filterValueGetter: (p) => (p.data?.authorities ?? []).join(", "),
          comparator: (a: Array<string> | undefined, b: Array<string> | undefined) =>
              (a ?? []).join(", ").localeCompare((b ?? []).join(", ")),
          cellClassRules: authoritiesEditableRules },
    ];

    const valuesContext: IUserValuesContext =
    {
        entriesFor:         (rowKey) => entriesRef.current[rowKey] ?? [],
        setEntries,
        isEditing:          (rowKey) => editingKeys.has(rowKey) && !busy,
        registerDetailGrid: (rowKey, api) => detailGrids.current.register(rowKey,api),
    };

    const gridContext =
    {
        expandedKey,
        toggleExpand,
        roles:           props.roles,
        isEditing:       (rowKey: string) => editingKeys.has(rowKey),
        removeAuthority: (rowKey: string, authority: string) =>
        {
            const row: IUserRow | undefined = rowsRef.current.find(r => r.rowKey === rowKey);

            updateRow(rowKey, { authorities: removeRole(row?.authorities ?? [],authority,props.roles) });
        },
    };

    const isGrowMode: boolean = props.height === "grow" || props.height == null;
    const isDetached: boolean = isGrowMode && props.onRenderToolbar != null;
    const gridHeight: string | number =
        props.height === undefined || props.height === "grow"
            ? "100%"
            : (props.height as number) * 36 + 48;

    const showCreate: boolean = props.showCreate ?? true;
    const showEdit:   boolean = props.showEdit   ?? true;
    const showSave:   boolean = props.showSave   ?? true;
    const showUndo:   boolean = props.showUndo   ?? true;
    const showDelete: boolean = props.showDelete ?? true;

    const toolbarContent: ReactElement = (
        <Group gap="sm" wrap="nowrap" style={{ flex: 1 }}>
            {showCreate && toolbarButton("Create User", "Create", createIcon, () => actions.current.create(), busy)}
            {showEdit   && toolbarButton("Edit Users", "Edit", editIcon, () => actions.current.edit(), !hasSelection || busy, isEditing)}
            {showSave   && toolbarButton("Save Users", "Save", saveIcon, () => actions.current.save(), !isEditing || busy)}
            {showUndo   && toolbarButton("Undo Changes", "Undo", undoIcon, () => actions.current.undo(), !isEditing || busy)}
            <Divider orientation="vertical" mx="md" />
            {showDelete && toolbarButton("Delete Users", "Delete", deleteIcon, () => actions.current.remove(), deleteDisabled)}
            <div style={{ flex: 1 }} />
        </Group>
    );

    useEffect(() =>
    {
        if (props.onRenderToolbar != null)
        {
            props.onRenderToolbar(toolbarContent);
        }
    }, [isEditing, hasSelection, deleteDisabled, busy, showCreate, showEdit, showSave, showUndo, showDelete]);

    const gridPanel: ReactElement = (
        <>
            <style>{gridStyles}</style>
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="sm" my={6}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%",
                                                     height: isDetached ? undefined : gridHeight }}>
                <UserValuesContext.Provider value={valuesContext}>
                    <AgGridReact<IUserRow>
                        ref={gridRef}
                        theme={masterGridTheme}
                        loading={loading}
                        overlayLoadingTemplate="Retrieving Users..."
                        rowData={displayData}
                        columnDefs={columnDefs}
                        defaultColDef={defaultColDef}
                        context={gridContext}
                        alwaysPassFilter={(node) => node.isSelected() === true}
                        undoRedoCellEditing={true}
                        undoRedoCellEditingLimit={20}
                        rowClassRules={{
                            "user-expanded-parent": (params: RowClassParams<IUserRow>) =>
                                params.data?.isDetailRow === false && params.data.rowKey === expandedKey,
                            "user-invalid-row": (params: RowClassParams<IUserRow>) =>
                                params.data != null && invalidKeys.has(params.data.rowKey),
                        }}
                        rowSelection={{ mode: "multiRow", checkboxes: true, headerCheckbox: true,
                                        selectAll: "filtered", enableClickSelection: false,
                                        isRowSelectable: (node) =>
                                            node.data?.isDetailRow !== true
                                            && (editingKeys.size === 0 || editingKeys.has(node.data?.rowKey ?? "")) }}
                        onSelectionChanged={(event) =>
                        {
                            setSelectedKeys(event.api.getSelectedRows().filter(r => !r.isDetailRow).map(r => r.rowKey));
                            event.api.refreshCells({ force: true });
                            event.api.onFilterChanged();
                        }}
                        onCellValueChanged={handleCellValueChanged}
                        onRowDataUpdated={(event) =>
                        {
                            if (pendingVisibleKey.current !== null)
                            {
                                const node = event.api.getRowNode(pendingVisibleKey.current);

                                pendingVisibleKey.current = null;

                                if (node != null)
                                {
                                    event.api.ensureNodeVisible(node);
                                }
                            }
                        }}
                        onCellKeyDown={handleMasterKeyDown}
                        getRowId={(params) => params.data.isDetailRow ? "detail-" + params.data.rowKey : params.data.rowKey}
                        getRowHeight={(params) =>
                            params.data?.isDetailRow === true
                                ? detailRowHeight(entriesByKey[params.data.rowKey]?.length
                                                  ?? Object.keys(params.data.values ?? {}).length)
                                : undefined}
                        isFullWidthRow={(params) => params.rowNode.data?.isDetailRow === true}
                        fullWidthCellRenderer={UserValuesDetail}
                        postSortRows={handlePostSortRows}
                    />
                </UserValuesContext.Provider>
            </div>
            <Modal opened={deleteModalOpened} onClose={closeDeleteModal} title="Confirm Delete" centered>
                <Text size="sm">Are you sure you want to delete the following users?</Text>
                <ul>
                    {rows.filter(r => deleteKeys.includes(r.rowKey)).map(r => <li key={r.rowKey}>{rowLabel(r)}</li>)}
                </ul>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={closeDeleteModal}>Cancel</Button>
                    <Button color="red" onClick={() => { void confirmDelete(); }}>Delete</Button>
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
        <Stack gap={0} style={isGrowMode
            ? { margin: "calc(-1 * var(--mantine-spacing-md))", height: "calc(100vh - var(--app-header-height))", overflow: "hidden", position: "relative" as const }
            : { position: "relative" as const }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbarContent}
            </Group>
            {loader}
            {gridPanel}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
