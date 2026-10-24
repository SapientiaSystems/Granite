//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { GetOutputRequestBuilder, HandleInputRequestBuilder }                   from "strata.frontendcontroller.service/RequestReply";
import type { FrontEndInput, FrontEndOutput, GetOutputReply, HandleInputReply } from "strata.frontendcontroller.service/RequestReply";
import type { IFrontEndControllerServiceClient }                                from "strata.frontendcontroller.client/RequestReply";
import { ServiceError }                                                         from "strata.client.core/Service";
import { CompletionError }                                                      from "strata.foundation.core/Concurrent";
import type { CompleteOktaLoginInput, LoginInput, LoginMethodsOutput,
              LogoutInput, LogoutOutput, RedirectOutput, SessionOutput,
              StartOktaLoginInput }                                             from "granite.frontendcontroller.service/RequestReply";
import type { ILoginMethods, ILogoutResult, ISessionService, ISessionStatus }   from "./ISessionService";

const NEW_SOURCE:          string = "Session.New";
const CURRENT_SOURCE:      string = "Session.Current";
const LOGIN_DESTINATION:   string = "LoginView";
const SESSION_DESTINATION: string = "SessionView";
const AUTHENTICATED:       string = "AUTHENTICATED";

export const UNAUTHENTICATED: ISessionStatus = { authenticated: false, authorities: [] };

export class FrontEndSessionService
    implements ISessionService
{
    private readonly createClient: () => IFrontEndControllerServiceClient;

    constructor(createClient: () => IFrontEndControllerServiceClient)
    {
        this.createClient = createClient;
    }

    async getLoginMethods(): Promise<ILoginMethods>
    {
        const output: LoginMethodsOutput = await this.read<LoginMethodsOutput>(NEW_SOURCE,LOGIN_DESTINATION);

        return { passwordEnabled: output.passwordEnabled === true, oktaEnabled: output.oktaEnabled === true };
    }

    async login(userName: string,password: string): Promise<ISessionStatus>
    {
        const input: LoginInput =
            { userId: 0, source: NEW_SOURCE, command: "Login", destination: LOGIN_DESTINATION, userName, password };

        return toStatus(await this.send<SessionOutput>(input));
    }

    async startOktaLogin(returnTo: string): Promise<string | undefined>
    {
        const input: StartOktaLoginInput =
            { userId: 0, source: NEW_SOURCE, command: "StartOktaLogin", destination: LOGIN_DESTINATION, returnTo };
        const output: RedirectOutput = await this.send<RedirectOutput>(input);

        return typeof output.url === "string" && output.url.length > 0 ? output.url : undefined;
    }

    async completeOktaLogin(code: string,state: string): Promise<ISessionStatus>
    {
        const input: CompleteOktaLoginInput =
            { userId: 0, source: NEW_SOURCE, command: "CompleteOktaLogin", destination: LOGIN_DESTINATION, code, state };

        return toStatus(await this.send<SessionOutput>(input));
    }

    async getCurrent(): Promise<ISessionStatus>
    {
        try
        {
            return toStatus(await this.read<SessionOutput>(CURRENT_SOURCE,SESSION_DESTINATION));
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
        const input: LogoutInput =
            { userId: 0, source: CURRENT_SOURCE, command: "Logout", destination: LOGIN_DESTINATION, logout: true };

        try
        {
            const output: LogoutOutput = await this.send<LogoutOutput>(input);

            return { loggedOut: output.loggedOut === true, endSessionUrl: output.endSessionUrl ?? undefined };
        }
        catch (error)
        {
            if (isUnauthorized(error))
                return { loggedOut: true };

            throw error;
        }
    }

    private async read<T extends FrontEndOutput>(source: string,destination: string): Promise<T>
    {
        const client: IFrontEndControllerServiceClient = this.createClient();

        try
        {
            const reply: GetOutputReply =
                await client.getOutput(
                    GetOutputRequestBuilder.newBuilder().setUserId(0).setSource(source).setDestination(destination).build());

            return unwrap<T>(reply);
        }
        finally
        {
            client.close();
        }
    }

    private async send<T extends FrontEndOutput>(input: FrontEndInput): Promise<T>
    {
        const client: IFrontEndControllerServiceClient = this.createClient();

        try
        {
            const reply: HandleInputReply =
                await client.handleInput(HandleInputRequestBuilder.newBuilder().setInput(input).build());

            return unwrap<T>(reply);
        }
        finally
        {
            client.close();
        }
    }
}

function unwrap<T extends FrontEndOutput>(reply: { success?: boolean; failureMessage?: string; output?: FrontEndOutput }): T
{
    if (reply.success === false)
        throw new Error(reply.failureMessage || "Request failed");

    if (!reply.output)
        throw new Error("Empty response");

    return reply.output as T;
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
