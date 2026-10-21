//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { SessionCommandServiceClient }       from "granite.session.client/RequestReply";
import type { ISessionCommandServiceClient } from "granite.session.client/RequestReply";
import type { ICompletionStage }             from "strata.foundation.core/Concurrent";
import type { AbstractServiceRequest }       from "strata.foundation.core/Transfer";
import type { ISessionApi, ITokenGrant }     from "./ISessionApi";

interface IGrantReply
{
    success?:        boolean;
    failureMessage?: string;
    accessToken?:    string;
    refreshToken?:   string;
    sessionData?:
    {
        sessionId?:   number;
        userId?:      number;
        authorities?: Array<string>;
    };
}

export class SessionApi
    implements ISessionApi
{
    private readonly baseUrl: string;

    constructor(apiBaseUrl: string)
    {
        this.baseUrl = apiBaseUrl + "/session-command-service";
    }

    async login(userName: string,password: string,deviceId: string): Promise<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        try
        {
            return toGrant(
                await toPromise(
                    client.login({ ...newRequest(), userName, credential: password, credentialKind: "PASSWORD", deviceId })));
        }
        finally
        {
            client.close();
        }
    }

    async loginWithOauth2(provider: string,idToken: string,deviceId: string): Promise<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        try
        {
            return toGrant(await toPromise(client.loginWithOauth2({ ...newRequest(), provider, idToken, deviceId })));
        }
        finally
        {
            client.close();
        }
    }

    async refresh(refreshToken: string): Promise<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        try
        {
            return toGrant(await toPromise(client.refresh({ ...newRequest(), refreshToken })));
        }
        finally
        {
            client.close();
        }
    }

    async logout(sessionId: number,accessToken: string): Promise<void>
    {
        const client: ISessionCommandServiceClient = this.createClient().setAuthorization(accessToken);

        try
        {
            await toPromise(client.logout({ ...newRequest(), sessionId }));
        }
        finally
        {
            client.close();
        }
    }

    private createClient(): ISessionCommandServiceClient
    {
        return new SessionCommandServiceClient(this.baseUrl);
    }
}

export function newRequest(): AbstractServiceRequest
{
    return { requestId: crypto.randomUUID(), timestamp: new Date().toISOString() };
}

export function toPromise<T>(stage: ICompletionStage<T>): Promise<T>
{
    return new Promise<T>((resolve,reject) =>
    {
        stage
            .thenApply((value: T) =>
            {
                resolve(value);
                return value;
            })
            .exceptionally((error: Error) =>
            {
                reject(error);
                return undefined as unknown as T;
            })
            .subscribe();
    });
}

export function toGrant(reply: IGrantReply): ITokenGrant
{
    if (!reply.success || !reply.accessToken || !reply.refreshToken || !reply.sessionData)
        throw new Error(reply.failureMessage ?? "authentication failed");

    return {
        accessToken:  reply.accessToken,
        refreshToken: reply.refreshToken,
        sessionId:    reply.sessionData.sessionId ?? 0,
        userId:       reply.sessionData.userId ?? 0,
        authorities:  reply.sessionData.authorities ?? [],
    };
}

//////////////////////////////////////////////////////////////////////////////
