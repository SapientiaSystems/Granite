//////////////////////////////////////////////////////////////////////////////
// Granite.Web.AuthenticationTest
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                              from "vitest";
import { screen, waitFor }                                                   from "@testing-library/react";
import userEvent                                                             from "@testing-library/user-event";
import { LoginForm }                                                        from "granite.web.authentication/Login";
import { authenticationFor, FakeNavigator, KEYS, methodsOutput, redirectOutput,
         scriptedFrontEnd, sessionOutput }                                   from "./TestSupport";
import { renderWithAuthentication }                                          from "./RenderSupport";

function renderForm(passwordEnabled: boolean,oktaEnabled: boolean,navigator = new FakeNavigator())
{
    const frontEnd = scriptedFrontEnd()
        .respond(KEYS.methods,() => methodsOutput(passwordEnabled,oktaEnabled))
        .respond(KEYS.start,() => redirectOutput("https://okta.example/authorize"));

    renderWithAuthentication(authenticationFor(frontEnd,undefined,navigator),"/",<LoginForm returnTo="/users" />);

    return { frontEnd, navigator };
}

describe("LoginForm",() =>
{
    it("shows only the password form when only password is enabled",async () =>
    {
        renderForm(true,false);

        expect(await screen.findByLabelText(/User name/)).toBeTruthy();
        expect(screen.queryByRole("button",{ name: "Log in with Okta" })).toBeNull();
    });

    it("shows only the Okta button when only Okta is enabled",async () =>
    {
        renderForm(false,true);

        expect(await screen.findByRole("button",{ name: "Log in with Okta" })).toBeTruthy();
        expect(screen.queryByLabelText(/User name/)).toBeNull();
        expect(screen.queryByLabelText(/Password/)).toBeNull();
    });

    it("shows both when both are enabled",async () =>
    {
        renderForm(true,true);

        expect(await screen.findByLabelText(/User name/)).toBeTruthy();
        expect(screen.getByRole("button",{ name: "Log in with Okta" })).toBeTruthy();
    });

    it("tells the user when no method is enabled",async () =>
    {
        renderForm(false,false);

        expect(await screen.findByText(/No login method is enabled/)).toBeTruthy();
        expect(screen.queryByLabelText(/User name/)).toBeNull();
        expect(screen.queryByRole("button",{ name: "Log in with Okta" })).toBeNull();
    });

    it("shows an alert when the methods cannot be loaded",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.methods,() => new Error("offline"));

        renderWithAuthentication(authenticationFor(frontEnd),"/",<LoginForm returnTo="/" />);

        expect((await screen.findByRole("alert")).textContent).toContain("Unable to load login options");
    });

    it("sends the password login and reports success",async () =>
    {
        const { frontEnd } = renderForm(true,false);

        await userEvent.type(await screen.findByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"secret");
        await userEvent.click(screen.getByRole("button",{ name: "Log in" }));

        await waitFor(() => expect(frontEnd.callsFor(KEYS.login)).toHaveLength(1));
        expect(frontEnd.callsFor(KEYS.login)[0].payload).toMatchObject({ userName: "alice", password: "secret" });
    });

    it("shows a refusal without saying why",async () =>
    {
        renderForm(true,false);

        await userEvent.type(await screen.findByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"wrong");
        await userEvent.click(screen.getByRole("button",{ name: "Log in" }));

        expect((await screen.findByRole("alert")).textContent).toBe("Login failed");
    });

    it("starts Okta with the return path and navigates to the returned URL",async () =>
    {
        const { frontEnd, navigator } = renderForm(false,true);

        await userEvent.click(await screen.findByRole("button",{ name: "Log in with Okta" }));

        await waitFor(() => expect(navigator.assigned).toEqual(["https://okta.example/authorize"]));
        expect(frontEnd.callsFor(KEYS.start)[0].payload).toMatchObject({ returnTo: "/users" });
    });

    it("shows an alert when Okta start is refused",async () =>
    {
        const frontEnd  = scriptedFrontEnd()
            .respond(KEYS.methods,() => methodsOutput(false,true))
            .respond(KEYS.start,() => sessionOutput(false));
        const navigator = new FakeNavigator();

        renderWithAuthentication(authenticationFor(frontEnd,undefined,navigator),"/",<LoginForm returnTo="/" />);
        await userEvent.click(await screen.findByRole("button",{ name: "Log in with Okta" }));

        expect((await screen.findByRole("alert")).textContent).toBe("Login failed");
        expect(navigator.assigned).toEqual([]);
    });
});

//////////////////////////////////////////////////////////////////////////////
