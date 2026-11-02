//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useDisclosure }     from "@mantine/hooks";
import type { ReactElement } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { AppShell, Burger, Group, Text } from "@mantine/core";
import type { IMenuTab }     from "./IMenuTab";
import { UserMenu }          from "granite.web.authentication/Login";

const menuTabs: Array<IMenuTab> =
[
    { label: "Home",         path: "/" },
    { label: "Users",  path: "/users" },
    { label: "Sessions",     path: "/sessions" },
    { label: "Help",         path: "/help" },
];

export function MenuBar(): ReactElement
{
    const [opened, { toggle, close }] = useDisclosure(false);

    return (
        <AppShell
            header={{ height: 56 }}
            navbar={{ width: 260, breakpoint: "sm", collapsed: { desktop: true, mobile: !opened } }}
            padding="md"
            withBorder={false}
        >
            <AppShell.Header style={{ background: "var(--app-bar-bg, #2e2e2e)" }}>
                <Group h="100%" px="md">
                    <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" color="white" />
                    <Text fw={700} size="lg" c="var(--mantine-color-graniteBrand-7)">
                        Granite
                    </Text>
                    <div style={{ width: 1, height: "40%", background: "rgba(255, 255, 255, 0.2)", marginInline: 8 }} />
                    <Group gap={4} visibleFrom="sm" h="100%">
                        {menuTabs.map((tab) => (
                            <NavLink
                                key={tab.path}
                                to={tab.path}
                                end={tab.path === "/"}
                                className={({ isActive }) =>
                                    isActive ? "app-bar-tab active" : "app-bar-tab"
                                }
                            >
                                {tab.label}
                            </NavLink>
                        ))}
                    </Group>
                    <UserMenu preferencesPath="/user" />
                </Group>
            </AppShell.Header>

            <AppShell.Navbar p="md" style={{ background: "var(--app-bar-bg, #2e2e2e)" }}>
                {menuTabs.map((tab) => (
                    <NavLink
                        key={tab.path}
                        to={tab.path}
                        end={tab.path === "/"}
                        className={({ isActive }) =>
                            isActive ? "nav-link active" : "nav-link"
                        }
                        onClick={close}
                    >
                        {tab.label}
                    </NavLink>
                ))}
            </AppShell.Navbar>

            <AppShell.Main>
                <Outlet />
            </AppShell.Main>
        </AppShell>
    );
}

//////////////////////////////////////////////////////////////////////////////
