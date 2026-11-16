//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export type AuthenticationStatus = "restoring" | "anonymous" | "authenticated" | "reauthenticating";

export interface IAuthenticationState
{
    status:          AuthenticationStatus;
    userId?:         number;
    userName?:       string;
    authorities:     Array<string>;
    passwordEnabled: boolean;
    passwordSet:     boolean;
    error?:          string;
}

export const RESTORING: IAuthenticationState = { status: "restoring", authorities: [], passwordEnabled: false, passwordSet: false };

export const ANONYMOUS: IAuthenticationState = { status: "anonymous", authorities: [], passwordEnabled: false, passwordSet: false };

//////////////////////////////////////////////////////////////////////////////
