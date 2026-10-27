//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AccountTest
//////////////////////////////////////////////////////////////////////////////

import { render }                                           from "@testing-library/react";
import { MantineProvider }                                  from "@mantine/core";
import type { ReactElement }                                from "react";
import { AuthenticationProvider, createAuthentication } from "granite.web.authentication/Authentication";
import type { IAuthentication, ISessionService }            from "granite.web.authentication/Authentication";
import { PreferencesProvider }                              from "granite.web.account/Preference";

export interface IFakePreferences
{
    values:      Record<string,unknown>;
    definitions: Array<Record<string,unknown>>;
}

export const THEME_DEFINITION: Record<string,unknown> =
    { key: "granite.theme", type: "ENUM", defaultValue: "light", allowedValues: ["light","dark"] };

export class FakePreferencesClient
{
    readonly gets:   Array<Record<string,unknown>> = [];
    readonly saves:  Array<Record<string,unknown>> = [];
    closed:          number                        = 0;
    state:           IFakePreferences;
    holdLoad:        boolean                       = false;
    failSave:        string | undefined            = undefined;

    constructor(state: IFakePreferences)
    {
        this.state = state;
    }

    private output(): Record<string,unknown>
    {
        return { userId: 1, source: "Preferences.Current", destination: "PreferencesView", command: null,
                 values: { ...this.state.values }, definitions: this.state.definitions, failureMessages: [] };
    }

    getPreferencesOutput(request: Record<string,unknown>): Promise<unknown>
    {
        this.gets.push(request);

        if (this.holdLoad)
            return new Promise(() => undefined);

        return Promise.resolve({ success: true, output: this.output() });
    }

    handlePreferencesInput(request: { input: Record<string,unknown> }): Promise<unknown>
    {
        this.saves.push(request.input);

        if (this.failSave !== undefined)
            return Promise.resolve({ success: false, failureMessage: this.failSave });

        this.state.values = { ...this.state.values, ...(request.input.values as Record<string,unknown>) };

        return Promise.resolve({ success: true, output: this.output() });
    }

    close(): void { this.closed++; }
}

function sessionService(authenticated: boolean): ISessionService
{
    const status = { authenticated, userName: authenticated ? "ann" : undefined, authorities: authenticated ? ["ROLE_USER"] : [] };

    return {
        getLoginMethods:   async () => ({ passwordEnabled: true, oktaEnabled: false }),
        login:             async () => status,
        startOktaLogin:    async () => undefined,
        completeOktaLogin: async () => status,
        getCurrent:        async () => status,
        logout:            async () => ({ loggedOut: true }),
    };
}

export function authenticationWith(client: FakePreferencesClient,authenticated: boolean = true): IAuthentication
{
    const base: IAuthentication =
        createAuthentication({ apiBaseUrl: "", fetchFunction: async () => new Response("{}"), service: sessionService(authenticated) });

    return { manager: base.manager, createFrontEndClient: (() => client) as unknown as IAuthentication["createFrontEndClient"] };
}

export function renderWithPreferences(client: FakePreferencesClient,content: ReactElement,authenticated: boolean = true): void
{
    render(
        <MantineProvider defaultColorScheme="light">
            <AuthenticationProvider authentication={authenticationWith(client,authenticated)}>
                <PreferencesProvider>
                    {content}
                </PreferencesProvider>
            </AuthenticationProvider>
        </MantineProvider>);
}

//////////////////////////////////////////////////////////////////////////////
