//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AccountTest
//////////////////////////////////////////////////////////////////////////////

import { render }                                       from "@testing-library/react";
import { MantineProvider }                              from "@mantine/core";
import type { ReactElement }                            from "react";
import { AuthenticationProvider, createAuthentication } from "granite.web.authentication/Authentication";
import type { IAuthentication, ISessionService,
              ISessionStatus }                          from "granite.web.authentication/Authentication";
import { PreferencesProvider }                          from "granite.web.account/Preference";

export interface IFakePreferences
{
    values:      Record<string,unknown>;
    definitions: Array<Record<string,unknown>>;
}

export interface IDeferredSave
{
    input:   Record<string,unknown>;
    resolve: (reply: unknown) => void;
}

export const THEME_DEFINITION: Record<string,unknown> =
    { key: "granite.theme", type: "ENUM", defaultValue: "light", allowedValues: ["light","dark"] };

export const FLAG_DEFINITION: Record<string,unknown> =
    { key: "app.flag", type: "BOOLEAN", defaultValue: false, allowedValues: [] };

export class FakePreferencesClient
{
    readonly gets:     Array<Record<string,unknown>> = [];
    readonly saves:    Array<Record<string,unknown>> = [];
    readonly deferred: Array<IDeferredSave>          = [];
    closed:            number                        = 0;
    state:             IFakePreferences;
    holdLoad:          boolean                       = false;
    failLoad:          string | undefined            = undefined;
    failSave:          string | undefined            = undefined;
    deferSaves:        boolean                       = false;
    loadFailures:      Array<string>                 = [];
    saveFailures:      Array<string>                 = [];

    constructor(state: IFakePreferences)
    {
        this.state = state;
    }

    reply(values: Record<string,unknown> = this.state.values,failureMessages: Array<string> = []): unknown
    {
        return {
            success: true,
            output:  { userId: 1, source: "Preferences.Current", destination: "PreferencesView", command: null,
                       values: { ...values }, definitions: this.state.definitions, failureMessages },
        };
    }

    getPreferencesOutput(request: Record<string,unknown>): Promise<unknown>
    {
        this.gets.push(request);

        if (this.holdLoad)
            return new Promise(() => undefined);

        if (this.failLoad !== undefined)
            return Promise.resolve({ success: false, failureMessage: this.failLoad });

        return Promise.resolve(this.reply(this.state.values,this.loadFailures));
    }

    handlePreferencesInput(request: { input: Record<string,unknown> }): Promise<unknown>
    {
        this.saves.push(request.input);

        if (this.failSave !== undefined)
            return Promise.resolve({ success: false, failureMessage: this.failSave });

        if (this.deferSaves)
            return new Promise(resolve => { this.deferred.push({ input: request.input, resolve }); });

        this.state.values = { ...this.state.values, ...(request.input.values as Record<string,unknown>) };

        return Promise.resolve(this.reply(this.state.values,this.saveFailures));
    }

    complete(index: number,values: Record<string,unknown>): void
    {
        this.deferred[index].resolve(this.reply({ ...this.state.values, ...values }));
    }

    close(): void { this.closed++; }
}

export function status(userName: string | undefined): ISessionStatus
{
    return { authenticated: userName !== undefined, userName, authorities: userName !== undefined ? ["ROLE_USER"] : [] };
}

export class FakeSession
    implements ISessionService
{
    current: ISessionStatus = status("ann");

    async getLoginMethods() { return { passwordEnabled: true, oktaEnabled: false }; }
    async login(userName: string) { return status(userName); }
    async startOktaLogin() { return undefined; }
    async completeOktaLogin() { return this.current; }
    async getCurrent() { return this.current; }
    async logout() { return { loggedOut: true }; }
}

export function authenticationWith(client: FakePreferencesClient,session: FakeSession): IAuthentication
{
    const base: IAuthentication =
        createAuthentication({ apiBaseUrl: "", fetchFunction: async () => new Response("{}"), service: session });

    return { manager: base.manager, createFrontEndClient: (() => client) as unknown as IAuthentication["createFrontEndClient"] };
}

export function renderWithPreferences(
    client:        FakePreferencesClient,
    content:       ReactElement,
    authenticated: boolean = true,
    session:       FakeSession = new FakeSession()): IAuthentication
{
    const authentication: IAuthentication = authenticationWith(client,session);

    if (!authenticated)
        session.current = status(undefined);

    render(
        <MantineProvider defaultColorScheme="light">
            <AuthenticationProvider authentication={authentication}>
                <PreferencesProvider>
                    {content}
                </PreferencesProvider>
            </AuthenticationProvider>
        </MantineProvider>);

    return authentication;
}

//////////////////////////////////////////////////////////////////////////////
