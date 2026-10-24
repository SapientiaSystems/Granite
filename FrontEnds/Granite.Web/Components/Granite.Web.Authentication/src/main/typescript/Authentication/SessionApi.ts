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

    login(userName: string,password: string,deviceId: string): ICompletionStage<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        return closeOnCompletion(
                client,
                client.login({ ...newRequest(), userName, credential: password, credentialKind: "PASSWORD", deviceId }))
            .thenApply(reply => toGrant(reply));
    }

    loginWithOauth2(provider: string,idToken: string,deviceId: string): ICompletionStage<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        return closeOnCompletion(client,client.loginWithOauth2({ ...newRequest(), provider, idToken, deviceId }))
            .thenApply(reply => toGrant(reply));
    }

    refresh(refreshToken: string): ICompletionStage<ITokenGrant>
    {
        const client: ISessionCommandServiceClient = this.createClient();

        return closeOnCompletion(client,client.refresh({ ...newRequest(), refreshToken }))
            .thenApply(reply => toGrant(reply));
    }

    logout(sessionId: number,accessToken: string): ICompletionStage<void>
    {
        const client: ISessionCommandServiceClient = this.createClient().setAuthorization(accessToken);

        return closeOnCompletion(client,client.logout({ ...newRequest(), sessionId }))
            .thenApply(() => undefined);
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

function closeOnCompletion<T>(client: ISessionCommandServiceClient,stage: ICompletionStage<T>): ICompletionStage<T>
{
    return stage.thenAccept(() => client.close(),() => client.close());
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
