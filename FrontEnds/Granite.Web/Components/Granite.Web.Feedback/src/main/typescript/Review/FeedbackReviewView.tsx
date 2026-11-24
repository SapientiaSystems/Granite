//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Feedback
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useState }                   from "react";
import type { CSSProperties, ReactElement }               from "react";
import { ActionIcon, Alert, Group, Loader, Stack, Text, Tooltip,
         useComputedColorScheme }                         from "@mantine/core";
import { AgGridReact }                                    from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry, themeQuartz } from "ag-grid-community";
import type { ColDef, CellValueChangedEvent }             from "ag-grid-community";
import { GraniteFrontEndControllerServiceClient }         from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient }   from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { FeedbackReviewInput, FeedbackReviewOutput,
              GetFeedbackReviewOutputReply,
              HandleFeedbackReviewInputReply }            from "granite.frontendcontroller.service/RequestReply";
import { canEdit, useAuthentication }                     from "granite.web.authentication/Authentication";
import { feedbackTypeLabel }                              from "../Feedback/FeedbackForm";
import { FEEDBACK_STATUSES, changedFeedback, formatInstant, toFeedbackRows } from "./FeedbackRows";
import type { IFeedbackRow }                              from "./FeedbackRows";

ModuleRegistry.registerModules([AllCommunityModule]);

const SOURCE:      string = "Feedback.All";
const DESTINATION: string = "FeedbackReviewView";
const SERVICE:     string = "frontendcontroller-user-service";

const gridTheme = themeQuartz.withParams({ columnBorder: true, headerBackgroundColor: "#e8e8e8", selectedRowBackgroundColor: "#3264C8" });

const svgProps = { xmlns: "http://www.w3.org/2000/svg", width: 20, height: 20, viewBox: "0 0 24 24",
                   fill: "none", stroke: "currentColor", strokeWidth: 2,
                   strokeLinecap: "round" as const, strokeLinejoin: "round" as const };

const refreshIcon: ReactElement = (<><polyline points="23 4 23 10 17 10" /><polyline points="1 20 1 14 7 14" />
                                     <path d="M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15" /></>);
const saveIcon:    ReactElement = (<><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z" />
                                     <polyline points="17 21 17 13 7 13 7 21" /><polyline points="7 3 7 8 15 8" /></>);
const undoIcon:    ReactElement = (<><polyline points="1 4 1 10 7 10" /><path d="M3.51 15a9 9 0 1 0 2.13-9.36L1 10" /></>);

export interface IFeedbackReviewViewProps
{
    readonly service?:   string;
    readonly height?:    string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

function toolbarButton(label: string, text: string, icon: ReactElement, onClick: () => void, disabled: boolean): ReactElement
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

export function FeedbackReviewView(props: IFeedbackReviewViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const editable: boolean               = canEdit(state);
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [original, setOriginal]         = useState<Array<IFeedbackRow>>([]);
    const [rows, setRows]                 = useState<Array<IFeedbackRow>>([]);
    const [loading, setLoading]           = useState<boolean>(true);
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [reload, setReload]             = useState<number>(0);
    const changes                         = useMemo(() => changedFeedback(original,rows), [original,rows]);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    const apply = (output: FeedbackReviewOutput | undefined): void =>
    {
        const next: Array<IFeedbackRow> = toFeedbackRows(output?.feedback,output?.userNames);

        setOriginal(next);
        setRows(next);
        setMessages(output?.failureMessages ?? []);
    };

    const fail = (message: string): void =>
    {
        setMessages([message]);
        setLoading(false);
    };

    useEffect(() =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();

        setLoading(true);
        client
            .getFeedbackReviewOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetFeedbackReviewOutputReply) =>
            {
                if (reply.success === false)
                    setMessages([reply.failureMessage ?? "Failed to load feedback"]);
                else
                    apply(reply.output);

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                fail(error?.message ?? "Failed to load feedback");
                return {} as GetFeedbackReviewOutputReply;
            })
            .subscribe(undefined,(error: Error) => fail(error?.message ?? "Failed to load feedback"));

