//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { createContext, useContext }            from "react";
import type { FetchFunction }                        from "../Http/CsrfFetch";
import type { IAuthenticationState }            from "./IAuthenticationState";
import type { IChangePasswordResult, ILoginMethods } from "./ISessionService";

export interface IAuthenticationContext
{
    state:                  IAuthenticationState;
    login:                  (userName: string,password: string) => Promise<void>;
    startOktaLogin:         (returnTo: string) => Promise<void>;
    completeOktaLogin:      (code: string,state: string) => Promise<string>;
    logout:                 () => Promise<boolean>;
    getLoginMethods:        () => Promise<ILoginMethods>;
    changePassword:         (currentPassword: string,newPassword: string) => Promise<IChangePasswordResult>;
    cancelReauthentication: () => void;
    createFrontEndClient:   <C>(create: (serviceUrl: string,fetchFunction: FetchFunction) => C,service: string) => C;
}

export const AuthenticationContext = createContext<IAuthenticationContext | null>(null);

export function useAuthentication(): IAuthenticationContext
{
    const context: IAuthenticationContext | null = useContext(AuthenticationContext);

    if (!context)
        throw new Error("useAuthentication must be used within an AuthenticationProvider");

    return context;
}

//////////////////////////////////////////////////////////////////////////////
