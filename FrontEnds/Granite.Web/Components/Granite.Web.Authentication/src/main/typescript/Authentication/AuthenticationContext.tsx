//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { createContext, useContext, useEffect, useMemo, useSyncExternalStore } from "react";
import type { ReactElement, ReactNode }                                        from "react";
import { AuthenticationManager }                                               from "./AuthenticationManager";
import type { IAuthenticationState }                                           from "./IAuthenticationState";
import { SessionApi }                                                          from "./SessionApi";
import { BrowserTokenStorage }                                                 from "./TokenStorage";
import { getRuntimeConfig }                                                    from "./RuntimeConfig";

export interface IAuthenticationContext
{
    state:           IAuthenticationState;
    login:           (userName: string,password: string) => Promise<void>;
    loginWithGoogle: (idToken: string) => Promise<void>;
    logout:          () => Promise<void>;
}

const AuthenticationContext = createContext<IAuthenticationContext | null>(null);

export interface IAuthenticationProviderProps
{
    children: ReactNode;
    manager?: AuthenticationManager;
}

export function createDefaultAuthenticationManager(): AuthenticationManager
{
    return new AuthenticationManager(new SessionApi(getRuntimeConfig().apiBaseUrl),new BrowserTokenStorage());
}

export function AuthenticationProvider({ children, manager }: IAuthenticationProviderProps): ReactElement
{
    const auth: AuthenticationManager = useMemo(() => manager ?? createDefaultAuthenticationManager(),[manager]);
    const state: IAuthenticationState =
        useSyncExternalStore(
            (listener: () => void) => auth.subscribe(listener),
            () => auth.getState());

    useEffect(() =>
    {
        if (auth.getState().status === "restoring")
            void auth.restore();
    },[auth]);

    const value: IAuthenticationContext =
        useMemo(
            () => ({
                state,
                login:           (userName: string,password: string) => auth.login(userName,password),
                loginWithGoogle: (idToken: string) => auth.loginWithGoogle(idToken),
                logout:          () => auth.logout(),
            }),
            [auth,state]);

    return <AuthenticationContext.Provider value={value}>{children}</AuthenticationContext.Provider>;
}

export function useAuthentication(): IAuthenticationContext
{
    const context: IAuthenticationContext | null = useContext(AuthenticationContext);

    if (!context)
        throw new Error("useAuthentication must be used within an AuthenticationProvider");

    return context;
}

//////////////////////////////////////////////////////////////////////////////
