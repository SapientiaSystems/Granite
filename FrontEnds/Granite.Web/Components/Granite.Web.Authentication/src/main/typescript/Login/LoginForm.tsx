//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useState }                                   from "react";
import type { FormEvent, ReactElement }                          from "react";
import { Alert, Button, Divider, Loader, PasswordInput, Stack,
         Text, TextInput }                                       from "@mantine/core";
import { useAuthentication }                                     from "../Authentication/AuthenticationContext";
import type { ILoginMethods }                                    from "../Authentication/ISessionService";

export interface ILoginFormProps
{
    returnTo:    string;
    onLoggedIn?: () => void;
}

export function LoginForm({ returnTo, onLoggedIn }: ILoginFormProps): ReactElement
{
    const { getLoginMethods, login, startOktaLogin } = useAuthentication();
    const [methods, setMethods]                      = useState<ILoginMethods | null>(null);
    const [loadFailed, setLoadFailed]                = useState<boolean>(false);
    const [userName, setUserName]                    = useState<string>("");
    const [password, setPassword]                    = useState<string>("");
    const [error, setError]                          = useState<string | null>(null);
    const [busy, setBusy]                            = useState<boolean>(false);

    useEffect(() =>
    {
        let active: boolean = true;

        getLoginMethods().then(
            (loaded: ILoginMethods) => { if (active) setMethods(loaded); },
            () => { if (active) setLoadFailed(true); });

        return () => { active = false; };
    },[getLoginMethods]);

    const attempt = async (action: () => Promise<void>): Promise<void> =>
    {
        setError(null);
        setBusy(true);

        try
        {
            await action();
            onLoggedIn?.();
        }
        catch (failure)
        {
            setError(failure instanceof Error ? failure.message : "Login failed");
        }
        finally
        {
            setBusy(false);
        }
    };

    const onSubmit = (event: FormEvent<HTMLFormElement>): void =>
    {
        event.preventDefault();
        void attempt(() => login(userName,password));
    };

    if (loadFailed)
        return <Alert color="red" role="alert">Unable to load login options</Alert>;

    if (!methods)
        return <Loader aria-label="Loading login options" />;

    if (!methods.passwordEnabled && !methods.oktaEnabled)
        return <Text>No login method is enabled.</Text>;

    return (
        <Stack gap="md">
            {error && <Alert color="red" role="alert">{error}</Alert>}
            {methods.passwordEnabled &&
                <form onSubmit={onSubmit}>
                    <Stack gap="md">
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
                            Login
                        </Button>
                    </Stack>
                </form>}
            {methods.passwordEnabled && methods.oktaEnabled && <Divider label="or" labelPosition="center" />}
            {methods.oktaEnabled &&
                <Button variant="default" loading={busy} onClick={() => { void attempt(() => startOktaLogin(returnTo)); }}>
                    Login with Okta
                </Button>}
        </Stack>
    );
}

//////////////////////////////////////////////////////////////////////////////
