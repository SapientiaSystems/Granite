//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                       from "react";
import type { ReactElement }                        from "react";
import { Stack, Group }                             from "@mantine/core";
import { FakeFrontEndControllerServiceClient }      from "granite.frontendcontroller.client/RequestReply";
import { FrontEndOutputMapper, GetOutputRequestBuilder } from "strata.frontendcontroller.service/RequestReply";
import type { GetOutputReply }                     from "strata.frontendcontroller.service/RequestReply";
import type { UsersOutput }                  from "granite.frontendcontroller.service/RequestReply";
import type { UserData }                     from "granite.user.service/RequestReply";
import { Optional }                                from "strata.foundation.core/Utility";
import { UsersEditableView }                 from "granite.web.user/EditableView";

export function UsersView(): ReactElement
{
    const [users, setUsers] = useState<Array<UserData>>([]);
    const [loading, setLoading]         = useState<boolean>(true);
    const [toolbar, setToolbar]         = useState<ReactElement | null>(null);

    useEffect(() =>
    {
        const service: FakeFrontEndControllerServiceClient = new FakeFrontEndControllerServiceClient();
        const mapper:  FrontEndOutputMapper                = FrontEndOutputMapper.of();

        service
            .getOutput(
                GetOutputRequestBuilder
                    .newBuilder()
                    .setUserId(1)
                    .setSource("User.All")
                    .setDestination("UsersEditableView")
                    .build())
            .thenApply((reply: GetOutputReply) =>
            {
                const output: Optional<UsersOutput> = mapper.mapFromReply<UsersOutput>(reply);

                output.ifPresent(data => setUsers(data.users ?? []));
                setLoading(false);
                return reply;
            })
            .exceptionally((error: Error) =>
            {
                console.error("Failed to get Users output", error);
                setLoading(false);
                return {} as GetOutputReply;
            })
            .subscribe();

        return () => { service.close(); };
    }, []);

    return (
        <Stack gap={0} className="view-container">
            <Group gap="sm" className="view-toolbar" px="sm" py={6} justify="flex-start" wrap="nowrap">
                {toolbar}
            </Group>
            <div style={{ flex: 1, minHeight: 0, overflow: "hidden", display: "flex", flexDirection: "column" }}>
                <UsersEditableView
                    users={users}
                    loading={loading}
                    height="grow"
                    onRenderToolbar={setToolbar}
                />
            </div>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
