//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export type AuthenticationStatus = "restoring" | "anonymous" | "authenticating" | "authenticated";

export interface IAuthenticationState
{
    status:               AuthenticationStatus;
    userId?:              number;
    userName?:            string;
    sessionId?:           number;
    authorities:          Array<string>;
    accessToken?:         string;
    accessTokenExpiresAt?: number;
    error?:               string;
}

export const ANONYMOUS: IAuthenticationState = { status: "anonymous", authorities: [] };

//////////////////////////////////////////////////////////////////////////////
