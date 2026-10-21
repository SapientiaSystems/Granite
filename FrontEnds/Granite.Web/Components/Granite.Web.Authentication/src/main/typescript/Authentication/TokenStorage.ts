//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

export interface IStoredSession
{
    refreshToken: string;
    sessionId:    number;
    userName?:    string;
}

export interface ITokenStorage
{
    load(): IStoredSession | null;

    save(session: IStoredSession): void;

    clear(): void;

    getDeviceId(): string;
}

const SESSION_KEY: string = "granite.auth.session";
const DEVICE_KEY:  string = "granite.auth.deviceId";

export class BrowserTokenStorage
    implements ITokenStorage
{
    load(): IStoredSession | null
    {
        try
        {
            const raw: string | null = window.sessionStorage.getItem(SESSION_KEY);

            return raw ? JSON.parse(raw) as IStoredSession : null;
        }
        catch
        {
            return null;
        }
    }

    save(session: IStoredSession): void
    {
        try
        {
            window.sessionStorage.setItem(SESSION_KEY,JSON.stringify(session));
        }
        catch
        {
            return;
        }
    }

    clear(): void
    {
        try
        {
            window.sessionStorage.removeItem(SESSION_KEY);
        }
        catch
        {
            return;
        }
    }

    getDeviceId(): string
    {
        try
        {
            let deviceId: string | null = window.localStorage.getItem(DEVICE_KEY);

            if (!deviceId)
            {
                deviceId = crypto.randomUUID();
                window.localStorage.setItem(DEVICE_KEY,deviceId);
            }

            return deviceId;
        }
        catch
        {
            return crypto.randomUUID();
        }
    }
}

//////////////////////////////////////////////////////////////////////////////
