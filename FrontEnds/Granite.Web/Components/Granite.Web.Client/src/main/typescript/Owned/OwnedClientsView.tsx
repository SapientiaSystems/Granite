//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }                  from "react";
import type { CSSProperties, ReactElement }              from "react";
import { Alert, Stack, Group, useComputedColorScheme }   from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }       from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { ClientData }                               from "granite.client.service/RequestReply";
import type { ClientsOutput, GetOwnedClientsOutputReply, HandleOwnedClientSecretInputReply,
              OwnedClientSecretInput, RoleData }         from "granite.frontendcontroller.service/RequestReply";
import { useAuthentication }                             from "granite.web.authentication/Authentication";
import { ClientsEditableView }                           from "../EditableView";
import type { IClientsResult }                           from "../EditableView";
import { toResult, toReveal }                            from "../Clients/ClientsOutputs";
import { SecretDialog }                                  from "../Clients/SecretDialog";
import type { ISecretReveal }                            from "../Clients/SecretDialog";

const OWNED_SOURCE:      string = "Client.Owned";
const OWNED_DESTINATION: string = "OwnedClientsView";
const OWNED_SERVICE:     string = "frontendcontroller-user-service";

export interface IOwnedClientsViewProps
{
    readonly service?:   string;
    readonly height?:    string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function OwnedClientsView(props: IOwnedClientsViewProps): ReactElement
{
    const service: string                 = props.service ?? OWNED_SERVICE;
    const { createFrontEndClient }        = useAuthentication();
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [clients, setClients]           = useState<Array<ClientData>>([]);
    const [roles, setRoles]               = useState<Array<RoleData>>([]);
    const [loading, setLoading]           = useState<boolean>(true);
    const [loadError, setLoadError]       = useState<string | null>(null);
    const [toolbar, setToolbar]           = useState<ReactElement | null>(null);
    const [reveal, setReveal]             = useState<ISecretReveal | null>(null);
    const closeLoad                       = useRef<(() => void) | null>(null);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    async function handleRotate(clientId: string): Promise<IClientsResult>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input:  OwnedClientSecretInput                  =
            { userId: null, source: OWNED_SOURCE, command: "Rotate", destination: OWNED_DESTINATION, clientId };

        try
        {
            const reply: HandleOwnedClientSecretInputReply =
                await client.handleOwnedClientSecretInput(HandleInputRequestBuilder.newBuilder<OwnedClientSecretInput>().setInput(input).build());

            if (reply.success === false)
                throw new Error(reply.failureMessage ?? "Request failed");

            const output: ClientsOutput | undefined = reply.output;

            if (!output)
                throw new Error("Empty response");

            const revealed: ISecretReveal | null = toReveal(output);

            if (revealed !== null)
                setReveal(revealed);

            return toResult(output);
        }
        finally
        {
            client.close();
        }
    }

    function load(): void
    {
        closeLoad.current?.();

        const client: IGraniteFrontEndControllerServiceClient = createClient();

        closeLoad.current = () => client.close();
        setLoading(true);
        setLoadError(null);

        client
            .getOwnedClientsOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setSource(OWNED_SOURCE)
                    .setDestination(OWNED_DESTINATION)
                    .build())
            .thenApply((reply: GetOwnedClientsOutputReply) =>
            {
                if (reply.success === false)
                    setLoadError(reply.failureMessage ?? "Failed to load clients");
                else
                {
                    const data: ClientsOutput | undefined = reply.output;

                    if (data)
                    {
                        setClients(data.clients ?? []);
                        setRoles(data.roles ?? []);

                        if ((data.failureMessages ?? []).length > 0)
                            setLoadError(data.failureMessages.join("; "));
                    }
                }

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                setLoadError(error?.message ?? "Failed to load clients");
                setLoading(false);
                return {} as GetOwnedClientsOutputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                setLoadError(error?.message ?? "Failed to load clients");
                setLoading(false);
            });
    }

    useEffect(() =>
    {
        load();

        return () => { closeLoad.current?.(); };
    },[]);

    return (
        <Stack
            gap={0}
            className={props.className === undefined ? "view-container" : "view-container " + props.className}
            style={{ height: props.height ?? "calc(100vh - var(--app-header-height, 0px))", minHeight: 0, display: "flex", flexDirection: "column", ...props.style }}
        >
            {loadError && <Alert color="red" mx="sm" mt="xs">{loadError}</Alert>}
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
                <ClientsEditableView
                    clients={clients}
                    roles={roles}
                    editable={false}
                    showOwners={false}
                    loading={loading}
                    onRenderToolbar={setToolbar}
                    onRotate={handleRotate}
                    onRefresh={load}
                />
            </div>
            <SecretDialog reveal={reveal} onClose={() => setReveal(null)} />
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
