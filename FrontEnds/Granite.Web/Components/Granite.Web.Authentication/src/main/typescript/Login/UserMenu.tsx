//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }                   from "react";
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
}

export function UserMenu(props: IUserMenuProps): ReactElement
{
    const { preferencesPath, items }  = props;
    const loginPath: string           = props.loginPath ?? "/login";
    const { state, logout }           = useAuthentication();
    const navigate                    = useNavigate();
    const location                    = useLocation();
    const loggedIn: boolean           = state.status === "authenticated" || state.status === "reauthenticating";
    const active: boolean             = preferencesPath !== undefined && location.pathname.startsWith(preferencesPath);

    return (
        <Menu shadow="md" width={200}>
            <Menu.Target>
                <UnstyledButton
                    className={active ? "app-bar-user-menu active" : "app-bar-user-menu"}
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
                {preferencesPath !== undefined &&
                    <Menu.Item onClick={() => navigate(preferencesPath)}>Preferences</Menu.Item>}
                {(items ?? []).map((item: IUserMenuItem) =>
                    <Menu.Item key={item.label} onClick={item.onClick}>{item.label}</Menu.Item>)}
                {loggedIn
                    ? <Menu.Item onClick={() => { void logout().then((done: boolean) => { if (done) navigate(loginPath); }); }}>Log out</Menu.Item>
                    : <Menu.Item onClick={() => navigate(loginPath)}>Log in</Menu.Item>}
            </Menu.Dropdown>
        </Menu>
    );
}

//////////////////////////////////////////////////////////////////////////////
