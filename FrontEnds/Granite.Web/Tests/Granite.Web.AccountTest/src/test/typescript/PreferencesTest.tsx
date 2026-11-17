//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AccountTest
//////////////////////////////////////////////////////////////////////////////

import { afterEach, describe, expect, it, vi }             from "vitest";
import { act, screen, waitFor }                            from "@testing-library/react";
import userEvent                                           from "@testing-library/user-event";
import { PreferencesView, usePreference }                  from "granite.web.account/Preference";
import { FLAG_DEFINITION, FakePreferencesClient, FakeSession,
         THEME_DEFINITION, renderWithPreferences, status } from "./TestSupport";

function scheme(): string | null
{
    return document.documentElement.getAttribute("data-mantine-color-scheme");
}

function clientWith(theme: string = "light",extra: Record<string,unknown> = {},definitions: Array<Record<string,unknown>> = []): FakePreferencesClient
{
    return new FakePreferencesClient({ values: { "granite.theme": theme, ...extra }, definitions: [THEME_DEFINITION,...definitions] });
}

function flagClient(theme: string = "light"): FakePreferencesClient
{
    return clientWith(theme,{ "app.flag": false },[FLAG_DEFINITION]);
}

function Flag()
{
    const [flag,setFlag,state] = usePreference<boolean>("app.flag");

    return (
        <div>
            <span data-testid="flag">{String(flag)}</span>
            <span data-testid="flag-error">{state.error ?? ""}</span>
            <button onClick={() => void setFlag(!flag)}>toggle flag</button>
        </div>
    );
}

afterEach(() =>
{
    vi.restoreAllMocks();
});

