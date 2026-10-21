//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

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
    login(userName: string,password: string,deviceId: string): Promise<ITokenGrant>;

    loginWithOauth2(provider: string,idToken: string,deviceId: string): Promise<ITokenGrant>;

    refresh(refreshToken: string): Promise<ITokenGrant>;

    logout(sessionId: number,accessToken: string): Promise<void>;
}

//////////////////////////////////////////////////////////////////////////////
