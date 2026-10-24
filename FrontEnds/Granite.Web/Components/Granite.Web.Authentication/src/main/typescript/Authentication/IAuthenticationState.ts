//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export type AuthenticationStatus = "restoring" | "anonymous" | "authenticated" | "reauthenticating";

export interface IAuthenticationState
{
    status:      AuthenticationStatus;
    userId?:     number;
    userName?:   string;
    authorities: Array<string>;
    error?:      string;
}

export const RESTORING: IAuthenticationState = { status: "restoring", authorities: [] };

export const ANONYMOUS: IAuthenticationState = { status: "anonymous", authorities: [] };

//////////////////////////////////////////////////////////////////////////////
