//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface ILoginMethods
{
    passwordEnabled: boolean;
    oktaEnabled:     boolean;
}

export interface ISessionStatus
{
    authenticated:    boolean;
    userName?:        string;
    authorities:      Array<string>;
    returnTo?:        string;
    passwordEnabled?: boolean;
    passwordSet?:     boolean;
}

export interface ILogoutResult
{
    loggedOut: boolean;
}

export interface IChangePasswordResult
{
    success:  boolean;
    message?: string;
}

export interface ISessionService
{
    getLoginMethods(): Promise<ILoginMethods>;

    login(userName: string,password: string): Promise<ISessionStatus>;

    startOktaLogin(returnTo: string): Promise<string | undefined>;

    completeOktaLogin(code: string,state: string): Promise<ISessionStatus>;

    getCurrent(): Promise<ISessionStatus>;

    logout(): Promise<ILogoutResult>;

    changePassword(currentPassword: string,newPassword: string): Promise<IChangePasswordResult>;
}

//////////////////////////////////////////////////////////////////////////////
