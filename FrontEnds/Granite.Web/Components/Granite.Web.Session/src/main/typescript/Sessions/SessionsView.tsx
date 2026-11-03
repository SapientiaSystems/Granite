//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Session
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                         from "react";
import type { CSSProperties, ReactElement }            from "react";
import { Alert, Stack, Group, useComputedColorScheme } from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }      from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetSessionsOutputReply, HandleSessionsInputReply,
              RoleData, SessionRowData, SessionsInput, SessionsOutput } from "granite.frontendcontroller.service/RequestReply";
import { canEdit, useAuthentication }                  from "granite.web.authentication/Authentication";
import { SessionsGrid }                                from "../View";
import type { ISessionsResult }                        from "../View";

const SOURCE_OPEN:  string = "Session.Open";
const SOURCE_ALL:   string = "Session.All";
const DESTINATION:  string = "SessionsView";
const SERVICE:      string = "frontendcontroller-user-service";

export interface ISessionsViewProps
{
    readonly service?:   string;
    readonly height?:    string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function SessionsView(props: ISessionsViewProps): ReactElement
{
    const service: string                     = props.service ?? SERVICE;
    const { state, createFrontEndClient }     = useAuthentication();
    const editable: boolean                   = canEdit(state);
    const dark: boolean                       = useComputedColorScheme("light") === "dark";
    const [sessions, setSessions]             = useState<Array<SessionRowData>>([]);
    const [currentSessionId, setCurrentId]    = useState<number | null>(null);
    const [roles, setRoles]                   = useState<Array<RoleData>>([]);
    const [includeClosed, setIncludeClosed]   = useState<boolean>(false);
    const [loading, setLoading]               = useState<boolean>(true);
    const [loadError, setLoadError]           = useState<string | null>(null);
    const [reload, setReload]                 = useState<number>(0);
    const [toolbar, setToolbar]               = useState<ReactElement | null>(null);

    const source: string = includeClosed ? SOURCE_ALL : SOURCE_OPEN;

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    const apply = (output: SessionsOutput): void =>
    {
        setSessions(output.sessions ?? []);
        setCurrentId(output.currentSessionId ?? null);
        setRoles(output.roles ?? []);
    };

    const handleEnd = (sessionIds: Array<number>): Promise<ISessionsResult> =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input:  SessionsInput                           =
            {
                userId:            null,
                source:            source,
                command:           "Close",
                destination:       DESTINATION,
                sessionIdsToClose: sessionIds
            } as SessionsInput;

        return new Promise<ISessionsResult>((resolve, reject) =>
        {
            client
                .handleSessionsInput(HandleInputRequestBuilder.newBuilder<SessionsInput>().setInput(input).build())
                .thenApply((reply: HandleSessionsInputReply) =>
                {
                    client.close();

                    if (reply.success === false)
                    {
                        reject(new Error(reply.failureMessage ?? "Request failed"));
                        return reply;
                    }

                    const output: SessionsOutput | undefined = reply.output;

                    if (!output)
                    {
                        reject(new Error("Empty response"));
                        return reply;
                    }

                    apply(output);
                    resolve({ sessions: output.sessions ?? [], failureMessages: output.failureMessages ?? [] });
                    return reply;
                })
                .exceptionally((error: Error) =>
                {
                    client.close();
                    reject(error);
                    return {} as HandleSessionsInputReply;
                })
                .subscribe(undefined, (error: Error) =>
                {
                    client.close();
                    reject(error);
                });
        });
    };

    useEffect(() =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();

        setLoading(true);
        setLoadError(null);

        client
            .getSessionsOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setSource(source)
                    .setDestination(DESTINATION)
                    .build())
            .thenApply((reply: GetSessionsOutputReply) =>
            {
                if (reply.success === false)
                    setLoadError(reply.failureMessage ?? "Failed to load sessions");
                else
                {
                    const data: SessionsOutput | undefined = reply.output;

                    if (data)
                    {
                        apply(data);

                        if ((data.failureMessages ?? []).length > 0)
                            setLoadError(data.failureMessages.join("; "));
                    }
                }

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                console.error("Failed to get Sessions output", error);
                setLoadError(error?.message ?? "Failed to load sessions");
                setLoading(false);
                return {} as GetSessionsOutputReply;
            })
            .subscribe(undefined, (error: Error) =>
            {
                setLoadError(error?.message ?? "Failed to load sessions");
                setLoading(false);
            });

        return () => { client.close(); };
    }, [source, reload]);

    return (
        <Stack
            gap={0}
            className={props.className === undefined ? "view-container" : "view-container " + props.className}
            style={{ height: props.height ?? "calc(100vh - var(--app-header-height, 0px))", minHeight: 0, display: "flex", flexDirection: "column", ...props.style }}
        >
            {loadError && <Alert color="red" mx="sm" mt="xs" withCloseButton onClose={() => setLoadError(null)}>{loadError}</Alert>}
            <Group
                gap="sm"
                className="view-toolbar"
                px="sm"
                py={6}
                justify="flex-start"
                wrap="nowrap"
                style={{ backgroundColor: dark ? "var(--mantine-color-dark-6)" : "var(--mantine-color-gray-3)", flexShrink: 0, alignItems: "center", height: 64 }}
            >
                {toolbar}
            </Group>
            <div style={{ flex: 1, minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
                <SessionsGrid
                    sessions={sessions}
                    currentSessionId={currentSessionId}
                    roles={roles}
                    loading={loading}
                    includeClosed={includeClosed}
                    showEnd={editable}
                    height="grow"
                    onRenderToolbar={setToolbar}
                    onIncludeClosedChange={setIncludeClosed}
                    onRefresh={() => setReload(previous => previous + 1)}
                    onEnd={handleEnd}
                />
            </div>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
