//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Client
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                          from "react";
import type { CSSProperties, ReactElement }              from "react";
import { Alert, Stack, Group, useComputedColorScheme }   from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }       from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { ClientData, CreateClientData, UpdateClientData } from "granite.client.service/RequestReply";
import type { ClientOwnerData, ClientsInput, ClientsOutput, GetClientsOutputReply,
              HandleClientsInputReply, RoleData }       from "granite.frontendcontroller.service/RequestReply";
import { canEdit, useAuthentication }                    from "granite.web.authentication/Authentication";
import { ClientsEditableView }                           from "../EditableView";
import type { IClientsResult }                           from "../EditableView";
import { toResult, toReveal }                            from "./ClientsOutputs";
import { SecretDialog }                                  from "./SecretDialog";
import type { ISecretReveal }                            from "./SecretDialog";

const CLIENTS_SOURCE:      string = "Client.All";
const CLIENTS_DESTINATION: string = "ClientsEditableView";
const CLIENTS_SERVICE:     string = "frontendcontroller-user-service";

export interface IClientsViewProps
{
    readonly service?:   string;
    readonly height?:    string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function ClientsView(props: IClientsViewProps): ReactElement
{
    const service: string                 = props.service ?? CLIENTS_SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const editable: boolean               = canEdit(state);
    const dark: boolean                   = useComputedColorScheme("light") === "dark";
    const [clients, setClients]           = useState<Array<ClientData>>([]);
    const [roles, setRoles]               = useState<Array<RoleData>>([]);
    const [users, setUsers]               = useState<Array<ClientOwnerData>>([]);
    const [loading, setLoading]           = useState<boolean>(true);
    const [loadError, setLoadError]       = useState<string | null>(null);
    const [toolbar, setToolbar]           = useState<ReactElement | null>(null);
    const [reveal, setReveal]             = useState<ISecretReveal | null>(null);

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    async function sendInput(command: string,fields: Partial<ClientsInput>): Promise<IClientsResult>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input:  ClientsInput                            =
            {
                userId:            null,
                source:            CLIENTS_SOURCE,
                command:           command,
                destination:       CLIENTS_DESTINATION,
                clientsToCreate:   [],
                clientsToUpdate:   [],
                clientIdsToDelete: [],
                ...fields
            };

        try
        {
            const reply: HandleClientsInputReply =
                await client.handleClientsInput(HandleInputRequestBuilder.newBuilder<ClientsInput>().setInput(input).build());

            if (reply.success === false)
                throw new Error(reply.failureMessage ?? "Request failed");

            const output: ClientsOutput | undefined = reply.output;

            if (!output)
                throw new Error("Empty response");

            const revealed: ISecretReveal | null = toReveal(output);

            if (revealed !== null)
                setReveal(revealed);

            setUsers(output.users ?? []);

            return toResult(output);
        }
        finally
        {
            client.close();
        }
    }

    const handleSave = (changes: { clientsToCreate: Array<CreateClientData>; clientsToUpdate: Array<UpdateClientData> }): Promise<IClientsResult> =>
        sendInput("Save",changes);

    const handleDelete = (clientIds: Array<string>): Promise<IClientsResult> =>
        sendInput("Delete",{ clientIdsToDelete: clientIds });

    const handleRotate = (clientId: string): Promise<IClientsResult> =>
        sendInput("Rotate",{ clientIdToRotate: clientId });

    useEffect(() =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();

        client
            .getClientsOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setSource(CLIENTS_SOURCE)
                    .setDestination(CLIENTS_DESTINATION)
                    .build())
            .thenApply((reply: GetClientsOutputReply) =>
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
                        setUsers(data.users ?? []);

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
                return {} as GetClientsOutputReply;
            })
            .subscribe(undefined,(error: Error) =>
            {
                setLoadError(error?.message ?? "Failed to load clients");
                setLoading(false);
            });

        return () => { client.close(); };
    },[]);

    return (
        <Stack
            gap={0}
            className={props.className === undefined ? "view-container" : "view-container " + props.className}
            style={{ height: props.height ?? "100%", minHeight: 0, display: "flex", flexDirection: "column", ...props.style }}
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
                    users={users}
                    editable={editable}
                    showOwners={true}
                    loading={loading}
                    onRenderToolbar={setToolbar}
                    onSave={handleSave}
                    onDelete={handleDelete}
                    onRotate={editable ? handleRotate : undefined}
                />
            </div>
            <SecretDialog reveal={reveal} onClose={() => setReveal(null)} />
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
