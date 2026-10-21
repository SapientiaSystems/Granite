//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { vi }                                 from "vitest";
import type { ISessionApi, ITokenGrant }      from "../../main/typescript/Authentication/ISessionApi";
import type { ITimer }                        from "../../main/typescript/Authentication/AuthenticationManager";
import type { IStoredSession, ITokenStorage } from "../../main/typescript/Authentication/TokenStorage";

export function base64Url(value: string): string
{
    const bytes: Uint8Array = new TextEncoder().encode(value);

    return btoa(String.fromCharCode(...bytes)).replace(/\+/g,"-").replace(/\//g,"_").replace(/=+$/,"");
}

export function makeJwt(claims: Record<string,unknown>): string
{
    return base64Url(JSON.stringify({ alg: "RS256" })) + "." + base64Url(JSON.stringify(claims)) + ".signature";
}

export function grant(n: number,expiresAtSeconds: number): ITokenGrant
{
    return {
        accessToken:  makeJwt({ sub: "1001", jti: "2002", exp: expiresAtSeconds }),
        refreshToken: "refresh-" + n,
        sessionId:    2002,
        userId:       1001,
        authorities:  ["ROLE_USER"],
    };
}

export interface IFakeSessionApi extends ISessionApi
{
    login:           ReturnType<typeof vi.fn>;
    loginWithOauth2: ReturnType<typeof vi.fn>;
    refresh:         ReturnType<typeof vi.fn>;
    logout:          ReturnType<typeof vi.fn>;
}

export function fakeApi(): IFakeSessionApi
{
    return {
        login:           vi.fn(),
        loginWithOauth2: vi.fn(),
        refresh:         vi.fn(),
        logout:          vi.fn().mockResolvedValue(undefined),
    };
}

export class MemoryTokenStorage
    implements ITokenStorage
{
    stored: IStoredSession | null;

    constructor(initial: IStoredSession | null = null)
    {
        this.stored = initial;
    }

    load(): IStoredSession | null { return this.stored; }

    save(session: IStoredSession): void { this.stored = session; }

    clear(): void { this.stored = null; }

    getDeviceId(): string { return "device-1"; }
}

export class ManualTimer
    implements ITimer
{
    current: number;
    pending: Array<{ handle: number; at: number; callback: () => void }>;
    private next: number;

    constructor(now: number)
    {
        this.current = now;
        this.pending = [];
        this.next    = 1;
    }

    set(callback: () => void,delayMillis: number): unknown
    {
        const handle: number = this.next++;

        this.pending.push({ handle, at: this.current + delayMillis, callback });
        return handle;
    }

    clear(handle: unknown): void
    {
        this.pending = this.pending.filter(entry => entry.handle !== handle);
    }

    now(): number { return this.current; }

    advanceTo(time: number): void
    {
        this.current = time;

        const due = this.pending.filter(entry => entry.at <= time);

        this.pending = this.pending.filter(entry => entry.at > time);
        due.forEach(entry => entry.callback());
    }
}

export async function flush(): Promise<void>
{
    for (let i = 0;i < 5;i++)
        await Promise.resolve();
}

//////////////////////////////////////////////////////////////////////////////
