//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement }        from "react";
import { Modal, Text }              from "@mantine/core";
import { useAuthentication }        from "../Authentication/AuthenticationContext";
import { SignInForm }               from "./SignInForm";

export function ReauthenticationDialog(): ReactElement
{
    const { state, cancelReauthentication } = useAuthentication();
    const returnTo: string                  = window.location.pathname + window.location.search + window.location.hash;

    return (
        <Modal
            opened
            centered
            onClose={cancelReauthentication}
            title="Session expired"
            closeOnClickOutside={false}
            closeButtonProps={{ "aria-label": "Cancel sign-in" }}
            transitionProps={{ duration: 0 }}
        >
            <Text size="sm" mb="md">
                {state.userName ? state.userName + ", sign in again to continue." : "Sign in again to continue."}
            </Text>
            <SignInForm returnTo={returnTo} />
        </Modal>
    );
}

//////////////////////////////////////////////////////////////////////////////
