//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }   from "react";
import { Group, Title, Stack } from "@mantine/core";
import { useAuthentication }   from "granite.web.authentication/Authentication";
import { PreferencesView }     from "granite.web.account/Preference";

export function ClientHostPreferencesView(): ReactElement
{
    const { state } = useAuthentication();

    return (
        <Stack gap={0} className="view-container">
            <Group gap="sm" className="view-toolbar" px="sm" py={6} />
            <Stack gap="md" p="md" style={{ flex: 1, minHeight: 0, overflowY: "auto" }}>
                <Title order={2}>User Preferences</Title>
                <PreferencesView
                    userName={state.userName ?? ""}
                    userRole="ClientAdmin"
                />
            </Stack>
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
