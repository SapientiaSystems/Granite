//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { GetOutputRequestBuilder, HandleInputRequestBuilder }                 from "strata.frontendcontroller.service/RequestReply";
import { ServiceError }                                                       from "strata.client.core/Service";
import { CompletionError }                                                    from "strata.foundation.core/Concurrent";
import type { IGraniteFrontEndControllerServiceClient }                       from "granite.frontendcontroller.client/RequestReply";
import type { GetSessionOutputReply, HandleSessionInputReply,
              SessionInput, SessionOutput }                                   from "granite.frontendcontroller.service/RequestReply";
import type { ILoginMethods, ILogoutResult, ISessionService, ISessionStatus } from "./ISessionService";

const NEW_SOURCE:          string = "Session.New";
const CURRENT_SOURCE:      string = "Session.Current";
const LOGIN_DESTINATION:   string = "LoginView";
const SESSION_DESTINATION: string = "SessionView";
const AUTHENTICATED:       string = "AUTHENTICATED";

export const UNAUTHENTICATED: ISessionStatus = { authenticated: false, authorities: [] };

export class FrontEndSessionService
    implements ISessionService
{
    private readonly createClient: () => IGraniteFrontEndControllerServiceClient;

    constructor(createClient: () => IGraniteFrontEndControllerServiceClient)
    {
        this.createClient = createClient;
    }

    async getLoginMethods(): Promise<ILoginMethods>
    {
        const output: SessionOutput = await this.read(NEW_SOURCE,LOGIN_DESTINATION);

        return { passwordEnabled: output.passwordEnabled === true, oktaEnabled: output.oktaEnabled === true };
    }

    async login(userName: string,password: string): Promise<ISessionStatus>
    {
        return toStatus(await this.send(NEW_SOURCE,"Login",LOGIN_DESTINATION,{ userName, password }));
    }

    async startOktaLogin(returnTo: string): Promise<string | undefined>
    {
        const output: SessionOutput = await this.send(NEW_SOURCE,"StartOktaLogin",LOGIN_DESTINATION,{ returnTo });

        return typeof output.redirectUrl === "string" && output.redirectUrl.length > 0 ? output.redirectUrl : undefined;
    }

    async completeOktaLogin(code: string,state: string): Promise<ISessionStatus>
    {
        return toStatus(await this.send(NEW_SOURCE,"CompleteOktaLogin",LOGIN_DESTINATION,{ code, state }));
    }

    async getCurrent(): Promise<ISessionStatus>
    {
        try
        {
            return toStatus(await this.read(CURRENT_SOURCE,SESSION_DESTINATION));
        }
        catch (error)
        {
            if (isUnauthorized(error))
                return UNAUTHENTICATED;

            throw error;
        }
    }

    async logout(): Promise<ILogoutResult>
    {
        try
        {
            const output: SessionOutput = await this.send(CURRENT_SOURCE,"Logout",LOGIN_DESTINATION,{});

            return { loggedOut: output.loggedOut === true };
        }
        catch (error)
        {
            if (isUnauthorized(error))
                return { loggedOut: true };

            throw error;
        }
    }

    private async read(source: string,destination: string): Promise<SessionOutput>
    {
        const client: IGraniteFrontEndControllerServiceClient = this.createClient();

        try
        {
            const reply: GetSessionOutputReply =
                await client.getSessionOutput(
                    GetOutputRequestBuilder.newBuilder().setSource(source).setDestination(destination).build());

            return unwrap(reply);
        }
        finally
        {
            client.close();
        }
    }

    private async send(source: string,command: string,destination: string,fields: Partial<SessionInput>): Promise<SessionOutput>
    {
        const client: IGraniteFrontEndControllerServiceClient = this.createClient();
        const input:  SessionInput                            = { userId: null, source, command, destination, ...fields };

        try
        {
            const reply: HandleSessionInputReply =
                await client.handleSessionInput(HandleInputRequestBuilder.newBuilder<SessionInput>().setInput(input).build());

            return unwrap(reply);
        }
        finally
        {
            client.close();
        }
    }
}

function unwrap(reply: { success?: boolean; failureMessage?: string; output?: SessionOutput }): SessionOutput
{
    if (reply.success === false)
        throw new Error(reply.failureMessage || "Request failed");

    if (!reply.output)
        throw new Error("Empty response");

    return reply.output;
}

function toStatus(output: SessionOutput): ISessionStatus
{
    return {
        authenticated: output.status === AUTHENTICATED,
        userName:      output.userName ?? undefined,
        authorities:   output.authorities ?? [],
        returnTo:      output.returnTo ?? undefined,
    };
}

function isUnauthorized(error: unknown): boolean
{
    const cause: unknown = error instanceof CompletionError ? error.getCause() : error;

    return cause instanceof ServiceError && cause.getStatus() === 401;
}

//////////////////////////////////////////////////////////////////////////////
