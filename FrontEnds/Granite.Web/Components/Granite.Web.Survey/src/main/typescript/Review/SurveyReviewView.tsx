//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Survey
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useState }                   from "react";
import type { CSSProperties, ReactElement }               from "react";
import { ActionIcon, Alert, Group, Stack, Text, Tooltip,
         useComputedColorScheme }                         from "@mantine/core";
import { AgGridReact }                                    from "ag-grid-react";
import { AllCommunityModule, ModuleRegistry, themeQuartz } from "ag-grid-community";
import type { ColDef, CellValueChangedEvent }             from "ag-grid-community";
import { GraniteFrontEndControllerServiceClient }         from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient }   from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetSurveyReviewOutputReply,
              HandleSurveyReviewInputReply,
              SurveyReviewInput, SurveyReviewOutput }     from "granite.frontendcontroller.service/RequestReply";
import type { SurveyQuestionData }                        from "granite.survey.service/RequestReply";
import { canEdit, useAuthentication }                     from "granite.web.authentication/Authentication";
import { questionKey, surveyQuestions }                   from "../Survey/SurveyForm";
import { SURVEY_STATUSES, averagesText, changedResponses, formatInstant, toSurveyRows } from "./SurveyRows";
import type { ISurveyRow }                                from "./SurveyRows";

ModuleRegistry.registerModules([AllCommunityModule]);

const SOURCE:      string = "Survey.All";
const DESTINATION: string = "SurveyReviewView";
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

export interface ISurveyReviewViewProps
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

export function SurveyReviewView(props: ISurveyReviewViewProps): ReactElement
{
    const service: string                 = props.service ?? SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const editable: boolean               = canEdit(state);
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [original, setOriginal]         = useState<Array<ISurveyRow>>([]);
    const [rows, setRows]                 = useState<Array<ISurveyRow>>([]);
    const [loading, setLoading]           = useState<boolean>(true);
    const [messages, setMessages]         = useState<Array<string>>([]);
    const [reload, setReload]             = useState<number>(0);
    const [questions, setQuestions]       = useState<Array<SurveyQuestionData>>(surveyQuestions(null));
    const [averages, setAverages]         = useState<Record<string,number>>({});
    const changes                         = useMemo(() => changedResponses(original,rows), [original,rows]);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    const apply = (output: SurveyReviewOutput | undefined): void =>
    {
        const next: Array<ISurveyRow> = toSurveyRows(output?.responses,output?.userNames);

        setQuestions(surveyQuestions(output?.questions));
        setAverages(output?.averages ?? {});

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
            .getSurveyReviewOutput(GetOutputRequestBuilder.newBuilder().setSource(SOURCE).setDestination(DESTINATION).build())
            .thenApply((reply: GetSurveyReviewOutputReply) =>
            {
                if (reply.success === false)
                    setMessages([reply.failureMessage ?? "Failed to load survey responses"]);
                else
                    apply(reply.output);

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                fail(error?.message ?? "Failed to load survey responses");
                return {} as GetSurveyReviewOutputReply;
            })
            .subscribe(undefined,(error: Error) => fail(error?.message ?? "Failed to load survey responses"));

        return () => { client.close(); };
    }, [reload]);

    function save(): void
    {
        if (changes.length === 0)
        {
            return;
        }

        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input: SurveyReviewInput =
            { userId: null, source: SOURCE, command: "Save", destination: DESTINATION, changes } as SurveyReviewInput;

        setLoading(true);
        client
            .handleSurveyReviewInput(HandleInputRequestBuilder.newBuilder<SurveyReviewInput>().setInput(input).build())
            .thenApply((reply: HandleSurveyReviewInputReply) =>
            {
                client.close();

                if (reply.success === false)
                    setMessages([reply.failureMessage ?? "Unable to save survey responses"]);
                else
                    apply(reply.output);

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                client.close();
                fail(error?.message ?? "Unable to save survey responses");
                return {} as HandleSurveyReviewInputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                client.close();
                fail(error?.message ?? "Unable to save survey responses");
            });
    }

    function handleCellValueChanged(event: CellValueChangedEvent<ISurveyRow>): void
    {
        const edited: ISurveyRow = event.data;

        setRows(previous => previous.map(row => row.rowKey === edited.rowKey ? { ...edited } : row));
    }

    const columnDefs: Array<ColDef<ISurveyRow>> =
    [
        { field: "responseId", headerName: "Id",       width: 90, filter: "agNumberColumnFilter" },
        { field: "created",    headerName: "Created",  flex: 1, sort: "desc", valueFormatter: (p) => formatInstant(p.value) },
        { field: "userName",   headerName: "User",     flex: 1 },
        ...questions.map((q): ColDef<ISurveyRow> =>
            ({ colId: "answer-" + questionKey(q), headerName: questionKey(q), headerTooltip: q.label, width: 120,
               filter: "agNumberColumnFilter", valueGetter: (p) => p.data?.answers[questionKey(q)] })),
        { field: "comment",    headerName: "Comment",  flex: 3, tooltipField: "comment" },
        { field: "status",     headerName: "Status",   width: 130, editable: editable,
          cellEditor: "agSelectCellEditor", cellEditorParams: { values: SURVEY_STATUSES } },
        { field: "notes",      headerName: "Notes",    flex: 2, editable: editable, tooltipField: "notes",
          cellEditor: "agLargeTextCellEditor", cellEditorPopup: true, cellEditorParams: { maxLength: 4000 } },
        { field: "sent",       headerName: "Sent",     flex: 1, valueFormatter: (p) => formatInstant(p.value) },
        { field: "received",   headerName: "Received", flex: 1, valueFormatter: (p) => formatInstant(p.value) },
    ];

    return (
        <Stack gap={0} className={props.className === undefined ? "view-container" : "view-container " + props.className}
               style={{ height: props.height ?? "calc(100vh - var(--app-header-height, 0px))", minHeight: 0, display: "flex", flexDirection: "column", ...props.style }}>
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap"
                   style={{ backgroundColor: dark ? "var(--mantine-color-dark-6)" : "var(--mantine-color-gray-3)", flexShrink: 0, alignItems: "center", height: 64 }}>
                {toolbarButton("Refresh Survey Responses","Refresh",refreshIcon,() => setReload(previous => previous + 1),loading)}
                {editable && toolbarButton("Save Changes","Save",saveIcon,save,loading || changes.length === 0)}
                {editable && toolbarButton("Undo Changes","Undo",undoIcon,() => setRows(original),loading || changes.length === 0)}
                {changes.length > 0 && <Text size="sm" c="dimmed">{changes.length} unsaved change(s)</Text>}
                {averagesText(questions,averages) !== "" && <Text size="sm">Averages: {averagesText(questions,averages)}</Text>}
            </Group>
            {messages.length > 0 &&
            <Alert color="red" withCloseButton onClose={() => setMessages([])} mx="sm" my={6}>
                {messages.map((m, i) => <div key={i}>{m}</div>)}
            </Alert>}
            <div className="ag-theme-quartz" style={{ flex: 1, minHeight: 0, width: "100%" }}>
                <AgGridReact<ISurveyRow>
                    theme={gridTheme}
                    loading={loading}
                    overlayLoadingTemplate="Retrieving Survey Responses..."
                    overlayNoRowsTemplate="No survey responses"
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
