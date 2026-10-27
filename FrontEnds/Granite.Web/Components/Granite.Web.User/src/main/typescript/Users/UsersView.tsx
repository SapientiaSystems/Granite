//////////////////////////////////////////////////////////////////////////////
// Granite.Web.User
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }                from "react";
import type { CSSProperties, ReactElement }            from "react";
import { Alert, Stack, Group, useComputedColorScheme } from "@mantine/core";
import { GraniteFrontEndControllerServiceClient }    from "granite.frontendcontroller.client/RequestReply";
import type { IGraniteFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetUsersOutputReply, HandleUsersInputReply, RoleData, UsersOutput } from "granite.frontendcontroller.service/RequestReply";
import type { UsersInput }                             from "granite.frontendcontroller.service/RequestReply";
import type { UserData, CreateUserData, UpdateUserData } from "granite.user.service/RequestReply";
import { UsersEditableView }                        from "../EditableView";
import type { IUsersResult }                        from "../EditableView";
import { canEdit, useAuthentication }                   from "granite.web.authentication/Authentication";

const USERS_SOURCE:      string = "User.All";
const USERS_DESTINATION: string = "UsersEditableView";
const USERS_SERVICE:     string = "frontendcontroller-user-service";

export interface IUsersViewProps
{
    readonly service?:   string;
    readonly height?:    string;
    readonly className?: string;
    readonly style?:     CSSProperties;
}

export function UsersView(props: IUsersViewProps): ReactElement
{
    const service: string                 = props.service ?? USERS_SERVICE;
    const { state, createFrontEndClient } = useAuthentication();
    const editable: boolean             = canEdit(state);
    const dark: boolean                 = useComputedColorScheme("light") === "dark";
    const [users, setUsers]             = useState<Array<UserData>>([]);
    const [roles, setRoles]             = useState<Array<RoleData>>([]);
    const [loading, setLoading]         = useState<boolean>(true);
    const [loadError, setLoadError]     = useState<string | null>(null);
    const [toolbar, setToolbar]         = useState<ReactElement | null>(null);
    const currentUserId: number | undefined = users.find(user => user.userName === state.userName)?.userId;
    const currentUserIdRef                  = useRef<number | undefined>(currentUserId);

    currentUserIdRef.current = currentUserId;

    const createClient = (): IGraniteFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new GraniteFrontEndControllerServiceClient(url,fetchFunction),service);

    const sendInput = (command: string, usersToCreate: Array<CreateUserData>, usersToUpdate: Array<UpdateUserData>, userIdsToDelete: Array<number>): Promise<IUsersResult> =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();
        const input:  UsersInput                        =
            {
                userId:      currentUserIdRef.current ?? null,
                source:      USERS_SOURCE,
                command:     command,
                destination: USERS_DESTINATION,
                usersToCreate,
                usersToUpdate,
                userIdsToDelete
            };

        return new Promise<IUsersResult>((resolve, reject) =>
        {
            client
                .handleUsersInput(HandleInputRequestBuilder.newBuilder<UsersInput>().setInput(input).build())
                .thenApply((reply: HandleUsersInputReply) =>
                {
                    client.close();

                    if (reply.success === false)
                    {
                        reject(new Error(reply.failureMessage ?? "Request failed"));
                        return reply;
                    }

                    const output: UsersOutput | undefined = reply.output;

                    if (!output)
                    {
                        reject(new Error("Empty response"));
                        return reply;
                    }

                    resolve({ users: output.users ?? [], failureMessages: output.failureMessages ?? [] });
                    return reply;
                })
                .exceptionally((error: Error) =>
                {
                    client.close();
                    reject(error);
                    return {} as HandleUsersInputReply;
                })
                .subscribe(undefined, (error: Error) =>
                {
                    client.close();
                    reject(error);
                });
        });
    };

    const handleSave = (changes: { usersToCreate: Array<CreateUserData>; usersToUpdate: Array<UpdateUserData> }): Promise<IUsersResult> =>
        sendInput("Save", changes.usersToCreate, changes.usersToUpdate, []);

    const handleDelete = (userIds: Array<number>): Promise<IUsersResult> =>
        sendInput("Delete", [], [], userIds);

    useEffect(() =>
    {
        const client: IGraniteFrontEndControllerServiceClient = createClient();

        client
            .getUsersOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setSource(USERS_SOURCE)
                    .setDestination(USERS_DESTINATION)
                    .build())
            .thenApply((reply: GetUsersOutputReply) =>
            {
                if (reply.success === false)
                    setLoadError(reply.failureMessage ?? "Failed to load users");
                else
                {
                    const data: UsersOutput | undefined = reply.output;

                    if (data)
                    {
                        setUsers(data.users ?? []);
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
                console.error("Failed to get Users output", error);
                setLoadError(error?.message ?? "Failed to load users");
                setLoading(false);
                return {} as GetUsersOutputReply;
            })
            .subscribe(undefined, (error: Error) =>
            {
                setLoadError(error?.message ?? "Failed to load users");
                setLoading(false);
            });

        return () => { client.close(); };
    }, []);

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
                <UsersEditableView
                    users={users}
                    roles={roles}
                    loading={loading}
                    height="grow"
                    onRenderToolbar={setToolbar}
                    showCreate={editable}
                    showEdit={editable}
                    showSave={editable}
                    showUndo={editable}
                    showDelete={editable}
                    currentUserId={currentUserId}
                    onSave={handleSave}
                    onDelete={handleDelete}
                />
            </div>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
