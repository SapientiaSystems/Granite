//////////////////////////////////////////////////////////////////////////////
// Granite.Web.ClientHost
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement } from "react";
import { Outlet }            from "react-router-dom";
import { AppShell, Group, Text } from "@mantine/core";
import { UserMenu }          from "granite.web.authentication/Login";

export function ClientHostFrame(): ReactElement
{
    return (
        <AppShell
            header={{ height: 56 }}
            padding="md"
            withBorder={false}
        >
            <AppShell.Header style={{ background: "var(--app-bar-bg, #2e2e2e)" }}>
                <Group h="100%" px="md">
                    <Text fw={700} size="lg" c="var(--mantine-color-graniteBrand-7)">
                        Granite Clients
                    </Text>
                    <UserMenu preferencesPath="/preferences" />
                </Group>
            </AppShell.Header>

            <AppShell.Main>
                <Outlet />
            </AppShell.Main>
        </AppShell>
    );
}

//////////////////////////////////////////////////////////////////////////////
