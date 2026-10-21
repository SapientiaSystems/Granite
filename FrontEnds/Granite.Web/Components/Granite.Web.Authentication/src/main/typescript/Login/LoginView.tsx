//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useCallback, useState }        from "react";
import type { FormEvent, ReactElement } from "react";
import { useLocation, useNavigate }     from "react-router-dom";
import { Alert, Button, Center, Divider, Paper,
         PasswordInput, Stack, TextInput, Title } from "@mantine/core";
import { useAuthentication }  from "../Authentication/AuthenticationContext";
import { getRuntimeConfig }   from "../Authentication/RuntimeConfig";
import { GoogleSignInButton } from "./GoogleSignInButton";

export interface ILoginViewProps
{
    googleClientId?: string;
}

export function LoginView({ googleClientId }: ILoginViewProps): ReactElement
{
    const { state, login, loginWithGoogle } = useAuthentication();
    const navigate                          = useNavigate();
    const location                          = useLocation();
    const [userName, setUserName]           = useState<string>("");
    const [password, setPassword]           = useState<string>("");
    const [error, setError]                 = useState<string | null>(null);
    const clientId: string                  = googleClientId ?? getRuntimeConfig().googleClientId;
    const target: string                    = (location.state as { from?: string } | null)?.from ?? "/";
    const busy: boolean                     = state.status === "authenticating";

    const finish = useCallback(
        async (attempt: () => Promise<void>) =>
        {
            setError(null);

            try
            {
                await attempt();
                navigate(target,{ replace: true });
            }
            catch (failure)
            {
                setError(failure instanceof Error ? failure.message : "Sign-in failed");
            }
        },
        [navigate,target]);

    const onSubmit = (event: FormEvent<HTMLFormElement>): void =>
    {
        event.preventDefault();
        void finish(() => login(userName,password));
    };

    const onGoogleCredential = useCallback(
        (idToken: string) => { void finish(() => loginWithGoogle(idToken)); },
        [finish,loginWithGoogle]);

    const onGoogleError = useCallback((failure: Error) => setError(failure.message),[]);

    return (
        <Center py="xl">
            <Paper withBorder shadow="sm" p="lg" w={360}>
                <form onSubmit={onSubmit}>
                    <Stack gap="md">
                        <Title order={3}>Sign in</Title>
                        {error && <Alert color="red" role="alert">{error}</Alert>}
                        <TextInput
                            label="User name"
                            autoComplete="username"
                            required
                            value={userName}
                            onChange={(event) => setUserName(event.currentTarget.value)}
                        />
                        <PasswordInput
                            label="Password"
                            autoComplete="current-password"
                            required
                            value={password}
                            onChange={(event) => setPassword(event.currentTarget.value)}
                        />
                        <Button type="submit" loading={busy} disabled={!userName || !password}>
                            Sign in
                        </Button>
                        {clientId &&
                            <>
                                <Divider label="or" labelPosition="center" />
                                <GoogleSignInButton
                                    clientId={clientId}
                                    onCredential={onGoogleCredential}
                                    onError={onGoogleError}
                                />
                            </>}
                    </Stack>
                </form>
            </Paper>
        </Center>
    );
}

//////////////////////////////////////////////////////////////////////////////
