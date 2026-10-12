//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }       from "react";
import type { ReactElement }                 from "react";
import { Stack, Group, ActionIcon, Loader, Tooltip,
         Text, Modal, Button, Divider }      from "@mantine/core";
import { useDisclosure }                     from "@mantine/hooks";
import { AgGridReact }                       from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry,
         themeQuartz }                       from "ag-grid-community";
import type { ColDef, CellClassParams,
              CellKeyDownEvent }             from "ag-grid-community";
import type { UserData }               from "granite.user.service/RequestReply";
import type { IUsersEditableViewProps } from "./IUsersEditableViewProps";

ModuleRegistry.registerModules([AllCommunityModule]);

const masterGridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8",
                                                  selectedRowBackgroundColor: "#3264C8" });
const defaultColDef: ColDef = { sortable: true, resizable: true, filter: true };

function createEmptyUser(existing: Array<UserData>): UserData
{
    const nextId = existing.reduce((max, e) => Math.max(max, e.userId ?? 0), 0) + 1;

    return { userId: nextId, userName: "" } as UserData;
}

export function UsersEditableView(props: IUsersEditableViewProps): ReactElement
{
    const [rowData, setRowData]           = useState<Array<UserData>>(props.users ?? []);
    const [savedRowData, setSavedRowData] = useState<Array<UserData>>(props.users ?? []);
    const [editingIds, setEditingIds]     = useState<Set<number>>(new Set());
    const [hasSelection, setHasSelection] = useState<boolean>(false);
    const [deleteIds, setDeleteIds]       = useState<Array<number>>([]);
    const [deleteModalOpened, { open: openDeleteModal, close: closeDeleteModal }] = useDisclosure(false);
    const gridRef                         = useRef<AgGridReact<UserData>>(null);

    const loading: boolean = props.loading ?? false;
    const isEditing        = editingIds.size > 0;

    useEffect(() =>
    {
        if (props.users != null)
        {
            setRowData(props.users);
            setSavedRowData(props.users);
        }
    }, [props.users]);

    useEffect(() =>
    {
        gridRef.current?.api?.resetRowHeights();
    }, [editingIds]);

    const isRowEditable = (id: number | undefined): boolean =>
        id != null && editingIds.has(id);

    const userEditClassRules: Record<string, (params: CellClassParams<UserData>) => boolean> =
    {
        "user-editable-cell":
            (params) =>
                params.data != null
                && editingIds.has(params.data.userId)
                && params.node.isSelected() !== true,
        "user-master-editable-selected-cell":
            (params) =>
                params.data != null
                && editingIds.has(params.data.userId)
                && params.node.isSelected() === true,
    };

    const columnDefs: Array<ColDef<UserData>> =
    [
        { field: "userId", headerName: "Id",  width: 120, headerTooltip: "User Id" },
        { field: "userName",     headerName: "User Name", flex: 1,
          headerTooltip: "User Name",
          editable: (p) => isRowEditable(p.data?.userId),
          cellClassRules: userEditClassRules },
    ];

    function handleCreate(): void
    {
        setSavedRowData(rowData);
        const newEntity = createEmptyUser(rowData);

        setRowData(prev => [...prev, newEntity]);
        setEditingIds(new Set([newEntity.userId]));
    }

    function handleEdit(): void
    {
        const selected = gridRef.current?.api?.getSelectedRows() ?? [];
        const ids      = selected.map(row => row.userId);

        if (ids.length > 0)
        {
            setSavedRowData(rowData);
            setEditingIds(new Set(ids));
        }
    }

    function handleSave(): void
    {
        setSavedRowData(rowData);
        setEditingIds(new Set());
    }

    function handleUndo(): void
    {
        setRowData(savedRowData);
        setEditingIds(new Set());
    }

    function handleApprove(): void
    {
        // Template placeholder: wire your approve-state transition here.
        console.log("Approve clicked for",
            gridRef.current?.api?.getSelectedRows().map(r => r.userId));
    }

    function handleCancel(): void
    {
        // Template placeholder: wire your cancel-state transition here.
        console.log("Cancel clicked for",
            gridRef.current?.api?.getSelectedRows().map(r => r.userId));
    }

    function handleDelete(): void
    {
        const selected = gridRef.current?.api?.getSelectedRows() ?? [];
        const ids      = selected.map(row => row.userId);

        if (ids.length > 0)
        {
            setDeleteIds(ids);
            openDeleteModal();
        }
    }

    function confirmDelete(): void
    {
        const idsToDelete = new Set(deleteIds);

        setRowData(prev => prev.filter(e => !idsToDelete.has(e.userId)));
        setEditingIds(prev =>
        {
            const next = new Set(prev);

            for (const id of idsToDelete)
            {
                next.delete(id);
            }

            return next;
        });
        setDeleteIds([]);
        closeDeleteModal();
    }

    function handleMasterKeyDown(event: CellKeyDownEvent<UserData>): void
    {
        const e = event.event as KeyboardEvent;

        if (e.ctrlKey && e.key === "z") { e.preventDefault(); event.api.undoCellEditing(); return; }
        if (e.ctrlKey && e.key === "y") { e.preventDefault(); event.api.redoCellEditing(); return; }
        if (e.ctrlKey && e.key === "e") { e.preventDefault(); handleEdit();   return; }
        if (e.ctrlKey && e.key === "n") { e.preventDefault(); handleCreate(); return; }

        if (e.ctrlKey && e.key === "s" && isEditing)
        {
            e.preventDefault();
            handleSave();
            return;
        }

        if (e.key === "Escape" && isEditing)
        {
            handleSave();
            return;
        }

        if (e.key === "Delete" && event.api.getEditingCells().length === 0)
        {
            handleDelete();
        }
    }

    const isGrowMode: boolean = props.height === "grow" || props.height == null;
    const isDetached: boolean = isGrowMode && props.onRenderToolbar != null;
    const gridHeight: string | number =
        props.height === undefined || props.height === "grow"
            ? "100%"
            : (props.height as number) * 36 + 48;

    const toolbarContent: ReactElement = (
        <Group gap="sm" wrap="nowrap" style={{ flex: 1 }}>
            {(props.showCreate ?? true) &&
            <Tooltip label="Create User">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleCreate}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <line x1="12" y1="5" x2="12" y2="19" />
                            <line x1="5" y1="12" x2="19" y2="12" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs">Create</Text>
                </Stack>
            </Tooltip>}
            {(props.showEdit ?? true) &&
            <Tooltip label="Edit Users">
                <Stack align="center" gap={2}>
                    <ActionIcon variant={isEditing ? "filled" : "default"}
                                color={isEditing ? "blue" : undefined}
                                size="lg" onClick={handleEdit} disabled={!hasSelection}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M17 3a2.85 2.85 0 0 1 4 4L7.5 20.5 2 22l1.5-5.5Z" />
                            <path d="m15 5 4 4" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!hasSelection ? "dimmed" : undefined}>Edit</Text>
                </Stack>
            </Tooltip>}
            {(props.showSave ?? true) &&
            <Tooltip label="Save Users">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleSave} disabled={!isEditing}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                            <polyline points="17 21 17 13 7 13 7 21" />
                            <polyline points="7 3 7 8 15 8" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!isEditing ? "dimmed" : undefined}>Save</Text>
                </Stack>
            </Tooltip>}
            {(props.showUndo ?? true) &&
            <Tooltip label="Undo Changes">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleUndo} disabled={!isEditing}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="1 4 1 10 7 10" />
                            <path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!isEditing ? "dimmed" : undefined}>Undo</Text>
                </Stack>
            </Tooltip>}
            <Divider orientation="vertical" mx="md" />
            {(props.showApprove ?? true) &&
            <Tooltip label="Approve Users">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleApprove} disabled={!hasSelection}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="20 6 9 17 4 12" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!hasSelection ? "dimmed" : undefined}>Approve</Text>
                </Stack>
            </Tooltip>}
            <Divider orientation="vertical" mx="md" />
            {(props.showCancel ?? true) &&
            <Tooltip label="Cancel Users">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleCancel} disabled={!hasSelection}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <circle cx="12" cy="12" r="10" />
                            <line x1="15" y1="9" x2="9" y2="15" />
                            <line x1="9" y1="9" x2="15" y2="15" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!hasSelection ? "dimmed" : undefined}>Cancel</Text>
                </Stack>
            </Tooltip>}
            {(props.showDelete ?? true) &&
            <Tooltip label="Delete Users">
                <Stack align="center" gap={2}>
                    <ActionIcon variant="default" size="lg" onClick={handleDelete} disabled={!hasSelection}>
                        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24"
                             fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                            <path d="M10 11v6" />
                            <path d="M14 11v6" />
                            <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2" />
                        </svg>
                    </ActionIcon>
                    <Text size="xs" c={!hasSelection ? "dimmed" : undefined}>Delete</Text>
                </Stack>
            </Tooltip>}
            <div style={{ flex: 1 }} />
        </Group>
    );

    useEffect(() =>
    {
        if (props.onRenderToolbar != null)
        {
            props.onRenderToolbar(toolbarContent);
        }
    }, [isEditing, hasSelection, editingIds]);

    if (isDetached)
    {
        return (
            <>
                <div style={{ flex: 1, minHeight: 0, position: "relative", display: "flex", flexDirection: "column" }}>
                    {loading && (
                        <div style={{
                            position: "absolute", inset: 0, display: "flex",
                            alignItems: "center", justifyContent: "center", zIndex: 10,
                        }}>
                            <Loader size="lg" />
                        </div>
                    )}
                    <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                        <AgGridReact<UserData>
                            ref={gridRef}
                            theme={masterGridTheme}
                            loading={loading}
                            overlayLoadingTemplate="Retrieving Users..."
                            rowData={rowData}
                            columnDefs={columnDefs}
                            defaultColDef={defaultColDef}
                            alwaysPassFilter={(node) => node.isSelected() === true}
                            undoRedoCellEditing={true}
                            undoRedoCellEditingLimit={20}
                            rowClassRules={{
                                "user-editable-row":
                                    (params) =>
                                        params.data != null
                                        && editingIds.has(params.data.userId),
                            }}
                            rowSelection={{ mode: "multiRow", checkboxes: true, headerCheckbox: true,
                                            selectAll: "filtered", enableClickSelection: false,
                                            isRowSelectable: (node) =>
                                                editingIds.size === 0
                                                || editingIds.has(node.data?.userId ?? -1) }}
                            onSelectionChanged={(event) =>
                            {
                                setHasSelection(event.api.getSelectedRows().length > 0);
                                event.api.refreshCells({ force: true });
                                event.api.onFilterChanged();
                            }}
                            onCellKeyDown={handleMasterKeyDown}
                            getRowId={(params) => "user-" + params.data.userId}
                        />
                    </div>
                </div>
                <Modal opened={deleteModalOpened} onClose={closeDeleteModal}
                       title="Confirm Delete" centered>
                    <Text size="sm">
                        Are you sure you want to delete the selected users?
                    </Text>
                    <Group justify="flex-end" mt="lg">
                        <Button variant="default" onClick={closeDeleteModal}>Cancel</Button>
                        <Button color="red" onClick={confirmDelete}>Delete</Button>
                    </Group>
                </Modal>
            </>
        );
    }

    return (
        <Stack gap={0} style={isGrowMode
            ? { margin: "calc(-1 * var(--mantine-spacing-md))", height: "calc(100vh - var(--app-header-height))", overflow: "hidden", position: "relative" as const }
            : { position: "relative" as const }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbarContent}
            </Group>
            {loading && (
                <div style={{
                    position: "absolute", inset: 0, display: "flex",
                    alignItems: "center", justifyContent: "center", zIndex: 10,
                }}>
                    <Loader size="lg" />
                </div>
            )}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%", height: gridHeight }}>
                <AgGridReact<UserData>
                    ref={gridRef}
                    theme={masterGridTheme}
                    loading={loading}
                    overlayLoadingTemplate="Retrieving Users..."
                    rowData={rowData}
                    columnDefs={columnDefs}
                    defaultColDef={defaultColDef}
                    alwaysPassFilter={(node) => node.isSelected() === true}
                    undoRedoCellEditing={true}
                    undoRedoCellEditingLimit={20}
                    rowClassRules={{
                        "user-editable-row":
                            (params) =>
                                params.data != null
                                && editingIds.has(params.data.userId),
                    }}
                    rowSelection={{ mode: "multiRow", checkboxes: true, headerCheckbox: true,
                                    selectAll: "filtered", enableClickSelection: false,
                                    isRowSelectable: (node) =>
                                        editingIds.size === 0
                                        || editingIds.has(node.data?.userId ?? -1) }}
                    onSelectionChanged={(event) =>
                    {
                        setHasSelection(event.api.getSelectedRows().length > 0);
                        event.api.refreshCells({ force: true });
                        event.api.onFilterChanged();
                    }}
                    onCellKeyDown={handleMasterKeyDown}
                    getRowId={(params) => "user-" + params.data.userId}
                />
            </div>
            <Modal opened={deleteModalOpened} onClose={closeDeleteModal}
                   title="Confirm Delete" centered>
                <Text size="sm">
                    Are you sure you want to delete the selected users?
                </Text>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={closeDeleteModal}>Cancel</Button>
                    <Button color="red" onClick={confirmDelete}>Delete</Button>
                </Group>
            </Modal>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
