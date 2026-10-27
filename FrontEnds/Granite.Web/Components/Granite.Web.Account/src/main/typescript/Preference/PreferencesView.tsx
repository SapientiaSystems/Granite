//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Account
//////////////////////////////////////////////////////////////////////////////

import { useEffect }                  from "react";
import type { ReactElement }          from "react";
import { Alert, Stack, Group, Paper, Title, Text,
         Badge, Switch }              from "@mantine/core";
import { useMantineColorScheme }      from "@mantine/core";
import type { IPreferencesViewProps } from "./IPreferencesViewProps";
import { usePreferences }             from "./PreferencesProvider";
import { usePreference }              from "./usePreference";
import { isTheme, writeCachedTheme }  from "./ThemeCache";
import type { Theme }                 from "./ThemeCache";

const sunIcon =
(
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="5" />
        <line x1="12" y1="1"  x2="12" y2="3" />
        <line x1="12" y1="21" x2="12" y2="23" />
        <line x1="4.22"  y1="4.22"  x2="5.64"  y2="5.64" />
        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78" />
        <line x1="1"  y1="12" x2="3"  y2="12" />
        <line x1="21" y1="12" x2="23" y2="12" />
        <line x1="4.22"  y1="19.78" x2="5.64"  y2="18.36" />
        <line x1="18.36" y1="5.64"  x2="19.78" y2="4.22" />
    </svg>
);

const moonIcon =
(
    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24"
         fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
    </svg>
);

export function PreferencesView(props: IPreferencesViewProps): ReactElement
{
    const { setColorScheme }                  = useMantineColorScheme();
    const { status }                          = usePreferences();
    const [stored, setTheme, themeState]      = usePreference<string>("granite.theme");
    const theme: Theme                        = isTheme(stored) ? stored : "light";

    useEffect(() =>
    {
        if (status === "ready")
        {
            setColorScheme(theme);
            writeCachedTheme(theme);
        }
    },[status,theme]);

    return (
        <Stack gap="lg" p="md" style={{ maxWidth: 520 }}>

            <Paper p="md" withBorder>
                <Title order={4} mb="sm">User</Title>
                <Group gap="xs">
                    <Text size="md" fw={600}>{props.userName}</Text>
                    <Badge variant="light" color="blue" size="lg">{props.userRole}</Badge>
                </Group>
            </Paper>

            <Paper p="md" withBorder>
                <Title order={4} mb="sm">Theme</Title>
                <Switch
                    checked={theme === "dark"}
                    onChange={() => void setTheme(theme === "dark" ? "light" : "dark")}
                    disabled={status !== "ready"}
                    size="lg"
                    color="dark.4"
                    onLabel={moonIcon}
                    offLabel={sunIcon}
                    label={theme === "dark" ? "Dark" : "Light"}
                />
                {themeState.error && <Alert color="red" mt="sm">{themeState.error}</Alert>}
            </Paper>

            {props.children}

        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
