//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { vi }                                                                  from "vitest";
import { CompletableObservable }                                               from "strata.foundation.core/Concurrent";
import type { ICompletionStage }                                               from "strata.foundation.core/Concurrent";
import { ServiceError, StandardResponse }                                      from "strata.client.core/Service";
import type { IGraniteFrontEndControllerServiceClient }                        from "granite.frontendcontroller.client/RequestReply";
import type { GetSessionOutputReply, GetSessionOutputRequest, HandleSessionInputReply,
              HandleSessionInputRequest, GetUsersOutputReply, GetUsersOutputRequest,
              HandleUsersInputReply, HandleUsersInputRequest }                 from "granite.frontendcontroller.service/RequestReply";
import type { FrontEndOutput }                                                 from "strata.frontendcontroller.service/RequestReply";
import { AuthenticationManager, FrontEndSessionService, createAuthentication } from "granite.web.authentication/Authentication";
import type { INavigator, IAuthentication }                                    from "granite.web.authentication/Authentication";

export interface IRecordedCall
{
    key:     string;
    payload: Record<string,unknown>;
}

export type Responder = (payload: Record<string,unknown>) => FrontEndOutput | Error;

export class FakeFrontEnd
    implements IGraniteFrontEndControllerServiceClient
{
    readonly calls: Array<IRecordedCall> = [];
    private readonly responders: Map<string,Responder> = new Map();

    respond(key: string,responder: Responder): FakeFrontEnd
    {
        this.responders.set(key,responder);
        return this;
    }

    callsFor(key: string): Array<IRecordedCall>
    {
        return this.calls.filter(call => call.key === key);
    }

    setAuthorization(): IGraniteFrontEndControllerServiceClient
    {
        throw new Error("the SPA must not set Authorization");
    }

    close(): void {}

    getUsersOutput(request: GetUsersOutputRequest): ICompletionStage<GetUsersOutputReply>
    {
        return this.answer(request.source + "->" + request.destination,request as unknown as Record<string,unknown>) as ICompletionStage<GetUsersOutputReply>;
    }

    handleUsersInput(request: HandleUsersInputRequest): ICompletionStage<HandleUsersInputReply>
    {
        const input = request.input as unknown as Record<string,unknown>;

        return this.answer(input.source + "->" + input.command + "->" + input.destination,input) as ICompletionStage<HandleUsersInputReply>;
    }

    getSessionOutput(request: GetSessionOutputRequest): ICompletionStage<GetSessionOutputReply>
    {
        return this.answer(request.source + "->" + request.destination,request as unknown as Record<string,unknown>) as ICompletionStage<GetSessionOutputReply>;
    }

    handleSessionInput(request: HandleSessionInputRequest): ICompletionStage<HandleSessionInputReply>
    {
        const input = request.input as unknown as Record<string,unknown>;

        return this.answer(input.source + "->" + input.command + "->" + input.destination,input) as ICompletionStage<HandleSessionInputReply>;
    }

    private answer(key: string,payload: Record<string,unknown>): ICompletionStage<unknown>
    {
        this.calls.push({ key, payload });

        const responder: Responder | undefined = this.responders.get(key);

        if (!responder)
            return CompletableObservable.fromError(new Error("no fake response for " + key));

        const result: FrontEndOutput | Error = responder(payload);

        return result instanceof Error
            ? CompletableObservable.fromError(result)
            : CompletableObservable.fromResult({ success: true, output: result });
    }
}

export function serviceError(status: number): ServiceError
{
    return new ServiceError(new StandardResponse("/s","p",new Response("",{ status })));
}

export class FakeNavigator
    implements INavigator
{
    readonly assigned: Array<string> = [];

    assign(url: string): void { this.assigned.push(url); }
}

export function sessionOutput(authenticated: boolean,userName: string | null = null,authorities: Array<string> = [],returnTo?: string): FrontEndOutput
{
    return {
        userId:      null,
        source:      "Session.Current",
        destination: "SessionView",
        command:     null,
        userName,
        authorities,
        status:      authenticated ? "AUTHENTICATED" : "UNAUTHENTICATED",
        ...(returnTo !== undefined ? { returnTo } : {}),
    } as FrontEndOutput;
}

export function methodsOutput(passwordEnabled: boolean,oktaEnabled: boolean): FrontEndOutput
{
    return { userId: null, source: "Session.New", destination: "LoginView", command: null, status: "UNAUTHENTICATED", passwordEnabled, oktaEnabled } as FrontEndOutput;
}

export function logoutOutput(loggedOut: boolean): FrontEndOutput
{
    return { userId: null, source: "Session.Current", destination: "LoginView", command: "Logout", status: "UNAUTHENTICATED", loggedOut } as FrontEndOutput;
}

export function redirectOutput(redirectUrl: string): FrontEndOutput
{
    return { userId: null, source: "Session.New", destination: "LoginView", command: "StartOktaLogin", status: "UNAUTHENTICATED", redirectUrl } as FrontEndOutput;
}

export const KEYS =
{
    methods:  "Session.New->LoginView",
    login:    "Session.New->Login->LoginView",
    start:    "Session.New->StartOktaLogin->LoginView",
    complete: "Session.New->CompleteOktaLogin->LoginView",
    current:  "Session.Current->SessionView",
    logout:   "Session.Current->Logout->LoginView",
    change:   "Session.Current->ChangePassword->ChangePasswordView",
};

export function scriptedFrontEnd(): FakeFrontEnd
{
    return new FakeFrontEnd()
        .respond(KEYS.methods,() => methodsOutput(true,false))
        .respond(KEYS.current,() => sessionOutput(false))
        .respond(KEYS.login,(payload) => payload.password === "secret" ? sessionOutput(true,String(payload.userName),["ROLE_USER"]) : sessionOutput(false))
        .respond(KEYS.logout,() => logoutOutput(true));
}

export function managerFor(frontEnd: FakeFrontEnd,navigator: FakeNavigator = new FakeNavigator()): AuthenticationManager
{
    return new AuthenticationManager(new FrontEndSessionService(() => frontEnd),navigator);
}

export function authenticationFor(
    frontEnd:      FakeFrontEnd,
    fetchFunction: (input: RequestInfo | URL,init?: RequestInit) => Promise<Response> = vi.fn(),
    navigator:     FakeNavigator = new FakeNavigator()): IAuthentication
{
    return createAuthentication({
        apiBaseUrl:    "",
        fetchFunction,
        navigator,
        cookieReader:  () => "csrf-token",
        service:       new FrontEndSessionService(() => frontEnd),
    });
}

export async function flush(): Promise<void>
{
    for (let i = 0;i < 10;i++)
        await Promise.resolve();
}

//////////////////////////////////////////////////////////////////////////////

export function changePasswordOutput(changed: boolean,message?: string): FrontEndOutput
{
    return {
        userId: null, source: "Session.Current", destination: "ChangePasswordView", command: "ChangePassword",
        status: changed ? "CHANGED" : "FAILED", failureMessages: changed || message === undefined ? [] : [message],
    } as FrontEndOutput;
}
