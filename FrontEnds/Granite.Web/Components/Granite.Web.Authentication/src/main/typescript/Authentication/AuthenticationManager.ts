//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { ANONYMOUS }                        from "./IAuthenticationState";
import type { IAuthenticationState }        from "./IAuthenticationState";
import type { ISessionApi, ITokenGrant }    from "./ISessionApi";
import type { ITokenStorage }               from "./TokenStorage";
import { decodeJwtClaims, expiresAtMillis } from "./JwtClaims";

export type AuthenticationListener = (state: IAuthenticationState) => void;

export interface ITimer
{
    set(callback: () => void,delayMillis: number): unknown;

    clear(handle: unknown): void;

    now(): number;
}

export const browserTimer: ITimer =
{
    set:   (callback: () => void,delayMillis: number) => setTimeout(callback,delayMillis),
    clear: (handle: unknown) => clearTimeout(handle as ReturnType<typeof setTimeout>),
    now:   () => Date.now(),
};

export const REFRESH_MARGIN_MILLIS: number = 60_000;

export class AuthenticationManager
{
    private readonly api:       ISessionApi;
    private readonly storage:   ITokenStorage;
    private readonly timer:     ITimer;
    private readonly listeners: Set<AuthenticationListener>;
    private state:              IAuthenticationState;
    private refreshToken:       string | undefined;
    private refreshHandle:      unknown;

    constructor(api: ISessionApi,storage: ITokenStorage,timer: ITimer = browserTimer)
    {
        this.api           = api;
        this.storage       = storage;
        this.timer         = timer;
        this.listeners     = new Set();
        this.state         = storage.load() ? { status: "restoring", authorities: [] } : ANONYMOUS;
        this.refreshToken  = undefined;
        this.refreshHandle = undefined;
    }

    getState(): IAuthenticationState
    {
        return this.state;
    }

    subscribe(listener: AuthenticationListener): () => void
    {
        this.listeners.add(listener);
        return () => { this.listeners.delete(listener); };
    }

    async login(userName: string,password: string): Promise<void>
    {
        await this.authenticate(userName,() => this.api.login(userName,password,this.storage.getDeviceId()));
    }

    async loginWithGoogle(idToken: string): Promise<void>
    {
        const claims = decodeJwtClaims(idToken);

        await this.authenticate(
            claims.email ?? claims.name ?? "Google user",
            () => this.api.loginWithOauth2("google",idToken,this.storage.getDeviceId()));
    }

    async restore(): Promise<void>
    {
        const stored = this.storage.load();

        if (!stored)
        {
            this.setState(ANONYMOUS);
            return;
        }

        try
        {
            this.accept(stored.userName,await this.api.refresh(stored.refreshToken));
        }
        catch
        {
            this.reset();
        }
    }

    async refreshNow(): Promise<void>
    {
        if (!this.refreshToken)
            return;

        try
        {
            this.accept(this.state.userName,await this.api.refresh(this.refreshToken));
        }
        catch
        {
            this.reset();
        }
    }

    async logout(): Promise<void>
    {
        const { sessionId, accessToken } = this.state;

        this.reset();

        if (sessionId !== undefined && accessToken !== undefined)
        {
            try
            {
                await this.api.logout(sessionId,accessToken);
            }
            catch
            {
                return;
            }
        }
    }

    private async authenticate(userName: string,grant: () => Promise<ITokenGrant>): Promise<void>
    {
        this.setState({ ...ANONYMOUS, status: "authenticating" });

        try
        {
            this.accept(userName,await grant());
        }
        catch (error)
        {
            this.cancelRefresh();
            this.refreshToken = undefined;
            this.storage.clear();
            this.setState({ ...ANONYMOUS, error: error instanceof Error ? error.message : String(error) });
            throw error;
        }
    }

    private accept(userName: string | undefined,grant: ITokenGrant): void
    {
        const expiresAt: number | undefined = expiresAtMillis(grant.accessToken);

        this.refreshToken = grant.refreshToken;
        this.storage.save({ refreshToken: grant.refreshToken, sessionId: grant.sessionId, userName });
        this.setState({
            status:               "authenticated",
            userId:               grant.userId,
            userName,
            sessionId:            grant.sessionId,
            authorities:          grant.authorities,
            accessToken:          grant.accessToken,
            accessTokenExpiresAt: expiresAt,
        });
        this.scheduleRefresh(expiresAt);
    }

    private scheduleRefresh(expiresAt: number | undefined): void
    {
        this.cancelRefresh();

        if (expiresAt === undefined)
            return;

        const delay: number = Math.max(0,expiresAt - this.timer.now() - REFRESH_MARGIN_MILLIS);

        this.refreshHandle = this.timer.set(() => { void this.refreshNow(); },delay);
    }

    private cancelRefresh(): void
    {
        if (this.refreshHandle !== undefined)
            this.timer.clear(this.refreshHandle);

        this.refreshHandle = undefined;
    }

    private reset(): void
    {
        this.cancelRefresh();
        this.refreshToken = undefined;
        this.storage.clear();
        this.setState(ANONYMOUS);
    }

    private setState(state: IAuthenticationState): void
    {
        this.state = state;
        this.listeners.forEach((listener: AuthenticationListener) => listener(state));
    }
}

//////////////////////////////////////////////////////////////////////////////
