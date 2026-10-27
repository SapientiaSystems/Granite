//////////////////////////////////////////////////////////////////////////////
// Granite.Web.Authentication
//////////////////////////////////////////////////////////////////////////////

import { describe, expect, it }                                from "vitest";
import { screen }                                              from "@testing-library/react";
import userEvent                                               from "@testing-library/user-event";
import { Route, Routes }                                       from "react-router-dom";
import type { ReactElement }                                   from "react";
import { useAuthentication }                                   from "../../main/typescript/Authentication/AuthenticationContext";
import { RequireAuthentication }                               from "../../main/typescript/Authentication/RequireAuthentication";
import { LoginView }                                           from "../../main/typescript/Login/LoginView";
import { authenticationFor, KEYS, scriptedFrontEnd, sessionOutput } from "./TestSupport";
import { renderWithAuthentication }                            from "./RenderSupport";

function WhoAmI(): ReactElement
{
    const { state } = useAuthentication();

    return <div>signed in as {state.userName}</div>;
}

const routes: ReactElement =
    <Routes>
        <Route path="/login" element={<LoginView />} />
        <Route path="/users" element={<RequireAuthentication><WhoAmI /></RequireAuthentication>} />
    </Routes>;

describe("RequireAuthentication and LoginView",() =>
{
    it("redirects an anonymous visitor to the login view",async () =>
    {
        renderWithAuthentication(authenticationFor(scriptedFrontEnd()),"/users",routes);

        expect(await screen.findByRole("heading",{ name: "Sign in" })).toBeTruthy();
    });

    it("signs in and returns to the protected route",async () =>
    {
        const frontEnd = scriptedFrontEnd();

        renderWithAuthentication(authenticationFor(frontEnd),"/users",routes);

        await userEvent.type(await screen.findByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"secret");
        await userEvent.click(screen.getByRole("button",{ name: "Sign in" }));

        expect(await screen.findByText("signed in as alice")).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.login)[0].payload).toMatchObject({ userName: "alice", password: "secret" });
    });

    it("shows the refusal and stays on the login view",async () =>
    {
        renderWithAuthentication(authenticationFor(scriptedFrontEnd()),"/login",routes);

        await userEvent.type(await screen.findByLabelText(/User name/),"alice");
        await userEvent.type(screen.getByLabelText(/Password/),"wrong");
        await userEvent.click(screen.getByRole("button",{ name: "Sign in" }));

        expect((await screen.findByRole("alert")).textContent).toContain("Sign-in failed");
        expect(screen.getByRole("heading",{ name: "Sign in" })).toBeTruthy();
    });

    it("restores an existing session before rendering the protected route",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.current,() => sessionOutput(true,"alice",["ROLE_USER"]));

        renderWithAuthentication(authenticationFor(frontEnd),"/users",routes);

        expect(await screen.findByText("signed in as alice")).toBeTruthy();
        expect(frontEnd.callsFor(KEYS.login)).toHaveLength(0);
    });

    it("leaves the login view when already signed in",async () =>
    {
        const frontEnd = scriptedFrontEnd().respond(KEYS.current,() => sessionOutput(true,"alice",["ROLE_USER"]));

        renderWithAuthentication(authenticationFor(frontEnd),"/login",
            <Routes>
                <Route path="/login" element={<LoginView />} />
                <Route path="/" element={<div>home</div>} />
            </Routes>);

        expect(await screen.findByText("home")).toBeTruthy();
    });

    it("does not offer Google sign-in",async () =>
    {
        renderWithAuthentication(authenticationFor(scriptedFrontEnd()),"/login",routes);

        await screen.findByLabelText(/User name/);

        expect(screen.queryByTestId("google-sign-in")).toBeNull();
    });
});

//////////////////////////////////////////////////////////////////////////////