describe("Preferences",() =>
{
    it("loadsPreferencesWhenAuthenticated",async () =>
    {
        const client = clientWith("dark");

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(scheme()).toBe("dark"));

        const request = client.gets[0];

        expect(client.gets).toHaveLength(1);
        expect(request.source).toBe("Preferences.Current");
        expect(request.destination).toBe("PreferencesView");
        expect((screen.getByRole("switch") as HTMLInputElement).checked).toBe(true);
    });

    it("doesNotLoadPreferencesWhenAnonymous",async () =>
    {
        const client = clientWith("dark");

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />,false);

        await new Promise(resolve => setTimeout(resolve,50));

        expect(client.gets).toHaveLength(0);
    });

    it("themeToggleSavesAndAppliesScheme",async () =>
    {
        const client = clientWith("light");

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByRole("switch"));

        await waitFor(() => expect(scheme()).toBe("dark"));

        const input = client.saves[0];

        expect(input.source).toBe("Preferences.Current");
        expect(input.command).toBe("Save");
        expect(input.destination).toBe("PreferencesView");
        expect(input.values).toEqual({ "granite.theme": "dark" });
        expect(window.localStorage.getItem("granite.theme")).toBe("dark");
    });

    it("failedThemeSaveKeepsSchemeAndCache",async () =>
    {
        const client = clientWith("light");

        client.failSave = "Save rejected";
        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await userEvent.click(await screen.findByRole("switch"));

        await screen.findByText("Save rejected");

        expect(scheme()).toBe("light");
        expect((screen.getByRole("switch") as HTMLInputElement).checked).toBe(false);
        expect(window.localStorage.getItem("granite.theme")).toBe("light");
    });

    it("saveFailureShowsErrorAndRevertsValue",async () =>
    {
        const client = flagClient();

        client.failSave = "Save rejected";
        renderWithPreferences(client,<Flag />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByText("toggle flag"));

        await waitFor(() => expect(screen.getByTestId("flag-error").textContent).toBe("Save rejected"));
        expect(screen.getByTestId("flag").textContent).toBe("false");
    });

    it("overlappingSavesKeepLatestValue",async () =>
    {
        const client = flagClient();

        client.deferSaves = true;
        renderWithPreferences(client,<Flag />);

        await waitFor(() => expect(client.gets).toHaveLength(1));

        const toggle = await screen.findByText("toggle flag");

        await userEvent.click(toggle);
        await userEvent.click(toggle);
        await waitFor(() => expect(client.deferred).toHaveLength(2));

        expect(client.saves.map(save => (save.values as Record<string,unknown>)["app.flag"])).toEqual([true,false]);

        await act(async () => { client.complete(1,{ "app.flag": false }); });
        await waitFor(() => expect(screen.getByTestId("flag").textContent).toBe("false"));

        await act(async () => { client.complete(0,{ "app.flag": true }); });

        expect(screen.getByTestId("flag").textContent).toBe("false");
    });

    it("childSectionUsesUsePreference",async () =>
    {
        const client = flagClient();

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User"><Flag /></PreferencesView>);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByText("toggle flag"));

        await waitFor(() => expect(screen.getByTestId("flag").textContent).toBe("true"));
        expect(client.saves[0].values).toEqual({ "app.flag": true });
    });

    it("showsLoadError",async () =>
    {
        const client = clientWith("light");

        client.failLoad = "Preferences unavailable";
        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        expect(await screen.findByText("Preferences unavailable")).toBeTruthy();
        expect((screen.getByRole("switch") as HTMLInputElement).disabled).toBe(true);
    });

    it("degradedReadKeepsCachedThemeAndShowsError",async () =>
    {
        window.localStorage.setItem("granite.theme","dark");

        const client = clientWith("light");

        client.loadFailures = ["unable to read preferences"];
        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        expect(await screen.findByText("unable to read preferences")).toBeTruthy();
        expect(scheme()).toBe("dark");
        expect(window.localStorage.getItem("granite.theme")).toBe("dark");
        expect((screen.getByRole("switch") as HTMLInputElement).disabled).toBe(true);
    });

    it("noCachedThemeDefaultsToDark",async () =>
    {
        window.localStorage.removeItem("granite.theme");

        const client = clientWith("light");

        client.loadFailures = ["unable to read preferences"];
        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        expect(await screen.findByText("unable to read preferences")).toBeTruthy();
        expect(scheme()).toBe("dark");
    });

    it("saveReplyWithFailureMessagesIsAFailure",async () =>
    {
        const client = flagClient();

        client.saveFailures = ["not saved"];
        renderWithPreferences(client,<Flag />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByText("toggle flag"));

        await waitFor(() => expect(screen.getByTestId("flag-error").textContent).toBe("not saved"));
        expect(screen.getByTestId("flag").textContent).toBe("false");
    });

    it("reloadsWhenTheUserChanges",async () =>
    {
        const client         = clientWith("light");
        const authentication = renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(client.gets).toHaveLength(1));

        client.state.values = { "granite.theme": "dark" };

        await act(async () => { await authentication.manager.login("bob","secret"); });

        await waitFor(() => expect(client.gets).toHaveLength(2));
        await waitFor(() => expect(scheme()).toBe("dark"));
    });

    it("keepsValuesWhileReauthenticating",async () =>
    {
        const client         = flagClient();
        const session        = new FakeSession();
        const authentication = renderWithPreferences(client,<Flag />,true,session);

        await userEvent.click(await screen.findByText("toggle flag"));
        await waitFor(() => expect(screen.getByTestId("flag").textContent).toBe("true"));

        session.current = status(undefined);

        await act(async () => { void authentication.manager.handleUnauthorized(); });
        await waitFor(() => expect(authentication.manager.getState().status).toBe("reauthenticating"));

        expect(screen.getByTestId("flag").textContent).toBe("true");
        expect(client.gets).toHaveLength(1);
    });

    it("cachedThemeAppliesBeforeLoad",async () =>
    {
        window.localStorage.setItem("granite.theme","dark");

        const client = clientWith("light");

        client.holdLoad = true;
        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(scheme()).toBe("dark"));
        expect(client.gets).toHaveLength(1);
    });

    it("cachedThemeAppliesWhenAnonymous",async () =>
    {
        window.localStorage.setItem("granite.theme","dark");

        renderWithPreferences(clientWith("light"),<div>login</div>,false);

        await waitFor(() => expect(scheme()).toBe("dark"));
    });

    it("themeWorksWithoutLocalStorage",async () =>
    {
        const failing = () => { throw new Error("storage blocked"); };

        vi.spyOn(window.localStorage,"getItem").mockImplementation(failing);
        vi.spyOn(window.localStorage,"setItem").mockImplementation(failing);
        vi.spyOn(console,"error").mockImplementation(() => undefined);
        vi.spyOn(console,"warn").mockImplementation(() => undefined);

        const client = clientWith("light");

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByRole("switch"));

        await waitFor(() => expect(scheme()).toBe("dark"));
        expect(client.saves[0].values).toEqual({ "granite.theme": "dark" });
    });
});

//////////////////////////////////////////////////////////////////////////////
