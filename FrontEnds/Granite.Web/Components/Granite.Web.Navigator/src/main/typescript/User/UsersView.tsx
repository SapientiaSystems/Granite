//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }               from "react";
import type { ReactElement }                        from "react";
import { Alert, Stack, Group }                      from "@mantine/core";
import type { IFrontEndControllerServiceClient }        from "strata.frontendcontroller.client/RequestReply";
import { FrontEndOutputMapper, GetOutputRequestBuilder, HandleInputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetOutputReply, HandleInputReply }    from "strata.frontendcontroller.service/RequestReply";
import type { UsersInput, UsersOutput }             from "granite.frontendcontroller.service/RequestReply";
import type { UserData, CreateUserData, UpdateUserData } from "granite.user.service/RequestReply";
import { Optional }                                 from "strata.foundation.core/Utility";
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

    const createClient = (): IFrontEndControllerServiceClient => createFrontEndClient(USERS_SERVICE);

    const sendInput = (command: string, usersToCreate: Array<CreateUserData>, usersToUpdate: Array<UpdateUserData>, userIdsToDelete: Array<number>): Promise<IUsersResult> =>
    {
        const client: IFrontEndControllerServiceClient = createClient();
        const mapper: FrontEndOutputMapper              = FrontEndOutputMapper.of();
        const input:  UsersInput                        =
            {
                userId:      currentUserIdRef.current ?? 0,
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
                .handleInput(HandleInputRequestBuilder.newBuilder().setInput(input).build())
                .thenApply((reply: HandleInputReply) =>
                {
                    client.close();

                    if (reply.success === false)
                    {
                        reject(new Error(reply.failureMessage ?? "Request failed"));
                        return reply;
                    }

                    const output: Optional<UsersOutput> = mapper.mapFromReply<UsersOutput>(reply);

                    if (!output.isPresent())
                    {
                        reject(new Error("Empty response"));
                        return reply;
                    }

                    resolve({ users: output.get().users ?? [], failureMessages: output.get().failureMessages ?? [] });
                    return reply;
                })
                .exceptionally((error: Error) =>
                {
                    client.close();
                    reject(error);
                    return {} as HandleInputReply;
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
        const service: IFrontEndControllerServiceClient = createClient();
        const mapper:  FrontEndOutputMapper              = FrontEndOutputMapper.of();

        service
            .getOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setUserId(0)
                    .setSource(USERS_SOURCE)
                    .setDestination(USERS_DESTINATION)
                    .build())
            .thenApply((reply: GetOutputReply) =>
            {
                if (reply.success === false)
                    setLoadError(reply.failureMessage ?? "Failed to load users");
                else
                {
                    const output: Optional<UsersOutput> = mapper.mapFromReply<UsersOutput>(reply);

                    output.ifPresent(data =>
                    {
                        setUsers(data.users ?? []);

                        if ((data.failureMessages ?? []).length > 0)
                            setLoadError(data.failureMessages.join("; "));
                    });
                }

                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                console.error("Failed to get Users output", error);
                setLoadError(error?.message ?? "Failed to load users");
                setLoading(false);
                return {} as GetOutputReply;
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
