//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useRef, useState }            from "react";
import type { ReactElement }                      from "react";
import { Link, useNavigate, useSearchParams }     from "react-router-dom";
import { Alert, Anchor, Center, Loader, Stack }   from "@mantine/core";
import { useAuthentication }                      from "../Authentication/AuthenticationContext";

export function LoginCallbackView(): ReactElement
{
    const { completeOktaLogin } = useAuthentication();
    const [params]              = useSearchParams();
    const navigate              = useNavigate();
    const started               = useRef<boolean>(false);
    const [error, setError]     = useState<string | null>(null);

    useEffect(() =>
    {
        if (started.current)
            return;

        started.current = true;

        const code:    string | null = params.get("code");
        const state:   string | null = params.get("state");
        const refusal: string | null = params.get("error");

        if (refusal || !code || !state)
        {
            setError("Sign-in was not completed");
            return;
        }

        completeOktaLogin(code,state).then(
            (returnTo: string) => navigate(returnTo,{ replace: true }),
            () => setError("Sign-in failed"));
    },[completeOktaLogin,navigate,params]);

    return (
        <Center py="xl">
            <Stack gap="md" align="center">
                {error
                    ? <>
                          <Alert color="red" role="alert">{error}</Alert>
                          <Anchor component={Link} to="/login">Back to sign in</Anchor>
                      </>
                    : <Loader aria-label="Completing sign-in" />}
            </Stack>
        </Center>
    );
}

//////////////////////////////////////////////////////////////////////////////
