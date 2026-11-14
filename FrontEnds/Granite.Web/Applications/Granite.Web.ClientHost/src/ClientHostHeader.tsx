//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement } from "react";
import { Group, Text }       from "@mantine/core";
import { UserMenu }          from "granite.web.authentication/Login";

export function ClientHostHeader(): ReactElement
{
    return (
        <Group h={56} px="md" style={{ background: "var(--app-bar-bg, #2e2e2e)" }}>
            <Text fw={700} size="lg" c="var(--mantine-color-graniteBrand-7)">
                Granite Clients
            </Text>
            <UserMenu loginPath="/login" />
        </Group>
    );
}

//////////////////////////////////////////////////////////////////////////////
