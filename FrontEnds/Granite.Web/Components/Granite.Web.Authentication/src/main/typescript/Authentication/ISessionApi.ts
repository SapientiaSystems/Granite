//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import type { ICompletionStage } from "strata.foundation.core/Concurrent";

export interface ITokenGrant
{
    accessToken:  string;
    refreshToken: string;
    sessionId:    number;
    userId:       number;
    authorities:  Array<string>;
}

export interface ISessionApi
{
    login(userName: string,password: string,deviceId: string): ICompletionStage<ITokenGrant>;

    loginWithOauth2(provider: string,idToken: string,deviceId: string): ICompletionStage<ITokenGrant>;

    refresh(refreshToken: string): ICompletionStage<ITokenGrant>;

    logout(sessionId: number,accessToken: string): ICompletionStage<void>;
}

//////////////////////////////////////////////////////////////////////////////