        return () => { client.close(); };
    }, [reload]);

    function save(): void
    {
        if (changes.length === 0)
        {
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input: FeedbackReviewInput =
            { userId: null, source: SOURCE, command: "Save", destination: DESTINATION, changes } as FeedbackReviewInput;

        setLoading(true);
        client
            .handleFeedbackReviewInput(HandleInputRequestBuilder.newBuilder<FeedbackReviewInput>().setInput(input).build())
            .thenApply((reply: HandleFeedbackReviewInputReply) =>
            {
                client.close();

                if (reply.success === false)
                    setMessages([reply.failureMessage ?? "Unable to save feedback"]);
                else
                    apply(reply.output);

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                client.close();
                fail(error?.message ?? "Unable to save feedback");
                return {} as HandleFeedbackReviewInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                client.close();
                fail(error?.message ?? "Unable to save feedback");
            });
    }

    function handleCellValueChanged(event: CellValueChangedEvent<IFeedbackRow>): void
    {
        const edited: IFeedbackRow = event.data;

        setRows(previous => previous.map(row => row.rowKey === edited.rowKey ? { ...edited } : row));
    }

    const columnDefs: Array<ColDef<IFeedbackRow>> =
    [
        { field: "feedbackId", headerName: "Id",       width: 90, filter: "agNumberColumnFilter" },
        { field: "created",    headerName: "Created",  flex: 1, sort: "desc", valueFormatter: (p) => formatInstant(p.value) },
        { field: "userName",   headerName: "User",     flex: 1 },
        { field: "type",       headerName: "Type",     width: 120, valueFormatter: (p) => feedbackTypeLabel(p.value) },
        { field: "summary",    headerName: "Summary",  flex: 2, tooltipField: "summary" },
        { field: "details",    headerName: "Details",  flex: 3, tooltipField: "details" },
        { field: "status",     headerName: "Status",   width: 130, editable: editable,
          cellEditor: "agSelectCellEditor", cellEditorParams: { values: FEEDBACK_STATUSES } },
        { field: "notes",      headerName: "Notes",    flex: 2, editable: editable, tooltipField: "notes",
          cellEditor: "agLargeTextCellEditor", cellEditorPopup: true, cellEditorParams: { maxLength: 4000 } },
        { field: "sent",       headerName: "Sent",     flex: 1, valueFormatter: (p) => formatInstant(p.value) },
        { field: "received",   headerName: "Received", flex: 1, valueFormatter: (p) => formatInstant(p.value) },
    ];

    return (
        <Stack gap={0} className={props.className === undefined ? "view-container" : "view-container " + props.className}
               style={{ height: props.height ?? "calc(100vh - var(--app-header-height, 0px))", minHeight: 0, display: "flex", flexDirection: "column", position: "relative", ...props.style }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap"
                   style={{ backgroundColor: dark ? "var(--mantine-color-dark-6)" : "var(--mantine-color-gray-3)", flexShrink: 0, alignItems: "center", height: 64 }}>
                {toolbarButton("Refresh Feedback","Refresh",refreshIcon,() => setReload(previous => previous + 1),loading)}
                {editable && toolbarButton("Save Changes","Save",saveIcon,save,loading || changes.length === 0)}
                {editable && toolbarButton("Undo Changes","Undo",undoIcon,() => setRows(original),loading || changes.length === 0)}
                {changes.length > 0 && <Text size="sm" c="dimmed">{changes.length} unsaved change(s)</Text>}
            </Group>
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="sm" my={6}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            {loading &&
            <div style={{ position: "absolute", inset: 0, display: "flex",
                          alignItems: "center", justifyContent: "center", zIndex: 10 }}>
                <Loader size="lg" />
            </div>}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                <AgGridReact<IFeedbackRow>
                    theme={gridTheme}
                    loading={loading}
                    overlayLoadingTemplate="Retrieving Feedback..."
                    overlayNoRowsTemplate="No feedback"
                    rowData={rows}
                    columnDefs={columnDefs}
                    defaultColDef={{ sortable: true, resizable: true, filter: true }}
                    getRowId={(params) => params.data.rowKey}
                    onCellValueChanged={handleCellValueChanged}
                    tooltipShowDelay={400}
                />
            </div>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
