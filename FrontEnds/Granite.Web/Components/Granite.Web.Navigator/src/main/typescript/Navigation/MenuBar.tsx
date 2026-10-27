//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Navigator
//////////////////////////////////////////////////////////////////////////////

import { useDisclosure }     from "@mantine/hooks";
import type { ReactElement } from "react";
import { NavLink, Outlet,
         useLocation,
         useNavigate }       from "react-router-dom";
import { AppShell, Burger, Group, Text, ActionIcon,
         Menu, UnstyledButton } from "@mantine/core";
import type { IMenuTab }     from "./IMenuTab";
import { useAuthentication } from "granite.web.authentication/Authentication";

const menuTabs: Array<IMenuTab> =
[
    { label: "Home",         path: "/" },
    { label: "Users",  path: "/users" },
    { label: "Help",         path: "/help" },
];

export function MenuBar(): ReactElement
{
    const [opened, { toggle, close }] = useDisclosure(false);
    const { state, logout }           = useAuthentication();
    const loggedIn: boolean           = state.status === "authenticated";
    const navigate                    = useNavigate();
    const location                    = useLocation();
    const isUserRouteActive: boolean  = location.pathname.startsWith("/user");

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
                    <Menu shadow="md" width={200}>
                        <Menu.Target>
                            <UnstyledButton
                                className={isUserRouteActive ? "app-bar-user-menu active" : "app-bar-user-menu"}
                                style={{ marginLeft: "auto" }}
                            >
                                <Group gap={2} align="center">
                                    <ActionIcon
                                        variant="subtle"
                                        color="gray"
                                        title="User menu"
                                        aria-label="User menu"
                                        size="lg"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                                             fill="none" stroke="white" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                                            <circle cx="12" cy="12" r="10" />
                                            <circle cx="12" cy="8" r="3" />
                                            <path d="M6 20.5c0-3 2.7-5.5 6-5.5s6 2.5 6 5.5" />
                                        </svg>
                                    </ActionIcon>
                                    <Text c="var(--accent)" size="sm" fw={500}>
                                        {loggedIn ? state.userName : "Log in"}
                                    </Text>
                                </Group>
                            </UnstyledButton>
                        </Menu.Target>
                        <Menu.Dropdown>
                            {loggedIn
                                ? <Menu.Item onClick={() => { void logout().then((done: boolean) => { if (done) navigate("/login"); }); }}>Logout</Menu.Item>
                                : <Menu.Item onClick={() => navigate("/login")}>Login</Menu.Item>}
                            <Menu.Item onClick={() => navigate("/user")}>
                                Manage Preferences
                            </Menu.Item>
                        </Menu.Dropdown>
                    </Menu>
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
