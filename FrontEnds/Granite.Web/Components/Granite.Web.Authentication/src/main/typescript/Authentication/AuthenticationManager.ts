//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { ANONYMOUS, RESTORING }                           from "./IAuthenticationState";
import type { IAuthenticationState }                      from "./IAuthenticationState";
import type { ILoginMethods, ISessionService, ISessionStatus } from "./ISessionService";
import { UNAUTHENTICATED }                                from "./FrontEndSessionService";

export type AuthenticationListener = (state: IAuthenticationState) => void;

export interface INavigator
{
    assign(url: string): void;
}

export const browserNavigator: INavigator =
{
    assign: (url: string) => window.location.assign(url),
};

interface IPrompt
{
    promise: Promise<boolean>;
    resolve: (retry: boolean) => void;
}

const SIGN_IN_FAILED:  string = "Sign-in failed";
const SIGN_OUT_FAILED: string = "Sign-out failed";

export class AuthenticationManager
{
    private readonly service:   ISessionService;
    private readonly navigator: INavigator;
    private readonly listeners: Set<AuthenticationListener>;
    private state:              IAuthenticationState;
    private prompt:             IPrompt | undefined;

    constructor(service: ISessionService,navigator: INavigator = browserNavigator)
    {
        this.service   = service;
        this.navigator = navigator;
        this.listeners = new Set();
        this.state     = RESTORING;
        this.prompt    = undefined;
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

    getLoginMethods(): Promise<ILoginMethods>
    {
        return this.service.getLoginMethods();
    }

    async restore(): Promise<void>
    {
        try
        {
            this.apply(await this.service.getCurrent());
        }
        catch (error)
        {
            this.setState({ ...ANONYMOUS, error: messageOf(error) });
        }
    }

    async login(userName: string,password: string): Promise<void>
    {
        let status: ISessionStatus;

        try
        {
            status = await this.service.login(userName,password);
        }
        catch (error)
        {
            this.recordError(messageOf(error));
            throw error;
        }

        this.requireAuthenticated(status);
        this.accept(status);
    }

    async startOktaLogin(returnTo: string): Promise<void>
    {
        let url: string | undefined;

        try
        {
            url = await this.service.startOktaLogin(localPath(returnTo));
        }
        catch (error)
        {
            this.recordError(messageOf(error));
            throw error;
        }

        if (!url)
        {
            this.recordError(SIGN_IN_FAILED);
            throw new Error(SIGN_IN_FAILED);
        }

        this.navigator.assign(url);
    }

    async completeOktaLogin(code: string,state: string): Promise<string>
    {
        let status: ISessionStatus;

        try
        {
            status = await this.service.completeOktaLogin(code,state);
        }
        catch (error)
        {
            this.recordError(messageOf(error));
            throw error;
        }

        this.requireAuthenticated(status);
        this.accept(status);

        return localPath(status.returnTo);
    }

    async logout(): Promise<boolean>
    {
        try
        {
            const result = await this.service.logout();

            if (!result.loggedOut)
            {
                this.recordError(SIGN_OUT_FAILED);
                return false;
            }

            this.settle(false);
            this.setState(ANONYMOUS);

            if (result.endSessionUrl)
                this.navigator.assign(result.endSessionUrl);

            return true;
        }
        catch (error)
        {
            this.recordError(messageOf(error));
            return false;
        }
    }

    async handleUnauthorized(): Promise<boolean>
    {
        if (this.prompt)
            return this.prompt.promise;

        const prompt: IPrompt = newPrompt();
        let current: ISessionStatus;

        this.prompt = prompt;

        try
        {
            current = await this.service.getCurrent();
        }
        catch
        {
            current = UNAUTHENTICATED;
        }

        if (this.prompt !== prompt)
            return prompt.promise;

        if (current.authenticated)
            this.accept(current);
        else if (this.state.status === "authenticated" || this.state.status === "reauthenticating")
            this.setState({ ...this.state, status: "reauthenticating", error: undefined });
        else
            this.settle(false);

        return prompt.promise;
    }

    cancelReauthentication(): void
    {
        if (this.state.status !== "reauthenticating")
            return;

        this.settle(false);
        this.setState(ANONYMOUS);
    }

    private apply(status: ISessionStatus): void
    {
        if (status.authenticated)
            this.accept(status);
        else
            this.setState(ANONYMOUS);
    }

    private accept(status: ISessionStatus): void
    {
        this.setState({ status: "authenticated", userName: status.userName, authorities: status.authorities });
        this.settle(true);
    }

    private requireAuthenticated(status: ISessionStatus): void
    {
        if (status.authenticated)
            return;

        this.recordError(SIGN_IN_FAILED);
        throw new Error(SIGN_IN_FAILED);
    }

    private settle(retry: boolean): void
    {
        const prompt: IPrompt | undefined = this.prompt;

        this.prompt = undefined;
        prompt?.resolve(retry);
    }

    private recordError(error: string): void
    {
        this.setState({ ...this.state, error });
    }

    private setState(state: IAuthenticationState): void
    {
        this.state = state;
        this.listeners.forEach((listener: AuthenticationListener) => listener(state));
    }
}

function newPrompt(): IPrompt
{
    let resolve: (retry: boolean) => void = () => undefined;
    const promise: Promise<boolean>       = new Promise<boolean>(done => { resolve = done; });

    return { promise, resolve };
}

function localPath(value: string | undefined): string
{
    return value && value.startsWith("/") && !value.startsWith("//") && !value.startsWith("/\\") ? value : "/";
}

function messageOf(error: unknown): string
{
    return error instanceof Error ? error.message : String(error);
}

//////////////////////////////////////////////////////////////////////////////
