//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AccountTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it, vi } from "vitest";
import { screen, waitFor }          from "@testing-library/react";
import userEvent                    from "@testing-library/user-event";
import { PreferencesView, usePreference } from "granite.web.account/Preference";
import { FakePreferencesClient, THEME_DEFINITION, renderWithPreferences } from "./TestSupport";

function scheme(): string | null
{
    return document.documentElement.getAttribute("data-mantine-color-scheme");
}

function clientWith(theme: string = "light",extra: Record<string,unknown> = {},definitions: Array<Record<string,unknown>> = []): FakePreferencesClient
{
    return new FakePreferencesClient({ values: { "granite.theme": theme, ...extra }, definitions: [THEME_DEFINITION,...definitions] });
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

    it("saveFailureShowsErrorAndRevertsValue",async () =>
    {
        const client = clientWith("light",{ "app.flag": false },[{ key: "app.flag", type: "BOOLEAN", defaultValue: false, allowedValues: [] }]);

        client.failSave = "Save rejected";
        renderWithPreferences(client,<Flag />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByText("toggle flag"));

        await waitFor(() => expect(screen.getByTestId("flag-error").textContent).toBe("Save rejected"));
        expect(screen.getByTestId("flag").textContent).toBe("false");
    });

    it("childSectionUsesUsePreference",async () =>
    {
        const client = clientWith("light",{ "app.flag": false },[{ key: "app.flag", type: "BOOLEAN", defaultValue: false, allowedValues: [] }]);

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User"><Flag /></PreferencesView>);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByText("toggle flag"));

        await waitFor(() => expect(screen.getByTestId("flag").textContent).toBe("true"));
        expect(client.saves[0].values).toEqual({ "app.flag": true });
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

        const client = clientWith("light");

        renderWithPreferences(client,<PreferencesView userName="ann" userRole="User" />);

        await waitFor(() => expect(client.gets).toHaveLength(1));
        await userEvent.click(await screen.findByRole("switch"));

        await waitFor(() => expect(scheme()).toBe("dark"));
        expect(client.saves[0].values).toEqual({ "granite.theme": "dark" });

        vi.restoreAllMocks();
    });
});

//////////////////////////////////////////////////////////////////////////////
