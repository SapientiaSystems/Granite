//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { IAuthenticationState } from "./IAuthenticationState";

export function hasAuthority(state: IAuthenticationState,authority: string): boolean
{
    return state.status === "authenticated" && state.authorities.includes(authority);
}

export function canEdit(state: IAuthenticationState): boolean
{
    return hasAuthority(state,"ROLE_ADMIN");
}

//////////////////////////////////////////////////////////////////////////////
