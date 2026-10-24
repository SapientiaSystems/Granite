//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { ReactElement, ReactNode } from "react";
import { Navigate, useLocation }        from "react-router-dom";
import { Center, Loader }               from "@mantine/core";
import { useAuthentication }            from "./AuthenticationContext";

export interface IRequireAuthenticationProps
{
    children:   ReactNode;
    loginPath?: string;
}

export function RequireAuthentication({ children, loginPath = "/login" }: IRequireAuthenticationProps): ReactElement
{
    const { state } = useAuthentication();
    const location  = useLocation();

    if (state.status === "restoring")
        return <Center p="xl"><Loader aria-label="Restoring session" /></Center>;

    if (state.status === "anonymous")
        return <Navigate to={loginPath} replace state={{ from: location.pathname + location.search }} />;

    return <>{children}</>;
}

//////////////////////////////////////////////////////////////////////////////
