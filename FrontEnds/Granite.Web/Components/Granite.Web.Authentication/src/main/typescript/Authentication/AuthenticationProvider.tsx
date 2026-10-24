//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { useEffect, useMemo, useSyncExternalStore } from "react";
import type { ReactElement, ReactNode }             from "react";
import { AuthenticationContext }                    from "./AuthenticationContext";
import type { IAuthenticationContext }              from "./AuthenticationContext";
import type { IAuthenticationState }                from "./IAuthenticationState";
import { createAuthentication }                     from "./createAuthentication";
import type { IAuthentication }                     from "./createAuthentication";
import { ReauthenticationDialog }                   from "../Login/ReauthenticationDialog";

export interface IAuthenticationProviderProps
{
    children:        ReactNode;
    authentication?: IAuthentication;
}

export function AuthenticationProvider({ children, authentication }: IAuthenticationProviderProps): ReactElement
{
    const { manager, createFrontEndClient }: IAuthentication =
        useMemo(() => authentication ?? createAuthentication(),[authentication]);
    const state: IAuthenticationState =
        useSyncExternalStore(
            (listener: () => void) => manager.subscribe(listener),
            () => manager.getState());

    useEffect(() =>
    {
        if (manager.getState().status === "restoring")
            void manager.restore();
    },[manager]);

    const actions =
        useMemo(
            () => ({
                login:                  (userName: string,password: string) => manager.login(userName,password),
                startOktaLogin:         (returnTo: string) => manager.startOktaLogin(returnTo),
                completeOktaLogin:      (code: string,oktaState: string) => manager.completeOktaLogin(code,oktaState),
                logout:                 () => manager.logout(),
                getLoginMethods:        () => manager.getLoginMethods(),
                cancelReauthentication: () => manager.cancelReauthentication(),
                createFrontEndClient,
            }),
            [manager,createFrontEndClient]);

    const value: IAuthenticationContext = useMemo(() => ({ state, ...actions }),[state,actions]);

    return (
        <AuthenticationContext.Provider value={value}>
            {children}
            {state.status === "reauthenticating" && <ReauthenticationDialog />}
        </AuthenticationContext.Provider>
    );
}

//////////////////////////////////////////////////////////////////////////////
