//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { CSSProperties, ReactElement }     from "react";
import { useLocation, useNavigate }            from "react-router-dom";
import { ActionIcon, Group, Menu, Text,
         UnstyledButton }                      from "@mantine/core";
import { useAuthentication }                   from "../Authentication/AuthenticationContext";

export interface IUserMenuItem
{
    label:   string;
    onClick: () => void;
}

export interface IUserMenuProps
{
    preferencesPath?: string;
    loginPath?:       string;
    items?:           Array<IUserMenuItem>;
    className?:       string;
    style?:           CSSProperties;
}

export function UserMenu(props: IUserMenuProps): ReactElement
{
    const { preferencesPath, items }  = props;
    const loginPath: string           = props.loginPath ?? "/login";
    const { state, logout }           = useAuthentication();
    const navigate                    = useNavigate();
    const location                    = useLocation();
    const loggedIn: boolean           = state.status === "authenticated" || state.status === "reauthenticating";
    const restoring: boolean          = state.status === "restoring";
    const pathname: string            = location.pathname;
    const active: boolean             = preferencesPath !== undefined && (pathname === preferencesPath || pathname.startsWith(preferencesPath + "/"));
    const classes: string             = ["app-bar-user-menu", active ? "active" : "", props.className ?? ""].filter((name: string) => name !== "").join(" ");

    return (
        <Menu shadow="md" width={200}>
            <Menu.Target>
                <UnstyledButton
                    className={classes}
                    style={{
                        marginLeft:   "auto",
                        display:      "flex",
                        alignItems:   "center",
                        borderBottom: "var(--user-menu-border-width, 3px) solid " + (active ? "var(--user-menu-active-color, var(--mantine-primary-color-filled))" : "var(--user-menu-border-color, transparent)"),
                        ...props.style
                    }}
                >
                    <Group gap={2} align="center">
                        <ActionIcon
                            variant="subtle"
                            color="gray"
                            title="User menu"
                            aria-label="User menu"
                            size="lg"
                            style={{ color: "inherit" }}
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24"
                                 fill="none" stroke="currentColor" strokeWidth="1" strokeLinecap="round" strokeLinejoin="round">
                                <circle cx="12" cy="12" r="10" />
                                <circle cx="12" cy="8" r="3" />
                                <path d="M6 20.5c0-3 2.7-5.5 6-5.5s6 2.5 6 5.5" />
                            </svg>
                        </ActionIcon>
                        <Text size="sm" fw={500}>
                            {restoring ? "" : loggedIn ? state.userName : "Login"}
                        </Text>
                    </Group>
                </UnstyledButton>
            </Menu.Target>
            <Menu.Dropdown>
                {restoring
                    ? null
                    : loggedIn
                    ? <Menu.Item onClick={() => { void logout().then((done: boolean) => { if (done) navigate(loginPath); }); }}>Logout</Menu.Item>
                    : <Menu.Item onClick={() => navigate(loginPath)}>Login</Menu.Item>}
                {preferencesPath !== undefined &&
                    <Menu.Item onClick={() => navigate(preferencesPath)}>Preferences</Menu.Item>}
                {(items ?? []).map((item: IUserMenuItem, index: number) =>
                    <Menu.Item key={`${index}-${item.label}`} onClick={item.onClick}>{item.label}</Menu.Item>)}
            </Menu.Dropdown>
        </Menu>
    );
}

//////////////////////////////////////////////////////////////////////////////
