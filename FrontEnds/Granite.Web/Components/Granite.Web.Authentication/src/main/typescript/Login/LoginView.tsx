//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }          from "react";
import { Navigate, useLocation }      from "react-router-dom";
import { Center, Paper, Stack, Title } from "@mantine/core";
import { useAuthentication }          from "../Authentication/AuthenticationContext";
import { LoginForm }                  from "./LoginForm";

export function LoginView(): ReactElement
{
    const { state } = useAuthentication();
    const location  = useLocation();
    const target: string = (location.state as { from?: string } | null)?.from ?? "/";

    if (state.status === "authenticated")
        return <Navigate to={target} replace />;

    return (
        <Center py="xl">
            <Paper withBorder shadow="sm" p="lg" w={360}>
                <Stack gap="md">
                    <Title order={3}>Log in</Title>
                    <LoginForm returnTo={target} />
                </Stack>
            </Paper>
        </Center>
    );
}

//////////////////////////////////////////////////////////////////////////////
