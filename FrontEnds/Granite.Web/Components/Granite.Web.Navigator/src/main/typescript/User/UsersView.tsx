//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }               from "react";
import type { ReactElement }                        from "react";
import { Alert, Stack, Group }                      from "@mantine/core";
import { UsersFrontEndControllerServiceClient }    from "granite.frontendcontroller.client/RequestReply";
import type { IUsersFrontEndControllerServiceClient } from "granite.frontendcontroller.client/RequestReply";
import { GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetUsersOutputReply, HandleUsersInputReply, UsersOutput } from "granite.frontendcontroller.service/RequestReply";
import type { UsersInput }                             from "granite.frontendcontroller.service/RequestReply";
import type { UserData, CreateUserData, UpdateUserData } from "granite.user.service/RequestReply";
import { UsersEditableView }                        from "granite.web.user/EditableView";
import type { IUsersResult }                        from "granite.web.user/EditableView";
import { canEdit, useAuthentication }                   from "granite.web.authentication/Authentication";

const USERS_SOURCE:      string = "User.All";
const USERS_DESTINATION: string = "UsersEditableView";
const USERS_SERVICE:     string = "frontendcontroller-user-service";

export function UsersView(): ReactElement
{
    const { state, createFrontEndClient } = useAuthentication();
    const editable: boolean             = canEdit(state);
    const [users, setUsers]             = useState<Array<UserData>>([]);
    const [loading, setLoading]         = useState<boolean>(true);
    const [loadError, setLoadError]     = useState<string | null>(null);
    const [toolbar, setToolbar]         = useState<ReactElement | null>(null);
    const currentUserId: number | undefined = users.find(user => user.userName === state.userName)?.userId;
    const currentUserIdRef                  = useRef<number | undefined>(currentUserId);

    currentUserIdRef.current = currentUserId;

    const createClient = (): IUsersFrontEndControllerServiceClient =>
        createFrontEndClient((url,fetchFunction) => new UsersFrontEndControllerServiceClient(url,fetchFunction),USERS_SERVICE);

    const sendInput = (command: string, usersToCreate: Array<CreateUserData>, usersToUpdate: Array<UpdateUserData>, userIdsToDelete: Array<number>): Promise<IUsersResult> =>
    {
        const client: IUsersFrontEndControllerServiceClient = createClient();
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
        const service: IUsersFrontEndControllerServiceClient = createClient();

        service
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

        return () => { service.close(); };
    }, []);

    return (
        <Stack gap={0} className="view-container">
            {loadError && <Alert color="red" mx="sm" mt="xs">{loadError}</Alert>}
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbar}
            </Group>
            <div style={{ flex: 1, minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
                <UsersEditableView
                    users={users}
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
